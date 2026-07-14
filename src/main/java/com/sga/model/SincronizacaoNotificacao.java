// src/main/java/com/sga/model/SincronizacaoNotificacao.java

package com.sga.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "tb_sincronizacao_notificacao")
public class SincronizacaoNotificacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_inicio", nullable = false)
	private LocalDate dataInicio;

	@Column(name = "data_fim", nullable = false)
	private LocalDate dataFim;

	@Column(name = "codigo_associado", length = 50)
	private String codigoAssociado;

	@Column(name = "total_associados")
	private Integer totalAssociados = 0;

	@Column(name = "total_registros")
	private Integer totalRegistros = 0;

	@Column(name = "status", length = 20)
	private String status = "CONCLUIDO";

	@Column(name = "usuario", length = 100)
	private String usuario;

	@Column(name = "data_sincronizacao")
	private LocalDateTime dataSincronizacao;

	@Column(name = "observacao", columnDefinition = "TEXT")
	private String observacao;

	@PrePersist
	protected void onCreate() {
		if (dataSincronizacao == null) {
			dataSincronizacao = LocalDateTime.now();
		}
		if (status == null) {
			status = "CONCLUIDO";
		}
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDate getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}

	public String getCodigoAssociado() {
		return codigoAssociado;
	}

	public void setCodigoAssociado(String codigoAssociado) {
		this.codigoAssociado = codigoAssociado;
	}

	public Integer getTotalAssociados() {
		return totalAssociados;
	}

	public void setTotalAssociados(Integer totalAssociados) {
		this.totalAssociados = totalAssociados;
	}

	public Integer getTotalRegistros() {
		return totalRegistros;
	}

	public void setTotalRegistros(Integer totalRegistros) {
		this.totalRegistros = totalRegistros;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getDataSincronizacao() {
		return dataSincronizacao;
	}

	public void setDataSincronizacao(LocalDateTime dataSincronizacao) {
		this.dataSincronizacao = dataSincronizacao;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
}