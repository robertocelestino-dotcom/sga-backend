// src/main/java/com/sga/service/RmExportBatchService.java

package com.sga.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sga.model.Fatura;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class RmExportBatchService {

    private static final Logger log = LoggerFactory.getLogger(RmExportBatchService.class);

    private static final int BATCH_SIZE = 200;

    @Autowired
    private RmExportService rmExportService;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;

    @Autowired
    @Qualifier("rmExecutor")
    private Executor rmExecutor;

    /**
     * 🔥 EXPORTA NOTAS PARA RM EM LOTE PARALELO
     */
    @Async("rmExecutor")
    public CompletableFuture<RmExportResultado> exportarParaRmEmLote(
            List<NotaDebitoSPC> notas,
            String usuario) {

        long inicio = System.currentTimeMillis();
        log.info("📤 Iniciando exportação para RM de {} notas", notas.size());

        if (notas.isEmpty()) {
            log.info("✅ Nenhuma nota para exportar RM");
            return CompletableFuture.completedFuture(new RmExportResultado(0, 0, 0));
        }

        try {
            // 🔥 GERAR ARQUIVO RM COMPLETO
            byte[] arquivoRm = rmExportService.exportarParaRm(notas);

            log.info("✅ Arquivo RM gerado com sucesso! Tamanho: {} bytes", arquivoRm.length);

            // 🔥 MARCAR FATURAS COMO PROCESSADAS RM
            marcarFaturasComoProcessadasRm(notas, usuario);

            return CompletableFuture.completedFuture(new RmExportResultado(notas.size(), notas.size(), 0));

        } catch (Exception e) {
            log.error("❌ Erro ao gerar arquivo RM: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(new RmExportResultado(notas.size(), 0, notas.size()));
        }
    }

    /**
     * 🔥 EXPORTA FATURAS PARA RM EM LOTE PARALELO
     */
    @Async("rmExecutor")
    public CompletableFuture<RmExportResultado> exportarFaturasParaRmEmLote(
            List<Fatura> faturas,
            String usuario) {

        log.info("📤 Buscando notas para {} faturas", faturas.size());

        // 🔥 CORRIGIDO: Extrair os notaDebitoId das faturas
        List<Long> notaIds = faturas.stream()
            .map(Fatura::getNotaDebitoId)
            .filter(id -> id != null)
            .collect(Collectors.toList());

        if (notaIds.isEmpty()) {
            log.warn("⚠️ Nenhuma fatura com notaDebitoId associado");
            return CompletableFuture.completedFuture(new RmExportResultado(0, 0, 0));
        }

        // 🔥 Buscar notas pelos IDs
        List<NotaDebitoSPC> notas = notaDebitoRepository.findByIdIn(notaIds);

        if (notas.isEmpty()) {
            log.warn("⚠️ Nenhuma nota encontrada para as faturas");
            return CompletableFuture.completedFuture(new RmExportResultado(0, 0, 0));
        }

        return exportarParaRmEmLote(notas, usuario);
    }

    /**
     * Marca as faturas como processadas no RM
     */
    private void marcarFaturasComoProcessadasRm(List<NotaDebitoSPC> notas, String usuario) {
        // Implementar se necessário
    }

    /**
     * Resultado da exportação RM
     */
    public static class RmExportResultado {
        private int total;
        private int sucessos;
        private int erros;

        public RmExportResultado(int total, int sucessos, int erros) {
            this.total = total;
            this.sucessos = sucessos;
            this.erros = erros;
        }

        public int getTotal() { return total; }
        public int getSucessos() { return sucessos; }
        public int getErros() { return erros; }
    }
}