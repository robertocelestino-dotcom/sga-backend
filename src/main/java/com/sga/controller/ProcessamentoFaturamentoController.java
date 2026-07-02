// src/main/java/com/sga/controller/ProcessamentoFaturamentoController.java

package com.sga.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.ExportacaoRmRequest;
import com.sga.dto.FaturaDetalheDTO;
import com.sga.dto.FaturaItemDTO;
import com.sga.dto.FaturaItemResponseDTO;
import com.sga.dto.FaturaResumoDTO;
import com.sga.dto.ProcessamentoRequest;
import com.sga.dto.ResultadoProcessamento;
import com.sga.model.Fatura;
import com.sga.repository.LoteProcessamentoRepository;
import com.sga.service.FaturaRmExportService;
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
	
	@Autowired
    private FaturaRmExportService faturaRmExportService;

    @Autowired
    private LoteProcessamentoRepository loteProcessamentoRepository;

	// ========== PROCESSAMENTO ==========

	@PostMapping("/processar")
	public ResponseEntity<ResultadoProcessamento> processarFaturamento(@RequestBody ProcessamentoRequest request,
			@RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

		log.info("🚀 Processando faturamento para {} associados",
				request.getAssociadosIds() != null ? request.getAssociadosIds().size() : 0);

		ResultadoProcessamento resultado = processamentoFaturamentoService.processarFaturamento(request);

		return ResponseEntity.ok(resultado);
	}

	@PostMapping("/simular")
	public ResponseEntity<ResultadoProcessamento> simularFaturamento(@RequestBody ProcessamentoRequest request) {

		log.info("🔍 Simulando faturamento para {} associados",
				request.getAssociadosIds() != null ? request.getAssociadosIds().size() : 0);

		request.setSimular(true);
		ResultadoProcessamento resultado = processamentoFaturamentoService.processarFaturamento(request);

		return ResponseEntity.ok(resultado);
	}

	@PostMapping("/processar/{associadoId}")
	public ResponseEntity<Fatura> processarFaturamentoAssociado(@PathVariable Long associadoId,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEmissao,
			@RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

		log.info("🚀 Processando faturamento para associado: {} na data: {}", associadoId, dataEmissao);

		Fatura fatura = faturaService.processarFaturamento(associadoId, dataEmissao, usuario);

		return ResponseEntity.ok(fatura);
	}

	// ========== CONSULTAS DE FATURAS ==========

	/**
	 * 🔥 Lista faturas com filtros - INCLUINDO FILTRO POR RÉGUA
	 */
	@GetMapping("/faturas")
	public ResponseEntity<Page<FaturaResumoDTO>> listarFaturas(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "20") int size,
	        @RequestParam(required = false) String sort,
	        @RequestParam(defaultValue = "desc") String direction,
	        @RequestParam(required = false) Integer mes,
	        @RequestParam(required = false) Integer ano,
	        @RequestParam(required = false) String numeroFatura,
	        @RequestParam(required = false) String associadoNome,
	        @RequestParam(required = false) String status,
	        @RequestParam(required = false) Long associadoId,
	        @RequestParam(required = false) Long reguaId) {  // 🔥 ADICIONADO

	    log.info("📋 Listando faturas com filtros:");
	    log.info("  - página: {}, tamanho: {}", page, size);
	    log.info("  - mês: {}, ano: {}", mes, ano);
	    log.info("  - numeroFatura: '{}'", numeroFatura);
	    log.info("  - associadoNome: '{}'", associadoNome);
	    log.info("  - status: '{}'", status);
	    log.info("  - associadoId: {}", associadoId);
	    log.info("  - reguaId: {}", reguaId);  // 🔥 LOG

	    String campoOrdenacao = "id";
	    if (sort != null && !sort.isEmpty()) {
	        switch (sort) {
	            case "dataEmissao": campoOrdenacao = "dataEmissao"; break;
	            case "valorTotal": campoOrdenacao = "valorTotal"; break;
	            case "numeroFatura": campoOrdenacao = "numeroFatura"; break;
	            default: campoOrdenacao = "id";
	        }
	    }

	    Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
	    Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, campoOrdenacao));

	    // 🔥 PASSAR reguaId PARA O SERVICE
	    Page<FaturaResumoDTO> faturas = faturaService.listarFaturasComFiltros(
	        numeroFatura, associadoNome, status, mes, ano, associadoId, reguaId, pageable);

	    log.info("✅ Total de faturas encontradas: {}", faturas.getTotalElements());

	    return ResponseEntity.ok(faturas);
	}
	
	@GetMapping("/faturas/associado/{associadoId}")
	public ResponseEntity<Page<Fatura>> listarFaturasPorAssociado(@PathVariable Long associadoId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		log.info("📋 Listando faturas do associado: {} - página: {}", associadoId, page);

		Pageable pageable = PageRequest.of(page, size, Sort.by("dataEmissao").descending());
		Page<Fatura> faturas = faturaService.listarPorAssociado(associadoId, pageable);

		return ResponseEntity.ok(faturas);
	}

	@GetMapping("/faturas/lote/{loteId}")
	public ResponseEntity<Page<Fatura>> listarFaturasPorLote(@PathVariable Long loteId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

		log.info("📋 Listando faturas do lote: {} - página: {}", loteId, page);

		Pageable pageable = PageRequest.of(page, size, Sort.by("dataEmissao").descending());
		Page<Fatura> faturas = faturaService.listarPorLoteProcessamento(loteId, pageable);

		return ResponseEntity.ok(faturas);
	}

	@GetMapping("/faturas/pendentes-rm")
	public ResponseEntity<List<Fatura>> listarFaturasPendentesRM() {
		log.info("📋 Listando faturas pendentes de integração com RM");

		List<Fatura> faturas = faturaService.listarFaturasPendentesRM();

		return ResponseEntity.ok(faturas);
	}

	@GetMapping("/faturas/{id}")
	public ResponseEntity<FaturaDetalheDTO> buscarFaturaPorId(@PathVariable Long id) {
		log.info("🔍 Buscando fatura ID: {}", id);

		FaturaDetalheDTO fatura = faturaService.buscarFaturaDetalheDTO(id);

		return ResponseEntity.ok(fatura);
	}

	@PostMapping("/faturas/{id}/marcar-processada-rm")
	public ResponseEntity<Fatura> marcarFaturaProcessadaRm(@PathVariable Long id,
			@RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

		log.info("🏷️ Marcando fatura {} como processada no RM", id);

		Fatura fatura = faturaService.marcarComoProcessadoRm(id);

		return ResponseEntity.ok(fatura);
	}

	// ========== EXPORTAÇÃO RM ==========

	@PostMapping("/faturas/{id}/exportar-rm")
	public ResponseEntity<byte[]> exportarRmFatura(
	        @PathVariable Long id,
	        @RequestParam Integer ultimoNumeroRps,
	        @RequestParam(required = false) Long reguaId,
	        @RequestParam(required = false) LocalDate mesReferencia,
	        @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
	    
	    log.info("📤 Exportando RM para fatura ID: {}", id);
	    
	    byte[] arquivo = faturaService.exportarRmFatura(id, ultimoNumeroRps, usuario, reguaId, mesReferencia);
	    
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fatura_" + id + ".rm")
	            .contentType(MediaType.TEXT_PLAIN)
	            .body(arquivo);
	}

	@PostMapping("/faturas/exportar-rm-lote")
	public ResponseEntity<byte[]> exportarRmLote(
	        @RequestBody ExportacaoRmRequest request,
	        @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
	    
	    log.info("📤 Exportando RM lote para {} faturas", request.getFaturaIds().size());
	    
	    byte[] arquivo = faturaService.exportarRmLote(
	        request.getFaturaIds(), 
	        request.getUltimoNumeroRps(), 
	        usuario,
	        request.getReguaId(),
	        request.getMesReferencia() != null ? LocalDate.parse(request.getMesReferencia()) : null
	    );
	    
	    return ResponseEntity.ok()
	            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exportacao_rm_lote.rm")
	            .contentType(MediaType.TEXT_PLAIN)
	            .body(arquivo);
	}

	@PostMapping("/faturas/exportar-rm-lote-metadados")
	public ResponseEntity<?> exportarRmLoteMetadados(
	        @RequestBody ExportacaoRmRequest request,
	        @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
	    
	    log.info("📤 Exportando RM lote com metadados para {} faturas", 
	        request.getFaturaIds() != null ? request.getFaturaIds().size() : 0);
	    
	    try {
	        if (request.getFaturaIds() == null || request.getFaturaIds().isEmpty()) {
	            return ResponseEntity.badRequest()
	                .body(Map.of("success", false, "message", "Nenhuma fatura selecionada"));
	        }
	        
	        LocalDate mesRef = request.getMesReferencia() != null ? 
	            LocalDate.parse(request.getMesReferencia() + "-01") : LocalDate.now();
	        
	        byte[] arquivo = faturaService.exportarRmLote(
	            request.getFaturaIds(), 
	            request.getUltimoNumeroRps(), 
	            usuario,
	            request.getReguaId(),
	            mesRef
	        );
	        
	        List<Map<String, Object>> detalhesDTO = new ArrayList<>();
	        BigDecimal valorTotal = BigDecimal.ZERO;
	        
	        for (Long id : request.getFaturaIds()) {
	            try {
	                FaturaDetalheDTO fatura = faturaService.buscarFaturaDetalheDTO(id);
	                Map<String, Object> faturaMap = new LinkedHashMap<>();
	                faturaMap.put("faturaId", fatura.getId());
	                faturaMap.put("numeroFatura", fatura.getNumeroFatura());
	                faturaMap.put("numeroRps", fatura.getNumeroRps());
	                faturaMap.put("status", "SUCESSO");
	                faturaMap.put("mensagem", null);
	                faturaMap.put("associadoNome", fatura.getAssociadoNome());
	                faturaMap.put("codigoRm", null);
	                faturaMap.put("codigoSpc", fatura.getCodigoSpc());
	                faturaMap.put("cnpjCpf", fatura.getCnpjCpf());
	                faturaMap.put("valorTotal", fatura.getValorTotal());
	                faturaMap.put("dataEmissao", fatura.getDataEmissao().toString());
	                faturaMap.put("dataVencimento", fatura.getDataVencimento().toString());
	                
	                if (fatura.getItens() != null) {
	                    List<Map<String, Object>> itensDTO = new ArrayList<>();
	                    for (FaturaItemDTO item : fatura.getItens()) {
	                        Map<String, Object> itemMap = new LinkedHashMap<>();
	                        itemMap.put("codigoProduto", item.getCodigoProduto());
	                        itemMap.put("descricao", item.getDescricao());
	                        itemMap.put("quantidade", item.getQuantidade());
	                        itemMap.put("valorUnitario", item.getValorUnitario());
	                        itemMap.put("valorTotal", item.getValorTotal());
	                        itensDTO.add(itemMap);
	                    }
	                    faturaMap.put("itens", itensDTO);
	                }
	                detalhesDTO.add(faturaMap);
	                valorTotal = valorTotal.add(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
	                
	            } catch (Exception e) {
	                log.warn("⚠️ Erro ao buscar detalhes da fatura {}", id, e);
	                Map<String, Object> fallback = new LinkedHashMap<>();
	                fallback.put("faturaId", id);
	                fallback.put("numeroFatura", "FAT-" + id);
	                fallback.put("numeroRps", 0);
	                fallback.put("status", "ERRO");
	                fallback.put("mensagem", e.getMessage());
	                fallback.put("associadoNome", "N/A");
	                fallback.put("cnpjCpf", "");
	                fallback.put("valorTotal", 0);
	                fallback.put("itens", new ArrayList<>());
	                detalhesDTO.add(fallback);
	            }
	        }
	        
	        Map<String, Object> response = new LinkedHashMap<>();
	        response.put("success", true);
	        response.put("loteId", System.currentTimeMillis());
	        response.put("totalFaturas", request.getFaturaIds().size());
	        response.put("faturasProcessadas", request.getFaturaIds().size());
	        response.put("faturasComErro", 0);
	        response.put("faturasIgnoradas", 0);
	        response.put("faturasIgnoradasIds", new ArrayList<>());
	        response.put("valorTotalIgnorado", 0);
	        response.put("primeiroNumeroRps", request.getUltimoNumeroRps() + 1);
	        response.put("ultimoNumeroRps", request.getUltimoNumeroRps() + request.getFaturaIds().size());
	        response.put("dataProcessamento", LocalDateTime.now().toString());
	        response.put("valorTotal", valorTotal);
	        response.put("detalhes", detalhesDTO);
	        response.put("arquivoBase64", Base64.getEncoder().encodeToString(arquivo));
	        
	        log.info("✅ Exportação RM com metadados concluída: {} faturas", request.getFaturaIds().size());
	        
	        return ResponseEntity.ok(response);
	        
	    } catch (Exception e) {
	        log.error("❌ Erro ao exportar RM lote com metadados", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	            .body(Map.of("success", false, "message", e.getMessage()));
	    }
	}
	
	// ========== EDIÇÃO DE ITENS ==========

	@PostMapping("/faturas/{faturaId}/itens")
	public ResponseEntity<FaturaItemResponseDTO> adicionarItemFatura(
	        @PathVariable Long faturaId,
	        @RequestBody FaturaItemDTO itemDTO,
	        @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
	    
	    log.info("➕ Adicionando item à fatura ID: {}", faturaId);
	    FaturaItemResponseDTO item = faturaService.adicionarItemFatura(faturaId, itemDTO, usuario);
	    return ResponseEntity.status(HttpStatus.CREATED).body(item);
	}

	@DeleteMapping("/faturas/{faturaId}/itens/{itemId}")
	public ResponseEntity<Void> removerItemFatura(
	        @PathVariable Long faturaId,
	        @PathVariable Long itemId,
	        @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
	    
	    log.info("🗑️ Removendo item {} da fatura {}", itemId, faturaId);
	    faturaService.removerItemFatura(faturaId, itemId, usuario);
	    return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/faturas/{faturaId}/itens/{itemId}")
	public ResponseEntity<FaturaItemResponseDTO> atualizarItemFatura(
	        @PathVariable Long faturaId,
	        @PathVariable Long itemId,
	        @RequestBody FaturaItemDTO itemDTO,
	        @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
	    
	    log.info("✏️ Atualizando item {} da fatura {}", itemId, faturaId);
	    FaturaItemResponseDTO item = faturaService.atualizarItemFatura(faturaId, itemId, itemDTO, usuario);
	    return ResponseEntity.ok(item);
	}
	
	/**
     * 🔥 EXCLUIR FATURA
     * Apenas faturas com status PENDENTE ou SIMULADO podem ser excluídas
     */
    @DeleteMapping("/faturas/{id}")
    public ResponseEntity<Void> excluirFatura(
            @PathVariable Long id,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        
        log.info("🗑️ Excluindo fatura ID: {} pelo usuário: {}", id, usuario);
        faturaService.excluirFatura(id, usuario);
        return ResponseEntity.noContent().build();
    }
	
}