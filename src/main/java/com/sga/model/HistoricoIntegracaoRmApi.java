// src/main/java/com/sga/model/HistoricoIntegracaoRmApi.java

package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "tb_historico_integracao_rm_api")
public class HistoricoIntegracaoRmApi {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_integracao")
	private LocalDateTime dataIntegracao;

	@Column(name = "quantidade_faturas")
	private Integer quantidadeFaturas;

	@Column(name = "quantidade_sucessos")
	private Integer quantidadeSucessos;

	@Column(name = "quantidade_erros")
	private Integer quantidadeErros;

	@Column(name = "status", length = 20)
	private String status; // SUCESSO, PARCIAL, ERRO

	@Column(name = "mensagem", columnDefinition = "TEXT")
	private String mensagem;

	@Column(name = "usuario", length = 100)
	private String usuario;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@PrePersist
	protected void onCreate() {
		criadoEm = LocalDateTime.now();
		dataIntegracao = LocalDateTime.now();
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDataIntegracao() {
		return dataIntegracao;
	}

	public void setDataIntegracao(LocalDateTime dataIntegracao) {
		this.dataIntegracao = dataIntegracao;
	}

	public Integer getQuantidadeFaturas() {
		return quantidadeFaturas;
	}

	public void setQuantidadeFaturas(Integer quantidadeFaturas) {
		this.quantidadeFaturas = quantidadeFaturas;
	}

	public Integer getQuantidadeSucessos() {
		return quantidadeSucessos;
	}

	public void setQuantidadeSucessos(Integer quantidadeSucessos) {
		this.quantidadeSucessos = quantidadeSucessos;
	}

	public Integer getQuantidadeErros() {
		return quantidadeErros;
	}

	public void setQuantidadeErros(Integer quantidadeErros) {
		this.quantidadeErros = quantidadeErros;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}
}