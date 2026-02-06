package com.sga.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.SistemaLog;
import com.sga.service.SistemaLogService;

@RestController
@RequestMapping("/api/logs")
//@PreAuthorize("hasRole('ADMIN')")
public class SistemaLogController {
    
    @Autowired
    private SistemaLogService sistemaLogService;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarLogs(
            @RequestParam(required = false) String modulo,
            @RequestParam(required = false) String tabela,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Boolean sucesso,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataHora").descending());
        
        // Converter LocalDate para LocalDateTime se necessário
        LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = dataFim != null ? dataFim.plusDays(1).atStartOfDay() : null;
        
        Page<SistemaLog> logs = sistemaLogService.buscarLogsComFiltros(
            tabela, acao, modulo, usuarioId, sucesso, inicio, fim, pageable
        );
        
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(logs.getTotalElements()))
            .header("X-Total-Pages", String.valueOf(logs.getTotalPages()))
            .body(Map.of(
                "content", logs.getContent(),
                "totalElements", logs.getTotalElements(),
                "totalPages", logs.getTotalPages(),
                "size", logs.getSize(),
                "number", logs.getNumber()
            ));
    }
    
    @GetMapping("/tabela/{tabela}/registro/{idRegistro}")
    public ResponseEntity<List<SistemaLog>> buscarLogsPorRegistro(
            @PathVariable String tabela,
            @PathVariable Long idRegistro) {
        
        List<SistemaLog> logs = sistemaLogService.buscarLogsPorRegistro(tabela, idRegistro);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<List<SistemaLog>> buscarLogsPorModulo(@PathVariable String modulo) {
        List<SistemaLog> logs = sistemaLogService.buscarLogsPorModulo(modulo);
        return ResponseEntity.ok(logs);
    }
    
    @GetMapping("/periodo")
    public ResponseEntity<List<SistemaLog>> buscarLogsPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        
        List<SistemaLog> logs = sistemaLogService.buscarLogsPorPeriodo(inicio, fim);
        return ResponseEntity.ok(logs);
    }
    
    @DeleteMapping("/limpar")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> limparLogsAntigos(@RequestParam(defaultValue = "90") int diasManter) {
        int quantidade = sistemaLogService.limparLogsAntigos(diasManter);
        return ResponseEntity.ok(Map.of(
            "mensagem", "Logs antigos removidos com sucesso",
            "quantidade", quantidade,
            "diasManter", diasManter
        ));
    }
    
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> getEstatisticas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fim) {
        
        Map<String, Object> estatisticas = sistemaLogService.obterEstatisticas(inicio, fim);
        return ResponseEntity.ok(estatisticas);
    }
    
    @GetMapping("/tabelas")
    public ResponseEntity<List<String>> getTabelasDistintas() {
        List<String> tabelas = sistemaLogService.buscarTabelasDistintas();
        return ResponseEntity.ok(tabelas);
    }
    
    @GetMapping("/acoes")
    public ResponseEntity<List<String>> getAcoesDistintas() {
        List<String> acoes = sistemaLogService.buscarAcoesDistintas();
        return ResponseEntity.ok(acoes);
    }
    
    @GetMapping("/modulos")
    public ResponseEntity<List<String>> getModulosDistintos() {
        List<String> modulos = sistemaLogService.buscarModulosDistintos();
        return ResponseEntity.ok(modulos);
    }
    
    @GetMapping("/teste")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, String>> teste() {
        // Método de teste para verificar se o endpoint está funcionando
        sistemaLogService.logSimples("TESTE", 0L, "TESTE", "SISTEMA", "Teste de log do sistema");
        return ResponseEntity.ok(Map.of(
            "status", "OK",
            "mensagem", "Sistema de logs funcionando corretamente",
            "timestamp", LocalDateTime.now().toString()
        ));
    }
}