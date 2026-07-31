// src/main/java/com/sga/service/RmApiConfigService.java

package com.sga.service;

import com.sga.dto.RmApiConfigDTO;
import com.sga.model.ConfiguracaoRmApi;
import com.sga.repository.ConfiguracaoRmApiRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RmApiConfigService {

    private static final Logger log = LoggerFactory.getLogger(RmApiConfigService.class);

    @Autowired
    private ConfiguracaoRmApiRepository repository;

    public List<RmApiConfigDTO> listarConfiguracoes() {
        log.info("📋 Listando configurações RM API");
        return repository.findAll().stream()
            .map(RmApiConfigDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public List<RmApiConfigDTO> listarConfiguracoesAtivas() {
        log.info("📋 Listando configurações RM API ativas");
        return repository.findByAtivoTrueOrderByNome().stream()
            .map(RmApiConfigDTO::fromEntity)
            .collect(Collectors.toList());
    }

    public RmApiConfigDTO buscarConfiguracaoAtiva() {
        log.info("🔍 Buscando configuração RM API ativa");
        return repository.findByAtivoTrue()
            .map(RmApiConfigDTO::fromEntity)
            .orElseThrow(() -> new RuntimeException("Nenhuma configuração RM API ativa encontrada"));
    }

    public RmApiConfigDTO buscarPorId(Long id) {
        log.info("🔍 Buscando configuração RM API por ID: {}", id);
        return repository.findById(id)
            .map(RmApiConfigDTO::fromEntity)
            .orElseThrow(() -> new RuntimeException("Configuração não encontrada: " + id));
    }

    @Transactional
    public RmApiConfigDTO criarConfiguracao(RmApiConfigDTO dto, String usuario) {
        log.info("➕ Criando configuração RM API: {}", dto.getNome());

        ConfiguracaoRmApi entity = dto.toEntity();
        entity.setCriadoEm(LocalDateTime.now());
        entity.setCriadoPor(usuario);
        entity.setAtualizadoEm(LocalDateTime.now());
        entity.setAtualizadoPor(usuario);

        // Se esta for a primeira configuração, ativar automaticamente
        if (repository.count() == 0) {
            entity.setAtivo(true);
        }

        ConfiguracaoRmApi saved = repository.save(entity);
        log.info("✅ Configuração RM API criada ID: {}", saved.getId());

        return RmApiConfigDTO.fromEntity(saved);
    }

    @Transactional
    public RmApiConfigDTO atualizarConfiguracao(Long id, RmApiConfigDTO dto, String usuario) {
        log.info("✏️ Atualizando configuração RM API ID: {}", id);

        ConfiguracaoRmApi existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuração não encontrada: " + id));

        existing.setNome(dto.getNome());
        existing.setDescricao(dto.getDescricao());
        existing.setWsUrl(dto.getWsUrl());
        existing.setWsUsername(dto.getWsUsername());
        existing.setWsPassword(dto.getWsPassword());
        existing.setWsColigada(dto.getWsColigada());
        existing.setWsSistema(dto.getWsSistema());
        existing.setWsUsuario(dto.getWsUsuario());
        existing.setWsDataServer(dto.getWsDataServer());
        existing.setWsTimeout(dto.getWsTimeout());
        existing.setWsTentativas(dto.getWsTentativas());
        existing.setWsAutoReconnect(dto.getWsAutoReconnect());
        existing.setAtivo(dto.getAtivo());
        existing.setIntegracaoAutomatica(dto.getIntegracaoAutomatica());
        existing.setAtualizadoEm(LocalDateTime.now());
        existing.setAtualizadoPor(usuario);

        // Se ativar esta, desativar as outras
        if (dto.getAtivo()) {
            repository.findAll().stream()
                .filter(c -> !c.getId().equals(id) && c.getAtivo())
                .forEach(c -> {
                    c.setAtivo(false);
                    repository.save(c);
                });
        }

        ConfiguracaoRmApi updated = repository.save(existing);
        log.info("✅ Configuração RM API atualizada ID: {}", updated.getId());

        return RmApiConfigDTO.fromEntity(updated);
    }

    @Transactional
    public void excluirConfiguracao(Long id) {
        log.info("🗑️ Excluindo configuração RM API ID: {}", id);
        repository.deleteById(id);
        log.info("✅ Configuração RM API excluída ID: {}", id);
    }

    public void ativarConfiguracao(Long id) {
        log.info("🔛 Ativando configuração RM API ID: {}", id);

        // Desativar todas
        repository.findAll().forEach(c -> {
            c.setAtivo(false);
            repository.save(c);
        });

        // Ativar a selecionada
        ConfiguracaoRmApi config = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Configuração não encontrada: " + id));
        config.setAtivo(true);
        repository.save(config);

        log.info("✅ Configuração RM API ativada ID: {}", id);
    }
}