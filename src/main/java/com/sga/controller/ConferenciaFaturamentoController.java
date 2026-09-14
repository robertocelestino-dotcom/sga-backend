package com.sga.controller;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.ConferenciaFaturaDetalheDTO;
import com.sga.dto.ConferenciaFaturamentoDTO;
import com.sga.dto.ConferenciaResumoDTO;
import com.sga.service.ConferenciaFaturamentoService;

@RestController
@RequestMapping("/api/faturamento/conferencia")
public class ConferenciaFaturamentoController {

    private static final Logger log = LoggerFactory.getLogger(ConferenciaFaturamentoController.class);

    @Autowired
    private ConferenciaFaturamentoService conferenciaService;

    /**
     * Lista todas as faturas com comparação com a nota de débito
     */
    @GetMapping("/faturas")
    @PreAuthorize("hasAuthority('FATURA_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<ConferenciaFaturamentoDTO>> listarConferencia(
            @RequestParam(required = false) Long reguaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String codigoSpc,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dataEmissao") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        log.info("📊 Listando conferência de faturamento - Régua: {}, Período: {} à {}, Código: {}, Status: {}",
                reguaId, dataInicio, dataFim, codigoSpc, status);

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ConferenciaFaturamentoDTO> result = conferenciaService.listarConferencia(
                reguaId, dataInicio, dataFim, codigoSpc, status, pageable);

        return ResponseEntity.ok(result);
    }

    /**
     * Detalha uma fatura específica com comparação item a item
     */
    @GetMapping("/fatura/{faturaId}")
    @PreAuthorize("hasAuthority('CONFERENCIA_FATURAMENTO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ConferenciaFaturaDetalheDTO> detalharConferencia(
            @PathVariable Long faturaId) {

        log.info("📊 Detalhando conferência da fatura ID: {}", faturaId);

        try {
            ConferenciaFaturaDetalheDTO detalhe = conferenciaService.detalharConferencia(faturaId);
            
            if (detalhe == null) {
                log.warn("⚠️ Detalhe da fatura {} não encontrado", faturaId);
                return ResponseEntity.notFound().build();
            }
            
            log.info("✅ Detalhe da fatura {} carregado com sucesso. Itens nota: {}, Itens fatura: {}",
                    faturaId, 
                    detalhe.getItensNota() != null ? detalhe.getItensNota().size() : 0,
                    detalhe.getItensFatura() != null ? detalhe.getItensFatura().size() : 0);
            
            return ResponseEntity.ok(detalhe);
            
        } catch (Exception e) {
            log.error("❌ Erro ao detalhar fatura {}: {}", faturaId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resumo da conferência por período (com filtros)
     */
    @GetMapping("/resumo")
    @PreAuthorize("hasAuthority('CONFERENCIA_FATURAMENTO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ConferenciaResumoDTO> resumoConferencia(
            @RequestParam(required = false) Long reguaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String codigoSpc,
            @RequestParam(required = false) String status) {

        log.info("📊 Resumo da conferência - Régua: {}, Período: {} à {}, Código: {}, Status: {}",
                reguaId, dataInicio, dataFim, codigoSpc, status);

        ConferenciaResumoDTO resumo = conferenciaService.resumoConferenciaComFiltros(
                reguaId, dataInicio, dataFim, codigoSpc, status);
        return ResponseEntity.ok(resumo);
    }

    /**
     * Exportar CSV com os dados da conferência
     */
    @GetMapping("/exportar-csv")
    @PreAuthorize("hasAuthority('FATURA_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> exportarCSV(
            @RequestParam(required = false) Long reguaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String codigoSpc) {

        log.info("📊 Exportando CSV da conferência - Régua: {}, Período: {} à {}", reguaId, dataInicio, dataFim);

        // TODO: Implementar exportação CSV
        return ResponseEntity.ok().build();
    }
}