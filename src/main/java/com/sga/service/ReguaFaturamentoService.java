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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoReguaDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.ReguaFaturamentoDTO;
import com.sga.dto.TipoArquivoReguaDTO;
import com.sga.model.Associado;
import com.sga.model.AssociadoRegua;
import com.sga.model.ReguaFaturamento;
import com.sga.model.TipoArquivoRegua;
import com.sga.repository.AssociadoReguaRepository;
import com.sga.repository.AssociadoRepository;
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
    
    // 🔥 NOVO MÉTODO - Buscar régua e retornar DTO
    @Transactional(readOnly = true)
    public ReguaFaturamentoDTO buscarReguaDTOPorId(Long id) {
        log.info("Buscando régua DTO por ID: {}", id);
        
        Optional<ReguaFaturamento> reguaOpt = reguaFaturamentoRepository.findById(id);
        
        if (reguaOpt.isEmpty()) {
            log.warn("Régua não encontrada com ID: {}", id);
            return null;
        }
        
        ReguaFaturamento regua = reguaOpt.get();
        
        // Forçar carregamento dos tipos de arquivo
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

 // src/main/java/com/sga/service/ReguaFaturamentoService.java

    @Transactional
    public ReguaFaturamento atualizarRegua(Long id, ReguaFaturamento regua, String usuario) {
        log.info("Atualizando régua ID: {}", id);
        
        ReguaFaturamento existing = reguaFaturamentoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Régua não encontrada"));
        
        // Atualizar campos básicos
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
        
        // 🔥 LIMPAR TIPOS ANTIGOS (usando o repository diretamente)
        if (existing.getTiposArquivo() != null && !existing.getTiposArquivo().isEmpty()) {
            tipoArquivoReguaRepository.deleteByReguaId(id);
            existing.getTiposArquivo().clear();
            log.info("🗑️ Tipos antigos removidos");
        }
        
        // 🔥 ADICIONAR NOVOS TIPOS
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
            
            // Salvar os tipos diretamente no repositório
            tipoArquivoReguaRepository.saveAll(novosTipos);
            existing.setTiposArquivo(novosTipos);
        }
        
        // Salvar a régua
        ReguaFaturamento saved = reguaFaturamentoRepository.save(existing);
        
        // 🔥 FORÇAR FLUSH E RECARREGAR
        reguaFaturamentoRepository.flush();
        
        // Recarregar para garantir que os tipos estão lá
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
        
        // 🔥 IMPORTANTE: Buscar os AssociadoRegua primeiro (sem ordenação problemática)
        Page<AssociadoRegua> associadosReguaPage = associadoReguaRepository.findByReguaIdAndAtivoTrue(reguaId, pageable);
        
        // Converter para Page<AssociadoResumoDTO>
        return associadosReguaPage.map(ar -> {
            Associado associado = ar.getAssociado();
            AssociadoResumoDTO dto = new AssociadoResumoDTO();
            
            // Mapear campos básicos
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
            
            // 🔥 Buscar faturamento mínimo (se existir)
            if (associado.getDefinicoesFaturamento() != null && !associado.getDefinicoesFaturamento().isEmpty()) {
                dto.setFaturamentoMinimo(associado.getDefinicoesFaturamento().get(0).getAssociado().getFaturamentoMinimo());
            }
            
            // Mapear relacionamentos
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

    // ========== CONVERSORES ==========
    
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
}