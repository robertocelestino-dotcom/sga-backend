package com.sga.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sga.service.VerificacaoImportacaoService;

@RestController
@RequestMapping("/api/importacao-spc")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class VerificacaoImportacaoController {

    @Autowired
    private VerificacaoImportacaoService verificacaoService;

    @GetMapping("/{id}/verificar")
    public ResponseEntity<?> verificar(@PathVariable Long importacaoId) {
        try {
            Map<String, Object> resp = verificacaoService.verificarImportacao(importacaoId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/divergencias-detalhadas")
    public ResponseEntity<?> detalhadas(@PathVariable Long importacaoId) {
        try {
            return ResponseEntity.ok(verificacaoService.verificarDivergenciasDetalhadas(importacaoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/{id}/relatorio")
    public ResponseEntity<?> relatorio(@PathVariable Long importacaoId) {
        try {
            return ResponseEntity.ok(verificacaoService.verificarImportacao(importacaoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
