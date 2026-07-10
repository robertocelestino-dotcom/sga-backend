package com.sga.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoReguaDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.ReguaFaturamentoDTO;
import com.sga.dto.TipoArquivoReguaDTO;
import com.sga.exception.ResourceNotFoundException;
import com.sga.model.Associado;
import com.sga.model.AssociadoRegua;
import com.sga.model.ReguaFaturamento;
import com.sga.model.TipoArquivoRegua;
import com.sga.repository.AssociadoReguaRepository;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.NotaDebitoSPCRepository;
import com.sga.repository.ReguaFaturamentoRepository;
import com.sga.repository.TipoArquivoReguaRepository;

@Service
public class ReguaFaturamentoService {

    private static final Logger log = LoggerFactory.getLogger(ReguaFaturamentoService.class);

    @Autowired
    private ReguaFaturamentoRepository reguaFaturamentoRepository;

    @Autowired
    private AssociadoReguaRepository associadoReguaRepository;

    @Autowired
    private AssociadoRepository associadoRepository;

    @Autowired
    private TipoArquivoReguaRepository tipoArquivoReguaRepository;
    
    @Autowired
    private FaturaRepository faturaRepository;
    
    @Autowired
    private NotaDebitoSPCRepository notaDebitoSPCRepository;
    
    // ========== RÉGUAS ==========

    @Transactional(readOnly = true)
    public Page<ReguaFaturamento> listarReguas(Pageable pageable) {
        log.info("Listando réguas com paginação");
        return reguaFaturamentoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<ReguaFaturamento> listarReguasAtivas() {
        log.info("Listando réguas ativas");
        return reguaFaturamentoRepository.findReguasAtivas();
    }

    @Transactional(readOnly = true)
    public Optional<ReguaFaturamento> buscarReguaPadrao() {
        log.info("Buscando régua padrão");
        return reguaFaturamentoRepository.findReguaPadrao();
    }

    @Transactional(readOnly = true)
    public Optional<ReguaFaturamento> buscarPorId(Long id) {
        log.info("Buscando régua por ID: {}", id);
        return reguaFaturamentoRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public ReguaFaturamentoDTO buscarReguaDTOPorId(Long id) {
        log.info("Buscando régua DTO por ID: {}", id);
        
        Optional<ReguaFaturamento> reguaOpt = reguaFaturamentoRepository.findById(id);
        
        if (reguaOpt.isEmpty()) {
            log.warn("Régua não encontrada com ID: {}", id);
            return null;
        }
        
        ReguaFaturamento regua = reguaOpt.get();
        
        if (regua.getTiposArquivo() != null) {
            regua.getTiposArquivo().size();
        }
        
        return toDTO(regua);
    }

    @Transactional
    public ReguaFaturamento criarRegua(ReguaFaturamento regua, String usuario) {
        log.info("Criando nova régua: {}", regua.getNome());
        regua.setCriadoPor(usuario);
        regua.setCriadoEm(LocalDateTime.now());
        return reguaFaturamentoRepository.save(regua);
    }

    @Transactional
    public ReguaFaturamento atualizarRegua(Long id, ReguaFaturamento regua, String usuario) {
        log.info("Atualizando régua ID: {}", id);
        
        ReguaFaturamento existing = reguaFaturamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Régua não encontrada"));
        
        existing.setNome(regua.getNome());
        existing.setDescricao(regua.getDescricao());
        existing.setDiaEmissao(regua.getDiaEmissao());
        existing.setPeriodo(regua.getPeriodo());
        existing.setSequencia(regua.getSequencia());
        existing.setTipoArquivo(regua.getTipoArquivo());
        existing.setOrdemImportacao(regua.getOrdemImportacao());
        existing.setEhPadrao(regua.getEhPadrao());
        existing.setAtivo(regua.getAtivo());
        existing.setCor(regua.getCor());
        existing.setIcone(regua.getIcone());
        existing.setAtualizadoPor(usuario);
        existing.setAtualizadoEm(LocalDateTime.now());
        
        if (existing.getTiposArquivo() != null && !existing.getTiposArquivo().isEmpty()) {
            tipoArquivoReguaRepository.deleteByReguaId(id);
            existing.getTiposArquivo().clear();
            log.info("🗑️ Tipos antigos removidos");
        }
        
        if (regua.getTiposArquivo() != null && !regua.getTiposArquivo().isEmpty()) {
            log.info("📝 Salvando {} tipos de arquivo", regua.getTiposArquivo().size());
            
            List<TipoArquivoRegua> novosTipos = new ArrayList<>();
            
            for (TipoArquivoRegua tipo : regua.getTiposArquivo()) {
                TipoArquivoRegua novoTipo = new TipoArquivoRegua();
                novoTipo.setTipo(tipo.getTipo());
                novoTipo.setOrdem(tipo.getOrdem());
                novoTipo.setRegua(existing);
                novosTipos.add(novoTipo);
                log.info("  ✅ Preparado tipo: {} - Ordem: {}", novoTipo.getTipo(), novoTipo.getOrdem());
            }
            
            tipoArquivoReguaRepository.saveAll(novosTipos);
            existing.setTiposArquivo(novosTipos);
        }
        
        ReguaFaturamento saved = reguaFaturamentoRepository.save(existing);
        reguaFaturamentoRepository.flush();
        ReguaFaturamento reloaded = reguaFaturamentoRepository.findById(id).orElse(saved);
        
        log.info("✅ Régua atualizada. Total de tipos salvos: {}", 
            reloaded.getTiposArquivo() != null ? reloaded.getTiposArquivo().size() : 0);
        
        return reloaded;
    }
    
    @Transactional
    public void excluirRegua(Long id) {
        log.info("Excluindo régua ID: {}", id);
        reguaFaturamentoRepository.deleteById(id);
    }

    // ========== ASSOCIADOS NA RÉGUA ==========

    @Transactional(readOnly = true)
    public List<AssociadoReguaDTO> listarAssociadosPorReguaDTO(Long reguaId) {
        log.info("Listando associados da régua ID: {}", reguaId);
        List<AssociadoRegua> associadosRegua = associadoReguaRepository.findByReguaIdAndAtivoTrue(reguaId);
        
        return associadosRegua.stream().map(ar -> {
            AssociadoReguaDTO dto = new AssociadoReguaDTO();
            dto.setId(ar.getId());
            dto.setAssociadoId(ar.getAssociado().getId());
            dto.setAssociadoNome(ar.getAssociado().getNomeRazao());
            dto.setAssociadoCodigoSpc(ar.getAssociado().getCodigoSpc());
            dto.setReguaId(ar.getRegua().getId());
            dto.setReguaNome(ar.getRegua().getNome());
            dto.setDataInicio(ar.getDataInicio());
            dto.setDataFim(ar.getDataFim());
            dto.setAtivo(ar.getAtivo());
            dto.setMotivoMigracao(ar.getMotivoMigracao());
            dto.setObservacao(ar.getObservacao());
            dto.setCriadoEm(ar.getCriadoEm());
            dto.setCriadoPor(ar.getCriadoPor());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<AssociadoResumoDTO> listarAssociadosPorReguaPaginado(Long reguaId, Pageable pageable) {
        log.info("📋 Listando associados da régua ID: {} com paginação", reguaId);
        
        Page<AssociadoRegua> associadosReguaPage = associadoReguaRepository.findByReguaIdAndAtivoTrue(reguaId, pageable);
        
        return associadosReguaPage.map(ar -> {
            Associado associado = ar.getAssociado();
            AssociadoResumoDTO dto = new AssociadoResumoDTO();
            
            dto.setId(associado.getId());
            dto.setNomeRazao(associado.getNomeRazao());
            dto.setNomeFantasia(associado.getNomeFantasia());
            dto.setCnpjCpf(associado.getCnpjCpf());
            dto.setCodigoSpc(associado.getCodigoSpc());
            dto.setCodigoRm(associado.getCodigoRm());
            dto.setTipoPessoa(associado.getTipoPessoa());
            dto.setStatus(associado.getStatus());
            dto.setDataFiliacao(associado.getDataFiliacao());
            dto.setDataCadastro(associado.getDataCadastro());
            dto.setDataInativacao(associado.getDataInativacao());
            dto.setDataInicioSuspensao(associado.getDataInicioSuspensao());
            dto.setDataFimSuspensao(associado.getDataFimSuspensao());
            dto.setMotivoInativacao(associado.getMotivoInativacao());
            dto.setMotivoSuspensao(associado.getMotivoSuspensao());
            
            if (associado.getVendedor() != null) {
                dto.setVendedorId(associado.getVendedor().getId());
                dto.setVendedorNome(associado.getVendedor().getNomeRazao());
            }
            
            if (associado.getVendedorExterno() != null) {
                dto.setVendedorExternoId(associado.getVendedorExterno().getId());
                dto.setVendedorExternoNome(associado.getVendedorExterno().getNomeRazao());
            }
            
            if (associado.getPlano() != null) {
                dto.setPlanoId(associado.getPlano().getId());
                dto.setPlanoTitulo(associado.getPlano().getPlano());
            }
            
            if (associado.getCategoria() != null) {
                dto.setCategoriaId(associado.getCategoria().getId());
                dto.setCategoriaNome(associado.getCategoria().getDescricao());
            }
            
            return dto;
        });
    }

    @Transactional
    public AssociadoRegua adicionarAssociadoARegua(Long associadoId, Long reguaId, LocalDate dataInicio, String usuario) {
        log.info("Adicionando associado {} à régua {}", associadoId, reguaId);
        
        Associado associado = associadoRepository.findById(associadoId)
            .orElseThrow(() -> new RuntimeException("Associado não encontrado com ID: " + associadoId));
        
        ReguaFaturamento regua = reguaFaturamentoRepository.findById(reguaId)
            .orElseThrow(() -> new RuntimeException("Régua não encontrada com ID: " + reguaId));
        
        boolean existeAtivo = associadoReguaRepository.existsByAssociadoIdAndAtivoTrue(associadoId);
        if (existeAtivo) {
            throw new RuntimeException("Associado já possui uma régua ativa");
        }
        
        AssociadoRegua associadoRegua = new AssociadoRegua();
        associadoRegua.setAssociado(associado);
        associadoRegua.setRegua(regua);
        associadoRegua.setDataInicio(dataInicio != null ? dataInicio : LocalDate.now());
        associadoRegua.setAtivo(true);
        associadoRegua.setCriadoPor(usuario);
        associadoRegua.setCriadoEm(LocalDateTime.now());
        
        return associadoReguaRepository.save(associadoRegua);
    }
    
    @Transactional
    public AssociadoReguaDTO adicionarAssociadoAReguaDTO(Long associadoId, Long reguaId, LocalDate dataInicio, String usuario) {
        log.info("Adicionando associado {} à régua {} (DTO)", associadoId, reguaId);
        AssociadoRegua associadoRegua = adicionarAssociadoARegua(associadoId, reguaId, dataInicio, usuario);
        return toAssociadoReguaDTO(associadoRegua);
    }

    @Transactional
    public AssociadoRegua migrarAssociado(Long associadoId, Long reguaDestinoId, LocalDate dataMigracao, String motivo, String usuario) {
        log.info("Migrando associado {} para régua {}", associadoId, reguaDestinoId);
        
        AssociadoRegua associacaoAtual = associadoReguaRepository.findByAssociadoIdAndAtivoTrue(associadoId)
            .orElseThrow(() -> new RuntimeException("Associado não possui régua ativa"));
        
        Long migradoDe = associacaoAtual.getRegua().getId();
        
        associacaoAtual.setAtivo(false);
        associacaoAtual.setDataFim(dataMigracao);
        associacaoAtual.setAtualizadoPor(usuario);
        associacaoAtual.setAtualizadoEm(LocalDateTime.now());
        associadoReguaRepository.save(associacaoAtual);
        
        Associado associado = associadoRepository.findById(associadoId)
            .orElseThrow(() -> new RuntimeException("Associado não encontrado"));
        ReguaFaturamento reguaDestino = reguaFaturamentoRepository.findById(reguaDestinoId)
            .orElseThrow(() -> new RuntimeException("Régua destino não encontrada"));
        
        AssociadoRegua novaAssociacao = new AssociadoRegua();
        novaAssociacao.setAssociado(associado);
        novaAssociacao.setRegua(reguaDestino);
        novaAssociacao.setDataInicio(dataMigracao);
        novaAssociacao.setAtivo(true);
        novaAssociacao.setMigradoDe(migradoDe);
        novaAssociacao.setMotivoMigracao(motivo);
        novaAssociacao.setCriadoPor(usuario);
        novaAssociacao.setCriadoEm(LocalDateTime.now());
        
        return associadoReguaRepository.save(novaAssociacao);
    }

    @Transactional
    public void removerAssociadoDaRegua(Long associadoId, String usuario) {
        log.info("Removendo associado {} da régua atual", associadoId);
        
        AssociadoRegua associacao = associadoReguaRepository.findByAssociadoIdAndAtivoTrue(associadoId)
            .orElseThrow(() -> new RuntimeException("Associado não possui régua ativa"));
        
        associacao.setAtivo(false);
        associacao.setDataFim(LocalDate.now());
        associacao.setAtualizadoPor(usuario);
        associacao.setAtualizadoEm(LocalDateTime.now());
        associadoReguaRepository.save(associacao);
    }

    @Transactional(readOnly = true)
    public Optional<AssociadoRegua> buscarAssociadoAtivo(Long associadoId) {
        return associadoReguaRepository.findByAssociadoIdAndAtivoTrue(associadoId);
    }

    // ========== MÉTODOS PARA FATURAMENTO CONSOLIDADO ==========

    /**
     * Lista associados da régua que possuem notas SPC no último consolidado
     * Busca automaticamente o maior vencimento/consolidado
     * 
     * @param reguaId ID da régua
     * @param nome Filtro por nome (opcional)
     * @param cnpjCpf Filtro por CNPJ/CPF (opcional)
     * @param pageable Paginação
     * @return Page com os associados consolidados
     */
    @Transactional(readOnly = true)
    public Page<AssociadoResumoDTO> listarAssociadosConsolidadoPaginado(
            Long reguaId, 
            String nome, 
            String cnpjCpf, 
            Pageable pageable) {
        
        log.info("📋 Buscando associados CONSOLIDADOS da régua {} - nome: {}, cnpj: {}", 
                reguaId, nome, cnpjCpf);
        
        // Verificar se a régua existe
        reguaFaturamentoRepository.findById(reguaId)
                .orElseThrow(() -> new ResourceNotFoundException("Régua não encontrada: " + reguaId));
        
        try {
            Page<Object[]> resultPage = associadoReguaRepository
                    .findAssociadosConsolidadoPorReguaNative(reguaId, nome, cnpjCpf, pageable);
            
            List<AssociadoResumoDTO> dtos = resultPage.getContent().stream()
                    .map(row -> {
                        AssociadoResumoDTO dto = new AssociadoResumoDTO();
                        
                        // Converter com segurança cada campo
                        dto.setId(row[0] != null ? ((Number) row[0]).longValue() : null);
                        dto.setNomeRazao(row[1] != null ? row[1].toString() : null);
                        dto.setCnpjCpf(row[2] != null ? row[2].toString() : null);
                        dto.setCodigoSpc(row[3] != null ? row[3].toString() : null);
                        dto.setStatus(row[4] != null ? row[4].toString() : null);
                        
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("✅ Encontrados {} associados consolidados (total: {})", 
                    dtos.size(), resultPage.getTotalElements());
            
            return new PageImpl<>(dtos, pageable, resultPage.getTotalElements());
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar associados consolidados: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Lista todos os IDs dos associados consolidados da régua
     * 
     * @param reguaId ID da régua
     * @return Lista de IDs dos associados com notas SPC
     */
    @Transactional(readOnly = true)
    public List<Long> listarTodosIdsAssociadosConsolidado(Long reguaId) {
        log.info("📌 Buscando IDs consolidados - régua ID: {}", reguaId);
        
        // Buscar todos os IDs da régua
        List<Long> idsAssociadosRegua = associadoReguaRepository.findAssociadoIdsByReguaId(reguaId);
        log.info("📊 IDs na régua: {}", idsAssociadosRegua.size());
        
        // Buscar IDs com notas SPC
        List<Long> idsComNotas = notaDebitoSPCRepository.findAssociadoIdsComNotasConsolidado();
        log.info("📊 IDs com notas SPC: {}", idsComNotas.size());
        
        // Interseção: IDs que estão na régua E têm notas SPC
        List<Long> resultado = idsAssociadosRegua.stream()
            .filter(idsComNotas::contains)
            .collect(Collectors.toList());
        
        log.info("✅ IDs após filtro consolidado: {}", resultado.size());
        
        return resultado;
    }
    
    /**
     * Busca associados por lista de IDs com paginação e filtros
     * 
     * @param ids Lista de IDs dos associados
     * @param nome Filtro por nome (opcional)
     * @param cnpjCpf Filtro por CNPJ/CPF (opcional)
     * @param pageable Paginação
     * @return Page com os associados
     */
    @Transactional(readOnly = true)
    public Page<AssociadoResumoDTO> buscarAssociadosPorIdsPaginado(
            List<Long> ids, 
            String nome, 
            String cnpjCpf, 
            Pageable pageable) {
        
        log.info("🔍 Buscando associados por IDs - total: {}, página: {}, size: {}", 
            ids.size(), pageable.getPageNumber(), pageable.getPageSize());
        
        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }
        
        Page<Associado> associadosPage = associadoRepository.findByIdInAndFiltros(
            ids, nome, cnpjCpf, pageable);
        
        return associadosPage.map(this::toAssociadoResumoDTO);
    }

    // ========== MÉTODOS EXISTENTES (MANTIDOS) ==========

    @Transactional(readOnly = true)
    public List<AssociadoResumoDTO> listarAssociadosNaoFaturados(Long reguaId, Integer mes, Integer ano) {
        log.info("📋 Listando associados NÃO FATURADOS da régua ID: {} no período {}/{}", reguaId, mes, ano);
        
        List<AssociadoRegua> associadosRegua = associadoReguaRepository.findByReguaIdAndAtivoTrue(reguaId);
        
        List<Long> associadosComFatura = faturaRepository.findAssociadosComFaturaNoPeriodo(mes, ano);
        
        log.info("📊 Associados já faturados no período: {}", associadosComFatura.size());
        
        return associadosRegua.stream()
            .map(AssociadoRegua::getAssociado)
            .filter(associado -> !associadosComFatura.contains(associado.getId()))
            .map(this::toAssociadoResumoDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AssociadoResumoDTO> listarTodosAssociadosResumo(Long reguaId) {
        log.info("📋 Listando TODOS os associados da régua ID: {}", reguaId);
        
        List<AssociadoRegua> associadosRegua = associadoReguaRepository.findByReguaIdAndAtivoTrue(reguaId);
        
        return associadosRegua.stream()
            .map(AssociadoRegua::getAssociado)
            .map(this::toAssociadoResumoDTO)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<Long> listarTodosIdsAssociadosPorRegua(Long reguaId) {
        log.info("📋 Listando TODOS os IDs dos associados da régua ID: {}", reguaId);
        
        return associadoReguaRepository.findByReguaIdAndAtivoTrue(reguaId).stream()
            .map(ar -> ar.getAssociado().getId())
            .collect(Collectors.toList());
    }

    // ========== CONVERSORES ==========
    
    /**
     * Converte ReguaFaturamento para DTO
     */
    public ReguaFaturamentoDTO toDTO(ReguaFaturamento entity) {
        if (entity == null) return null;
        
        ReguaFaturamentoDTO dto = new ReguaFaturamentoDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        dto.setDiaEmissao(entity.getDiaEmissao());
        dto.setPeriodo(entity.getPeriodo());
        dto.setSequencia(entity.getSequencia());
        dto.setTipoArquivo(entity.getTipoArquivo());
        dto.setOrdemImportacao(entity.getOrdemImportacao());
        dto.setEhPadrao(entity.getEhPadrao());
        dto.setAtivo(entity.getAtivo());
        dto.setCor(entity.getCor());
        dto.setIcone(entity.getIcone());
        dto.setCriadoEm(entity.getCriadoEm());
        dto.setCriadoPor(entity.getCriadoPor());
        dto.setAtualizadoEm(entity.getAtualizadoEm());
        dto.setAtualizadoPor(entity.getAtualizadoPor());
        
        if (entity.getTiposArquivo() != null && !entity.getTiposArquivo().isEmpty()) {
            List<TipoArquivoReguaDTO> tiposDTO = entity.getTiposArquivo().stream()
                .map(t -> new TipoArquivoReguaDTO(t.getId(), t.getTipo(), t.getOrdem()))
                .collect(Collectors.toList());
            dto.setTiposArquivo(tiposDTO);
        }
        
        return dto;
    }
    
    /**
     * Converte AssociadoRegua para DTO
     */
    private AssociadoReguaDTO toAssociadoReguaDTO(AssociadoRegua entity) {
        if (entity == null) return null;
        
        AssociadoReguaDTO dto = new AssociadoReguaDTO();
        dto.setId(entity.getId());
        dto.setAssociadoId(entity.getAssociado().getId());
        dto.setAssociadoNome(entity.getAssociado().getNomeRazao());
        dto.setAssociadoCodigoSpc(entity.getAssociado().getCodigoSpc());
        dto.setReguaId(entity.getRegua().getId());
        dto.setReguaNome(entity.getRegua().getNome());
        dto.setDataInicio(entity.getDataInicio());
        dto.setDataFim(entity.getDataFim());
        dto.setAtivo(entity.getAtivo());
        dto.setMotivoMigracao(entity.getMotivoMigracao());
        dto.setObservacao(entity.getObservacao());
        dto.setCriadoEm(entity.getCriadoEm());
        dto.setCriadoPor(entity.getCriadoPor());
        return dto;
    }
    
    /**
     * Converte Associado para AssociadoResumoDTO
     */
    private AssociadoResumoDTO toAssociadoResumoDTO(Associado associado) {
        if (associado == null) return null;
        
        AssociadoResumoDTO dto = new AssociadoResumoDTO();
        dto.setId(associado.getId());
        dto.setCodigoSpc(associado.getCodigoSpc());
        dto.setNomeRazao(associado.getNomeRazao());
        dto.setNomeFantasia(associado.getNomeFantasia());
        dto.setCnpjCpf(associado.getCnpjCpf());
        dto.setTipoPessoa(associado.getTipoPessoa());
        dto.setStatus(associado.getStatus());
        dto.setDataFiliacao(associado.getDataFiliacao());
        dto.setDataCadastro(associado.getDataCadastro());
        dto.setDataInativacao(associado.getDataInativacao());
        dto.setDataInicioSuspensao(associado.getDataInicioSuspensao());
        dto.setDataFimSuspensao(associado.getDataFimSuspensao());
        dto.setMotivoInativacao(associado.getMotivoInativacao());
        dto.setMotivoSuspensao(associado.getMotivoSuspensao());
        
        if (associado.getVendedor() != null) {
            dto.setVendedorId(associado.getVendedor().getId());
            dto.setVendedorNome(associado.getVendedor().getNomeRazao());
        }
        
        if (associado.getVendedorExterno() != null) {
            dto.setVendedorExternoId(associado.getVendedorExterno().getId());
            dto.setVendedorExternoNome(associado.getVendedorExterno().getNomeRazao());
        }
        
        if (associado.getPlano() != null) {
            dto.setPlanoId(associado.getPlano().getId());
            dto.setPlanoTitulo(associado.getPlano().getPlano());
        }
        
        if (associado.getCategoria() != null) {
            dto.setCategoriaId(associado.getCategoria().getId());
            dto.setCategoriaNome(associado.getCategoria().getDescricao());
        }
        
        return dto;
    }
}