// src/main/java/com/sga/service/SincronizacaoNotificacaoService.java

package com.sga.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.SincronizacaoNotificacao;
import com.sga.repository.SincronizacaoNotificacaoRepository;
import com.sga.repository.NotificacaoAssociadoRepository;

@Service
public class SincronizacaoNotificacaoService {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoNotificacaoService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private SincronizacaoNotificacaoRepository sincronizacaoRepository;

    @Autowired
    private NotificacaoAssociadoRepository notificacaoAssociadoRepository;

    /**
     * 🔥 SALVAR REGISTRO DE SINCRONIZAÇÃO
     */
    @Transactional
    public SincronizacaoNotificacao salvarSincronizacao(
            LocalDate dataInicio,
            LocalDate dataFim,
            String codigoAssociado,
            Integer totalAssociados,
            Integer totalRegistros,
            String usuario) {

        log.info("📝 Salvando registro de sincronização - Início: {}, Fim: {}, Total: {}",
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), totalAssociados);

        SincronizacaoNotificacao sincronizacao = new SincronizacaoNotificacao();
        sincronizacao.setDataInicio(dataInicio);
        sincronizacao.setDataFim(dataFim);
        sincronizacao.setCodigoAssociado(codigoAssociado);
        sincronizacao.setTotalAssociados(totalAssociados);
        sincronizacao.setTotalRegistros(totalRegistros);
        sincronizacao.setUsuario(usuario);
        sincronizacao.setStatus("CONCLUIDO");
        sincronizacao.setObservacao("Sincronização realizada com sucesso");

        return sincronizacaoRepository.save(sincronizacao);
    }

    /**
     * 🔥 VERIFICAR SE JÁ EXISTE SINCRONIZAÇÃO
     */
    public boolean verificarSincronizacaoExistente(LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {
        return sincronizacaoRepository.existsByPeriodoAndAssociado(dataInicio, dataFim, codigoAssociado);
    }

    /**
     * 🔥 LISTAR HISTÓRICO DE SINCRONIZAÇÕES
     */
    public Page<SincronizacaoNotificacao> listarSincronizacoes(Pageable pageable) {
        return sincronizacaoRepository.findAllByOrderByDataSincronizacaoDesc(pageable);
    }

    /**
     * 🔥 DESFAZER SINCRONIZAÇÃO POR ID
     */
    @Transactional
    public Map<String, Object> desfazerSincronizacao(Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            SincronizacaoNotificacao sincronizacao = sincronizacaoRepository.findById(id)
                    .orElse(null);

            if (sincronizacao == null) {
                response.put("success", false);
                response.put("message", "Sincronização não encontrada");
                return response;
            }

            // 🔥 VERIFICAR SE PODE DESFAZER
            Map<String, Object> verificacao = verificarPodeDesfazer(id);
            if (!(Boolean) verificacao.get("podeDesfazer")) {
                response.put("success", false);
                response.put("message", verificacao.get("motivo"));
                return response;
            }

            // 🔥 REMOVER DADOS DA TABELA LOCAL
            int registrosRemovidos = notificacaoAssociadoRepository.deleteByPeriodo(
                    sincronizacao.getDataInicio(),
                    sincronizacao.getDataFim(),
                    sincronizacao.getCodigoAssociado());

            // 🔥 MARCAR COMO CANCELADO
            sincronizacao.setStatus("CANCELADO");
            sincronizacao.setObservacao("Sincronização desfeita pelo usuário");
            sincronizacaoRepository.save(sincronizacao);

            response.put("success", true);
            response.put("message", "Sincronização desfeita com sucesso!");
            response.put("registrosRemovidos", registrosRemovidos);
            response.put("sincronizacaoId", id);

            log.info("✅ Sincronização {} desfeita - {} registros removidos", id, registrosRemovidos);

        } catch (Exception e) {
            log.error("❌ Erro ao desfazer sincronização {}: {}", id, e.getMessage());
            response.put("success", false);
            response.put("message", "Erro ao desfazer sincronização: " + e.getMessage());
        }

        return response;
    }

    /**
     * 🔥 DESFAZER POR PERÍODO
     */
    @Transactional
    public Map<String, Object> desfazerSincronizacaoPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 🔥 REMOVER DADOS DA TABELA LOCAL
            int registrosRemovidos = notificacaoAssociadoRepository.deleteByPeriodo(
                    dataInicio, dataFim, codigoAssociado);

            // 🔥 MARCAR SINCRONIZAÇÕES COMO CANCELADAS
            List<SincronizacaoNotificacao> sincronizacoes = sincronizacaoRepository.findByPeriodo(dataInicio, dataFim);
            for (SincronizacaoNotificacao s : sincronizacoes) {
                s.setStatus("CANCELADO");
                s.setObservacao("Sincronização desfeita pelo usuário (período)");
                sincronizacaoRepository.save(s);
            }

            response.put("success", true);
            response.put("message", "Sincronização do período desfeita com sucesso!");
            response.put("registrosRemovidos", registrosRemovidos);
            response.put("sincronizacoesAfetadas", sincronizacoes.size());

            log.info("✅ Sincronização do período desfeita - {} registros removidos, {} sincronizações afetadas",
                    registrosRemovidos, sincronizacoes.size());

        } catch (Exception e) {
            log.error("❌ Erro ao desfazer sincronização por período: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Erro ao desfazer sincronização: " + e.getMessage());
        }

        return response;
    }

    /**
     * 🔥 VERIFICAR SE PODE DESFAZER
     */
    public Map<String, Object> verificarPodeDesfazer(Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            SincronizacaoNotificacao sincronizacao = sincronizacaoRepository.findById(id)
                    .orElse(null);

            if (sincronizacao == null) {
                response.put("podeDesfazer", false);
                response.put("motivo", "Sincronização não encontrada");
                return response;
            }

            // 🔥 VERIFICAR SE JÁ FOI FATURADO
            boolean temFatura = notificacaoAssociadoRepository.existsFaturadoByPeriodo(
                    sincronizacao.getDataInicio(),
                    sincronizacao.getDataFim(),
                    sincronizacao.getCodigoAssociado());

            if (temFatura) {
                response.put("podeDesfazer", false);
                response.put("motivo", "Existem notificações já faturadas para este período");
                response.put("detalhe", "Não é possível desfazer sincronização com notificações faturadas");
                return response;
            }

            // 🔥 VERIFICAR STATUS
            if ("CANCELADO".equals(sincronizacao.getStatus())) {
                response.put("podeDesfazer", false);
                response.put("motivo", "Sincronização já foi cancelada anteriormente");
                return response;
            }

            response.put("podeDesfazer", true);
            response.put("motivo", "Pode desfazer a sincronização");
            response.put("totalRegistros", sincronizacao.getTotalRegistros());
            response.put("totalAssociados", sincronizacao.getTotalAssociados());

        } catch (Exception e) {
            log.error("❌ Erro ao verificar se pode desfazer: {}", e.getMessage());
            response.put("podeDesfazer", false);
            response.put("motivo", "Erro ao verificar: " + e.getMessage());
        }

        return response;
    }
}