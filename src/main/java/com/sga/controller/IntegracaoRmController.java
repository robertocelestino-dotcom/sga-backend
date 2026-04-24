package com.sga.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sga.model.ConfiguracaoRm;
import com.sga.service.IntegracaoRmService;

@RestController
@RequestMapping("/api/integracao-rm")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class IntegracaoRmController {

    private static final Logger log = LoggerFactory.getLogger(IntegracaoRmController.class);

    @Autowired
    private IntegracaoRmService integracaoRmService;

    // ==================== CONFIGURAÇÕES ====================

    @GetMapping("/configuracoes")
    public ResponseEntity<List<ConfiguracaoRm>> listarConfiguracoes() {
        log.info("📋 Listando configurações RM");
        return ResponseEntity.ok(integracaoRmService.listarConfiguracoes());
    }

    @GetMapping("/configuracoes/ativas")
    public ResponseEntity<List<ConfiguracaoRm>> listarConfiguracoesAtivas() {
        log.info("📋 Listando configurações RM ativas");
        return ResponseEntity.ok(integracaoRmService.listarConfiguracoesAtivas());
    }

    @GetMapping("/configuracoes/{id}")
    public ResponseEntity<ConfiguracaoRm> buscarConfiguracaoPorId(@PathVariable Long id) {
        log.info("🔍 Buscando configuração RM por ID: {}", id);
        return integracaoRmService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/configuracoes")
    public ResponseEntity<ConfiguracaoRm> criarConfiguracao(@RequestBody ConfiguracaoRm configuracao) {
        log.info("➕ Criando nova configuração RM");
        ConfiguracaoRm created = integracaoRmService.criarConfiguracao(configuracao);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/configuracoes/{id}")
    public ResponseEntity<ConfiguracaoRm> atualizarConfiguracao(@PathVariable Long id,
            @RequestBody ConfiguracaoRm configuracao) {
        log.info("✏️ Atualizando configuração RM ID: {}", id);
        return ResponseEntity.ok(integracaoRmService.atualizarConfiguracao(id, configuracao));
    }

    @DeleteMapping("/configuracoes/{id}")
    public ResponseEntity<Void> excluirConfiguracao(@PathVariable Long id) {
        log.info("🗑️ Excluindo configuração RM ID: {}", id);
        integracaoRmService.excluirConfiguracao(id);
        return ResponseEntity.noContent().build();
    }
}