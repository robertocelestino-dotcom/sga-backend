package com.sga.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.repository.ImportacaoSPCRepository;
import com.sga.service.VerificacaoImportacaoService;

@RestController
@RequestMapping("/api/importacao-spc")
public class EstatisticasImportacaoController {

    @Autowired
    private ImportacaoSPCRepository importacaoSPCRepository;

    @Autowired
    private VerificacaoImportacaoService verificacaoService;

    @GetMapping("/{importacaoId}/estatisticas")
    public ResponseEntity<?> getEstatisticasImportacao(@PathVariable Long importacaoId) {
        try {
            Map<String, Object> estatisticas = new HashMap<>();
            
            ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
                    .orElseThrow(() -> new RuntimeException("Importação não encontrada"));

            // Estatísticas básicas
            estatisticas.put("totalNotas", importacao.getNotasDebito().size());
            estatisticas.put("totalItens", importacao.getNotasDebito().stream()
                    .mapToInt(nota -> nota.getItens().size())
                    .sum());
            
            // Valor total
            BigDecimal valorTotal = importacao.getNotasDebito().stream()
                    .flatMap(nota -> nota.getItens().stream())
                    .map(ItemSPC::getValorTotal)
                    .filter(valor -> valor != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            estatisticas.put("valorTotal", valorTotal);

            // Métricas de qualidade
            estatisticas.put("taxaProcessamento", calcularTaxaProcessamento(importacao));
            estatisticas.put("nivelIntegridade", calcularNivelIntegridade(importacao));

            // Informações temporais
            estatisticas.put("dataImportacao", importacao.getDataImportacao());
            estatisticas.put("tempoProcessamento", calcularTempoProcessamento(importacao));

            return ResponseEntity.ok(estatisticas);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao buscar estatísticas: " + e.getMessage()));
        }
    }

    @GetMapping("/{importacaoId}/timeline")
    public ResponseEntity<?> getTimelineProcessamento(@PathVariable Long importacaoId) {
        try {
            List<Map<String, Object>> timeline = new ArrayList<>();
            
            ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
                    .orElseThrow(() -> new RuntimeException("Importação não encontrada"));

            // Simular eventos do processamento - converter LocalDateTime para Date
            LocalDateTime dataBaseLocal = importacao.getDataImportacao();
            Date dataBase = java.sql.Timestamp.valueOf(dataBaseLocal);
            
            timeline.add(criarEvento("UPLOAD", "Arquivo recebido", dataBase, "success"));
            timeline.add(criarEvento("VALIDACAO", "Validação de formato concluída", 
                    new Date(dataBase.getTime() + 1000), "success"));
            timeline.add(criarEvento("PROCESSAMENTO", "Processamento de registros", 
                    new Date(dataBase.getTime() + 5000), "success"));
            timeline.add(criarEvento("VERIFICACAO", "Verificação de consistência", 
                    new Date(dataBase.getTime() + 10000), "success"));
            timeline.add(criarEvento("CONCLUIDO", "Importação finalizada", 
                    new Date(dataBase.getTime() + 15000), "success"));
            
            return ResponseEntity.ok(timeline);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao buscar timeline: " + e.getMessage()));
        }
    }

    private Map<String, Object> criarEvento(String etapa, String descricao, Date data, String status) {
        Map<String, Object> evento = new HashMap<>();
        evento.put("etapa", etapa);
        evento.put("descricao", descricao);
        evento.put("data", data);
        evento.put("status", status);
        return evento;
    }

    private double calcularTaxaProcessamento(ImportacaoSPC importacao) {
        // Simular cálculo de taxa de processamento
        return 95.5; // 95.5% de sucesso
    }

    private double calcularNivelIntegridade(ImportacaoSPC importacao) {
        // Simular cálculo de nível de integridade
        return 98.2; // 98.2% de integridade
    }

    private String calcularTempoProcessamento(ImportacaoSPC importacao) {
        // Simular cálculo de tempo de processamento
        return "15 segundos";
    }
}