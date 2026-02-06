// src/main/java/com/sga/controller/AssociadoController.java - VERSÃO CORRIGIDA
package com.sga.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.AssociadoDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.EmailDTO;
import com.sga.dto.EnderecoDTO;
import com.sga.dto.TelefoneDTO;
import com.sga.model.Categoria;
import com.sga.model.Planos;
import com.sga.model.Vendedor;
import com.sga.service.AssociadoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/associados")
@RequiredArgsConstructor
public class AssociadoController {

	private static final Logger logger = LoggerFactory.getLogger(AssociadoController.class);

	@Autowired
	private AssociadoService associadoService;

	@GetMapping
	public ResponseEntity<Page<AssociadoResumoDTO>> listar(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "nomeRazao") String sort,
			@RequestParam(defaultValue = "asc") String direction, @RequestParam(required = false) String codigoSpc,
			@RequestParam(required = false) String codigoRm, @RequestParam(required = false) String cnpjCpf,
			@RequestParam(required = false) String nomeRazao, @RequestParam(required = false) String tipoPessoa,
			@RequestParam(required = false) String status, @RequestParam(required = false) Long vendedorId,
			@RequestParam(required = false) Long planoId, @RequestParam(required = false) Long categoriaId) {

		logger.info("Listando associados - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);
		logger.info("Filtros - codigoSpc: {}, nomeRazao: {}, cnpjCpf: {}, status: {}", codigoSpc, nomeRazao, cnpjCpf,
				status);

		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

		Page<AssociadoResumoDTO> associados = associadoService.listarComFiltros(pageable, codigoSpc, nomeRazao, cnpjCpf,
				status);

		logger.info("Encontrados {} associados", associados.getTotalElements());
		return ResponseEntity.ok(associados);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AssociadoDTO> buscarPorId(@PathVariable Long id) {
		logger.info("Buscando associado por ID: {}", id);

		AssociadoDTO associado = associadoService.buscarPorId(id);
		return ResponseEntity.ok(associado);
	}

	@GetMapping("/cnpj-cpf/{cnpjCpf}")
	public ResponseEntity<AssociadoDTO> buscarPorCnpjCpf(@PathVariable String cnpjCpf) {
		logger.info("Buscando associado por CNPJ/CPF: {}", cnpjCpf);

		AssociadoDTO associado = associadoService.buscarPorCnpjCpf(cnpjCpf);
		return ResponseEntity.ok(associado);
	}

	@PostMapping
	public ResponseEntity<AssociadoDTO> criar(@Valid @RequestBody AssociadoDTO associadoDTO) {
		logger.info("Criando novo associado: {}", associadoDTO.getNomeRazao());

		AssociadoDTO novoAssociado = associadoService.criar(associadoDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(novoAssociado);
	}

	@PutMapping("/{id}")
	public ResponseEntity<AssociadoDTO> atualizar(@PathVariable Long id,
			@Valid @RequestBody AssociadoDTO associadoDTO) {
		logger.info("Atualizando associado ID: {}", id);

		AssociadoDTO atualizado = associadoService.atualizar(id, associadoDTO);
		return ResponseEntity.ok(atualizado);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		logger.info("Excluindo associado ID: {}", id);

		associadoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

	// Métodos para carregar combos/dropdowns
	@GetMapping("/vendedores")
	public ResponseEntity<List<Vendedor>> buscarVendedores() {
		List<Vendedor> vendedores = associadoService.buscarTodosVendedores();
		return ResponseEntity.ok(vendedores);
	}

	@GetMapping("/planos")
	public ResponseEntity<List<Planos>> buscarPlanos() {
		List<Planos> planos = associadoService.buscarTodosPlanos();
		return ResponseEntity.ok(planos);
	}

	@GetMapping("/categorias")
	public ResponseEntity<List<Categoria>> buscarCategorias() {
		List<Categoria> categorias = associadoService.buscarTodasCategorias();
		return ResponseEntity.ok(categorias);
	}

	// Endpoints para estatísticas
	@GetMapping("/estatisticas/total")
	public ResponseEntity<Long> getTotalAssociados() {
		Long total = associadoService.countTotalAssociados();
		return ResponseEntity.ok(total);
	}

	@GetMapping("/estatisticas/ativos")
	public ResponseEntity<Long> getAssociadosAtivos() {
		Long ativos = associadoService.countAssociadosAtivos();
		return ResponseEntity.ok(ativos);
	}

	// Health check
	@GetMapping("/health")
	public ResponseEntity<?> healthCheck() {
		try {
			Long total = associadoService.countTotalAssociados();
			return ResponseEntity.ok().body(new HealthResponse("UP", "Associado Service", total));
		} catch (Exception e) {
			logger.error("Health check failed: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new HealthResponse("DOWN", "Associado Service", 0L));
		}
	}

	// Classe interna para resposta de health check
	public static class HealthResponse {
		private String status;
		private String service;
		private Long totalAssociados;

		public HealthResponse(String status, String service, Long totalAssociados) {
			this.status = status;
			this.service = service;
			this.totalAssociados = totalAssociados;
		}

		// Getters
		public String getStatus() {
			return status;
		}

		public String getService() {
			return service;
		}

		public Long getTotalAssociados() {
			return totalAssociados;
		}

		// Setters (para serialização JSON)
		public void setStatus(String status) {
			this.status = status;
		}

		public void setService(String service) {
			this.service = service;
		}

		public void setTotalAssociados(Long totalAssociados) {
			this.totalAssociados = totalAssociados;
		}
	}

	// No AssociadoController.java, adicione estes endpoints:

	@PutMapping("/{id}/enderecos")
	public ResponseEntity<List<EnderecoDTO>> atualizarEnderecos(@PathVariable Long id,
			@Valid @RequestBody List<EnderecoDTO> enderecosDTO) {

		logger.info("Atualizando endereços do associado ID: {}", id);

		List<EnderecoDTO> enderecosAtualizados = associadoService.atualizarEnderecos(id, enderecosDTO);
		return ResponseEntity.ok(enderecosAtualizados);
	}

	@PutMapping("/{id}/telefones")
	public ResponseEntity<List<TelefoneDTO>> atualizarTelefones(@PathVariable Long id,
			@Valid @RequestBody List<TelefoneDTO> telefonesDTO) {

		logger.info("Atualizando telefones do associado ID: {}", id);

		List<TelefoneDTO> telefonesAtualizados = associadoService.atualizarTelefones(id, telefonesDTO);
		return ResponseEntity.ok(telefonesAtualizados);
	}

	@PutMapping("/{id}/emails")
	public ResponseEntity<List<EmailDTO>> atualizarEmails(@PathVariable Long id,
			@Valid @RequestBody List<EmailDTO> emailsDTO) {

		logger.info("Atualizando emails do associado ID: {}", id);

		List<EmailDTO> emailsAtualizados = associadoService.atualizarEmails(id, emailsDTO);
		return ResponseEntity.ok(emailsAtualizados);
	}

	// Endpoints para buscar separadamente (opcional)
	@GetMapping("/{id}/enderecos")
	public ResponseEntity<List<EnderecoDTO>> buscarEnderecos(@PathVariable Long id) {
		logger.info("Buscando endereços do associado ID: {}", id);

		List<EnderecoDTO> enderecos = associadoService.buscarEnderecosPorAssociadoId(id);
		return ResponseEntity.ok(enderecos);
	}

	@GetMapping("/{id}/telefones")
	public ResponseEntity<List<TelefoneDTO>> buscarTelefones(@PathVariable Long id) {
		logger.info("Buscando telefones do associado ID: {}", id);

		List<TelefoneDTO> telefones = associadoService.buscarTelefonesPorAssociadoId(id);
		return ResponseEntity.ok(telefones);
	}

	@GetMapping("/{id}/emails")
	public ResponseEntity<List<EmailDTO>> buscarEmails(@PathVariable Long id) {
		logger.info("Buscando emails do associado ID: {}", id);

		List<EmailDTO> emails = associadoService.buscarEmailsPorAssociadoId(id);
		return ResponseEntity.ok(emails);
	}

}