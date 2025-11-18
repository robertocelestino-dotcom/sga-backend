package com.sga.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.service.VerificacaoImportacaoService;

@RestController
@RequestMapping("/api/importacao-spc")
public class VerificacaoImportacaoController {

    @Autowired
    private VerificacaoImportacaoService verificacaoService;

    @GetMapping("/{importacaoId}/verificar")
    public ResponseEntity<?> verificarImportacao(@PathVariable Long importacaoId) {
        try {
            Map<String, Object> resultado = verificacaoService.verificarImportacao(importacaoId);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro na verificação: " + e.getMessage()));
        }
    }

    @GetMapping("/{importacaoId}/divergencias-detalhadas")
    public ResponseEntity<?> getDivergenciasDetalhadas(@PathVariable Long importacaoId) {
        try {
            Map<String, Object> divergencias = verificacaoService.verificarDivergenciasDetalhadas(importacaoId);
            return ResponseEntity.ok(divergencias);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao buscar divergências: " + e.getMessage()));
        }
    }

    @GetMapping("/{importacaoId}/relatorio")
    public ResponseEntity<?> gerarRelatorioVerificacao(@PathVariable Long importacaoId) {
        try {
            Map<String, Object> relatorio = verificacaoService.verificarImportacao(importacaoId);
            
            // Formatar para exibição no frontend
            Map<String, Object> resposta = Map.of(
                "importacao", Map.of(
                    "id", relatorio.get("importacaoId"),
                    "arquivo", relatorio.get("nomeArquivo"),
                    "status", relatorio.get("status")
                ),
                "possuiDivergencias", relatorio.get("possuiDivergencias"),
                "totalDivergencias", relatorio.get("totalDivergencias"),
                "resultados", relatorio.get("resultados")
            );
            
            return ResponseEntity.ok(resposta);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Erro ao gerar relatório: " + e.getMessage()));
        }
    }
}