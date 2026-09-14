package com.sga.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.model.FranquiaServicoCoberto;
import com.sga.repository.FranquiaServicoCobertoRepository;
import com.sga.service.FranquiaCoberturaCache;

@RestController
@RequestMapping("/api/admin/franquia-cobertura")
@PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
public class FranquiaCoberturaController {

	private final FranquiaServicoCobertoRepository repository;
	private final FranquiaCoberturaCache cache;

	public FranquiaCoberturaController(FranquiaServicoCobertoRepository repository, FranquiaCoberturaCache cache) {
		this.repository = repository;
		this.cache = cache;
	}

	@GetMapping
	public List<FranquiaServicoCoberto> listar() {
		return repository.findAll();
	}

	@GetMapping("/franquia/{franquiaNomeBase}")
	public List<FranquiaServicoCoberto> listarPorFranquia(@PathVariable String franquiaNomeBase) {
		return repository.findAll().stream().filter(f -> f.getFranquiaNomeBase().equalsIgnoreCase(franquiaNomeBase))
				.collect(Collectors.toList()); // ✅ Java 11 compatível
	}

	@PostMapping
	@Transactional
	public FranquiaServicoCoberto criar(@RequestBody FranquiaServicoCoberto cobertura) {
		FranquiaServicoCoberto salvo = repository.save(cobertura);
		cache.limparCache();
		return salvo;
	}

	@PutMapping("/{id}")
	@Transactional
	public FranquiaServicoCoberto atualizar(@PathVariable Long id, @RequestBody FranquiaServicoCoberto cobertura) {
		FranquiaServicoCoberto existente = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Cobertura não encontrada: " + id));
		existente.setFranquiaNomeBase(cobertura.getFranquiaNomeBase());
		existente.setServicoNomeBase(cobertura.getServicoNomeBase());
		existente.setPrioridade(cobertura.getPrioridade());
		existente.setAtivo(cobertura.getAtivo());
		existente.setObservacao(cobertura.getObservacao());
		FranquiaServicoCoberto atualizado = repository.save(existente);
		cache.limparCache();
		return atualizado;
	}

	@DeleteMapping("/{id}")
	@Transactional
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		repository.deleteById(id);
		cache.limparCache();
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/refresh-cache")
	public ResponseEntity<Void> refreshCache() {
		cache.limparCache();
		return ResponseEntity.ok().build();
	}
}