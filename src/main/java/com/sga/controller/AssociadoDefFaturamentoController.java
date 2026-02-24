package com.sga.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.AssociadoDefFaturamentoDTO;
import com.sga.dto.AssociadoDefFaturamentoResumoDTO;
import com.sga.service.AssociadoDefFaturamentoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/associados-def-faturamento")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssociadoDefFaturamentoController {

	private static final Logger logger = LoggerFactory.getLogger(AssociadoDefFaturamentoController.class);
	
	@Autowired
	private AssociadoDefFaturamentoService associadoDefFaturamentoService;

	/**
	 * Listar todas as definições de faturamento de um associado GET
	 * /api/associados-def-faturamento/associado/{associadoId}
	 */
	@GetMapping("/associado/{associadoId}")
	public ResponseEntity<List<AssociadoDefFaturamentoResumoDTO>> listarPorAssociado(@PathVariable Long associadoId) {
		logger.info("📋 Listando definições de faturamento do associado ID: {}", associadoId);
		List<AssociadoDefFaturamentoResumoDTO> result = associadoDefFaturamentoService.listarPorAssociado(associadoId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Listar definições de faturamento com paginação GET
	 * /api/associados-def-faturamento/associado/{associadoId}/paginado
	 */
	@GetMapping("/associado/{associadoId}/paginado")
	public ResponseEntity<Page<AssociadoDefFaturamentoResumoDTO>> listarPorAssociadoPaginado(
			@PathVariable Long associadoId,
			@PageableDefault(page = 0, size = 10, sort = "diaEmissao", direction = Sort.Direction.ASC) Pageable pageable) {
		logger.info("📋 Listando definições de faturamento do associado ID: {} - página: {}", associadoId,
				pageable.getPageNumber());
		Page<AssociadoDefFaturamentoResumoDTO> result = associadoDefFaturamentoService
				.listarPorAssociadoPaginado(associadoId, pageable);
		return ResponseEntity.ok(result);
	}

	/**
	 * Listar dias de emissão disponíveis para um associado GET
	 * /api/associados-def-faturamento/associado/{associadoId}/dias
	 */
	@GetMapping("/associado/{associadoId}/dias")
	public ResponseEntity<List<Integer>> listarDiasEmissaoPorAssociado(@PathVariable Long associadoId) {
		logger.info("📅 Listando dias de emissão do associado ID: {}", associadoId);
		List<Integer> result = associadoDefFaturamentoService.listarDiasEmissaoPorAssociado(associadoId);
		return ResponseEntity.ok(result);
	}

	/**
	 * Listar definições por dia de emissão GET
	 * /api/associados-def-faturamento/associado/{associadoId}/dia/{diaEmissao}
	 */
	@GetMapping("/associado/{associadoId}/dia/{diaEmissao}")
	public ResponseEntity<List<AssociadoDefFaturamentoResumoDTO>> listarPorDiaEmissao(@PathVariable Long associadoId,
			@PathVariable Integer diaEmissao) {
		logger.info("📋 Listando definições de faturamento do associado ID: {} para dia: {}", associadoId, diaEmissao);
		List<AssociadoDefFaturamentoResumoDTO> result = associadoDefFaturamentoService.listarPorDiaEmissao(associadoId,
				diaEmissao);
		return ResponseEntity.ok(result);
	}

	/**
	 * Buscar definição por ID GET /api/associados-def-faturamento/{id}
	 */
	@GetMapping("/{id}")
	public ResponseEntity<AssociadoDefFaturamentoDTO> buscarPorId(@PathVariable Long id) {
		logger.info("🔍 Buscando definição de faturamento por ID: {}", id);
		AssociadoDefFaturamentoDTO dto = associadoDefFaturamentoService.buscarPorId(id);
		return ResponseEntity.ok(dto);
	}

	/**
	 * Criar nova definição de faturamento POST /api/associados-def-faturamento
	 */
	@PostMapping
	public ResponseEntity<AssociadoDefFaturamentoDTO> criar(@Valid @RequestBody AssociadoDefFaturamentoDTO dto,
			@RequestHeader(value = "X-Usuario", required = false) String usuario) {
		logger.info("➕ Criando definição de faturamento para associado ID: {}, dia: {}", dto.getAssociadoId(),
				dto.getDiaEmissao());
		dto.setUsuario(usuario != null ? usuario : "SISTEMA");
		AssociadoDefFaturamentoDTO created = associadoDefFaturamentoService.criar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	/**
	 * Criar múltiplas definições em lote POST /api/associados-def-faturamento/lote
	 */
	@PostMapping("/lote")
	public ResponseEntity<List<AssociadoDefFaturamentoDTO>> criarEmLote(
			@Valid @RequestBody List<AssociadoDefFaturamentoDTO> dtos,
			@RequestHeader(value = "X-Usuario", required = false) String usuario) {
		logger.info("➕➕ Criando {} definições de faturamento em lote", dtos.size());
		dtos.forEach(dto -> dto.setUsuario(usuario != null ? usuario : "SISTEMA"));
		List<AssociadoDefFaturamentoDTO> created = associadoDefFaturamentoService.criarEmLote(dtos);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	/**
	 * Atualizar definição de faturamento PUT /api/associados-def-faturamento/{id}
	 */
	@PutMapping("/{id}")
	public ResponseEntity<AssociadoDefFaturamentoDTO> atualizar(@PathVariable Long id,
			@Valid @RequestBody AssociadoDefFaturamentoDTO dto,
			@RequestHeader(value = "X-Usuario", required = false) String usuario) {
		logger.info("✏️ Atualizando definição de faturamento ID: {}", id);
		dto.setUsuario(usuario != null ? usuario : "SISTEMA");
		AssociadoDefFaturamentoDTO updated = associadoDefFaturamentoService.atualizar(id, dto);
		return ResponseEntity.ok(updated);
	}

	/**
	 * Excluir definição de faturamento DELETE /api/associados-def-faturamento/{id}
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		logger.info("🗑️ Excluindo definição de faturamento ID: {}", id);
		associadoDefFaturamentoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Excluir todas as definições de um associado DELETE
	 * /api/associados-def-faturamento/associado/{associadoId}
	 */
	@DeleteMapping("/associado/{associadoId}")
	public ResponseEntity<Void> excluirPorAssociado(@PathVariable Long associadoId) {
		logger.info("🗑️ Excluindo todas as definições de faturamento do associado ID: {}", associadoId);
		associadoDefFaturamentoService.excluirPorAssociado(associadoId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Health check GET /api/associados-def-faturamento/health
	 */
	@GetMapping("/health")
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("AssociadoDefFaturamentoController está funcionando");
	}

}