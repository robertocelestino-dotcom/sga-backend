package com.sga.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.AssociadoPlanoDTO;
import com.sga.service.AssociadoPlanoService;

@RestController
@RequestMapping("/api/associados-planos")
@CrossOrigin(origins = "*")
public class AssociadoPlanoController {

	private final AssociadoPlanoService service;

	public AssociadoPlanoController(AssociadoPlanoService service) {
		this.service = service;
	}

	@PostMapping("/associar")
	public ResponseEntity<AssociadoPlanoDTO> associarPlano(@RequestParam Long associadoId, @RequestParam Long planoId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataAdesao,
			@RequestParam(required = false) String observacao) {

		AssociadoPlanoDTO dto = service.associarPlano(associadoId, planoId, dataAdesao, observacao);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/{id}/cancelar")
	public ResponseEntity<AssociadoPlanoDTO> cancelarPlano(@PathVariable Long id,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataCancelamento,
			@RequestParam(required = false) String motivo) {

		AssociadoPlanoDTO dto = service.cancelarPlano(id, dataCancelamento, motivo);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/associado/{associadoId}")
	public ResponseEntity<List<AssociadoPlanoDTO>> listarPlanosDoAssociado(@PathVariable Long associadoId) {

		List<AssociadoPlanoDTO> dtos = service.listarPlanosDoAssociado(associadoId);
		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/associado/{associadoId}/ativos")
	public ResponseEntity<List<AssociadoPlanoDTO>> listarPlanosAtivos(@PathVariable Long associadoId) {

		List<AssociadoPlanoDTO> dtos = service.listarPlanosAtivos(associadoId);
		return ResponseEntity.ok(dtos);
	}
}