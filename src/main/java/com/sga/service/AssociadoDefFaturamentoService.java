package com.sga.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoDefFaturamentoDTO;
import com.sga.dto.AssociadoDefFaturamentoResumoDTO;
import com.sga.exception.ResourceNotFoundException;
import com.sga.model.Associado;
import com.sga.model.AssociadoDefFaturamento;
import com.sga.model.Planos;
import com.sga.repository.AssociadoDefFaturamentoRepository;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.PlanosRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssociadoDefFaturamentoService {

	private static final Logger logger = LoggerFactory.getLogger(AssociadoDefFaturamentoService.class);

	@Autowired
	private AssociadoDefFaturamentoRepository associadoDefFaturamentoRepository;
	
	@Autowired
	private AssociadoRepository associadoRepository;
	
	@Autowired
	private PlanosRepository planosRepository;
	
	@Autowired
	private SistemaLogService sistemaLogService;

	/**
	 * Criar uma nova definição de faturamento para um associado
	 */
	@Transactional
	public AssociadoDefFaturamentoDTO criar(AssociadoDefFaturamentoDTO dto) {
		logger.info("Criando definição de faturamento para associado ID: {}, diaEmissao: {}", dto.getAssociadoId(),
				dto.getDiaEmissao());

		// Validar se já existe para o mesmo dia
		if (associadoDefFaturamentoRepository.existsByAssociadoIdAndDiaEmissao(dto.getAssociadoId(),
				dto.getDiaEmissao())) {
			throw new IllegalArgumentException(String.format(
					"Já existe uma definição de faturamento para o dia %d deste associado", dto.getDiaEmissao()));
		}

		AssociadoDefFaturamento entity = convertToEntity(dto);
		AssociadoDefFaturamento saved = associadoDefFaturamentoRepository.save(entity);

		logger.info("Definição de faturamento criada com ID: {}", saved.getId());

		// Registrar log
		try {
			sistemaLogService.logCriacao("AssociadoDefFaturamento", saved.getId().toString(),
					"Criada definição de faturamento para associado ID: " + dto.getAssociadoId() + " - Dia emissão: "
							+ dto.getDiaEmissao() + ", Dia vencimento: " + dto.getDiaVencimento());
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		return convertToDTO(saved);
	}

	/**
	 * Criar múltiplas definições de faturamento em lote
	 */
	@Transactional
	public List<AssociadoDefFaturamentoDTO> criarEmLote(List<AssociadoDefFaturamentoDTO> dtos) {
		logger.info("Criando {} definições de faturamento em lote", dtos.size());

		List<AssociadoDefFaturamento> entities = dtos.stream().map(this::convertToEntity).collect(Collectors.toList());

		List<AssociadoDefFaturamento> saved = associadoDefFaturamentoRepository.saveAll(entities);

		logger.info("{} definições de faturamento criadas com sucesso", saved.size());

		// Registrar log resumido
		try {
			if (!saved.isEmpty()) {
				sistemaLogService.logCriacao("AssociadoDefFaturamento", "LOTE", "Criadas " + saved.size()
						+ " definições de faturamento para associado ID: " + saved.get(0).getAssociado().getId());
			}
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		return saved.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	/**
	 * Atualizar uma definição de faturamento existente
	 */
	@Transactional
	public AssociadoDefFaturamentoDTO atualizar(Long id, AssociadoDefFaturamentoDTO dto) {
		logger.info("Atualizando definição de faturamento ID: {}", id);

		AssociadoDefFaturamento entity = associadoDefFaturamentoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Definição de faturamento não encontrada: " + id));

		// Verificar se está tentando mudar o dia de emissão e se já existe outro com
		// este dia
		if (!entity.getDiaEmissao().equals(dto.getDiaEmissao()) && associadoDefFaturamentoRepository
				.existsByAssociadoIdAndDiaEmissao(entity.getAssociado().getId(), dto.getDiaEmissao())) {
			throw new IllegalArgumentException(String.format(
					"Já existe uma definição de faturamento para o dia %d deste associado", dto.getDiaEmissao()));
		}

		updateEntity(entity, dto);
		AssociadoDefFaturamento updated = associadoDefFaturamentoRepository.save(entity);

		logger.info("Definição de faturamento atualizada ID: {}", updated.getId());

		// Registrar log
		try {
			sistemaLogService.logAtualizacao("AssociadoDefFaturamento", updated.getId().toString(),
					"Atualizada definição de faturamento - Dia emissão: " + updated.getDiaEmissao()
							+ ", Dia vencimento: " + updated.getDiaVencimento());
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		return convertToDTO(updated);
	}

	/**
	 * Buscar por ID
	 */
	@Transactional(readOnly = true)
	public AssociadoDefFaturamentoDTO buscarPorId(Long id) {
		logger.info("Buscando definição de faturamento por ID: {}", id);

		AssociadoDefFaturamento entity = associadoDefFaturamentoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Definição de faturamento não encontrada: " + id));

		return convertToDTO(entity);
	}

	/**
	 * Listar todas as definições de um associado
	 */
	@Transactional(readOnly = true)
	public List<AssociadoDefFaturamentoResumoDTO> listarPorAssociado(Long associadoId) {
		logger.info("Listando definições de faturamento do associado ID: {}", associadoId);

		return associadoDefFaturamentoRepository.findByAssociadoId(associadoId).stream().map(this::convertToResumoDTO)
				.collect(Collectors.toList());
	}

	/**
	 * Listar com paginação
	 */
	@Transactional(readOnly = true)
	public Page<AssociadoDefFaturamentoResumoDTO> listarPorAssociadoPaginado(Long associadoId, Pageable pageable) {
		logger.info("Listando definições de faturamento do associado ID: {} - página: {}", associadoId,
				pageable.getPageNumber());

		return associadoDefFaturamentoRepository.findByAssociadoId(associadoId, pageable).map(this::convertToResumoDTO);
	}

	/**
	 * Listar definições por dia de emissão
	 */
	@Transactional(readOnly = true)
	public List<AssociadoDefFaturamentoResumoDTO> listarPorDiaEmissao(Long associadoId, Integer diaEmissao) {
		logger.info("Listando definições de faturamento do associado ID: {} para dia: {}", associadoId, diaEmissao);

		return associadoDefFaturamentoRepository.findByAssociadoIdAndDiaEmissao(associadoId, diaEmissao).stream()
				.map(this::convertToResumoDTO).collect(Collectors.toList());
	}

	/**
	 * Listar dias de emissão disponíveis para um associado
	 */
	@Transactional(readOnly = true)
	public List<Integer> listarDiasEmissaoPorAssociado(Long associadoId) {
		logger.info("Listando dias de emissão do associado ID: {}", associadoId);

		return associadoDefFaturamentoRepository.findDiasEmissaoByAssociadoId(associadoId);
	}

	/**
	 * Excluir uma definição de faturamento
	 */
	@Transactional
	public void excluir(Long id) {
		logger.info("Excluindo definição de faturamento ID: {}", id);

		AssociadoDefFaturamento entity = associadoDefFaturamentoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Definição de faturamento não encontrada: " + id));

		associadoDefFaturamentoRepository.delete(entity);

		// Registrar log
		try {
			sistemaLogService.logExclusao("AssociadoDefFaturamento", id.toString(),
					"Excluída definição de faturamento - Dia emissão: " + entity.getDiaEmissao());
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}

		logger.info("Definição de faturamento excluída ID: {}", id);
	}

	/**
	 * Excluir todas as definições de um associado
	 */
	@Transactional
	public void excluirPorAssociado(Long associadoId) {
		logger.info("Excluindo todas as definições de faturamento do associado ID: {}", associadoId);

		associadoDefFaturamentoRepository.deleteByAssociadoId(associadoId);

		// Registrar log
		try {
			sistemaLogService.logExclusao("AssociadoDefFaturamento", "ASSOCIADO_" + associadoId,
					"Excluídas todas as definições de faturamento do associado");
		} catch (Exception e) {
			logger.warn("Não foi possível registrar log de auditoria: {}", e.getMessage());
		}
	}

	// ==================== MÉTODOS DE CONVERSÃO ====================

	private AssociadoDefFaturamento convertToEntity(AssociadoDefFaturamentoDTO dto) {
		AssociadoDefFaturamento entity = new AssociadoDefFaturamento();

		entity.setId(dto.getId());

		// Associado
		if (dto.getAssociadoId() != null) {
			Associado associado = associadoRepository.findById(dto.getAssociadoId()).orElseThrow(
					() -> new ResourceNotFoundException("Associado não encontrado: " + dto.getAssociadoId()));
			entity.setAssociado(associado);
		}

		// Plano (opcional)
		if (dto.getPlanoId() != null) {
			Planos plano = planosRepository.findById(dto.getPlanoId())
					.orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado: " + dto.getPlanoId()));
			entity.setPlano(plano);
		}

		entity.setValorDef(dto.getValorDef());
		entity.setDiaEmissao(dto.getDiaEmissao());
		entity.setDiaVencimento(dto.getDiaVencimento());
		entity.setObservacao(dto.getObservacao());

		return entity;
	}

	private void updateEntity(AssociadoDefFaturamento entity, AssociadoDefFaturamentoDTO dto) {
		// Não atualiza associado
		if (dto.getPlanoId() != null) {
			Planos plano = planosRepository.findById(dto.getPlanoId())
					.orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado: " + dto.getPlanoId()));
			entity.setPlano(plano);
		} else {
			entity.setPlano(null);
		}

		entity.setValorDef(dto.getValorDef());
		entity.setDiaEmissao(dto.getDiaEmissao());
		entity.setDiaVencimento(dto.getDiaVencimento());
		entity.setObservacao(dto.getObservacao());
	}

	private AssociadoDefFaturamentoDTO convertToDTO(AssociadoDefFaturamento entity) {
		AssociadoDefFaturamentoDTO dto = new AssociadoDefFaturamentoDTO();

		dto.setId(entity.getId());
		dto.setAssociadoId(entity.getAssociado() != null ? entity.getAssociado().getId() : null);
		dto.setPlanoId(entity.getPlano() != null ? entity.getPlano().getId() : null);
		dto.setValorDef(entity.getValorDef());
		dto.setDiaEmissao(entity.getDiaEmissao());
		dto.setDiaVencimento(entity.getDiaVencimento());
		dto.setObservacao(entity.getObservacao());

		return dto;
	}

	private AssociadoDefFaturamentoResumoDTO convertToResumoDTO(AssociadoDefFaturamento entity) {
		AssociadoDefFaturamentoResumoDTO dto = new AssociadoDefFaturamentoResumoDTO();

		dto.setId(entity.getId());

		if (entity.getAssociado() != null) {
			dto.setAssociadoId(entity.getAssociado().getId());
			dto.setAssociadoNome(entity.getAssociado().getNomeRazao());
		}

		if (entity.getPlano() != null) {
			dto.setPlanoId(entity.getPlano().getId());
			dto.setPlanoNome(entity.getPlano().getPlano());
		}

		dto.setValorDef(entity.getValorDef());
		dto.setDiaEmissao(entity.getDiaEmissao());
		dto.setDiaVencimento(entity.getDiaVencimento());
		dto.setObservacao(entity.getObservacao());

		return dto;
	}
}