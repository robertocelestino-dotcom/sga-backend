// src/main/java/com/sga/service/PdfBatchService.java

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

@Service
public class PdfBatchService {

    private static final Logger log = LoggerFactory.getLogger(PdfBatchService.class);

    private static final int BATCH_SIZE = 50;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    @Qualifier("pdfExecutor")
    private Executor pdfExecutor;

    /**
     * 🔥 GERA PDFS EM LOTE DE FORMA PARALELA
     */
    @Async("pdfExecutor")
    public CompletableFuture<List<PdfResultado>> gerarPdfsEmLote(
            List<Fatura> faturas,
            List<NotaDebitoSPC> notas) {

        long inicio = System.currentTimeMillis();
        log.info("📄 Iniciando geração paralela de {} PDFs", faturas.size());

        // 1. Dividir em lotes
        List<List<Fatura>> lotes = dividirEmLotes(faturas, BATCH_SIZE);
        log.info("📦 Dividido em {} lotes de até {} PDFs", lotes.size(), BATCH_SIZE);

        // 2. Processar cada lote em paralelo
        List<CompletableFuture<List<PdfResultado>>> futures = lotes.stream()
            .map(lote -> CompletableFuture.supplyAsync(() -> 
                gerarPdfsLote(lote, notas), pdfExecutor))
            .collect(Collectors.toList());

        // 3. Aguardar todos
        List<PdfResultado> resultados = futures.stream()
            .map(CompletableFuture::join)
            .flatMap(List::stream)
            .collect(Collectors.toList());

        long tempo = System.currentTimeMillis() - inicio;
        log.info("✅ {} PDFs gerados em {} ms (média: {} ms/PDF)", 
            resultados.size(), tempo, tempo / Math.max(1, resultados.size()));

        return CompletableFuture.completedFuture(resultados);
    }

    private List<PdfResultado> gerarPdfsLote(List<Fatura> faturas, List<NotaDebitoSPC> notas) {
        List<PdfResultado> resultados = new ArrayList<>();

        for (Fatura fatura : faturas) {
            try {
                // 🔥 CORRIGIDO: Buscar nota pelo notaDebitoId da fatura
                Long notaDebitoId = fatura.getNotaDebitoId();
                
                if (notaDebitoId == null) {
                    log.warn("⚠️ Fatura ID {} não possui notaDebitoId associado", fatura.getId());
                    continue;
                }

                // Buscar a nota pelo ID
                NotaDebitoSPC nota = notas.stream()
                    .filter(n -> n.getId().equals(notaDebitoId))
                    .findFirst()
                    .orElse(null);

                if (nota == null) {
                    log.warn("⚠️ Nota com ID {} não encontrada para fatura ID: {}", 
                        notaDebitoId, fatura.getId());
                    continue;
                }

                // 🔥 Gerar PDF usando o serviço existente
                byte[] pdf = pdfExportService.gerarNotaDebitoPdf(nota);

                PdfResultado resultado = new PdfResultado();
                resultado.setFaturaId(fatura.getId());
                resultado.setNotaId(nota.getId());
                resultado.setPdf(pdf);
                resultado.setSucesso(true);

                resultados.add(resultado);

            } catch (Exception e) {
                log.error("❌ Erro ao gerar PDF da fatura {}: {}", fatura.getId(), e.getMessage());
                PdfResultado erro = new PdfResultado();
                erro.setFaturaId(fatura.getId());
                erro.setSucesso(false);
                erro.setMensagemErro(e.getMessage());
                resultados.add(erro);
            }
        }

        return resultados;
    }

    private List<List<Fatura>> dividirEmLotes(List<Fatura> lista, int tamanhoLote) {
        List<List<Fatura>> lotes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamanhoLote) {
            int end = Math.min(i + tamanhoLote, lista.size());
            lotes.add(lista.subList(i, end));
        }
        return lotes;
    }

    /**
     * Resultado da geração de PDF
     */
    public static class PdfResultado {
        private Long faturaId;
        private Long notaId;
        private byte[] pdf;
        private boolean sucesso;
        private String mensagemErro;

        public Long getFaturaId() { return faturaId; }
        public void setFaturaId(Long faturaId) { this.faturaId = faturaId; }
        public Long getNotaId() { return notaId; }
        public void setNotaId(Long notaId) { this.notaId = notaId; }
        public byte[] getPdf() { return pdf; }
        public void setPdf(byte[] pdf) { this.pdf = pdf; }
        public boolean isSucesso() { return sucesso; }
        public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }
        public String getMensagemErro() { return mensagemErro; }
        public void setMensagemErro(String mensagemErro) { this.mensagemErro = mensagemErro; }
    }
}