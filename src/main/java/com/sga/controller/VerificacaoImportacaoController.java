package com.sga.controller;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.ImportacaoResumoDTO;
import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.dto.NotaFaturamentoGridDTO;
import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.NotaDebitoSPCRepository;
import com.sga.service.VerificacaoImportacaoService;

@RestController
@RequestMapping("/api/verificacao-importacao")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class VerificacaoImportacaoController {

    @Autowired
    private VerificacaoImportacaoService verificacaoService;

    
    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;
    
    // ---------------------------------------------------------
    // 1. ÚLTIMA IMPORTAÇÃO (para carregamento inicial)
    // ---------------------------------------------------------
    @GetMapping("/ultima")
    public ResponseEntity<?> obterUltimaImportacao() {
        try {
            ImportacaoResumoDTO ultima = (ImportacaoResumoDTO) verificacaoService.obterUltimaImportacao();
            return ResponseEntity.ok(ultima);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 2. LISTAR IMPORTACOES (para dropdown/filtro)
    // ---------------------------------------------------------
    @GetMapping("/listar")
    public ResponseEntity<List<ImportacaoResumoDTO>> listarImportacoes() {
        return ResponseEntity.ok(verificacaoService.listarImportacoes());
    }
    
    // ---------------------------------------------------------
    // 2.1 LISTAR IMPORTACOES PAGINADAS (opcional, para muitas importações)
    // ---------------------------------------------------------
    @GetMapping("/listar-paginado")
    public ResponseEntity<Page<ImportacaoResumoDTO>> listarImportacoesPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(verificacaoService.listarImportacoesPaginado(pageable));
    }

    // ---------------------------------------------------------
    // 3. RESUMO DA IMPORTACAÇÃO (quadro resumo)
    // ---------------------------------------------------------
    @GetMapping("/{id}/resumo")
    public ResponseEntity<?> resumo(@PathVariable("id") Long importacaoId) {
        try {
            return ResponseEntity.ok(verificacaoService.obterResumo(importacaoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 4. LISTA DE NOTAS COM PAGINAÇÃO E FILTRO (grid principal)
    // ---------------------------------------------------------
    @GetMapping("/{id}/notas")
    public ResponseEntity<?> listarNotas(
            @PathVariable("id") Long importacaoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filtro) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NotaFaturamentoGridDTO> notas = verificacaoService.listarNotasPaginadas(
                    importacaoId, pageable, filtro);
            return ResponseEntity.ok(notas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 5. DETALHES DE UMA NOTA ESPECÍFICA (para modal)
    // ---------------------------------------------------------
    @GetMapping("/nota/{notaId}/detalhes")
    public ResponseEntity<?> detalhesNota(@PathVariable("notaId") Long notaId) {
        try {
            Map<String, Object> detalhes = verificacaoService.obterDetalhesNota(notaId);
            return ResponseEntity.ok(detalhes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 6. EXPORTAÇÕES (mantendo compatibilidade)
    // ---------------------------------------------------------

    // Exportar resumo PDF
    @GetMapping("/{id}/resumo/pdf")
    public ResponseEntity<?> exportarResumoPdf(@PathVariable("id") Long importacaoId) {
        try {
            byte[] pdf = verificacaoService.exportarResumoPdf(importacaoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename("importacao_" + importacaoId + "_resumo.pdf")
                    .build()
            );
            
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Exportar resumo CSV
    @GetMapping("/{id}/resumo/csv")
    public ResponseEntity<?> exportarResumoCsv(@PathVariable("id") Long importacaoId) {
        try {
            byte[] csv = verificacaoService.exportarResumoCsv(importacaoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename("importacao_" + importacaoId + "_resumo.csv")
                    .build()
            );
            
            return new ResponseEntity<>(csv, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // Exportar nota individual PDF
    @GetMapping("/nota/{notaId}/pdf")
    public ResponseEntity<?> exportarNotaPdf(@PathVariable("notaId") Long notaId) {
        try {
            byte[] pdf = verificacaoService.exportarNotaPdf(notaId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename("nota_importacao_" + notaId + ".pdf")
                    .build()
            );
            
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    
    // Exportar grid de notas para Excel/CSV
    @GetMapping("/{id}/notas/exportar")
    public ResponseEntity<?> exportarNotasGrid(
            @PathVariable("id") Long importacaoId,
            @RequestParam(defaultValue = "csv") String formato) {
        try {
            byte[] arquivo;
            MediaType contentType;
            String extensao;
            
            if ("excel".equalsIgnoreCase(formato)) {
                arquivo = verificacaoService.exportarNotasExcel(importacaoId);
                contentType = MediaType.APPLICATION_OCTET_STREAM;
                extensao = "xlsx";
            } else {
                arquivo = verificacaoService.exportarNotasCsv(importacaoId);
                contentType = MediaType.TEXT_PLAIN;
                extensao = "csv";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(contentType);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename("notas_importacao_" + importacaoId + "." + extensao)
                    .build()
            );
            
            return new ResponseEntity<>(arquivo, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // ---------------------------------------------------------
    // 🔒 SEÇÃO LEGADA – mantém funções antigas
    // ---------------------------------------------------------

    @GetMapping("/importacao-spc/{id}/verificar")
    public ResponseEntity<?> verificar(@PathVariable Long importacaoId) {
        try {
            Map<String, Object> resp = verificacaoService.verificarImportacao(importacaoId);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/importacao-spc/{id}/divergencias-detalhadas")
    public ResponseEntity<?> detalhadas(@PathVariable Long importacaoId) {
        try {
            return ResponseEntity.ok(verificacaoService.verificarDivergenciasDetalhadas(importacaoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping("/importacao-spc/{id}/relatorio")
    public ResponseEntity<?> relatorio(@PathVariable Long importacaoId) {
        try {
            return ResponseEntity.ok(verificacaoService.verificarImportacao(importacaoId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
    
    // Detalhes da importação (legado)
    @GetMapping("/{id}")
    public ResponseEntity<?> getImportacao(@PathVariable("id") Long importacaoId) {
        try {
            Map<String, Object> detalhes = verificacaoService.verificarImportacao(importacaoId);
            return ResponseEntity.ok(detalhes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // Exportação CSV compatível com frontend
    @GetMapping("/{id}/export/csv")
    public ResponseEntity<?> exportarCsv(@PathVariable("id") Long importacaoId) {
        return exportarResumoCsv(importacaoId);
    }

    // Exportação PDF compatível com frontend  
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<?> exportarPdf(@PathVariable("id") Long importacaoId) {
        return exportarResumoPdf(importacaoId);
    }
    
    /*
    @GetMapping("/{id}/notas")
    public ResponseEntity<Page<NotaDebitoResumoDTO>> listarNotas(
            @PathVariable("id") Long importacaoId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(verificacaoService.listarNotas(importacaoId, pageable));
    }
 	*/
}