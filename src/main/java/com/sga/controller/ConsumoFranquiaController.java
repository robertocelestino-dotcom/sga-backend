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

import com.sga.dto.ConsumoFranquiaDTO;
import com.sga.service.ConsumoFranquiaService;

@RestController
@RequestMapping("/api/consumo-franquia")
@CrossOrigin(origins = "*")
public class ConsumoFranquiaController {

	private final ConsumoFranquiaService service;

	public ConsumoFranquiaController(ConsumoFranquiaService service) {
		this.service = service;
	}

	@PostMapping("/registrar")
	public ResponseEntity<ConsumoFranquiaDTO> registrarConsumo(@RequestParam Long associadoId,
			@RequestParam Long produtoId, @RequestParam Integer quantidade,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

		ConsumoFranquiaDTO dto = service.registrarConsumo(associadoId, produtoId, quantidade, data);
		return ResponseEntity.ok(dto);
	}

	@PostMapping("/registrar-lote")
	public ResponseEntity<ConsumoFranquiaDTO> registrarConsumoEmLote(@RequestParam Long associadoId,
			@RequestParam Long produtoId, @RequestParam Integer quantidade,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

		ConsumoFranquiaDTO dto = service.registrarConsumoEmLote(associadoId, produtoId, quantidade, data);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/consumo")
	public ResponseEntity<ConsumoFranquiaDTO> buscarConsumo(@RequestParam Long associadoId,
			@RequestParam Long produtoId, @RequestParam Integer ano, @RequestParam Integer mes) {

		ConsumoFranquiaDTO dto = service.buscarConsumo(associadoId, produtoId, ano, mes);
		return ResponseEntity.ok(dto);
	}

	@GetMapping("/associado/{associadoId}/mes")
	public ResponseEntity<List<ConsumoFranquiaDTO>> listarConsumosDoMes(@PathVariable Long associadoId,
			@RequestParam Integer ano, @RequestParam Integer mes) {

		List<ConsumoFranquiaDTO> dtos = service.listarConsumosDoMes(associadoId, ano, mes);
		return ResponseEntity.ok(dtos);
	}

	@GetMapping("/associado/{associadoId}/excedentes")
	public ResponseEntity<List<ConsumoFranquiaDTO>> listarConsumosComExcedente(@PathVariable Long associadoId,
			@RequestParam Integer ano, @RequestParam Integer mes) {

		List<ConsumoFranquiaDTO> dtos = service.listarConsumosComExcedente(associadoId, ano, mes);
		return ResponseEntity.ok(dtos);
	}
}
