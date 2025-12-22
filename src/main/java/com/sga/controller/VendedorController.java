package com.sga.controller;

import com.sga.dto.VendedorDTO;
import com.sga.dto.VendedorResumoDTO;
import com.sga.service.VendedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendedores")
@RequiredArgsConstructor
public class VendedorController {

	private VendedorService vendedorService;

	@PostMapping
	public ResponseEntity<VendedorDTO> criar(@RequestBody VendedorDTO dto) {
		// Validação simples
		if (dto.getNomeRazao() == null || dto.getNomeRazao().trim().isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		VendedorDTO vendedor = vendedorService.criar(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(vendedor);
	}

	@PutMapping("/{id}")
	public ResponseEntity<VendedorDTO> atualizar(@PathVariable Long id, @RequestBody VendedorDTO dto) {
		VendedorDTO vendedor = vendedorService.atualizar(id, dto);
		return ResponseEntity.ok(vendedor);
	}

	@GetMapping("/{id}")
	public ResponseEntity<VendedorDTO> buscarPorId(@PathVariable Long id) {
		VendedorDTO vendedor = vendedorService.buscarPorId(id);
		return ResponseEntity.ok(vendedor);
	}

	@GetMapping
	public ResponseEntity<Page<VendedorResumoDTO>> listar(
			Pageable pageable,
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String status) {
		Page<VendedorResumoDTO> vendedores = vendedorService.listar(pageable, nome, status);
		return ResponseEntity.ok(vendedores);
	}

	@GetMapping("/ativos")
	public ResponseEntity<List<VendedorResumoDTO>> listarAtivos() {
		List<VendedorResumoDTO> vendedores = vendedorService.listarTodosAtivos();
		return ResponseEntity.ok(vendedores);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		vendedorService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/{id}/inativar")
	public ResponseEntity<Void> inativar(@PathVariable Long id) {
		vendedorService.inativar(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return ResponseEntity.ok("VendedorController está funcionando!");
	}
}