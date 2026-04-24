// src/main/java/com/sga/controller/ProcessamentoFaturamentoController.java

package com.sga.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.ProcessamentoRequest;
import com.sga.dto.ResultadoProcessamento;
import com.sga.model.Fatura;
import com.sga.service.FaturaService;
import com.sga.service.ProcessamentoFaturamentoService;

@RestController
@RequestMapping("/api/faturamento")
public class ProcessamentoFaturamentoController {

    private static final Logger log = LoggerFactory.getLogger(ProcessamentoFaturamentoController.class);

    @Autowired
    private ProcessamentoFaturamentoService processamentoFaturamentoService;

    @Autowired
    private FaturaService faturaService;

    // ========== PROCESSAMENTO ==========

    /**
     * Processa faturamento para os associados selecionados
     */
    @PostMapping("/processar")
    public ResponseEntity<ResultadoProcessamento> processarFaturamento(
            @RequestBody ProcessamentoRequest request,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        
        log.info("🚀 Processando faturamento para {} associados", 
            request.getAssociadosIds() != null ? request.getAssociadosIds().size() : 0);
        
        ResultadoProcessamento resultado = processamentoFaturamentoService.processarFaturamento(request);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Simula faturamento (pré-visualização)
     */
    @PostMapping("/simular")
    public ResponseEntity<ResultadoProcessamento> simularFaturamento(
            @RequestBody ProcessamentoRequest request) {
        
        log.info("🔍 Simulando faturamento para {} associados", 
            request.getAssociadosIds() != null ? request.getAssociadosIds().size() : 0);
        
        request.setSimular(true);
        ResultadoProcessamento resultado = processamentoFaturamentoService.processarFaturamento(request);
        
        return ResponseEntity.ok(resultado);
    }

    /**
     * Processa faturamento para um associado específico
     */
    @PostMapping("/processar/{associadoId}")
    public ResponseEntity<Fatura> processarFaturamentoAssociado(
            @PathVariable Long associadoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissao,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        
        log.info("🚀 Processando faturamento para associado: {} na data: {}", associadoId, dataEmissao);
        
        Fatura fatura = faturaService.processarFaturamento(associadoId, dataEmissao, usuario);
        
        return ResponseEntity.ok(fatura);
    }

    // ========== CONSULTAS DE FATURAS ==========

    /**
     * Lista todas as faturas com paginação
     */
    @GetMapping("/faturas")
    public ResponseEntity<Page<Fatura>> listarFaturas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction) {
        
        log.info("📋 Listando faturas - página: {}, tamanho: {}", page, size);
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // 🔥 Usar um campo que existe: "id" ou "dataEmissao" (sem vírgula)
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, "id"));
        
        Page<Fatura> faturas = faturaService.listarFaturas(pageable);
        
        return ResponseEntity.ok(faturas);
    }

    /**
     * Lista faturas por associado
     */
    @GetMapping("/faturas/associado/{associadoId}")
    public ResponseEntity<Page<Fatura>> listarFaturasPorAssociado(
            @PathVariable Long associadoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Listando faturas do associado: {} - página: {}", associadoId, page);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataEmissao").descending());
        Page<Fatura> faturas = faturaService.listarPorAssociado(associadoId, pageable);
        
        return ResponseEntity.ok(faturas);
    }

    /**
     * Lista faturas por lote de processamento
     */
    @GetMapping("/faturas/lote/{loteId}")
    public ResponseEntity<Page<Fatura>> listarFaturasPorLote(
            @PathVariable Long loteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("📋 Listando faturas do lote: {} - página: {}", loteId, page);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("dataEmissao").descending());
        Page<Fatura> faturas = faturaService.listarPorLoteProcessamento(loteId, pageable);
        
        return ResponseEntity.ok(faturas);
    }

    /**
     * Lista faturas pendentes de integração com RM
     */
    @GetMapping("/faturas/pendentes-rm")
    public ResponseEntity<List<Fatura>> listarFaturasPendentesRM() {
        log.info("📋 Listando faturas pendentes de integração com RM");
        
        List<Fatura> faturas = faturaService.listarFaturasPendentesRM();
        
        return ResponseEntity.ok(faturas);
    }

    /**
     * Busca fatura por ID
     */
    @GetMapping("/faturas/{id}")
    public ResponseEntity<Fatura> buscarFaturaPorId(@PathVariable Long id) {
        log.info("🔍 Buscando fatura ID: {}", id);
        
        return faturaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Marca fatura como processada no RM
     */
    @PostMapping("/faturas/{id}/marcar-processada-rm")
    public ResponseEntity<Fatura> marcarFaturaProcessadaRm(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        
        log.info("🏷️ Marcando fatura {} como processada no RM", id);
        
        Fatura fatura = faturaService.marcarComoProcessadoRm(id);
        
        return ResponseEntity.ok(fatura);
    }
}