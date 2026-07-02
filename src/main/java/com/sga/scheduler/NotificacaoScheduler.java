// src/main/java/com/sga/scheduler/NotificacaoScheduler.java
package com.sga.scheduler;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sga.service.NotificacaoIntegracaoService;

@Component
public class NotificacaoScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoScheduler.class);

    @Autowired
    private NotificacaoIntegracaoService notificacaoIntegracaoService;

    @Scheduled(cron = "${notificacao.scheduler.cron:0 30 1 * * ?}")
    public void sincronizarNotificacoesAutomatico() {
        log.info("🔄 Iniciando sincronização automática de notificações...");

        try {
            LocalDate data = LocalDate.now().minusMonths(1);
            Integer mes = data.getMonthValue();
            Integer ano = data.getYear();  // 🔥 CORRIGIDO: getFullYear() → getYear()

            int processados = notificacaoIntegracaoService.sincronizarNotificacoes(mes, ano, null);
            log.info("✅ Sincronização automática concluída! {} associados processados", processados);

        } catch (Exception e) {
            log.error("❌ Erro na sincronização automática: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 30 2 * * ?")
    public void sincronizarNotificacoesPorPeriodoAutomatico() {
        log.info("🔄 Iniciando sincronização automática por período (mês atual)...");

        try {
            LocalDate data = LocalDate.now();
            Integer mes = data.getMonthValue();
            Integer ano = data.getYear();  // 🔥 CORRIGIDO: getFullYear() → getYear()

            int processados = notificacaoIntegracaoService.sincronizarNotificacoes(mes, ano, null);
            log.info("✅ Sincronização por período concluída! {} associados processados", processados);

        } catch (Exception e) {
            log.error("❌ Erro na sincronização por período: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 30 3 * * ?")
    public void sincronizarAgrupadoPorPeriodoAutomatico() {
        log.info("🔄 Iniciando sincronização agrupada por período...");

        try {
            LocalDate data = LocalDate.now();
            Integer mes = data.getMonthValue();
            Integer ano = data.getYear();  // 🔥 CORRIGIDO: getFullYear() → getYear()

            int processados = notificacaoIntegracaoService.sincronizarNotificacoesAgrupadas(mes, ano, null);
            log.info("✅ Sincronização agrupada por período concluída! {} associados processados", processados);

        } catch (Exception e) {
            log.error("❌ Erro na sincronização agrupada por período: {}", e.getMessage(), e);
        }
    }
}