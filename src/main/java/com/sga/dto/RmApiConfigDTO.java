// src/main/java/com/sga/dto/RmApiConfigDTO.java

package com.sga.dto;

import com.sga.model.ConfiguracaoRmApi;

public class RmApiConfigDTO {

    private Long id;
    private String nome;
    private String descricao;
    private String wsUrl;
    private String wsUsername;
    private String wsPassword;
    private String wsColigada;
    private String wsSistema;
    private String wsUsuario;
    private String wsDataServer;
    private Integer wsTimeout;
    private Integer wsTentativas;
    private Boolean wsAutoReconnect;
    private Boolean ativo;
    private Boolean integracaoAutomatica;

    // ============================================================
    // CONSTRUTORES
    // ============================================================

    public RmApiConfigDTO() {
    }

    public RmApiConfigDTO(ConfiguracaoRmApi entity) {
        fromEntity(entity);
    }

    // ============================================================
    // MÉTODOS DE CONVERSÃO
    // ============================================================

    public static RmApiConfigDTO fromEntity(ConfiguracaoRmApi entity) {
        if (entity == null) return null;

        RmApiConfigDTO dto = new RmApiConfigDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setDescricao(entity.getDescricao());
        dto.setWsUrl(entity.getWsUrl());
        dto.setWsUsername(entity.getWsUsername());
        dto.setWsPassword(entity.getWsPassword());
        dto.setWsColigada(entity.getWsColigada());
        dto.setWsSistema(entity.getWsSistema());
        dto.setWsUsuario(entity.getWsUsuario());
        dto.setWsDataServer(entity.getWsDataServer());
        dto.setWsTimeout(entity.getWsTimeout());
        dto.setWsTentativas(entity.getWsTentativas());
        dto.setWsAutoReconnect(entity.getWsAutoReconnect());
        dto.setAtivo(entity.getAtivo());
        dto.setIntegracaoAutomatica(entity.getIntegracaoAutomatica());
        return dto;
    }

    public ConfiguracaoRmApi toEntity() {
        ConfiguracaoRmApi entity = new ConfiguracaoRmApi();
        entity.setId(this.id);
        entity.setNome(this.nome);
        entity.setDescricao(this.descricao);
        entity.setWsUrl(this.wsUrl);
        entity.setWsUsername(this.wsUsername);
        entity.setWsPassword(this.wsPassword);
        entity.setWsColigada(this.wsColigada);
        entity.setWsSistema(this.wsSistema);
        entity.setWsUsuario(this.wsUsuario);
        entity.setWsDataServer(this.wsDataServer);
        entity.setWsTimeout(this.wsTimeout);
        entity.setWsTentativas(this.wsTentativas);
        entity.setWsAutoReconnect(this.wsAutoReconnect);
        entity.setAtivo(this.ativo);
        entity.setIntegracaoAutomatica(this.integracaoAutomatica);
        return entity;
    }

    // ============================================================
    // GETTERS E SETTERS
    // ============================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getWsUrl() {
        return wsUrl;
    }

    public void setWsUrl(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public String getWsUsername() {
        return wsUsername;
    }

    public void setWsUsername(String wsUsername) {
        this.wsUsername = wsUsername;
    }

    public String getWsPassword() {
        return wsPassword;
    }

    public void setWsPassword(String wsPassword) {
        this.wsPassword = wsPassword;
    }

    public String getWsColigada() {
        return wsColigada;
    }

    public void setWsColigada(String wsColigada) {
        this.wsColigada = wsColigada;
    }

    public String getWsSistema() {
        return wsSistema;
    }

    public void setWsSistema(String wsSistema) {
        this.wsSistema = wsSistema;
    }

    public String getWsUsuario() {
        return wsUsuario;
    }

    public void setWsUsuario(String wsUsuario) {
        this.wsUsuario = wsUsuario;
    }

    public String getWsDataServer() {
        return wsDataServer;
    }

    public void setWsDataServer(String wsDataServer) {
        this.wsDataServer = wsDataServer;
    }

    public Integer getWsTimeout() {
        return wsTimeout;
    }

    public void setWsTimeout(Integer wsTimeout) {
        this.wsTimeout = wsTimeout;
    }

    public Integer getWsTentativas() {
        return wsTentativas;
    }

    public void setWsTentativas(Integer wsTentativas) {
        this.wsTentativas = wsTentativas;
    }

    public Boolean getWsAutoReconnect() {
        return wsAutoReconnect;
    }

    public void setWsAutoReconnect(Boolean wsAutoReconnect) {
        this.wsAutoReconnect = wsAutoReconnect;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Boolean getIntegracaoAutomatica() {
        return integracaoAutomatica;
    }

    public void setIntegracaoAutomatica(Boolean integracaoAutomatica) {
        this.integracaoAutomatica = integracaoAutomatica;
    }
}