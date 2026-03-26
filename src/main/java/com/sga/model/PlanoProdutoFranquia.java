package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "tb_plano_produto_franquia", uniqueConstraints = @UniqueConstraint(columnNames = { "plano_id",
		"produto_id" }))
public class PlanoProdutoFranquia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plano_id", nullable = false)
	private Planos plano; // Campo: plano (acessado como ppf.plano.id)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto; // Campo: produto (acessado como ppf.produto.id)

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "franquia_id", nullable = false)
	private Produto franquia; // Campo: franquia (acessado como ppf.franquia.id)

	@Column(name = "limite_franquia", nullable = false)
	private Integer limiteFranquia;

	@Column(name = "periodo_franquia", length = 20)
	private String periodoFranquia = "MENSAL";

	@Column(name = "valor_excedente", precision = 10, scale = 2)
	private BigDecimal valorExcedente;

	@Column(name = "permite_excedente")
	private Boolean permiteExcedente = true;

	@Column(name = "ativo")
	private Boolean ativo = true;

	@Column(name = "data_criacao")
	private LocalDateTime dataCriacao;

	@Column(name = "data_atualizacao")
	private LocalDateTime dataAtualizacao;

	@PrePersist
	protected void onCreate() {
		dataCriacao = LocalDateTime.now();
		dataAtualizacao = LocalDateTime.now();
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

	public Planos getPlano() {
		return plano;
	}

	public void setPlano(Planos plano) {
		this.plano = plano;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public Produto getFranquia() {
		return franquia;
	}

	public void setFranquia(Produto franquia) {
		this.franquia = franquia;
	}

	public Integer getLimiteFranquia() {
		return limiteFranquia;
	}

	public void setLimiteFranquia(Integer limiteFranquia) {
		this.limiteFranquia = limiteFranquia;
	}

	public String getPeriodoFranquia() {
		return periodoFranquia;
	}

	public void setPeriodoFranquia(String periodoFranquia) {
		this.periodoFranquia = periodoFranquia;
	}

	public BigDecimal getValorExcedente() {
		return valorExcedente;
	}

	public void setValorExcedente(BigDecimal valorExcedente) {
		this.valorExcedente = valorExcedente;
	}

	public Boolean getPermiteExcedente() {
		return permiteExcedente;
	}

	public void setPermiteExcedente(Boolean permiteExcedente) {
		this.permiteExcedente = permiteExcedente;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
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