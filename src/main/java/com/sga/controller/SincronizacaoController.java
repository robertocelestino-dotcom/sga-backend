// src/main/java/com/sga/controller/SincronizacaoController.java

package com.sga.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.SincronizacaoNotificacao;
import com.sga.service.SincronizacaoNotificacaoService;

@RestController
@RequestMapping("/api/sincronizacoes")
public class SincronizacaoController {

    private static final Logger log = LoggerFactory.getLogger(SincronizacaoController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private SincronizacaoNotificacaoService sincronizacaoService;

    /**
     * 🔥 LISTAR HISTÓRICO DE SINCRONIZAÇÕES (PAGINADO)
     */
    @GetMapping
    public ResponseEntity<Page<SincronizacaoNotificacao>> listarSincronizacoes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("📋 Listando histórico de sincronizações - Página: {}, Tamanho: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataSincronizacao"));
        Page<SincronizacaoNotificacao> resultado = sincronizacaoService.listarSincronizacoes(pageable);

        log.info("📊 Encontradas {} sincronizações", resultado.getTotalElements());

        return ResponseEntity.ok(resultado);
    }

    /**
     * 🔥 VERIFICAR SE JÁ FOI SINCRONIZADO
     */
    @GetMapping("/verificar")
    public ResponseEntity<Map<String, Object>> verificarSincronizacao(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🔍 Verificando sincronização - Início: {}, Fim: {}, Código: {}",
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), codigoAssociado);

        boolean existe = sincronizacaoService.verificarSincronizacaoExistente(dataInicio, dataFim, codigoAssociado);

        Map<String, Object> response = new HashMap<>();
        response.put("existe", existe);
        response.put("dataInicio", dataInicio);
        response.put("dataFim", dataFim);
        response.put("codigoAssociado", codigoAssociado);

        log.info("✅ Sincronização existente: {}", existe);

        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 DESFAZER SINCRONIZAÇÃO (CANCELAR)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> desfazerSincronizacao(@PathVariable Long id) {

        log.info("🗑️ Desfazendo sincronização ID: {}", id);

        Map<String, Object> response = sincronizacaoService.desfazerSincronizacao(id);

        log.info("✅ Desfazer sincronização concluído: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 DESFAZER POR PERÍODO
     */
    @DeleteMapping("/desfazer-periodo")
    public ResponseEntity<Map<String, Object>> desfazerPorPeriodo(
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataInicio,
            @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") LocalDate dataFim,
            @RequestParam(required = false) String codigoAssociado) {

        log.info("🗑️ Desfazendo sincronização por período - Início: {}, Fim: {}, Código: {}",
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), codigoAssociado);

        Map<String, Object> response = sincronizacaoService.desfazerSincronizacaoPorPeriodo(
                dataInicio, dataFim, codigoAssociado);

        log.info("✅ Desfazer por período concluído: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 VERIFICAR SE PODE DESFAZER
     */
    @GetMapping("/{id}/pode-desfazer")
    public ResponseEntity<Map<String, Object>> podeDesfazer(@PathVariable Long id) {

        log.info("🔍 Verificando se pode desfazer sincronização ID: {}", id);

        Map<String, Object> response = sincronizacaoService.verificarPodeDesfazer(id);

        log.info("✅ Pode desfazer: {}", response);

        return ResponseEntity.ok(response);
    }
}