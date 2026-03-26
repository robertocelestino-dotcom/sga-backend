package com.sga.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "associado_plano", indexes = @Index(name = "idx_associado_plano_status", columnList = "associado_id, status"))
public class AssociadoPlano {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "associado_id", nullable = false)
	private Associado associado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plano_id", nullable = false)
	private Planos plano;

	@Column(name = "data_adesao", nullable = false)
	private LocalDate dataAdesao;

	@Column(name = "data_cancelamento")
	private LocalDate dataCancelamento;

	@Column(name = "status", length = 20)
	private String status = "ATIVO";

	@Column(name = "observacao", length = 500)
	private String observacao;

	@Column(name = "data_criacao")
	private LocalDateTime dataCriacao;

	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao;

	@PrePersist
	protected void onCreate() {
		dataCriacao = LocalDateTime.now();
		dataAtualizacao = LocalDateTime.now();
		if (dataAdesao == null) {
			dataAdesao = LocalDate.now();
		}
	}

	@PreUpdate
	protected void onUpdate() {
		dataAtualizacao = LocalDateTime.now();
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Associado getAssociado() {
		return associado;
	}

	public void setAssociado(Associado associado) {
		this.associado = associado;
	}

	public Planos getPlano() {
		return plano;
	}

	public void setPlano(Planos plano) {
		this.plano = plano;
	}

	public LocalDate getDataAdesao() {
		return dataAdesao;
	}

	public void setDataAdesao(LocalDate dataAdesao) {
		this.dataAdesao = dataAdesao;
	}

	public LocalDate getDataCancelamento() {
		return dataCancelamento;
	}

	public void setDataCancelamento(LocalDate dataCancelamento) {
		this.dataCancelamento = dataCancelamento;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public LocalDateTime getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}
}
