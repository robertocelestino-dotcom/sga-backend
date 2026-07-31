// src/main/java/com/sga/model/ConfiguracaoRmApi.java

package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_configuracao_rm_api")
public class ConfiguracaoRmApi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(length = 200)
	private String descricao;

	@Column(name = "ws_url", length = 500)
	private String wsUrl = "http://localhost:8051/wsMov/MEX?wsdl";

	@Column(name = "ws_username", length = 100)
	private String wsUsername = "mestre";

	@Column(name = "ws_password", length = 100)
	private String wsPassword = "totvs";

	@Column(name = "ws_coligada", length = 10)
	private String wsColigada = "1";

	@Column(name = "ws_sistema", length = 10)
	private String wsSistema = "T";

	@Column(name = "ws_usuario", length = 100)
	private String wsUsuario = "mestre";

	@Column(name = "ws_data_server", length = 100)
	private String wsDataServer = "MovMovimentoTBCData";

	@Column(name = "ws_timeout")
	private Integer wsTimeout = 30000;

	@Column(name = "ws_tentativas")
	private Integer wsTentativas = 3;

	@Column(name = "ws_auto_reconnect")
	private Boolean wsAutoReconnect = true;

	@Column(name = "ativo")
	private Boolean ativo = true;

	@Column(name = "integracao_automatica")
	private Boolean integracaoAutomatica = false;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "criado_por", length = 100)
	private String criadoPor;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@Column(name = "atualizado_por", length = 100)
	private String atualizadoPor;

	// Getters e Setters
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

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public String getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(String criadoPor) {
		this.criadoPor = criadoPor;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public String getAtualizadoPor() {
		return atualizadoPor;
	}

	public void setAtualizadoPor(String atualizadoPor) {
		this.atualizadoPor = atualizadoPor;
	}
}