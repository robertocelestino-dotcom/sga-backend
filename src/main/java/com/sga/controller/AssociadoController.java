package com.sga.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.sga.dto.AssociadoDefFaturamentoDTO;
import com.sga.dto.AssociadoDefFaturamentoResumoDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.EmailDTO;
import com.sga.dto.EnderecoDTO;
import com.sga.dto.TelefoneDTO;
import com.sga.model.Categoria;
import com.sga.model.Planos;
import com.sga.model.Vendedor;
import com.sga.service.AssociadoDefFaturamentoService;
import com.sga.service.AssociadoService;

@RestController
@RequestMapping("/api/associados")
public class AssociadoController {

	private static final Logger logger = LoggerFactory.getLogger(AssociadoController.class);

	@Autowired
	private AssociadoService associadoService;

	@Autowired
	private AssociadoDefFaturamentoService associadoDefFaturamentoService;

	@GetMapping
	public ResponseEntity<Page<AssociadoResumoDTO>> listar(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "nomeRazao") String sort,
			@RequestParam(defaultValue = "asc") String direction, @RequestParam(required = false) String codigoSpc,
			@RequestParam(required = false) String codigoRm, @RequestParam(required = false) String cnpjCpf,
			@RequestParam(required = false) String nomeRazao, @RequestParam(required = false) String tipoPessoa,
			@RequestParam(required = false) String status, @RequestParam(required = false) Long vendedorId,
			@RequestParam(required = false) Long planoId, @RequestParam(required = false) Long categoriaId) {

		Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
		Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

		Page<AssociadoResumoDTO> associados = associadoService.listarComFiltros(pageable, codigoSpc, nomeRazao, cnpjCpf,
				status);

		return ResponseEntity.ok(associados);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AssociadoDTO> buscarPorId(@PathVariable Long id) {
		AssociadoDTO associado = associadoService.buscarPorId(id);
		return ResponseEntity.ok(associado);
	}

	@GetMapping("/cnpj-cpf/{cnpjCpf}")
	public ResponseEntity<AssociadoDTO> buscarPorCnpjCpf(@PathVariable String cnpjCpf) {
		AssociadoDTO associado = associadoService.buscarPorCnpjCpf(cnpjCpf);
		return ResponseEntity.ok(associado);
	}

	@PostMapping
	public ResponseEntity<AssociadoDTO> criar(@Valid @RequestBody AssociadoDTO associadoDTO) {
		AssociadoDTO novoAssociado = associadoService.criar(associadoDTO);
		return ResponseEntity.status(HttpStatus.CREATED).body(novoAssociado);
	}

	@PutMapping("/{id}")
	public ResponseEntity<AssociadoDTO> atualizar(@PathVariable Long id,
			@Valid @RequestBody AssociadoDTO associadoDTO) {
		AssociadoDTO atualizado = associadoService.atualizar(id, associadoDTO);
		return ResponseEntity.ok(atualizado);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> excluir(@PathVariable Long id) {
		associadoService.excluir(id);
		return ResponseEntity.noContent().build();
	}

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

	@GetMapping("/health")
	public ResponseEntity<?> healthCheck() {
		try {
			Long total = associadoService.countTotalAssociados();
			return ResponseEntity.ok().body(new HealthResponse("UP", "Associado Service", total));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
					.body(new HealthResponse("DOWN", "Associado Service", 0L));
		}
	}

	/**
	 * 🔥 ENDPOINT CORRIGIDO - Importação em lote com criação automática de
	 * configuração de faturamento
	 */
	@PostMapping("/importacao/lote")
	public ResponseEntity<List<AssociadoDTO>> importarAssociadosEmLote(@RequestBody List<AssociadoDTO> associados) {
		logger.info("📥 Importando {} associados em lote (UPSERT)", associados.size());

		List<AssociadoDTO> associadosImportados = new ArrayList<>();
		int criados = 0;
		int atualizados = 0;
		int erros = 0;
		int configuracoesCriadas = 0;

		for (AssociadoDTO dto : associados) {
			try {
				// Validação básica
				if (dto.getCnpjCpf() == null || dto.getNomeRazao() == null) {
					logger.warn("⚠️ Associado ignorado: dados obrigatórios faltando");
					erros++;
					continue;
				}

				// Valores padrão
				if (dto.getStatus() == null)
					dto.setStatus("A");
				if (dto.getTipoPessoa() == null)
					dto.setTipoPessoa("F");

				// 🔥 GARANTIR QUE AS CONFIGURAÇÕES DE FATURAMENTO EXISTAM
				boolean precisaCriarConfiguracao = false;

				if (dto.getDefinicoesFaturamento() == null || dto.getDefinicoesFaturamento().isEmpty()) {
					precisaCriarConfiguracao = true;
					AssociadoDefFaturamentoDTO faturamento = new AssociadoDefFaturamentoDTO();

					// Plano padrão: 5
					Long planoId = dto.getPlanoId() != null ? dto.getPlanoId() : 5L;
					faturamento.setPlanoId(planoId);
					faturamento.setDiaEmissao(26);
					faturamento.setDiaVencimento(10);
					faturamento.setValorDef(BigDecimal.valueOf(85.00));
					faturamento.setObservacao("Configuração padrão - Importação em lote");

					dto.setDefinicoesFaturamento(List.of(faturamento));
					logger.info("📅 Configuração de faturamento adicionada para {}", dto.getNomeRazao());
				}

				// Verificar se já existe associado pelo CNPJ/CPF
				AssociadoDTO resultado;
				boolean associadoExistia = false;

				try {
					AssociadoDTO existente = associadoService.buscarPorCnpjCpf(dto.getCnpjCpf());
					if (existente != null && existente.getId() != null) {
						associadoExistia = true;
						dto.setId(existente.getId());
						resultado = associadoService.atualizar(existente.getId(), dto);
						atualizados++;
						logger.info("✏️ Associado atualizado: {}", dto.getNomeRazao());

						// 🔥 Verificar se já existe configuração de faturamento para o associado
						// atualizado
						var configsExistentes = associadoDefFaturamentoService.listarPorAssociado(existente.getId());
						if (configsExistentes == null || configsExistentes.isEmpty()) {
							// Criar configuração padrão para associado existente que não tem
							AssociadoDefFaturamentoDTO novaConfig = new AssociadoDefFaturamentoDTO();
							novaConfig.setAssociadoId(existente.getId());
							novaConfig.setPlanoId(dto.getPlanoId() != null ? dto.getPlanoId() : 5L);
							novaConfig.setDiaEmissao(26);
							novaConfig.setDiaVencimento(10);
							novaConfig.setValorDef(BigDecimal.valueOf(85.00));
							novaConfig.setObservacao("Configuração padrão - Criada automaticamente");

							associadoDefFaturamentoService.criar(novaConfig);
							configuracoesCriadas++;
							logger.info("📅 Configuração de faturamento criada para associado existente ID: {}",
									existente.getId());
						}
					} else {
						throw new Exception("Associado não encontrado");
					}
				} catch (Exception e) {
					// Associado não existe, criar novo
					associadoExistia = false;
					resultado = associadoService.criar(dto);
					criados++;
					configuracoesCriadas++;
					logger.info("✅ Associado criado com configuração de faturamento: {}", dto.getNomeRazao());
				}

				// 🔥 Se for novo associado e a configuração não foi criada pelo serviço, criar
				// manualmente
				if (!associadoExistia && resultado != null && resultado.getId() != null) {
					var configsExistentes = associadoDefFaturamentoService.listarPorAssociado(resultado.getId());
					if (configsExistentes == null || configsExistentes.isEmpty()) {
						AssociadoDefFaturamentoDTO novaConfig = new AssociadoDefFaturamentoDTO();
						novaConfig.setAssociadoId(resultado.getId());
						novaConfig.setPlanoId(dto.getPlanoId() != null ? dto.getPlanoId() : 5L);
						novaConfig.setDiaEmissao(26);
						novaConfig.setDiaVencimento(10);
						novaConfig.setValorDef(BigDecimal.valueOf(85.00));
						novaConfig.setObservacao("Configuração padrão - Criada automaticamente");

						associadoDefFaturamentoService.criar(novaConfig);
						logger.info("📅 Configuração de faturamento criada para novo associado ID: {}",
								resultado.getId());
					}
				}

				associadosImportados.add(resultado);

			} catch (Exception e) {
				logger.error("❌ Erro ao processar associado {}: {}", dto.getNomeRazao(), e.getMessage(), e);
				erros++;
			}
		}

		logger.info("📊 Importação concluída: {} criados, {} atualizados, {} erros, {} configurações criadas", criados,
				atualizados, erros, configuracoesCriadas);

		return ResponseEntity.ok().header("X-Importacao-Criados", String.valueOf(criados))
				.header("X-Importacao-Atualizados", String.valueOf(atualizados))
				.header("X-Importacao-Erros", String.valueOf(erros))
				.header("X-Importacao-Configuracoes", String.valueOf(configuracoesCriadas)).body(associadosImportados);
	}

	@PutMapping("/{id}/enderecos")
	public ResponseEntity<List<EnderecoDTO>> atualizarEnderecos(@PathVariable Long id,
			@Valid @RequestBody List<EnderecoDTO> enderecosDTO) {
		List<EnderecoDTO> enderecosAtualizados = associadoService.atualizarEnderecos(id, enderecosDTO);
		return ResponseEntity.ok(enderecosAtualizados);
	}

	@PutMapping("/{id}/telefones")
	public ResponseEntity<List<TelefoneDTO>> atualizarTelefones(@PathVariable Long id,
			@Valid @RequestBody List<TelefoneDTO> telefonesDTO) {
		List<TelefoneDTO> telefonesAtualizados = associadoService.atualizarTelefones(id, telefonesDTO);
		return ResponseEntity.ok(telefonesAtualizados);
	}

	@PutMapping("/{id}/emails")
	public ResponseEntity<List<EmailDTO>> atualizarEmails(@PathVariable Long id,
			@Valid @RequestBody List<EmailDTO> emailsDTO) {
		List<EmailDTO> emailsAtualizados = associadoService.atualizarEmails(id, emailsDTO);
		return ResponseEntity.ok(emailsAtualizados);
	}

	@GetMapping("/{id}/enderecos")
	public ResponseEntity<List<EnderecoDTO>> buscarEnderecos(@PathVariable Long id) {
		List<EnderecoDTO> enderecos = associadoService.buscarEnderecosPorAssociadoId(id);
		return ResponseEntity.ok(enderecos);
	}

	@GetMapping("/{id}/telefones")
	public ResponseEntity<List<TelefoneDTO>> buscarTelefones(@PathVariable Long id) {
		List<TelefoneDTO> telefones = associadoService.buscarTelefonesPorAssociadoId(id);
		return ResponseEntity.ok(telefones);
	}

	@GetMapping("/{id}/emails")
	public ResponseEntity<List<EmailDTO>> buscarEmails(@PathVariable Long id) {
		List<EmailDTO> emails = associadoService.buscarEmailsPorAssociadoId(id);
		return ResponseEntity.ok(emails);
	}
	
	// Adicione o endpoint:
	@GetMapping("/{id}/configuracoes-faturamento")
	public ResponseEntity<List<AssociadoDefFaturamentoDTO>> buscarConfiguracoesFaturamento(@PathVariable Long id) {
	    logger.info("📋 Buscando configurações de faturamento do associado ID: {}", id);
	    
	    List<AssociadoDefFaturamentoResumoDTO> configs = associadoDefFaturamentoService.listarPorAssociado(id);
	    
	    List<AssociadoDefFaturamentoDTO> result = configs.stream().map(config -> {
	        AssociadoDefFaturamentoDTO dto = new AssociadoDefFaturamentoDTO();
	        dto.setId(config.getId());
	        dto.setAssociadoId(id);
	        dto.setPlanoId(config.getPlanoId());
	        dto.setDiaEmissao(config.getDiaEmissao());
	        dto.setDiaVencimento(config.getDiaVencimento());
	        dto.setValorDef(config.getValorDef());
	        return dto;
	    }).collect(Collectors.toList());
	    
	    return ResponseEntity.ok(result);
	}

	public static class HealthResponse {
		private String status;
		private String service;
		private Long totalAssociados;

		public HealthResponse(String status, String service, Long totalAssociados) {
			this.status = status;
			this.service = service;
			this.totalAssociados = totalAssociados;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public Long getTotalAssociados() {
			return totalAssociados;
		}

		public void setTotalAssociados(Long totalAssociados) {
			this.totalAssociados = totalAssociados;
		}
	}
}