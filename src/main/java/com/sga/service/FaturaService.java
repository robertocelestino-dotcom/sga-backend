package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.FaturaDetalheDTO;
import com.sga.dto.FaturaItemDTO;
import com.sga.dto.FaturaItemResponseDTO;
import com.sga.dto.FaturaResumoDTO;
import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.LoteProcessamento;
import com.sga.model.ReguaFaturamento;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.FaturaItemRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.LoteProcessamentoRepository;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class FaturaService {

    private static final Logger log = LoggerFactory.getLogger(FaturaService.class);

    @Autowired
    private FaturaRepository faturaRepository;
    
    @Autowired
    private AssociadoRepository associadoRepository;

    @Autowired
    private FaturaItemRepository faturaItemRepository;

    @Autowired
    private AssociadoService associadoService;

    @Autowired
    private ReguaFaturamentoService reguaService;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;
    
    @Autowired
    private LoteProcessamentoRepository loteProcessamentoRepository;
    
    @Autowired
    private FaturaRmExportService faturaRmExportService;

    // ========== CONSULTAS ==========

    @Transactional(readOnly = true)
    public Page<Fatura> listarFaturas(Pageable pageable) {
        log.info("Listando todas as faturas");
        return faturaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<FaturaResumoDTO> listarFaturasResumo(Pageable pageable) {
        log.info("Listando resumo de faturas");
        Page<Fatura> faturas = faturaRepository.findAll(pageable);
        return faturas.map(this::toResumoDTO);
    }

    @Transactional(readOnly = true)
    public Page<FaturaResumoDTO> listarFaturasResumoPorPeriodo(Integer mes, Integer ano, Pageable pageable) {
        log.info("Listando resumo de faturas por período: {}/{}", mes, ano);
        Page<Fatura> faturas = faturaRepository.findByMesReferenciaAndAnoReferencia(mes, ano, pageable);
        return faturas.map(this::toResumoDTO);
    }

    @Transactional(readOnly = true)
    public Page<Fatura> listarPorAssociado(Long associadoId, Pageable pageable) {
        log.info("Listando faturas do associado: {}", associadoId);
        return faturaRepository.findByAssociadoId(associadoId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Fatura> listarPorLoteProcessamento(Long loteId, Pageable pageable) {
        log.info("Listando faturas do lote: {}", loteId);
        return faturaRepository.findByLoteProcessamentoId(loteId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Fatura> listarFaturasPendentesRM() {
        log.info("Listando faturas pendentes de integração RM");
        return faturaRepository.findByProcessadoRmFalse();
    }

    @Transactional(readOnly = true)
    public Optional<Fatura> buscarPorId(Long id) {
        log.info("Buscando fatura por ID: {}", id);
        return faturaRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public FaturaDetalheDTO buscarFaturaDetalheDTO(Long id) {
        log.info("Buscando fatura detalhe DTO por ID: {}", id);
        
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        
        return toDetalheDTO(fatura);
    }

    @Transactional(readOnly = true)
    public Page<FaturaResumoDTO> listarFaturasComFiltros(
            String numeroFatura,
            String associadoNome,
            String status,
            Integer mes,
            Integer ano,
            Long associadoId,
            Long reguaId,
            Pageable pageable) {
        
        log.info("📋 ===== FATURAS COM FILTROS =====");
        log.info("  - reguaId recebido: {}", reguaId);
        log.info("  - mes: {}, ano: {}", mes, ano);
        log.info("  - associadoNome: '{}'", associadoNome);
        
        Page<Fatura> faturasPage = faturaRepository.findFaturasComFiltros(
            numeroFatura, associadoNome, status, mes, ano, associadoId, reguaId, pageable);
        log.info("📋 Listando faturas com filtros - reguaId: {}", reguaId);
        
        List<Long> associadoIds = faturasPage.getContent().stream()
            .map(Fatura::getAssociado)
            .filter(a -> a != null)
            .map(Associado::getId)
            .distinct()
            .collect(Collectors.toList());
        
        List<Long> loteIds = faturasPage.getContent().stream()
            .map(Fatura::getLoteProcessamentoId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        
        Map<Long, Associado> associadoMap = new HashMap<>();
        if (!associadoIds.isEmpty()) {
            List<Associado> associados = associadoRepository.findAllById(associadoIds);
            associadoMap = associados.stream()
                .collect(Collectors.toMap(Associado::getId, a -> a));
        }
        
        Map<Long, LoteProcessamento> loteMap = new HashMap<>();
        if (!loteIds.isEmpty()) {
            List<LoteProcessamento> lotes = loteProcessamentoRepository.findAllById(loteIds);
            loteMap = lotes.stream()
                .collect(Collectors.toMap(LoteProcessamento::getId, l -> l));
        }
        
        final Map<Long, Associado> finalAssociadoMap = associadoMap;
        final Map<Long, LoteProcessamento> finalLoteMap = loteMap;
        
        return faturasPage.map(fatura -> {
            FaturaResumoDTO dto = new FaturaResumoDTO();
            dto.setId(fatura.getId());
            dto.setNumeroFatura(fatura.getNumeroFatura());
            dto.setValorTotal(fatura.getValorTotal());
            dto.setDataEmissao(fatura.getDataEmissao());
            dto.setDataVencimento(fatura.getDataVencimento());
            dto.setStatus(fatura.getStatus());
            dto.setMesReferencia(fatura.getMesReferencia());
            dto.setAnoReferencia(fatura.getAnoReferencia());
            
            if (fatura.getAssociado() != null) {
                Long id = fatura.getAssociado().getId();
                Associado associado = finalAssociadoMap.get(id);
                if (associado != null) {
                    dto.setAssociadoId(associado.getId());
                    dto.setAssociadoNome(associado.getNomeRazao());
                    dto.setCnpjCpf(associado.getCnpjCpf());
                    dto.setCodigoSpc(associado.getCodigoSpc());
                }
            }
            
            Long loteId = fatura.getLoteProcessamentoId();
            if (loteId != null) {
                LoteProcessamento lote = finalLoteMap.get(loteId);
                if (lote != null && lote.getRegua() != null) {
                    ReguaFaturamento regua = lote.getRegua();
                    dto.setReguaId(regua.getId());
                    dto.setReguaNome(regua.getNome());
                    dto.setReguaCor(regua.getCor());
                }
            }
            
            return dto;
        });
    }

    // ========== ATUALIZAÇÕES ==========

    @Transactional
    public Fatura marcarComoProcessadoRm(Long id) {
        log.info("Marcando fatura {} como processada no RM", id);
        
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        
        fatura.setProcessadoRm(true);
        fatura.setDataProcessamento(LocalDateTime.now());
        
        return faturaRepository.save(fatura);
    }

    @Transactional
    public Fatura atualizarStatus(Long id, String status) {
        log.info("Atualizando status da fatura {} para {}", id, status);
        
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        
        fatura.setStatus(status);
        fatura.setAtualizadoEm(LocalDateTime.now());
        
        return faturaRepository.save(fatura);
    }

    @Transactional
    public Fatura processarFaturamento(Long associadoId, LocalDate dataEmissao, String usuario) {
        log.info("Processando faturamento para associado: {} na data: {}", associadoId, dataEmissao);
        return null;
    }

    // ========== EXPORTAÇÃO RM ==========

    @Transactional
    public byte[] exportarRmFatura(Long id, Integer ultimoNumeroRps, String usuario, Long reguaId, LocalDate mesReferencia) {
        log.info("📤 Exportando RM para fatura ID: {}", id);
        
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        
        // 🔥 BUSCAR LOTE EXISTENTE - NÃO CRIA NOVO
        LoteProcessamento lote = buscarLoteExistente(reguaId, mesReferencia);
        
        // Se não encontrar lote, lançar erro (deve ter sido criado pelo processamento)
        if (lote == null) {
            throw new RuntimeException("Nenhum lote encontrado para a régua " + reguaId + 
                " e mês " + mesReferencia + ". Execute o processamento primeiro.");
        }
        
        int novoNumeroRps = ultimoNumeroRps + 1;
        
        fatura.setProcessadoRm(true);
        fatura.setLoteProcessamentoId(lote.getId());
        fatura.setDataProcessamento(LocalDateTime.now());
        fatura.setStatus("PAGA");
        fatura.setNumeroRps(novoNumeroRps);
        faturaRepository.save(fatura);
        
        return faturaRmExportService.exportarRmFatura(id, novoNumeroRps, lote);
    }

    /**
     * 🔥 EXPORTA RM LOTE - APENAS REUTILIZA LOTE EXISTENTE
     */
    @Transactional
    public byte[] exportarRmLote(List<Long> faturaIds, Integer ultimoNumeroRps, String usuario, Long reguaId, LocalDate mesReferencia) {
        log.info("📤 Exportando RM lote para {} faturas: {}", faturaIds.size(), faturaIds);
        log.info("📤 reguaId recebido: {}", reguaId);
        
        if (faturaIds == null || faturaIds.isEmpty()) {
            log.warn("⚠️ Nenhuma fatura para exportar!");
            return new byte[0];
        }
        
        // 🔥 BUSCAR LOTE EXISTENTE - NÃO CRIA NOVO
        LoteProcessamento lote = buscarLoteExistente(reguaId, mesReferencia);
        
        // Se não encontrar lote, lançar erro (deve ter sido criado pelo processamento)
        if (lote == null) {
            throw new RuntimeException("Nenhum lote encontrado para a régua " + reguaId + 
                " e mês " + mesReferencia + ". Execute o processamento primeiro.");
        }
        
        log.info("♻️ Usando lote existente ID: {}, Régua: {}, Mês: {}, Status: {}", 
            lote.getId(), 
            lote.getRegua() != null ? lote.getRegua().getNome() : "null",
            lote.getMesReferencia(),
            lote.getStatus());
        
        // Atualizar faturas
        int numeroRpsAtual = ultimoNumeroRps;
        int faturasProcessadas = 0;
        int erros = 0;
        
        log.info("🔄 Atualizando faturas com lote ID: {}", lote.getId());
        int rpsAtual = numeroRpsAtual + 1;
        
        for (Long id : faturaIds) {
            try {
                Optional<Fatura> faturaOpt = faturaRepository.findById(id);
                if (faturaOpt.isPresent()) {
                    Fatura fatura = faturaOpt.get();
                    
                    // Verificar se a fatura já está associada a um lote
                    if (fatura.getLoteProcessamentoId() != null && 
                        !fatura.getLoteProcessamentoId().equals(lote.getId())) {
                        log.warn("⚠️ Fatura {} já está em outro lote: {}", id, fatura.getLoteProcessamentoId());
                    }
                    
                    fatura.setProcessadoRm(true);
                    fatura.setLoteProcessamentoId(lote.getId());
                    fatura.setDataProcessamento(LocalDateTime.now());
                    fatura.setStatus("PAGA");
                    fatura.setNumeroRps(rpsAtual);
                    
                    faturaRepository.saveAndFlush(fatura);
                    
                    faturasProcessadas++;
                    rpsAtual++;
                } else {
                    log.warn("⚠️ Fatura {} não encontrada!", id);
                    erros++;
                }
            } catch (Exception e) {
                log.error("❌ Erro ao processar fatura {}: {}", id, e.getMessage(), e);
                erros++;
            }
        }
        
        log.info("📊 Faturas atualizadas: {} processadas, {} erros", faturasProcessadas, erros);
        
        // Atualizar lote
        lote.setTotalFaturasGeradas(lote.getTotalFaturasGeradas() + faturasProcessadas);
        lote.setTotalErros(lote.getTotalErros() + erros);
        lote.setDataFimProcessamento(LocalDateTime.now());
        lote.setStatus("CONCLUIDO");
        loteProcessamentoRepository.saveAndFlush(lote);
        
        // Gerar arquivo RM
        try {
            log.info("📤 Gerando arquivo RM...");
            byte[] arquivo = faturaRmExportService.exportarRmLote(
                faturaIds, ultimoNumeroRps, usuario, reguaId, mesReferencia
            );
            log.info("✅ Arquivo RM gerado, tamanho: {} bytes", arquivo.length);
            return arquivo;
        } catch (Exception e) {
            log.error("❌ Erro na geração do arquivo RM: {}", e.getMessage(), e);
            
            lote.setTotalFaturasGeradas(lote.getTotalFaturasGeradas() + faturasProcessadas);
            lote.setTotalErros(lote.getTotalErros() + erros);
            lote.setDataFimProcessamento(LocalDateTime.now());
            lote.setStatus("ERRO");
            lote.setObservacao("Erro na geração do arquivo RM: " + e.getMessage());
            loteProcessamentoRepository.saveAndFlush(lote);
            
            throw new RuntimeException("Erro na geração do arquivo RM lote: " + e.getMessage());
        }
    }

    // ========== EDIÇÃO DE ITENS ==========

    /**
     * Adiciona um item a uma fatura
     */
    @Transactional
    public FaturaItemResponseDTO adicionarItemFatura(Long faturaId, FaturaItemDTO itemDTO, String usuario) {
        log.info("➕ Adicionando item à fatura ID: {}", faturaId);
        
        if (faturaId == null) {
            throw new IllegalArgumentException("ID da fatura não pode ser nulo");
        }
        if (itemDTO == null) {
            throw new IllegalArgumentException("Dados do item não podem ser nulos");
        }
        if (!itemDTO.isValid()) {
            throw new IllegalArgumentException("Dados do item incompletos ou inválidos: " + itemDTO);
        }
        
        Fatura fatura = faturaRepository.findById(faturaId)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + faturaId));
        
        if (!"PENDENTE".equals(fatura.getStatus()) && !"SIMULADO".equals(fatura.getStatus())) {
            throw new RuntimeException("Fatura não pode ser editada. Status atual: " + fatura.getStatus());
        }
        
        FaturaItem item = new FaturaItem();
        item.setFatura(fatura);
        item.setCodigoProduto(itemDTO.getCodigoProduto() != null ? itemDTO.getCodigoProduto().trim() : "");
        item.setDescricao(itemDTO.getDescricao() != null ? itemDTO.getDescricao().trim() : "");
        item.setQuantidade(itemDTO.getQuantidade() != null ? itemDTO.getQuantidade() : BigDecimal.ONE);
        item.setValorUnitario(itemDTO.getValorUnitario() != null ? itemDTO.getValorUnitario() : BigDecimal.ZERO);
        item.setValorTotal(item.getQuantidade().multiply(item.getValorUnitario()));
        item.setTipoLancamento(itemDTO.getTipoLancamento() != null ? itemDTO.getTipoLancamento() : "D");
        
        FaturaItem saved = faturaItemRepository.save(item);
        
        recalcularTotalFatura(fatura);
        
        log.info("✅ Item adicionado à fatura {}: {} (ID: {})", faturaId, item.getDescricao(), saved.getId());
        
        return new FaturaItemResponseDTO(saved);
    }

    /**
     * Remove um item de uma fatura
     */
    @Transactional
    public void removerItemFatura(Long faturaId, Long itemId, String usuario) {
        log.info("🗑️ Removendo item {} da fatura {}", itemId, faturaId);
        
        if (faturaId == null) {
            throw new IllegalArgumentException("ID da fatura não pode ser nulo");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("ID do item inválido: " + itemId);
        }
        
        Fatura fatura = faturaRepository.findById(faturaId)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + faturaId));
        
        if (!"PENDENTE".equals(fatura.getStatus()) && !"SIMULADO".equals(fatura.getStatus())) {
            throw new RuntimeException("Fatura não pode ser editada. Status atual: " + fatura.getStatus());
        }
        
        FaturaItem item = faturaItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item não encontrado: " + itemId));
        
        if (!item.getFatura().getId().equals(faturaId)) {
            throw new RuntimeException("Item não pertence a esta fatura");
        }
        
        faturaItemRepository.delete(item);
        
        recalcularTotalFatura(fatura);
        
        log.info("✅ Item removido da fatura {}", faturaId);
    }

    /**
     * Atualiza um item de uma fatura
     */
    @Transactional
    public FaturaItemResponseDTO atualizarItemFatura(Long faturaId, Long itemId, FaturaItemDTO itemDTO, String usuario) {
        log.info("✏️ Atualizando item {} da fatura {}", itemId, faturaId);
        
        if (faturaId == null) {
            throw new IllegalArgumentException("ID da fatura não pode ser nulo");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("ID do item inválido: " + itemId);
        }
        if (itemDTO == null) {
            throw new IllegalArgumentException("Dados do item não podem ser nulos");
        }
        if (!itemDTO.isValid()) {
            throw new IllegalArgumentException("Dados do item incompletos ou inválidos: " + itemDTO);
        }
        
        Fatura fatura = faturaRepository.findById(faturaId)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + faturaId));
        
        if (!"PENDENTE".equals(fatura.getStatus()) && !"SIMULADO".equals(fatura.getStatus())) {
            throw new RuntimeException("Fatura não pode ser editada. Status atual: " + fatura.getStatus());
        }
        
        FaturaItem item = faturaItemRepository.findById(itemId)
            .orElseThrow(() -> new RuntimeException("Item não encontrado: " + itemId));
        
        if (!item.getFatura().getId().equals(faturaId)) {
            throw new RuntimeException("Item não pertence a esta fatura");
        }
        
        if (itemDTO.getCodigoProduto() != null) {
            item.setCodigoProduto(itemDTO.getCodigoProduto().trim());
        }
        if (itemDTO.getDescricao() != null) {
            item.setDescricao(itemDTO.getDescricao().trim());
        }
        if (itemDTO.getQuantidade() != null) {
            item.setQuantidade(itemDTO.getQuantidade());
        }
        if (itemDTO.getValorUnitario() != null) {
            item.setValorUnitario(itemDTO.getValorUnitario());
        }
        if (itemDTO.getTipoLancamento() != null && !itemDTO.getTipoLancamento().isEmpty()) {
            item.setTipoLancamento(itemDTO.getTipoLancamento());
        }
        
        item.setValorTotal(item.getQuantidade().multiply(item.getValorUnitario()));
        
        FaturaItem updated = faturaItemRepository.save(item);
        
        recalcularTotalFatura(fatura);
        
        log.info("✅ Item atualizado na fatura {}: {} (ID: {})", faturaId, item.getDescricao(), updated.getId());
        
        return new FaturaItemResponseDTO(updated);
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * 🔥 BUSCA LOTE EXISTENTE - NÃO CRIA NOVO
     * Retorna null se não encontrar
     */
    private LoteProcessamento buscarLoteExistente(Long reguaId, LocalDate mesReferencia) {
        log.info("🔍 Buscando lote existente para régua: {}, mês: {}", reguaId, mesReferencia);
        
        if (reguaId == null || mesReferencia == null) {
            log.warn("⚠️ reguaId ou mesReferencia nulo, não é possível buscar lote");
            return null;
        }
        
        // Extrair mês e ano
        Integer mes = mesReferencia.getMonthValue();
        Integer ano = mesReferencia.getYear();
        
        log.info("📅 Buscando por MÊS/ANO: {}/{} (ignorando dia)", mes, ano);
        
        // 🔥 Buscar lote existente para este mês/ano e régua
        List<LoteProcessamento> lotesExistentes = loteProcessamentoRepository
            .findByMesAnoAndReguaId(mes, ano, reguaId);
        
        // Filtrar apenas lotes que não estão em ERRO
        lotesExistentes = lotesExistentes.stream()
            .filter(l -> !"ERRO".equals(l.getStatus()))
            .collect(Collectors.toList());
        
        if (!lotesExistentes.isEmpty()) {
            // Pegar o lote mais recente
            LoteProcessamento lote = lotesExistentes.get(lotesExistentes.size() - 1);
            log.info("✅ Lote encontrado: ID {}, Mês: {}, Status: {}, Faturas: {}", 
                lote.getId(), 
                lote.getMesReferencia(),
                lote.getStatus(),
                lote.getTotalFaturasGeradas());
            return lote;
        }
        
        log.warn("⚠️ Nenhum lote encontrado para régua {} e mês {}/{}", reguaId, mes, ano);
        return null;
    }

    private FaturaResumoDTO toResumoDTO(Fatura fatura) {
        if (fatura == null) return null;
        
        FaturaResumoDTO dto = new FaturaResumoDTO();
        dto.setId(fatura.getId());
        dto.setNumeroFatura(fatura.getNumeroFatura());
        dto.setValorTotal(fatura.getValorTotal());
        dto.setDataEmissao(fatura.getDataEmissao());
        dto.setDataVencimento(fatura.getDataVencimento());
        dto.setStatus(fatura.getStatus());
        dto.setMesReferencia(fatura.getMesReferencia());
        dto.setAnoReferencia(fatura.getAnoReferencia());
        
        if (fatura.getAssociado() != null) {
            dto.setAssociadoNome(fatura.getAssociado().getNomeRazao());
            dto.setCnpjCpf(fatura.getAssociado().getCnpjCpf());
            dto.setCodigoSpc(fatura.getAssociado().getCodigoSpc());
            dto.setAssociadoId(fatura.getAssociado().getId());
        }
        
        return dto;
    }

    private FaturaDetalheDTO toDetalheDTO(Fatura fatura) {
        if (fatura == null) return null;
        
        FaturaDetalheDTO dto = new FaturaDetalheDTO();
        dto.setId(fatura.getId());
        dto.setNumeroFatura(fatura.getNumeroFatura());
        dto.setValorTotal(fatura.getValorTotal());
        dto.setDataEmissao(fatura.getDataEmissao());
        dto.setDataVencimento(fatura.getDataVencimento());
        dto.setStatus(fatura.getStatus());
        dto.setProcessadoRm(fatura.getProcessadoRm());
        dto.setMesReferencia(fatura.getMesReferencia());
        dto.setAnoReferencia(fatura.getAnoReferencia());
        dto.setCriadoEm(fatura.getCriadoEm());
        dto.setObservacao(fatura.getObservacao());
        dto.setNumeroRps(fatura.getNumeroRps());
        
        if (fatura.getAssociado() != null) {
            dto.setAssociadoId(fatura.getAssociado().getId());
            dto.setAssociadoNome(fatura.getAssociado().getNomeRazao());
            dto.setCnpjCpf(fatura.getAssociado().getCnpjCpf());
            dto.setCodigoSpc(fatura.getAssociado().getCodigoSpc());
        }
        
        List<FaturaItemDTO> itensDTO = new ArrayList<>();
        if (fatura.getItens() != null && !fatura.getItens().isEmpty()) {
            for (FaturaItem item : fatura.getItens()) {
                FaturaItemDTO itemDTO = new FaturaItemDTO();
                itemDTO.setId(item.getId());
                itemDTO.setCodigoProduto(item.getCodigoProduto());
                itemDTO.setDescricao(item.getDescricao());
                itemDTO.setQuantidade(item.getQuantidade());
                itemDTO.setValorUnitario(item.getValorUnitario());
                itemDTO.setValorTotal(item.getValorTotal());
                itemDTO.setTipoLancamento(item.getTipoLancamento());
                itensDTO.add(itemDTO);
            }
        }
        dto.setItens(itensDTO);
        
        return dto;
    }
    
    /**
     * Recalcula o valor total da fatura
     */
    private void recalcularTotalFatura(Fatura fatura) {
        List<FaturaItem> itens = faturaItemRepository.findByFaturaId(fatura.getId());
        
        BigDecimal total = itens.stream()
            .map(FaturaItem::getValorTotal)
            .filter(v -> v != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        fatura.setValorTotal(total);
        fatura.setAtualizadoEm(LocalDateTime.now());
        faturaRepository.save(fatura);
        
        log.info("💰 Total recalculado da fatura {}: R$ {}", fatura.getId(), total);
    }
    
    /**
     * 🔥 EXCLUIR FATURA
     * Apenas faturas com status PENDENTE ou SIMULADO podem ser excluídas
     */
    @Transactional
    public void excluirFatura(Long id, String usuario) {
        log.info("🗑️ Excluindo fatura ID: {} pelo usuário: {}", id, usuario);
        
        // 1. Verificar se a fatura existe
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        
        // 2. Verificar se a fatura pode ser excluída (apenas PENDENTE ou SIMULADO)
        if (!"PENDENTE".equals(fatura.getStatus()) && !"SIMULADO".equals(fatura.getStatus())) {
            throw new RuntimeException("Não é possível excluir uma fatura com status: " + fatura.getStatus() + 
                ". Apenas faturas PENDENTE ou SIMULADO podem ser excluídas.");
        }
        
        // 3. Verificar se a fatura já foi processada no RM
        if (fatura.getProcessadoRm() != null && fatura.getProcessadoRm()) {
            throw new RuntimeException("Não é possível excluir uma fatura já processada no RM.");
        }
        
        // 4. Remover os itens da fatura
        List<FaturaItem> itens = faturaItemRepository.findByFaturaId(id);
        if (itens != null && !itens.isEmpty()) {
            faturaItemRepository.deleteAll(itens);
            log.info("🗑️ Removidos {} itens da fatura {}", itens.size(), id);
        }
        
        // 5. Remover a fatura
        faturaRepository.delete(fatura);
        log.info("✅ Fatura {} excluída com sucesso pelo usuário {}", id, usuario);
    }
    
    
    
    
}