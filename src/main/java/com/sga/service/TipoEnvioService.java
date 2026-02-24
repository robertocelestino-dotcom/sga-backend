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

import com.sga.dto.TipoEnvioDTO;
import com.sga.dto.TipoEnvioResumoDTO;
import com.sga.exception.ResourceNotFoundException;
import com.sga.model.TipoEnvio;
import com.sga.repository.TipoEnvioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TipoEnvioService {

	private static final Logger logger = LoggerFactory.getLogger(TipoEnvioService.class);
	
	@Autowired
	private TipoEnvioRepository tipoEnvioRepository;

	@Transactional
	public TipoEnvioDTO criar(TipoEnvioDTO dto) {
		logger.info("Criando novo tipo de envio: {}", dto.getDescricao());

		if (tipoEnvioRepository.existsByCodigo(dto.getCodigo())) {
			throw new IllegalArgumentException("Já existe um tipo de envio com o código: " + dto.getCodigo());
		}

		TipoEnvio tipoEnvio = new TipoEnvio();
		tipoEnvio.setCodigo(dto.getCodigo());
		tipoEnvio.setDescricao(dto.getDescricao());
		tipoEnvio.setAtivo(dto.getAtivo() != null ? dto.getAtivo() : true);
		tipoEnvio.setObservacao(dto.getObservacao());

		TipoEnvio saved = tipoEnvioRepository.save(tipoEnvio);
		return convertToDTO(saved);
	}

	@Transactional
	public TipoEnvioDTO atualizar(Long id, TipoEnvioDTO dto) {
		logger.info("Atualizando tipo de envio ID: {}", id);

		TipoEnvio tipoEnvio = tipoEnvioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de envio não encontrado: " + id));

		// Verificar se o código já existe em outro registro
		if (!tipoEnvio.getCodigo().equals(dto.getCodigo()) && tipoEnvioRepository.existsByCodigo(dto.getCodigo())) {
			throw new IllegalArgumentException("Já existe um tipo de envio com o código: " + dto.getCodigo());
		}

		tipoEnvio.setCodigo(dto.getCodigo());
		tipoEnvio.setDescricao(dto.getDescricao());
		tipoEnvio.setAtivo(dto.getAtivo());
		tipoEnvio.setObservacao(dto.getObservacao());

		TipoEnvio updated = tipoEnvioRepository.save(tipoEnvio);
		return convertToDTO(updated);
	}

	@Transactional(readOnly = true)
	public TipoEnvioDTO buscarPorId(Long id) {
		logger.info("Buscando tipo de envio por ID: {}", id);

		TipoEnvio tipoEnvio = tipoEnvioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de envio não encontrado: " + id));

		return convertToDTO(tipoEnvio);
	}

	@Transactional(readOnly = true)
	public List<TipoEnvioResumoDTO> listarAtivos() {
		logger.info("Listando tipos de envio ativos");

		return tipoEnvioRepository.findByAtivoTrueOrderByCodigo().stream().map(this::convertToResumoDTO)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<TipoEnvioResumoDTO> buscarComFiltros(String descricao, Boolean ativo, Pageable pageable) {
		logger.info("Buscando tipos de envio com filtros - descricao: {}, ativo: {}", descricao, ativo);

		return tipoEnvioRepository.buscarComFiltros(descricao, ativo, pageable).map(this::convertToResumoDTO);
	}

	@Transactional
	public void excluir(Long id) {
		logger.info("Excluindo tipo de envio ID: {}", id);

		TipoEnvio tipoEnvio = tipoEnvioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de envio não encontrado: " + id));

		tipoEnvioRepository.delete(tipoEnvio);
	}

	@Transactional
	public TipoEnvioDTO alterarStatus(Long id, Boolean ativo) {
		logger.info("Alterando status do tipo de envio ID: {} para ativo: {}", id, ativo);

		TipoEnvio tipoEnvio = tipoEnvioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Tipo de envio não encontrado: " + id));

		tipoEnvio.setAtivo(ativo);
		TipoEnvio updated = tipoEnvioRepository.save(tipoEnvio);

		return convertToDTO(updated);
	}

	private TipoEnvioDTO convertToDTO(TipoEnvio tipoEnvio) {
		return new TipoEnvioDTO(tipoEnvio.getId(), tipoEnvio.getCodigo(), tipoEnvio.getDescricao(),
				tipoEnvio.getAtivo(), tipoEnvio.getObservacao());
	}

	private TipoEnvioResumoDTO convertToResumoDTO(TipoEnvio tipoEnvio) {
		return new TipoEnvioResumoDTO(tipoEnvio.getId(), tipoEnvio.getCodigo(), tipoEnvio.getDescricao(),
				tipoEnvio.getAtivo());
	}
}