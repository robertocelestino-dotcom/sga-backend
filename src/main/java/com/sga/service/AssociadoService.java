package com.sga.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.EmailDTO;
import com.sga.dto.EnderecoDTO;
import com.sga.dto.TelefoneDTO;
import com.sga.model.Associado;
import com.sga.model.Categoria;
import com.sga.model.Email;
import com.sga.model.Endereco;
import com.sga.model.Planos;
import com.sga.model.Telefone;
import com.sga.model.Vendedor;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.CategoriaRepository;
import com.sga.repository.EmailRepository;
import com.sga.repository.EnderecoRepository;
import com.sga.repository.PlanosRepository;
import com.sga.repository.TelefoneRepository;
import com.sga.repository.VendedorRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class AssociadoService {

	private static final Logger logger = LoggerFactory.getLogger(AssociadoService.class);

	@Autowired
	private AssociadoRepository associadoRepository;

	@Autowired
	private VendedorRepository vendedorRepository;

	@Autowired
	private PlanosRepository planosRepository;

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private EnderecoRepository enderecoRepository;

	@Autowired
	private TelefoneRepository telefoneRepository;

	@Autowired
	private EmailRepository emailRepository;

	@Autowired
	private SistemaLogService sistemaLogService;

	// ========== MÉTODOS DO CONTROLLER ==========

	@Transactional(readOnly = true)
	public Page<AssociadoResumoDTO> listarComFiltros(Pageable pageable, String codigoSpc, String nome, String cnpjCpf,
			String status) {
		logger.info("Listando associados com filtros - Nome: {}, CNPJ/CPF: {}, Status: {}, Código SPC: {}", nome,
				cnpjCpf, status, codigoSpc);

		// Use o método existente no repository
		Page<Associado> associados = associadoRepository.findByFiltrosCombinados(codigoSpc, nome, cnpjCpf, status,
				pageable);

		return associados.map(this::toResumoDTO);
	}

	@Transactional(readOnly = true)
	public AssociadoDTO buscarPorId(Long id) {
		logger.info("Buscando associado por ID: {}", id);

		Associado associado = associadoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + id));

		return toDTO(associado);
	}

	@Transactional(readOnly = true)
	public AssociadoDTO buscarPorCnpjCpf(String cnpjCpf) {
		logger.info("Buscando associado por CNPJ/CPF: {}", cnpjCpf);

		Optional<Associado> associado = associadoRepository.findByCnpjCpf(cnpjCpf);

		return associado.map(this::toDTO)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com CNPJ/CPF: " + cnpjCpf));
	}

	@Transactional
	public void excluir(Long id) {
		logger.info("Excluindo associado ID: {}", id);

		Associado associado = associadoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + id));

		associadoRepository.delete(associado);

		try {
			sistemaLogService.logExclusao("Associado", id.toString(),
					"Excluído associado: " + associado.getNomeRazao());
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		logger.info("Associado excluído: {} - {}", id, associado.getNomeRazao());
	}

	@Transactional(readOnly = true)
	public List<Vendedor> buscarTodosVendedores() {
		logger.info("Buscando todos os vendedores");
		return vendedorRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Planos> buscarTodosPlanos() {
		logger.info("Buscando todos os planos");
		return planosRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<Categoria> buscarTodasCategorias() {
		logger.info("Buscando todas as categorias");
		return categoriaRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Long countTotalAssociados() {
		logger.info("Contando total de associados");
		return associadoRepository.count();
	}

	@Transactional(readOnly = true)
	public Long countAssociadosAtivos() {
		logger.info("Contando associados ativos");
		return associadoRepository.countByStatus("A");
	}

	// ========== CONVERSORES DTO ==========

	private AssociadoResumoDTO toResumoDTO(Associado associado) {
		if (associado == null)
			return null;

		AssociadoResumoDTO dto = new AssociadoResumoDTO();
		dto.setId(associado.getId());
		dto.setCodigoSpc(associado.getCodigoSpc());
		dto.setCodigoRm(associado.getCodigoRm());
		dto.setCnpjCpf(associado.getCnpjCpf());
		dto.setNomeRazao(associado.getNomeRazao());
		dto.setNomeFantasia(associado.getNomeFantasia());
		dto.setTipoPessoa(associado.getTipoPessoa());
		dto.setStatus(associado.getStatus());
		dto.setFaturamentoMinimo(associado.getFaturamentoMinimo());
		dto.setDataFiliacao(associado.getDataFiliacao());
		dto.setDataCadastro(associado.getDataCadastro());

		// NOVOS CAMPOS
		dto.setDataInativacao(associado.getDataInativacao());
		dto.setDataInicioSuspensao(associado.getDataInicioSuspensao());
		dto.setDataFimSuspensao(associado.getDataFimSuspensao());
		dto.setMotivoInativacao(associado.getMotivoInativacao());
		dto.setMotivoSuspensao(associado.getMotivoSuspensao());

		// Vendedor
		if (associado.getVendedor() != null) {
			dto.setVendedorId(associado.getVendedor().getId());
			dto.setVendedorNome(associado.getVendedor().getNomeRazao());
		}

		// Vendedor Externo - CORREÇÃO: Verifica se é Vendedor primeiro
		Object vendedorExternoObj = associado.getVendedorExterno();
		if (vendedorExternoObj != null) {
			// Verifica se o objeto é uma instância de Vendedor
			if (vendedorExternoObj instanceof Vendedor) {
				Vendedor vendedorExterno = (Vendedor) vendedorExternoObj;
				dto.setVendedorExternoId(vendedorExterno.getId());
				dto.setVendedorExternoNome(vendedorExterno.getNomeRazao());
			} else {
				// Se não for Vendedor, tenta extrair um ID de alguma forma
				logger.debug("Vendedor externo não é instância de Vendedor: {}", vendedorExternoObj.getClass());
				// Pode ser que seja apenas um ID numérico armazenado
				// Neste caso, você precisa saber como seu modelo armazena isso
				// Vou deixar vazio por enquanto
			}
		}

		// Plano
		if (associado.getPlano() != null) {
			dto.setPlanoId(associado.getPlano().getId());
			dto.setPlanoTitulo(associado.getPlano().getPlano());
		}

		// Categoria
		if (associado.getCategoria() != null) {
			dto.setCategoriaId(associado.getCategoria().getId());
			dto.setCategoriaNome(associado.getCategoria().getDescricao());
		}

		return dto;
	}

	// Converter Entity para DTO completo
	public AssociadoDTO toDTO(Associado associado) {
		if (associado == null)
			return null;

		AssociadoDTO dto = new AssociadoDTO();
		dto.setId(associado.getId());
		dto.setCodigoSpc(associado.getCodigoSpc());
		dto.setCodigoRm(associado.getCodigoRm());
		dto.setCnpjCpf(associado.getCnpjCpf());
		dto.setNomeRazao(associado.getNomeRazao());
		dto.setNomeFantasia(associado.getNomeFantasia());
		dto.setTipoPessoa(associado.getTipoPessoa());
		dto.setStatus(associado.getStatus());
		dto.setFaturamentoMinimo(associado.getFaturamentoMinimo());
		dto.setDataFiliacao(associado.getDataFiliacao());
		dto.setDataCadastro(associado.getDataCadastro());

		// NOVOS CAMPOS
		dto.setDataInativacao(associado.getDataInativacao());
		dto.setDataInicioSuspensao(associado.getDataInicioSuspensao());
		dto.setDataFimSuspensao(associado.getDataFimSuspensao());
		dto.setMotivoInativacao(associado.getMotivoInativacao());
		dto.setMotivoSuspensao(associado.getMotivoSuspensao());

		// Vendedor
		if (associado.getVendedor() != null) {
			dto.setVendedorId(associado.getVendedor().getId());
			dto.setVendedorNome(associado.getVendedor().getNomeRazao());
		}

		// Vendedor Externo - CORREÇÃO: Verifica se é Vendedor
		Object vendedorExternoObj = associado.getVendedorExterno();
		if (vendedorExternoObj != null) {
			if (vendedorExternoObj instanceof Vendedor) {
				Vendedor vendedorExterno = (Vendedor) vendedorExternoObj;
				dto.setVendedorExternoId(vendedorExterno.getId().intValue());
				dto.setVendedorExternoNome(vendedorExterno.getNomeRazao());
			} else {
				logger.debug("Vendedor externo no DTO não é instância de Vendedor: {}", vendedorExternoObj.getClass());
				// Se não for Vendedor, não preenche os campos
			}
		}

		// Plano e Categoria
		if (associado.getPlano() != null) {
			dto.setPlanoId(associado.getPlano().getId());
		}

		if (associado.getCategoria() != null) {
			dto.setCategoriaId(associado.getCategoria().getId());
		}

		// Sub-entidades
		List<EnderecoDTO> enderecosDTO = new ArrayList<>();
		if (associado.getEnderecos() != null) {
			for (Endereco endereco : associado.getEnderecos()) {
				enderecosDTO.add(toEnderecoDTO(endereco));
			}
		}
		dto.setEnderecos(enderecosDTO);

		List<EmailDTO> emailsDTO = new ArrayList<>();
		if (associado.getEmails() != null) {
			for (Email email : associado.getEmails()) {
				emailsDTO.add(toEmailDTO(email));
			}
		}
		dto.setEmails(emailsDTO);

		List<TelefoneDTO> telefonesDTO = new ArrayList<>();
		if (associado.getTelefones() != null) {
			for (Telefone telefone : associado.getTelefones()) {
				telefonesDTO.add(toTelefoneDTO(telefone));
			}
		}
		dto.setTelefones(telefonesDTO);

		return dto;
	}

	// ========== MÉTODOS DE CONVERSÃO PARA SUB-ENTIDADES ==========

	private EnderecoDTO toEnderecoDTO(Endereco endereco) {
		if (endereco == null)
			return null;

		EnderecoDTO dto = new EnderecoDTO();
		dto.setId(endereco.getId());
		dto.setCep(endereco.getCep());
		dto.setLogradouro(endereco.getLogradouro());
		dto.setNumero(endereco.getNumero());
		dto.setComplemento(endereco.getComplemento());
		dto.setBairro(endereco.getBairro());
		dto.setCidade(endereco.getCidade());
		dto.setEstado(endereco.getEstado());
		dto.setTipoEndereco(endereco.getTipoEndereco());

		return dto;
	}

	private EmailDTO toEmailDTO(Email email) {
		if (email == null)
			return null;

		EmailDTO dto = new EmailDTO();
		dto.setId(email.getId());
		dto.setEmail(email.getEmail());
		dto.setTipoEmail(email.getTipoEmail());

		return dto;
	}

	private TelefoneDTO toTelefoneDTO(Telefone telefone) {
		if (telefone == null)
			return null;

		TelefoneDTO dto = new TelefoneDTO();
		dto.setId(telefone.getId());
		dto.setDdd(telefone.getDdd());
		dto.setNumero(telefone.getNumero());
		dto.setTipoTelefone(telefone.getTipoTelefone());

		return dto;
	}

	// ========== VALIDAÇÕES ==========

	private void validarDadosBasicos(AssociadoDTO dto) {
		if (dto.getNomeRazao() == null || dto.getNomeRazao().trim().isEmpty()) {
			throw new IllegalArgumentException("Nome/Razão Social é obrigatório");
		}

		if (dto.getCnpjCpf() == null || dto.getCnpjCpf().trim().isEmpty()) {
			throw new IllegalArgumentException("CNPJ/CPF é obrigatório");
		}

		if (dto.getTipoPessoa() == null || dto.getTipoPessoa().trim().isEmpty()) {
			throw new IllegalArgumentException("Tipo de Pessoa é obrigatório");
		}

		// Verificar se CNPJ/CPF já existe (apenas para criação)
		if (dto.getId() == null) {
			Optional<Associado> associadoExistente = associadoRepository.findByCnpjCpf(dto.getCnpjCpf());
			if (associadoExistente.isPresent()) {
				throw new IllegalArgumentException("Já existe um associado com este CNPJ/CPF: " + dto.getCnpjCpf());
			}
		}
	}

	// ========== MÉTODOS PRINCIPAIS ==========

	@Transactional
	public AssociadoDTO atualizar(Long id, AssociadoDTO associadoDTO) {
		logger.info("Atualizando associado ID: {}", id);

		Associado associado = associadoRepository.findById(id).orElseThrow(() -> {
			logger.error("Associado não encontrado com ID: {}", id);
			return new EntityNotFoundException("Associado não encontrado com ID: " + id);
		});

		// Salvar status anterior
		String statusAnterior = associado.getStatus();
		String novoStatus = associadoDTO.getStatus();

		// Validar mudança de status
		if (novoStatus != null && !novoStatus.equals(statusAnterior)) {
			validarMudancaStatus(associado, novoStatus);
		}

		updateEntityFromDTO(associado, associadoDTO);

		// Aplicar regras de status
		aplicarRegrasStatus(associado);

		Associado updated = associadoRepository.save(associado);
		logger.info("Associado atualizado: {} - {}", updated.getId(), updated.getNomeRazao());

		// REGISTRA LOG
		try {
			String mensagem = "Atualizado associado: " + updated.getNomeRazao();
			if (statusAnterior != null && !statusAnterior.equals(novoStatus)) {
				mensagem += " (Status alterado: " + statusAnterior + " → " + novoStatus + ")";
			}
			sistemaLogService.logAtualizacao("Associado", updated.getId().toString(), mensagem);
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		return toDTO(updated);
	}

	@Transactional
	public AssociadoDTO criar(AssociadoDTO associadoDTO) {
		logger.info("Criando novo associado: {}", associadoDTO.getNomeRazao());

		validarDadosBasicos(associadoDTO);

		Associado associado = new Associado();
		updateEntityFromDTO(associado, associadoDTO);

		aplicarRegrasStatus(associado);

		Associado saved = associadoRepository.save(associado);
		logger.info("Associado criado com ID: {}", saved.getId());

		// REGISTRA LOG
		try {
			sistemaLogService.logCriacao("Associado", saved.getId().toString(),
					"Criado associado: " + saved.getNomeRazao() + " (Status: " + saved.getStatus() + ")");
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		return toDTO(saved);
	}

	// ========== VALIDAÇÕES DE STATUS ==========

	private void validarMudancaStatus(Associado associado, String novoStatus) {
		String statusAtual = associado.getStatus();

		if ("I".equals(statusAtual) && !"A".equals(novoStatus)) {
			throw new IllegalStateException("Associado inativo só pode ser reativado para 'Ativo'");
		}

		if ("S".equals(statusAtual) && "I".equals(novoStatus)) {
			throw new IllegalStateException("Associado suspenso não pode ser inativado diretamente. Reative primeiro.");
		}

		if ("I".equals(novoStatus)) {
			validarInativacao(associado);
		} else if ("S".equals(novoStatus)) {
			validarSuspensao(associado);
		}
	}

	private void validarInativacao(Associado associado) {
		if (associado.getDataInativacao() == null) {
			associado.setDataInativacao(LocalDate.now());
		}

		if (associado.getMotivoInativacao() == null || associado.getMotivoInativacao().trim().isEmpty()) {
			throw new IllegalArgumentException("Motivo da inativação é obrigatório");
		}

		// Limpar suspensão
		associado.setDataInicioSuspensao(null);
		associado.setDataFimSuspensao(null);
		associado.setMotivoSuspensao(null);
	}

	private void validarSuspensao(Associado associado) {
		if (associado.getDataInicioSuspensao() == null) {
			associado.setDataInicioSuspensao(LocalDate.now());
		}

		if (associado.getDataFimSuspensao() == null) {
			associado.setDataFimSuspensao(associado.getDataInicioSuspensao().plusMonths(1));
		}

		if (associado.getDataFimSuspensao().isBefore(associado.getDataInicioSuspensao())) {
			throw new IllegalArgumentException("Data de fim da suspensão deve ser após a data de início");
		}

		if (associado.getMotivoSuspensao() == null || associado.getMotivoSuspensao().trim().isEmpty()) {
			throw new IllegalArgumentException("Motivo da suspensão é obrigatório");
		}

		// Limpar inativação
		associado.setDataInativacao(null);
		associado.setMotivoInativacao(null);
	}

	private void aplicarRegrasStatus(Associado associado) {
		// Validação básica
		if ("A".equals(associado.getStatus())) {
			// Verificar suspensão expirada
			if (associado.getDataFimSuspensao() != null && associado.getDataFimSuspensao().isBefore(LocalDate.now())) {
				logger.info("Suspensão expirada para associado {}", associado.getId());
				// Limpar suspensão
				associado.setDataInicioSuspensao(null);
				associado.setDataFimSuspensao(null);
				associado.setMotivoSuspensao(null);
			}
		}
	}

	// ========== MÉTODO updateEntityFromDTO CORRIGIDO ==========

	private void updateEntityFromDTO(Associado associado, AssociadoDTO dto) {
		logger.info("Atualizando entidade a partir do DTO para: {}", dto.getNomeRazao());

		try {
			// 1. DADOS BÁSICOS - COM TRATAMENTO DE NULL
			associado.setCodigoSpc(limparString(dto.getCodigoSpc()));
			associado.setCodigoRm(limparString(dto.getCodigoRm()));
			associado.setNomeFantasia(limparString(dto.getNomeFantasia()));

			// Campos obrigatórios
			if (dto.getCnpjCpf() != null) {
				associado.setCnpjCpf(dto.getCnpjCpf().trim());
			}

			if (dto.getNomeRazao() != null) {
				associado.setNomeRazao(dto.getNomeRazao().trim());
			}

			if (dto.getTipoPessoa() != null) {
				associado.setTipoPessoa(dto.getTipoPessoa());
			}

			// Status - com valor padrão seguro
			associado.setStatus(
					(dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) ? dto.getStatus().trim() : "A");

			// 2. DATAS - COM TRATAMENTO DE NULL
			if (dto.getDataCadastro() != null) {
				associado.setDataCadastro(dto.getDataCadastro());
			} else if (associado.getDataCadastro() == null) {
				// Só define data atual se não existir data anterior
				associado.setDataCadastro(LocalDateTime.now());
			}

			// Datas opcionais - podem ser null
			associado.setDataFiliacao(dto.getDataFiliacao());
			associado.setDataInativacao(dto.getDataInativacao());
			associado.setDataInicioSuspensao(dto.getDataInicioSuspensao());
			associado.setDataFimSuspensao(dto.getDataFimSuspensao());

			// 3. TEXTOS OPCIONAIS - podem ser null
			associado.setMotivoInativacao(dto.getMotivoInativacao());
			associado.setMotivoSuspensao(dto.getMotivoSuspensao());

			// 4. FATURAMENTO MÍNIMO
			associado.setFaturamentoMinimo(dto.getFaturamentoMinimo());

			// 5. RELACIONAMENTOS - TODOS COM VERIFICAÇÃO DE NULL
			try {
				// Vendedor Interno
				if (dto.getVendedorId() != null) {
					Vendedor vendedor = vendedorRepository.findById(dto.getVendedorId())
							.orElseThrow(() -> new EntityNotFoundException(
									"Vendedor não encontrado com ID: " + dto.getVendedorId()));
					associado.setVendedor(vendedor);
				} else {
					associado.setVendedor(null);
				}
			} catch (Exception e) {
				logger.error("Erro ao processar vendedor interno: {}", e.getMessage());
				associado.setVendedor(null);
			}

			// Vendedor Externo - COM TRATAMENTO ROBUSTO
			try {
				if (dto.getVendedorExternoId() != null) {
					// Tenta buscar como Vendedor (convertendo Long se necessário)
					Long vendedorExternoId;
					if (dto.getVendedorExternoId() instanceof Number) {
						vendedorExternoId = ((Number) dto.getVendedorExternoId()).longValue();
					} else {
						vendedorExternoId = Long.parseLong(dto.getVendedorExternoId().toString());
					}

					Optional<Vendedor> vendedorExternoOpt = vendedorRepository.findById(vendedorExternoId);
					if (vendedorExternoOpt.isPresent()) {
						associado.setVendedorExterno(vendedorExternoOpt.get());
					} else {
						logger.warn("Vendedor externo não encontrado com ID {}, limpando campo", vendedorExternoId);
						associado.setVendedorExterno(null);
					}
				} else {
					associado.setVendedorExterno(null);
				}
			} catch (Exception e) {
				logger.error("Erro ao processar vendedor externo: {}", e.getMessage());
				associado.setVendedorExterno(null);
			}

			// Plano
			try {
				if (dto.getPlanoId() != null) {
					Planos plano = planosRepository.findById(dto.getPlanoId()).orElseThrow(
							() -> new EntityNotFoundException("Plano não encontrado com ID: " + dto.getPlanoId()));
					associado.setPlano(plano);
				} else {
					associado.setPlano(null);
				}
			} catch (Exception e) {
				logger.error("Erro ao processar plano: {}", e.getMessage());
				associado.setPlano(null);
			}

			// Categoria
			try {
				if (dto.getCategoriaId() != null) {
					Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
							.orElseThrow(() -> new EntityNotFoundException(
									"Categoria não encontrada com ID: " + dto.getCategoriaId()));
					associado.setCategoria(categoria);
				} else {
					associado.setCategoria(null);
				}
			} catch (Exception e) {
				logger.error("Erro ao processar categoria: {}", e.getMessage());
				associado.setCategoria(null);
			}

			// 6. SUB-ENTIDADES - COM LIMPEZA APROPRIADA
			atualizarEnderecos(associado, dto.getEnderecos());
			atualizarTelefones(associado, dto.getTelefones());
			atualizarEmails(associado, dto.getEmails());

			logger.info("Entidade atualizada com sucesso para associado: {}", associado.getNomeRazao());

		} catch (IllegalArgumentException | EntityNotFoundException e) {
			logger.error("Erro de validação: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Erro inesperado ao atualizar entidade: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao processar dados do associado: " + e.getMessage(), e);
		}
	}

	// ========== MÉTODOS AUXILIARES PARA ATUALIZAR SUB-ENTIDADES ==========

	/**
	 * Atualiza endereços do associado (UPSERT)
	 * Se o endereço já existe (mesmo tipo), atualiza; senão, adiciona novo
	 */
	private void atualizarEnderecos(Associado associado, List<EnderecoDTO> enderecosDTO) {
	    if (enderecosDTO == null) {
	        if (associado.getEnderecos() != null) {
	            associado.getEnderecos().clear();
	        } else {
	            associado.setEnderecos(new ArrayList<>());
	        }
	        return;
	    }

	    if (associado.getEnderecos() == null) {
	        associado.setEnderecos(new ArrayList<>());
	    }

	    Map<String, Endereco> enderecosPorTipo = new HashMap<>();
	    for (Endereco e : associado.getEnderecos()) {
	        if (e.getTipoEndereco() != null) {
	            enderecosPorTipo.put(e.getTipoEndereco(), e);
	        }
	    }

	    List<Endereco> enderecosAtualizados = new ArrayList<>();

	    for (EnderecoDTO dto : enderecosDTO) {
	        if (dto != null && temDadosMinimos(dto)) {
	            String tipoEndereco = dto.getTipoEndereco() != null ? dto.getTipoEndereco() : "COMERCIAL";
	            Endereco endereco;

	            if (enderecosPorTipo.containsKey(tipoEndereco)) {
	                endereco = enderecosPorTipo.get(tipoEndereco);
	            } else {
	                endereco = new Endereco();
	                endereco.setAssociado(associado);
	            }

	            // 🔥 TRUNCAR CAMPOS PARA EVITAR ERRO DE TAMANHO
	            String estado = dto.getEstado();
	            if (estado != null && estado.length() > 2) {
	                estado = estado.substring(0, 2);
	                logger.warn("Estado truncado de '{}' para '{}'", dto.getEstado(), estado);
	            }
	            
	            String cidade = dto.getCidade();
	            if (cidade != null && cidade.length() > 100) {
	                cidade = cidade.substring(0, 100);
	                logger.warn("Cidade truncada de {} para {}", dto.getCidade().length(), cidade.length());
	            }
	            
	            String logradouro = dto.getLogradouro();
	            if (logradouro != null && logradouro.length() > 200) {
	                logradouro = logradouro.substring(0, 200);
	                logger.warn("Logradouro truncado");
	            }
	            
	            String bairro = dto.getBairro();
	            if (bairro != null && bairro.length() > 100) {
	                bairro = bairro.substring(0, 100);
	            }
	            
	            String complemento = dto.getComplemento();
	            if (complemento != null && complemento.length() > 200) {
	                complemento = complemento.substring(0, 200);
	            }

	            endereco.setCep(limparString(dto.getCep()));
	            endereco.setLogradouro(limparString(logradouro));
	            endereco.setNumero(limparString(dto.getNumero()));
	            endereco.setComplemento(limparString(complemento));
	            endereco.setBairro(limparString(bairro));
	            endereco.setCidade(limparString(cidade));
	            endereco.setEstado(limparString(estado));
	            endereco.setTipoEndereco(tipoEndereco);

	            enderecosAtualizados.add(endereco);
	        }
	    }

	    associado.getEnderecos().clear();
	    associado.getEnderecos().addAll(enderecosAtualizados);
	}

	/**
	 * Atualiza telefones do associado (UPSERT)
	 * Se o telefone já existe (mesmo tipo), atualiza; senão, adiciona novo
	 */
	private void atualizarTelefones(Associado associado, List<TelefoneDTO> telefonesDTO) {
	    if (telefonesDTO == null) {
	        if (associado.getTelefones() != null) {
	            associado.getTelefones().clear();
	        } else {
	            associado.setTelefones(new ArrayList<>());
	        }
	        return;
	    }

	    // Inicializar lista se necessário
	    if (associado.getTelefones() == null) {
	        associado.setTelefones(new ArrayList<>());
	    }

	    // Mapa de telefones existentes por tipo
	    Map<String, Telefone> telefonesPorTipo = new HashMap<>();
	    for (Telefone t : associado.getTelefones()) {
	        if (t.getTipoTelefone() != null) {
	            telefonesPorTipo.put(t.getTipoTelefone(), t);
	        }
	    }

	    List<Telefone> telefonesAtualizados = new ArrayList<>();

	    for (TelefoneDTO dto : telefonesDTO) {
	        if (dto != null && temDadosMinimos(dto)) {
	            String tipoTelefone = dto.getTipoTelefone() != null ? dto.getTipoTelefone() : "CELULAR";
	            Telefone telefone;

	            // 🔥 Verificar se já existe telefone do mesmo tipo
	            if (telefonesPorTipo.containsKey(tipoTelefone)) {
	                // Atualizar existente
	                telefone = telefonesPorTipo.get(tipoTelefone);
	                logger.debug("✏️ Atualizando telefone tipo: {} para associado ID: {}", tipoTelefone, associado.getId());
	            } else {
	                // Criar novo telefone
	                telefone = new Telefone();
	                telefone.setAssociado(associado);
	                logger.debug("➕ Adicionando novo telefone tipo: {} para associado ID: {}", tipoTelefone, associado.getId());
	            }

	            // Atualizar campos
	            telefone.setDdd(limparString(dto.getDdd()));
	            telefone.setNumero(limparString(dto.getNumero()));
	            telefone.setTipoTelefone(tipoTelefone);
	            telefone.setWhatsapp(dto.getWhatsapp() != null ? dto.getWhatsapp() : false);
	            telefone.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

	            telefonesAtualizados.add(telefone);
	        }
	    }

	    // Limpar e adicionar todos os telefones
	    associado.getTelefones().clear();
	    associado.getTelefones().addAll(telefonesAtualizados);

	    logger.info("📞 Telefones atualizados: {} registros para associado ID: {}", telefonesAtualizados.size(), associado.getId());
	}

	/**
	 * Atualiza emails do associado (UPSERT)
	 * Se o email já existe (mesmo tipo), atualiza; senão, adiciona novo
	 */
	private void atualizarEmails(Associado associado, List<EmailDTO> emailsDTO) {
	    if (emailsDTO == null) {
	        if (associado.getEmails() != null) {
	            associado.getEmails().clear();
	        } else {
	            associado.setEmails(new ArrayList<>());
	        }
	        return;
	    }

	    // Inicializar lista se necessário
	    if (associado.getEmails() == null) {
	        associado.setEmails(new ArrayList<>());
	    }

	    // Mapa de emails existentes por tipo
	    Map<String, Email> emailsPorTipo = new HashMap<>();
	    for (Email e : associado.getEmails()) {
	        if (e.getTipoEmail() != null) {
	            emailsPorTipo.put(e.getTipoEmail(), e);
	        }
	    }

	    List<Email> emailsAtualizados = new ArrayList<>();

	    for (EmailDTO dto : emailsDTO) {
	        if (dto != null && dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
	            String tipoEmail = dto.getTipoEmail() != null ? dto.getTipoEmail() : "COMERCIAL";
	            Email email;

	            // 🔥 Verificar se já existe email do mesmo tipo
	            if (emailsPorTipo.containsKey(tipoEmail)) {
	                // Atualizar existente
	                email = emailsPorTipo.get(tipoEmail);
	                logger.debug("✏️ Atualizando email tipo: {} para associado ID: {}", tipoEmail, associado.getId());
	            } else {
	                // Criar novo email
	                email = new Email();
	                email.setAssociado(associado);
	                logger.debug("➕ Adicionando novo email tipo: {} para associado ID: {}", tipoEmail, associado.getId());
	            }

	            // Atualizar campos
	            email.setEmail(dto.getEmail().trim());
	            email.setTipoEmail(tipoEmail);
	            email.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);

	            emailsAtualizados.add(email);
	        }
	    }

	    // Limpar e adicionar todos os emails
	    associado.getEmails().clear();
	    associado.getEmails().addAll(emailsAtualizados);

	    logger.info("📧 Emails atualizados: {} registros para associado ID: {}", emailsAtualizados.size(), associado.getId());
	}

	// ========== MÉTODOS AUXILIARES DE VALIDAÇÃO ==========

	private boolean temDadosMinimos(EnderecoDTO endereco) {
		return (endereco.getCep() != null && !endereco.getCep().trim().isEmpty())
				|| (endereco.getLogradouro() != null && !endereco.getLogradouro().trim().isEmpty())
				|| (endereco.getCidade() != null && !endereco.getCidade().trim().isEmpty());
	}

	private boolean temDadosMinimos(TelefoneDTO telefone) {
		return (telefone.getDdd() != null && !telefone.getDdd().trim().isEmpty())
				&& (telefone.getNumero() != null && !telefone.getNumero().trim().isEmpty());
	}

	// ========== MÉTODOS ADICIONAIS ==========

	@Transactional
	public AssociadoDTO reativarAssociado(Long id, String motivo) {
		logger.info("Reativando associado ID: {}", id);

		Associado associado = associadoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + id));

		if (!"I".equals(associado.getStatus())) {
			throw new IllegalStateException("Só é possível reativar associados inativos");
		}

		associado.setStatus("A");
		associado.setDataInativacao(null);
		associado.setMotivoInativacao(null);

		Associado reativado = associadoRepository.save(associado);
		logger.info("Associado reativado: {} - {}", reativado.getId(), reativado.getNomeRazao());

		try {
			sistemaLogService.logAtualizacao("Associado", reativado.getId().toString(), "Associado reativado: "
					+ reativado.getNomeRazao() + (motivo != null ? " (Motivo: " + motivo + ")" : ""));
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		return toDTO(reativado);
	}

	@Transactional
	public void verificarSuspensoesExpiradas() {
		logger.info("Verificando suspensões expiradas...");

		List<Associado> suspensos = associadoRepository.findByStatus("S");
		int reativados = 0;

		for (Associado associado : suspensos) {
			if (associado.getDataFimSuspensao() != null && associado.getDataFimSuspensao().isBefore(LocalDate.now())) {

				logger.info("Suspensão expirada para associado {}: {}", associado.getId(), associado.getNomeRazao());

				associado.setStatus("A");
				associado.setDataInicioSuspensao(null);
				associado.setDataFimSuspensao(null);
				associado.setMotivoSuspensao("Suspensão expirada automaticamente");

				associadoRepository.save(associado);
				reativados++;

				try {
					sistemaLogService.logAtualizacao("Associado", associado.getId().toString(),
							"Reativação automática por expiração de suspensão: " + associado.getNomeRazao());
				} catch (Exception e) {
					logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
				}
			}
		}

		logger.info("Reativados {} associados com suspensão expirada", reativados);
	}

	@Transactional(readOnly = true)
	public List<AssociadoDTO> findByStatus(String status) {
		logger.info("Buscando associados por status: {}", status);

		List<Associado> associados = associadoRepository.findByStatus(status);

		return associados.stream().map(this::toDTO).collect(Collectors.toList());
	}

	// ========== MÉTODOS AUXILIARES ==========

	private String limparString(String valor) {
		// return valor != null ? valor.trim() : "";
		return valor != null && !valor.trim().isEmpty() ? valor.trim() : null;
	}

	@Transactional
	public List<EnderecoDTO> atualizarEnderecos(Long associadoId, List<EnderecoDTO> enderecosDTO) {
		logger.info("Atualizando endereços para associado ID: {}", associadoId);

		Associado associado = associadoRepository.findById(associadoId)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + associadoId));

		// 🔴 CORREÇÃO: Primeiro deletar todos os endereços existentes
		enderecoRepository.deleteByAssociadoId(associadoId);

		List<Endereco> novosEnderecos = new ArrayList<>();

		for (EnderecoDTO dto : enderecosDTO) {
			if (dto.getLogradouro() != null || dto.getCep() != null || dto.getCidade() != null) {
				Endereco endereco = new Endereco();
				endereco.setCep(dto.getCep());
				endereco.setLogradouro(dto.getLogradouro());
				endereco.setNumero(dto.getNumero());
				endereco.setComplemento(dto.getComplemento());
				endereco.setBairro(dto.getBairro());
				endereco.setCidade(dto.getCidade());
				endereco.setEstado(dto.getEstado());
				endereco.setTipoEndereco(dto.getTipoEndereco());
				endereco.setAssociado(associado);

				novosEnderecos.add(endereco);
			}
		}

		List<Endereco> enderecosSalvos = enderecoRepository.saveAll(novosEnderecos);

		logger.info("Endereços atualizados: {} registros para associado ID: {}", enderecosSalvos.size(), associadoId);

		return enderecosSalvos.stream().map(this::toEnderecoDTO).collect(Collectors.toList());
	}

	@Transactional
	public List<TelefoneDTO> atualizarTelefones(Long associadoId, List<TelefoneDTO> telefonesDTO) {
		logger.info("Atualizando telefones para associado ID: {}", associadoId);

		Associado associado = associadoRepository.findById(associadoId)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + associadoId));

		// 🔴 CORREÇÃO: Primeiro deletar todos os telefones existentes
		telefoneRepository.deleteByAssociadoId(associadoId);

		List<Telefone> novosTelefones = new ArrayList<>();

		for (TelefoneDTO dto : telefonesDTO) {
			if (dto.getNumero() != null || dto.getDdd() != null) {
				Telefone telefone = new Telefone();
				telefone.setDdd(dto.getDdd());
				telefone.setNumero(dto.getNumero());
				telefone.setTipoTelefone(dto.getTipoTelefone());
				telefone.setWhatsapp(dto.getWhatsapp() != null ? dto.getWhatsapp() : false);
				telefone.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
				telefone.setAssociado(associado);

				novosTelefones.add(telefone);
			}
		}

		List<Telefone> telefonesSalvos = telefoneRepository.saveAll(novosTelefones);

		logger.info("Telefones atualizados: {} registros para associado ID: {}", telefonesSalvos.size(), associadoId);

		return telefonesSalvos.stream().map(this::toTelefoneDTO).collect(Collectors.toList());
	}

	@Transactional
	public List<EmailDTO> atualizarEmails(Long associadoId, List<EmailDTO> emailsDTO) {
		logger.info("Atualizando emails para associado ID: {}", associadoId);

		Associado associado = associadoRepository.findById(associadoId)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + associadoId));

		// 🔴 CORREÇÃO: Primeiro deletar todos os emails existentes
		emailRepository.deleteByAssociadoId(associadoId);

		List<Email> novosEmails = new ArrayList<>();

		for (EmailDTO dto : emailsDTO) {
			if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
				Email email = new Email();
				email.setEmail(dto.getEmail().trim());
				email.setTipoEmail(dto.getTipoEmail());
				email.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
				email.setAssociado(associado);

				novosEmails.add(email);
			}
		}

		List<Email> emailsSalvos = emailRepository.saveAll(novosEmails);

		logger.info("Emails atualizados: {} registros para associado ID: {}", emailsSalvos.size(), associadoId);

		return emailsSalvos.stream().map(this::toEmailDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<EnderecoDTO> buscarEnderecosPorAssociadoId(Long associadoId) {
		List<Endereco> enderecos = enderecoRepository.findByAssociadoId(associadoId);
		return enderecos.stream().map(this::toEnderecoDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<TelefoneDTO> buscarTelefonesPorAssociadoId(Long associadoId) {
		List<Telefone> telefones = telefoneRepository.findByAssociadoId(associadoId);
		return telefones.stream().map(this::toTelefoneDTO).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<EmailDTO> buscarEmailsPorAssociadoId(Long associadoId) {
		List<Email> emails = emailRepository.findByAssociadoId(associadoId);
		return emails.stream().map(this::toEmailDTO).collect(Collectors.toList());
	}

	public Associado buscarPorIdEntity(Long id) {
		return associadoRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Associado não encontrado com ID: " + id));
	}
	
	/**
	 * Busca associado pelo código SPC
	 */
	public Optional<Associado> buscarPorCodigoSpc(String codigoSpc) {
	    logger.info("Buscando associado por código SPC: {}", codigoSpc);
	    return associadoRepository.findByCodigoSpc(codigoSpc);
	}
	
	// ========== MÉTODO DE IMPORTAÇÃO COM UPSERT ==========

	/**
	 * Importa ou atualiza um associado (UPSERT)
	 * Se o associado já existe e forcarAtualizacao = true, atualiza os dados
	 * 
	 * @param dto DTO com os dados do associado
	 * @param usuario Usuario que está realizando a operação
	 * @return Associado importado/atualizado
	 */
	@Transactional
	public Associado importarAssociado(AssociadoDTO dto, String usuario) {
	    logger.info("📥 Importando associado: {} - CNPJ/CPF: {}", dto.getNomeRazao(), dto.getCnpjCpf());
	    
	    // Buscar associado existente por CNPJ/CPF
	    Optional<Associado> associadoExistente = associadoRepository.findByCnpjCpf(dto.getCnpjCpf());
	    
	    Associado associado;
	    boolean isNovo = false;
	    
	    if (associadoExistente.isPresent()) {
	        if (Boolean.TRUE.equals(dto.getForcarAtualizacao())) {
	            // 🔥 ATUALIZAR associado existente
	            associado = associadoExistente.get();
	            logger.info("🔄 Atualizando associado existente ID: {}", associado.getId());
	            
	            // Atualizar campos básicos
	            associado.setNomeRazao(dto.getNomeRazao());
	            associado.setNomeFantasia(dto.getNomeFantasia());
	            associado.setTipoPessoa(dto.getTipoPessoa());
	            associado.setCodigoSpc(limparString(dto.getCodigoSpc()));
	            associado.setCodigoRm(limparString(dto.getCodigoRm()));
	            associado.setStatus(dto.getStatus() != null ? dto.getStatus() : "A");
	            associado.setFaturamentoMinimo(dto.getFaturamentoMinimo());
	            associado.setDataFiliacao(dto.getDataFiliacao());
	            // 🔥 REMOVIDO: associado.setAtualizadoEm(LocalDateTime.now()); - campo não existe
	            
	            // 🔥 ATUALIZAR VENDEDOR EXTERNO
	            if (dto.getVendedorExternoId() != null) {
	                try {
	                    Long vendedorExternoId = dto.getVendedorExternoId().longValue();
	                    Optional<Vendedor> vendedorExterno = vendedorRepository.findById(vendedorExternoId);
	                    if (vendedorExterno.isPresent()) {
	                        associado.setVendedorExterno(vendedorExterno.get());
	                        logger.info("✅ Vendedor externo atualizado para ID: {}", vendedorExternoId);
	                    } else {
	                        logger.warn("⚠️ Vendedor externo não encontrado com ID: {}", vendedorExternoId);
	                    }
	                } catch (Exception e) {
	                    logger.error("❌ Erro ao atualizar vendedor externo: {}", e.getMessage());
	                }
	            }
	            
	            // Atualizar vendedor interno
	            if (dto.getVendedorId() != null) {
	                try {
	                    Optional<Vendedor> vendedor = vendedorRepository.findById(dto.getVendedorId());
	                    vendedor.ifPresent(associado::setVendedor);
	                } catch (Exception e) {
	                    logger.error("❌ Erro ao atualizar vendedor: {}", e.getMessage());
	                }
	            }
	            
	            // Atualizar plano
	            if (dto.getPlanoId() != null) {
	                try {
	                    Optional<Planos> plano = planosRepository.findById(dto.getPlanoId());
	                    plano.ifPresent(associado::setPlano);
	                } catch (Exception e) {
	                    logger.error("❌ Erro ao atualizar plano: {}", e.getMessage());
	                }
	            }
	            
	            // Atualizar categoria
	            if (dto.getCategoriaId() != null) {
	                try {
	                    Optional<Categoria> categoria = categoriaRepository.findById(dto.getCategoriaId());
	                    categoria.ifPresent(associado::setCategoria);
	                } catch (Exception e) {
	                    logger.error("❌ Erro ao atualizar categoria: {}", e.getMessage());
	                }
	            }
	            
	            // Atualizar endereços, telefones, emails
	            atualizarEnderecos(associado, dto.getEnderecos());
	            atualizarTelefones(associado, dto.getTelefones());
	            atualizarEmails(associado, dto.getEmails());
	            
	        } else {
	            // Não forçar atualização, apenas retornar o existente
	            logger.info("ℹ️ Associado já existe e forcarAtualizacao=false, ignorando atualização");
	            return associadoExistente.get();
	        }
	    } else {
	        // CRIAR novo associado
	        isNovo = true;
	        associado = new Associado();
	        associado.setCnpjCpf(dto.getCnpjCpf());
	        associado.setNomeRazao(dto.getNomeRazao());
	        associado.setNomeFantasia(dto.getNomeFantasia());
	        associado.setTipoPessoa(dto.getTipoPessoa());
	        associado.setCodigoSpc(limparString(dto.getCodigoSpc()));
	        associado.setCodigoRm(limparString(dto.getCodigoRm()));
	        associado.setStatus(dto.getStatus() != null ? dto.getStatus() : "A");
	        associado.setFaturamentoMinimo(dto.getFaturamentoMinimo());
	        associado.setDataFiliacao(dto.getDataFiliacao());
	        associado.setDataCadastro(LocalDateTime.now());
	        // 🔥 REMOVIDO: associado.setCriadoEm(LocalDateTime.now()); - campo não existe
	        // 🔥 REMOVIDO: associado.setCriadoPor(usuario); - campo não existe
	        
	        // Buscar e setar vendedor interno
	        if (dto.getVendedorId() != null) {
	            try {
	                Optional<Vendedor> vendedor = vendedorRepository.findById(dto.getVendedorId());
	                vendedor.ifPresent(associado::setVendedor);
	            } catch (Exception e) {
	                logger.error("❌ Erro ao buscar vendedor: {}", e.getMessage());
	            }
	        }
	        
	        // 🔥 BUSCAR E SETAR VENDEDOR EXTERNO
	        if (dto.getVendedorExternoId() != null) {
	            try {
	                Long vendedorExternoId = dto.getVendedorExternoId().longValue();
	                Optional<Vendedor> vendedorExterno = vendedorRepository.findById(vendedorExternoId);
	                if (vendedorExterno.isPresent()) {
	                    associado.setVendedorExterno(vendedorExterno.get());
	                    logger.info("✅ Vendedor externo configurado para ID: {}", vendedorExternoId);
	                } else {
	                    logger.warn("⚠️ Vendedor externo não encontrado com ID: {}", vendedorExternoId);
	                }
	            } catch (Exception e) {
	                logger.error("❌ Erro ao buscar vendedor externo: {}", e.getMessage());
	            }
	        }
	        
	        // Buscar e setar plano
	        if (dto.getPlanoId() != null) {
	            try {
	                Optional<Planos> plano = planosRepository.findById(dto.getPlanoId());
	                plano.ifPresent(associado::setPlano);
	            } catch (Exception e) {
	                logger.error("❌ Erro ao buscar plano: {}", e.getMessage());
	            }
	        }
	        
	        // Buscar e setar categoria
	        if (dto.getCategoriaId() != null) {
	            try {
	                Optional<Categoria> categoria = categoriaRepository.findById(dto.getCategoriaId());
	                categoria.ifPresent(associado::setCategoria);
	            } catch (Exception e) {
	                logger.error("❌ Erro ao buscar categoria: {}", e.getMessage());
	            }
	        }
	        
	        // Processar endereços, telefones, emails
	        if (dto.getEnderecos() != null && !dto.getEnderecos().isEmpty()) {
	            atualizarEnderecos(associado, dto.getEnderecos());
	        }
	        
	        if (dto.getTelefones() != null && !dto.getTelefones().isEmpty()) {
	            atualizarTelefones(associado, dto.getTelefones());
	        }
	        
	        if (dto.getEmails() != null && !dto.getEmails().isEmpty()) {
	            atualizarEmails(associado, dto.getEmails());
	        }
	    }
	    
	    // Salvar associado
	    Associado saved = associadoRepository.save(associado);
	    
	    logger.info("✅ Associado {} com sucesso: ID {}", isNovo ? "criado" : "atualizado", saved.getId());
	    
	    return saved;
	}	

}