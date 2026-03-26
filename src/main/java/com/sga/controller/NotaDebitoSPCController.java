package com.sga.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.service.VerificacaoImportacaoService;

@RestController
@RequestMapping("/api/notas-debito")
public class NotaDebitoSPCController {

    private static final Logger log = LoggerFactory.getLogger(NotaDebitoSPCController.class);
    
    private final VerificacaoImportacaoService verificacaoImportacaoService;

    // Construtor para injeção de dependência
    public NotaDebitoSPCController(VerificacaoImportacaoService verificacaoImportacaoService) {
        this.verificacaoImportacaoService = verificacaoImportacaoService;
    }

    /**
     * Lista notas de débito com paginação e filtro
     */
    @GetMapping
    public Page<NotaDebitoResumoDTO> listarNotas(
            @RequestParam Long importacaoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filtro
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return verificacaoImportacaoService.listarNotasComFiltro(importacaoId, filtro, pageable);
    }

    /**
     * Busca detalhes completos de uma nota específica
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotaDetalhes(@PathVariable Long id) {
        log.info("📥 Buscando detalhes da nota ID: {}", id);
        
        try {
            Map<String, Object> detalhes = verificacaoImportacaoService.obterDetalhesNota(id);
            return ResponseEntity.ok(detalhes);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar detalhes da nota {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Visualizar PDF da nota
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> visualizarNotaPDF(@PathVariable Long id) {
        log.info("📄 Gerando PDF para nota ID: {}", id);
        
        try {
            byte[] pdf = verificacaoImportacaoService.exportarNotaPdf(id);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=nota_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("❌ Erro ao gerar PDF da nota {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Exportar notas como CSV
     */
    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarNotasCSV(@RequestParam Long importacaoId) {
        log.info("📊 Exportando notas CSV para importação ID: {}", importacaoId);
        
        try {
            byte[] csv = verificacaoImportacaoService.exportarNotasCsv(importacaoId);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=notas_importacao_" + importacaoId + ".csv")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(csv);
        } catch (Exception e) {
            log.error("❌ Erro ao exportar CSV: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}