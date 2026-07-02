// src/main/java/com/sga/service/ConfiguracaoRmService.java

package com.sga.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sga.model.ConfiguracaoRm;
import com.sga.repository.ConfiguracaoRmRepository;

@Service
public class ConfiguracaoRmService {

	private static final Logger log = LoggerFactory.getLogger(ConfiguracaoRmService.class);

	@Autowired
	private ConfiguracaoRmRepository configuracaoRmRepository;

	public List<ConfiguracaoRm> listarTodos() {
		log.info("Listando todas as configurações RM");
		return configuracaoRmRepository.findAll();
	}

	public List<ConfiguracaoRm> listarAtivos() {
		log.info("Listando configurações RM ativas");
		return configuracaoRmRepository.findByAtivoTrue();
	}

	public Optional<ConfiguracaoRm> buscarPorId(Long id) {
		log.info("Buscando configuração RM por ID: {}", id);
		return configuracaoRmRepository.findById(id);
	}

	public Optional<ConfiguracaoRm> buscarPorTipoMovimento(String tipoMovimento) {
		log.info("Buscando configuração RM por tipo movimento: {}", tipoMovimento);
		return configuracaoRmRepository.findByTipoMovimentoAndAtivoTrue(tipoMovimento);
	}

	public ConfiguracaoRm salvar(ConfiguracaoRm configuracao) {
		log.info("Salvando configuração RM: {}", configuracao.getDescricao());
		return configuracaoRmRepository.save(configuracao);
	}

	public void excluir(Long id) {
		log.info("Excluindo configuração RM ID: {}", id);
		configuracaoRmRepository.deleteById(id);
	}
}