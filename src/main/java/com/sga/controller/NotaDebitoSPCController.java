package com.sga.controller;

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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notas-debito")
@RequiredArgsConstructor
public class NotaDebitoSPCController {

	private final VerificacaoImportacaoService service;

    NotaDebitoSPCController(VerificacaoImportacaoService service) {
        this.service = service;
    }

	@GetMapping
    public Page<NotaDebitoResumoDTO> listarNotas(
            @RequestParam Long importacaoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String filtro
    ) {
        Pageable pageable = PageRequest.of(page, size);
        // Chama o método JÁ EXISTENTE no VerificacaoImportacaoService
        return service.listarNotas(importacaoId, pageable);
    }

	@GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> visualizarNotaPDF(@PathVariable Long id) {
        // Use o método existente
        byte[] pdf = service.exportarNotaPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=nota_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
