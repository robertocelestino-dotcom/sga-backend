package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.ConfiguracaoRm;
import com.sga.model.ConfiguracaoRmParametro;
import com.sga.repository.ConfiguracaoRmRepository;
import com.sga.repository.ConfiguracaoRmParametroRepository;

@Service
public class IntegracaoRmService {

    private static final Logger log = LoggerFactory.getLogger(IntegracaoRmService.class);

    @Autowired
    private ConfiguracaoRmRepository configuracaoRmRepository;

    @Autowired
    private ConfiguracaoRmParametroRepository parametroRepository;

    // ==================== CONFIGURAÇÕES ====================

    @Transactional(readOnly = true)
    public List<ConfiguracaoRm> listarConfiguracoes() {
        log.info("📋 Listando configurações RM");
        return configuracaoRmRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ConfiguracaoRm> listarConfiguracoesAtivas() {
        log.info("📋 Listando configurações RM ativas");
        return configuracaoRmRepository.findByAtivoTrue();
    }

    @Transactional(readOnly = true)
    public Optional<ConfiguracaoRm> buscarPorId(Long id) {
        log.info("🔍 Buscando configuração RM por ID: {}", id);
        return configuracaoRmRepository.findById(id);
    }

    @Transactional
    public ConfiguracaoRm criarConfiguracao(ConfiguracaoRm configuracao) {
        log.info("➕ Criando nova configuração RM: {}", configuracao.getDescricao());
        configuracao.setCriadoEm(LocalDateTime.now());
        
        ConfiguracaoRm saved = configuracaoRmRepository.save(configuracao);
        
        // Salvar parâmetros
        if (configuracao.getParametros() != null && !configuracao.getParametros().isEmpty()) {
            for (ConfiguracaoRmParametro parametro : configuracao.getParametros()) {
                parametro.setConfiguracaoRm(saved);
                parametroRepository.save(parametro);
            }
        }
        
        return saved;
    }

    @Transactional
    public ConfiguracaoRm atualizarConfiguracao(Long id, ConfiguracaoRm configuracao) {
        log.info("✏️ Atualizando configuração RM ID: {}", id);
        
        ConfiguracaoRm existing = configuracaoRmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Configuração não encontrada"));
        
        // Atualizar campos básicos
        existing.setDescricao(configuracao.getDescricao());
        existing.setAtivo(configuracao.getAtivo());
        existing.setTipoMovimento(configuracao.getTipoMovimento());
        existing.setCodigoTmv(configuracao.getCodigoTmv());
        existing.setCentroCusto(configuracao.getCentroCusto());
        existing.setCondicaoPagamento(configuracao.getCondicaoPagamento());
        existing.setSerie(configuracao.getSerie());
        existing.setContaCaixa(configuracao.getContaCaixa());
        existing.setCodigoServico(configuracao.getCodigoServico());
        existing.setMunicipioServico(configuracao.getMunicipioServico());
        existing.setUfServico(configuracao.getUfServico());
        existing.setAtualizadoEm(LocalDateTime.now());
        
        // Remover parâmetros antigos
        parametroRepository.deleteByConfiguracaoRmId(id);
        
        // Adicionar novos parâmetros
        if (configuracao.getParametros() != null && !configuracao.getParametros().isEmpty()) {
            for (ConfiguracaoRmParametro parametro : configuracao.getParametros()) {
                parametro.setId(null);
                parametro.setConfiguracaoRm(existing);
                parametroRepository.save(parametro);
            }
        }
        
        return configuracaoRmRepository.save(existing);
    }

    @Transactional
    public void excluirConfiguracao(Long id) {
        log.info("🗑️ Excluindo configuração RM ID: {}", id);
        parametroRepository.deleteByConfiguracaoRmId(id);
        configuracaoRmRepository.deleteById(id);
    }
}