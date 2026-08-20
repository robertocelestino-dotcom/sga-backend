// src/main/java/com/sga/service/MigracaoReguaService.java

package com.sga.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; // 🔥 IMPORT ADICIONADO
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.FaturaResumoDTO;
import com.sga.dto.HistoricoMigracaoDTO;
import com.sga.dto.MigracaoReguaRequest;
import com.sga.dto.MigracaoReguaResponse;
import com.sga.model.Associado;
import com.sga.model.AssociadoRegua;
import com.sga.model.Fatura;
import com.sga.model.HistoricoMigracaoRegua;
import com.sga.model.ReguaFaturamento;
import com.sga.repository.AssociadoReguaRepository;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.HistoricoMigracaoReguaRepository;
import com.sga.repository.ReguaFaturamentoRepository;

@Service
public class MigracaoReguaService {

    private static final Logger log = LoggerFactory.getLogger(MigracaoReguaService.class);
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private AssociadoReguaRepository associadoReguaRepository;

    @Autowired
    private AssociadoRepository associadoRepository;

    @Autowired
    private ReguaFaturamentoRepository reguaFaturamentoRepository;

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private HistoricoMigracaoReguaRepository historicoMigracaoRepository;

    @Autowired
    private SistemaLogService sistemaLogService;

    // ============================================================
    // VERIFICAR MIGRAÇÃO
    // ============================================================

    @Transactional(readOnly = true)
    public MigracaoReguaResponse verificarMigracao(Long associadoId, Long novaReguaId) {
        log.info("🔍 Verificando migração do associado {} para régua {}", associadoId, novaReguaId);

        Associado associado = associadoRepository.findById(associadoId)
                .orElseThrow(() -> new RuntimeException("Associado não encontrado com ID: " + associadoId));

        ReguaFaturamento reguaDestino = reguaFaturamentoRepository.findById(novaReguaId)
                .orElseThrow(() -> new RuntimeException("Régua destino não encontrada com ID: " + novaReguaId));

        AssociadoRegua associacaoAtual = associadoReguaRepository
                .findByAssociadoIdAndAtivoTrue(associadoId)
                .orElse(null);

        MigracaoReguaResponse response = new MigracaoReguaResponse();
        response.setAssociadoId(associadoId);
        response.setAssociadoNome(associado.getNomeRazao());

        if (associacaoAtual == null) {
            response.setReguaOrigemId(null);
            response.setReguaOrigemNome("Nenhuma");
            response.setReguaDestinoId(novaReguaId);
            response.setReguaDestinoNome(reguaDestino.getNome());
            response.setStatus("PODE_MIGRAR");
            response.setMensagem("Associado não possui régua ativa. Pode ser adicionado à nova régua.");
            response.setFaturasPendentes(false);
            response.setTotalFaturasPendentes(0);
            return response;
        }

        Long reguaAtualId = associacaoAtual.getRegua().getId();
        String reguaAtualNome = associacaoAtual.getRegua().getNome();

        response.setReguaOrigemId(reguaAtualId);
        response.setReguaOrigemNome(reguaAtualNome);
        response.setReguaDestinoId(novaReguaId);
        response.setReguaDestinoNome(reguaDestino.getNome());

        if (reguaAtualId.equals(novaReguaId)) {
            response.setStatus("MESMA_REGUA");
            response.setMensagem("Associado já está na régua '" + reguaAtualNome + "'");
            response.setFaturasPendentes(false);
            response.setTotalFaturasPendentes(0);
            return response;
        }

        List<Fatura> faturasPendentes = faturaRepository
                .findByAssociadoIdAndStatus(associadoId, "PENDENTE");

        response.setFaturasPendentes(!faturasPendentes.isEmpty());
        response.setTotalFaturasPendentes(faturasPendentes.size());

        if (!faturasPendentes.isEmpty()) {
            response.setFaturasPendentesList(faturasPendentes.stream()
                    .map(this::toFaturaResumoDTO)
                    .collect(Collectors.toList()));
            response.setStatus("TEM_FATURAS_PENDENTES");
            response.setMensagem("Associado possui " + faturasPendentes.size() + " faturas pendentes");
        } else {
            response.setStatus("PODE_MIGRAR");
            response.setMensagem("Associado pode ser migrado");
        }

        log.info("✅ Verificação concluída: Status={}, Faturas Pendentes={}", 
                response.getStatus(), response.getTotalFaturasPendentes());
        
        return response;
    }

    // ============================================================
    // VERIFICAR DATA DA MIGRAÇÃO
    // ============================================================

    /**
     * 🔥 VERIFICA SE JÁ EXISTE UMA MIGRAÇÃO COM A MESMA DATA
     */
    @Transactional(readOnly = true)
    public boolean existeMigracaoComData(Long associadoId, Long reguaDestinoId, LocalDate dataMigracao) {
        log.info("🔍 Verificando se já existe migração com data {} para associado {} e régua {}", 
                 dataMigracao, associadoId, reguaDestinoId);
        
        // Verificar se existe um registro com a mesma combinação (ativo ou inativo)
        boolean existe = associadoReguaRepository
                .existsByAssociadoIdAndReguaIdAndDataInicio(associadoId, reguaDestinoId, dataMigracao);
        
        if (existe) {
            String dataFormatada = dataMigracao.format(DATE_FORMATTER);
            log.warn("⚠️ Já existe um registro para o associado {} na régua {} com data {}", 
                     associadoId, reguaDestinoId, dataFormatada);
        } else {
            log.info("✅ Nenhuma migração encontrada com a data {}", dataMigracao);
        }
        
        return existe;
    }

    // ============================================================
    // EXECUTAR MIGRAÇÃO
    // ============================================================

    @Transactional
    public MigracaoReguaResponse executarMigracao(MigracaoReguaRequest request, String usuario) {
        log.info("🔄 ========== INICIANDO MIGRAÇÃO ==========");
        log.info("   Associado ID: {}", request.getAssociadoId());
        log.info("   Nova Régua ID: {}", request.getNovaReguaId());
        log.info("   Data Migração: {}", request.getDataMigracao());
        log.info("   Usuário: {}", usuario);
        log.info("   Forçar: {}", request.isForcarMigracao());
        log.info("   Motivo: {}", request.getMotivo());

        Long associadoId = request.getAssociadoId();
        Long novaReguaId = request.getNovaReguaId();
        boolean forcarMigracao = request.isForcarMigracao();
        String motivo = request.getMotivo();
        LocalDate dataMigracao = request.getDataMigracao() != null ? 
                request.getDataMigracao() : LocalDate.now();

        // 1. Buscar associado
        Associado associado = associadoRepository.findById(associadoId)
                .orElseThrow(() -> new RuntimeException("Associado não encontrado com ID: " + associadoId));
        log.info("✅ Associado encontrado: {}", associado.getNomeRazao());

        // 2. Buscar e validar régua destino
        ReguaFaturamento reguaDestino = reguaFaturamentoRepository.findById(novaReguaId)
                .orElseThrow(() -> new RuntimeException("Régua destino não encontrada com ID: " + novaReguaId));
        log.info("✅ Régua destino: {}", reguaDestino.getNome());

        if (reguaDestino.getPermiteMigracao() != null && !reguaDestino.getPermiteMigracao()) {
            throw new RuntimeException("A régua '" + reguaDestino.getNome() + "' NÃO permite migração de associados");
        }

        // 🔥 VALIDAÇÃO: Verificar se já existe migração com a mesma data
        boolean existeMigracaoComData = associadoReguaRepository
                .existsByAssociadoIdAndReguaIdAndDataInicio(associadoId, novaReguaId, dataMigracao);
        
        if (existeMigracaoComData) {
            String dataFormatada = dataMigracao.format(DATE_FORMATTER);
            throw new RuntimeException("Já existe uma migração para este associado na régua '" + 
                                       reguaDestino.getNome() + "' com a data " + dataFormatada + 
                                       ". Por favor, selecione outra data.");
        }

        // 3. Buscar associação atual
        AssociadoRegua associacaoAtual = associadoReguaRepository
                .findByAssociadoIdAndAtivoTrue(associadoId)
                .orElse(null);

        Long reguaAtualId = null;
        String reguaAtualNome = "Nenhuma";
        ReguaFaturamento reguaOrigem = null;

        if (associacaoAtual != null) {
            reguaAtualId = associacaoAtual.getRegua().getId();
            reguaAtualNome = associacaoAtual.getRegua().getNome();
            reguaOrigem = associacaoAtual.getRegua();
            log.info("✅ Régua atual: {}", reguaAtualNome);

            if (reguaAtualId.equals(novaReguaId)) {
                throw new RuntimeException("Associado já está na régua '" + reguaAtualNome + "'");
            }

            if (reguaOrigem.getPermiteMigracao() != null && !reguaOrigem.getPermiteMigracao()) {
                throw new RuntimeException("A régua atual '" + reguaAtualNome + "' NÃO permite remoção de associados");
            }
        } else {
            log.info("ℹ️ Associado não possui régua ativa");
        }

        // 4. Verificar faturas pendentes
        List<Fatura> faturasPendentes = faturaRepository
                .findByAssociadoIdAndStatus(associadoId, "PENDENTE");

        log.info("📋 Faturas pendentes: {}", faturasPendentes.size());

        if (!faturasPendentes.isEmpty() && !forcarMigracao) {
            throw new RuntimeException("Associado possui " + faturasPendentes.size() +
                    " faturas pendentes. Utilize a opção 'forçar migração' para prosseguir.");
        }

        // 5. Inativar associação atual (REMOVER)
        if (associacaoAtual != null) {
            log.info("📌 Inativando associação anterior...");
            associacaoAtual.setAtivo(false);
            associacaoAtual.setDataFim(dataMigracao);
            associacaoAtual.setAtualizadoPor(usuario);
            associacaoAtual.setAtualizadoEm(LocalDateTime.now());
            associadoReguaRepository.save(associacaoAtual);
            log.info("✅ Associação anterior inativada: Régua {}", reguaAtualId);
        }

        // 6. Criar nova associação (INSERIR)
        log.info("📌 Criando nova associação...");
        AssociadoRegua novaAssociacao = new AssociadoRegua();
        novaAssociacao.setAssociado(associado);
        novaAssociacao.setRegua(reguaDestino);
        novaAssociacao.setDataInicio(dataMigracao);
        novaAssociacao.setAtivo(true);
        novaAssociacao.setMigradoDe(reguaAtualId);
        novaAssociacao.setMotivoMigracao(motivo);
        novaAssociacao.setCriadoPor(usuario);
        novaAssociacao.setCriadoEm(LocalDateTime.now());

        if (!faturasPendentes.isEmpty()) {
            novaAssociacao.setObservacao("Migração forçada com " + faturasPendentes.size() + " faturas pendentes");
        }

        AssociadoRegua saved = associadoReguaRepository.save(novaAssociacao);
        log.info("✅ Nova associação criada: ID {}, Régua {}", saved.getId(), reguaDestino.getNome());

        // 7. Registrar histórico
        log.info("📝 Registrando histórico...");
        HistoricoMigracaoRegua historico = new HistoricoMigracaoRegua();
        historico.setAssociado(associado);
        historico.setReguaOrigem(reguaOrigem);
        historico.setReguaDestino(reguaDestino);
        historico.setDataMigracao(LocalDateTime.now());
        historico.setUsuario(usuario);
        historico.setMotivo(motivo);
        historico.setStatus("SUCESSO");
        historico.setFaturasPendentes(faturasPendentes.size());
        historico.setMigracaoForcada(!faturasPendentes.isEmpty());

        if (!faturasPendentes.isEmpty()) {
            historico.setObservacao("Migração forçada - " + faturasPendentes.size() + " faturas pendentes");
        }

        HistoricoMigracaoRegua historicoSalvo = historicoMigracaoRepository.save(historico);
        log.info("✅ Histórico registrado: ID {}", historicoSalvo.getId());

        // 8. Atualizar associado
        log.info("📌 Atualizando associado...");
        try {
            associado.setUltimaMigracao(LocalDateTime.now());
            associado.setUsuarioUltimaMigracao(usuario);
            associadoRepository.save(associado);
            log.info("✅ Associado atualizado");
        } catch (Exception e) {
            log.warn("⚠️ Não foi possível atualizar campos de migração do associado: {}", e.getMessage());
        }

        // 9. Log de auditoria
        try {
            String logMessage = String.format(
                    "MIGRAÇÃO DE RÉGUA: %s → %s | Usuário: %s | Motivo: %s | %s | Faturas pendentes: %d | Data: %s",
                    reguaAtualNome,
                    reguaDestino.getNome(),
                    usuario,
                    motivo != null ? motivo : "Não informado",
                    !faturasPendentes.isEmpty() ? "FORÇADA" : "NORMAL",
                    faturasPendentes.size(),
                    dataMigracao
            );
            sistemaLogService.logAtualizacao("Associado", associadoId.toString(), logMessage);
            log.info("✅ Log de auditoria registrado");
        } catch (Exception e) {
            log.warn("⚠️ Não foi possível registrar log de auditoria: {}", e.getMessage());
        }

        // 10. Montar resposta
        MigracaoReguaResponse response = new MigracaoReguaResponse();
        response.setAssociadoId(associadoId);
        response.setAssociadoNome(associado.getNomeRazao());
        response.setReguaOrigemId(reguaAtualId);
        response.setReguaOrigemNome(reguaAtualNome);
        response.setReguaDestinoId(novaReguaId);
        response.setReguaDestinoNome(reguaDestino.getNome());
        response.setDataMigracao(LocalDateTime.now());
        response.setDataInicio(dataMigracao);
        response.setStatus("SUCESSO");
        response.setMensagem("Migração realizada com sucesso!");
        response.setHistoricoId(historicoSalvo.getId());
        response.setFaturasPendentes(!faturasPendentes.isEmpty());
        response.setTotalFaturasPendentes(faturasPendentes.size());
        response.setMigracaoForcada(!faturasPendentes.isEmpty());

        log.info("✅ ========== MIGRAÇÃO CONCLUÍDA ==========");
        log.info("   Associado: {} → Régua {}", associado.getNomeRazao(), reguaDestino.getNome());
        log.info("   Histórico ID: {}", historicoSalvo.getId());
        log.info("   Data: {}", dataMigracao);

        return response;
    }

    // ============================================================
    // HISTÓRICO DE MIGRAÇÕES
    // ============================================================

    @Transactional(readOnly = true)
    public List<HistoricoMigracaoRegua> buscarHistoricoMigracao(Long associadoId) {
        log.info("📋 Buscando histórico de migrações do associado {}", associadoId);
        return historicoMigracaoRepository.findByAssociadoIdOrderByDataMigracaoDesc(associadoId);
    }

    @Transactional(readOnly = true)
    public List<ReguaFaturamento> listarReguasDisponiveisParaMigracao(Long associadoId) {
        log.info("📋 Listando réguas disponíveis para migração do associado {}", associadoId);

        AssociadoRegua associacaoAtual = associadoReguaRepository
                .findByAssociadoIdAndAtivoTrue(associadoId)
                .orElse(null);

        Long reguaAtualId = associacaoAtual != null ? associacaoAtual.getRegua().getId() : null;

        List<ReguaFaturamento> todasReguas = reguaFaturamentoRepository.findAllAtivas();

        return todasReguas.stream()
                .filter(r -> !r.getId().equals(reguaAtualId))
                .filter(r -> r.getPermiteMigracao() == null || r.getPermiteMigracao())
                .collect(Collectors.toList());
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    private FaturaResumoDTO toFaturaResumoDTO(Fatura fatura) {
        FaturaResumoDTO dto = new FaturaResumoDTO();
        dto.setId(fatura.getId());
        dto.setNumeroFatura(fatura.getNumeroFatura());
        dto.setValorTotal(fatura.getValorTotal());
        dto.setDataEmissao(fatura.getDataEmissao());
        dto.setDataVencimento(fatura.getDataVencimento());
        dto.setStatus(fatura.getStatus());
        return dto;
    }
    
    /**
     * 🔥 BUSCA HISTÓRICO DE MIGRAÇÕES DE UM ASSOCIADO (RETORNA DTO)
     */
    @Transactional(readOnly = true)
    public List<HistoricoMigracaoDTO> buscarHistoricoMigracaoDTO(Long associadoId) {
        log.info("📋 Buscando histórico de migrações do associado {} (DTO)", associadoId);
        
        List<HistoricoMigracaoRegua> historico = historicoMigracaoRepository
                .findByAssociadoIdOrderByDataMigracaoDesc(associadoId);
        
        return historico.stream()
                .map(this::toHistoricoMigracaoDTO)
                .collect(Collectors.toList());
    }
    /**
    * 🔥 CONVERTE HistoricoMigracaoRegua PARA HistoricoMigracaoDTO
    */
   private HistoricoMigracaoDTO toHistoricoMigracaoDTO(HistoricoMigracaoRegua entity) {
       if (entity == null) return null;
       
       HistoricoMigracaoDTO dto = new HistoricoMigracaoDTO();
       dto.setId(entity.getId());
       
       // Associado
       if (entity.getAssociado() != null) {
           dto.setAssociadoId(entity.getAssociado().getId());
           dto.setAssociadoNome(entity.getAssociado().getNomeRazao());
       }
       
       // Régua Origem
       if (entity.getReguaOrigem() != null) {
           dto.setReguaOrigemId(entity.getReguaOrigem().getId());
           dto.setReguaOrigemNome(entity.getReguaOrigem().getNome());
       }
       
       // Régua Destino
       if (entity.getReguaDestino() != null) {
           dto.setReguaDestinoId(entity.getReguaDestino().getId());
           dto.setReguaDestinoNome(entity.getReguaDestino().getNome());
       }
       
       dto.setDataMigracao(entity.getDataMigracao());
       dto.setUsuario(entity.getUsuario());
       dto.setMotivo(entity.getMotivo());
       dto.setStatus(entity.getStatus());
       dto.setObservacao(entity.getObservacao());
       dto.setFaturasPendentes(entity.getFaturasPendentes());
       dto.setMigracaoForcada(entity.getMigracaoForcada());
       
       return dto;
   }

}