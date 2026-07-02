package com.sga.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.NotificacaoAssociadoDTO;
import com.sga.dto.NotificacaoSumarizadaDTO;
import com.sga.service.NotificacaoIntegracaoService;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoController.class);

    @Autowired
    private NotificacaoIntegracaoService notificacaoIntegracaoService;

    // ========== SINCRONIZAÇÃO ==========

    @PostMapping("/sincronizar")
    public ResponseEntity<Map<String, Object>> sincronizarNotificacoes(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("📥 Solicitando sincronização - Mês: {}, Ano: {}, Código: {}", mes, ano, codigoAssociado);

        if (mes == null || ano == null) {
            LocalDate data = LocalDate.now().minusMonths(1);
            mes = data.getMonthValue();
            ano = data.getYear();
        }

        int processados = notificacaoIntegracaoService.sincronizarNotificacoes(mes, ano, codigoAssociado);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sincronização concluída!");
        response.put("mes", mes);
        response.put("ano", ano);
        response.put("associadosProcessados", processados);
        response.put("dataSincronizacao", LocalDateTime.now());

        log.info("✅ Sincronização concluída - {} associados processados", processados);

        return ResponseEntity.ok(response);
    }

    // ========== CONSULTAS ==========

    @GetMapping("/sumarizadas")
    public ResponseEntity<List<NotificacaoSumarizadaDTO>> buscarNotificacoesSumarizadas(
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🔍 Buscando notificações sumarizadas - Código: {}", codigoAssociado);

        List<NotificacaoSumarizadaDTO> resultado = notificacaoIntegracaoService
                .buscarNotificacoesSumarizadas(codigoAssociado);

        log.info("📊 Encontradas {} notificações sumarizadas", resultado.size());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/agrupadas")
    public ResponseEntity<List<NotificacaoSumarizadaDTO>> buscarNotificacoesAgrupadas(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("📊 Buscando notificações agrupadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        List<NotificacaoSumarizadaDTO> resultado = notificacaoIntegracaoService
                .buscarNotificacoesAgrupadas(mes, ano, codigoAssociado);

        log.info("📊 Encontradas {} notificações agrupadas", resultado.size());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/detalhadas")
    public ResponseEntity<List<NotificacaoSumarizadaDTO>> buscarNotificacoesDetalhadas(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            @RequestParam String codigoAssociado) {

        log.info("📋 Buscando notificações detalhadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        List<NotificacaoSumarizadaDTO> resultado = notificacaoIntegracaoService
                .buscarNotificacoesDetalhadas(mes, ano, codigoAssociado);

        log.info("📋 Encontradas {} notificações detalhadas", resultado.size());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/associado/{associadoId}")
    public ResponseEntity<NotificacaoAssociadoDTO> buscarNotificacoesPorAssociado(
            @PathVariable Long associadoId,
            @RequestParam Integer mes,
            @RequestParam Integer ano) {

        log.info("🔍 Buscando notificações do associado {} - {}/{}", associadoId, mes, ano);

        NotificacaoAssociadoDTO notificacao = notificacaoIntegracaoService
                .buscarNotificacoesPorAssociadoPeriodo(associadoId, mes, ano);

        if (notificacao == null) {
            log.warn("⚠️ Nenhuma notificação encontrada para associado {} - {}/{}", associadoId, mes, ano);
        } else {
            log.info("✅ Notificações encontradas para associado {}: Total: {}, SMS: {}, E-mail: {}, Cartas: {}",
                    associadoId,
                    notificacao.getTotalRegistros(),
                    notificacao.getSmsTotal(),
                    notificacao.getEmailsTotal(),
                    notificacao.getCartasTotal());
        }

        return ResponseEntity.ok(notificacao);
    }

    @GetMapping("/nao-processados")
    public ResponseEntity<List<NotificacaoAssociadoDTO>> buscarNaoProcessados(
            @RequestParam Integer mes,
            @RequestParam Integer ano) {

        log.info("🔍 Buscando notificações não processadas - {}/{}", mes, ano);

        List<NotificacaoAssociadoDTO> resultado = notificacaoIntegracaoService
                .buscarNaoProcessados(mes, ano);

        log.info("📊 Encontradas {} notificações não processadas", resultado.size());

        return ResponseEntity.ok(resultado);
    }

    // ========== CONSULTAS POR PERÍODO ==========

    @GetMapping("/por-periodo")
    public ResponseEntity<List<NotificacaoSumarizadaDTO>> buscarNotificacoesPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("📅 Buscando notificações por período - Início: {}, Fim: {}, Código: {}",
                dataInicio, dataFim, codigoAssociado);

        List<NotificacaoSumarizadaDTO> resultado = notificacaoIntegracaoService
                .buscarNotificacoesPorPeriodo(dataInicio, dataFim, codigoAssociado);

        log.info("📅 Encontradas {} notificações no período", resultado.size());

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/agrupadas-por-periodo")
    public ResponseEntity<List<NotificacaoSumarizadaDTO>> buscarNotificacoesAgrupadasPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("📊 Buscando notificações agrupadas por período - Início: {}, Fim: {}, Código: {}",
                dataInicio, dataFim, codigoAssociado);

        List<NotificacaoSumarizadaDTO> resultado = notificacaoIntegracaoService
                .buscarNotificacoesAgrupadasPorPeriodo(dataInicio, dataFim, codigoAssociado);

        log.info("📊 Encontradas {} notificações agrupadas no período", resultado.size());

        return ResponseEntity.ok(resultado);
    }

    // ========== SINCRONIZAÇÃO POR PERÍODO ==========

    @PostMapping("/sincronizar-por-periodo")
    public ResponseEntity<Map<String, Object>> sincronizarPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🔄 Sincronizando por período - Início: {}, Fim: {}", dataInicio, dataFim);

        int processados = notificacaoIntegracaoService
                .sincronizarNotificacoesPorPeriodo(dataInicio, dataFim, codigoAssociado);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sincronização por período concluída!");
        response.put("dataInicio", dataInicio);
        response.put("dataFim", dataFim);
        response.put("associadosProcessados", processados);
        response.put("dataSincronizacao", LocalDateTime.now());

        log.info("✅ Sincronização por período concluída - {} associados processados", processados);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sincronizar-agrupado-periodo")
    public ResponseEntity<Map<String, Object>> sincronizarAgrupadoPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🔄 Sincronizando agrupado por período - Início: {}, Fim: {}", dataInicio, dataFim);

        int processados = notificacaoIntegracaoService
                .sincronizarNotificacoesAgrupadasPorPeriodo(dataInicio, dataFim, codigoAssociado);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sincronização agrupada por período concluída!");
        response.put("dataInicio", dataInicio);
        response.put("dataFim", dataFim);
        response.put("associadosProcessados", processados);
        response.put("dataSincronizacao", LocalDateTime.now());

        log.info("✅ Sincronização agrupada por período concluída - {} associados processados", processados);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sincronizar-agrupado")
    public ResponseEntity<Map<String, Object>> sincronizarAgrupado(
            @RequestParam Integer mes,
            @RequestParam Integer ano,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🔄 Sincronizando notificações agrupadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        int processados = notificacaoIntegracaoService
                .sincronizarNotificacoesAgrupadas(mes, ano, codigoAssociado);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sincronização agrupada concluída!");
        response.put("mes", mes);
        response.put("ano", ano);
        response.put("associadosProcessados", processados);
        response.put("dataSincronizacao", LocalDateTime.now());

        log.info("✅ Sincronização agrupada concluída - {} associados processados", processados);

        return ResponseEntity.ok(response);
    }

    // ========== HEALTH CHECK ==========

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        log.info("🏥 Health check - NotificacaoController");

        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "NotificacaoController");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
    
    /**
     * 🔥 Dispara sincronização manual via scheduler
     */
    @PostMapping("/sincronizar-manual")
    public ResponseEntity<Map<String, Object>> sincronizarManual(
            @RequestParam(required = false) Integer mes,
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🔄 Disparando sincronização manual...");

        if (mes == null || ano == null) {
            LocalDate data = LocalDate.now();
            mes = data.getMonthValue();
            ano = data.getYear();
        }

        int processados = notificacaoIntegracaoService.sincronizarNotificacoes(mes, ano, codigoAssociado);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Sincronização manual concluída!");
        response.put("mes", mes);
        response.put("ano", ano);
        response.put("associadosProcessados", processados);
        response.put("dataSincronizacao", LocalDateTime.now());

        log.info("✅ Sincronização manual concluída - {} associados processados", processados);

        return ResponseEntity.ok(response);
    }
}