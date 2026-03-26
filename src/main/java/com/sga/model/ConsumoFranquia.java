package com.sga.model;

import java.math.BigDecimal;
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
@Table(name = "tb_consumo_franquia", indexes = {
		@Index(name = "idx_consumo_periodo", columnList = "associado_id, produto_id, ano, mes"),
		@Index(name = "idx_consumo_associado", columnList = "associado_id, ano, mes") })
public class ConsumoFranquia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "associado_id", nullable = false)
	private Associado associado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "plano_id", nullable = false)
	private Planos plano;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "produto_id", nullable = false)
	private Produto produto;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "franquia_id", nullable = false)
	private Produto franquia;

	@Column(name = "ano", nullable = false)
	private Integer ano;

	@Column(name = "mes", nullable = false)
	private Integer mes;

	@Column(name = "quantidade_utilizada", nullable = false)
	private Integer quantidadeUtilizada = 0;

	@Column(name = "limite_franquia", nullable = false)
	private Integer limiteFranquia;

	@Column(name = "quantidade_excedente")
	private Integer quantidadeExcedente = 0;

	@Column(name = "valor_excedente", precision = 10, scale = 2)
	private BigDecimal valorExcedente;

	@Column(name = "data_ultimo_consumo")
	private LocalDateTime dataUltimoConsumo;

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

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getQuantidadeUtilizada() {
		return quantidadeUtilizada;
	}

	public void setQuantidadeUtilizada(Integer quantidadeUtilizada) {
		this.quantidadeUtilizada = quantidadeUtilizada;
	}

	public Integer getLimiteFranquia() {
		return limiteFranquia;
	}

	public void setLimiteFranquia(Integer limiteFranquia) {
		this.limiteFranquia = limiteFranquia;
	}

	public Integer getQuantidadeExcedente() {
		return quantidadeExcedente;
	}

	public void setQuantidadeExcedente(Integer quantidadeExcedente) {
		this.quantidadeExcedente = quantidadeExcedente;
	}

	public BigDecimal getValorExcedente() {
		return valorExcedente;
	}

	public void setValorExcedente(BigDecimal valorExcedente) {
		this.valorExcedente = valorExcedente;
	}

	public LocalDateTime getDataUltimoConsumo() {
		return dataUltimoConsumo;
	}

	public void setDataUltimoConsumo(LocalDateTime dataUltimoConsumo) {
		this.dataUltimoConsumo = dataUltimoConsumo;
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
