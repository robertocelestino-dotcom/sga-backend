package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlanoDTO {
	private Long id;
	private String codigo;
	private String plano;
	private String descricao;
	private BigDecimal valorMensal;
	private BigDecimal valorAnual;
	private String periodoCobranca;
	private String status;
	private Integer limiteAssociados;
	private String observacoes;
	private LocalDateTime dataCadastro;

	public PlanoDTO() {
		
	}

	// Getters e Setters para todos os campos
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getPlano() {
		return plano;
	}

	public void setPlano(String plano) {
		this.plano = plano;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getValorMensal() {
		return valorMensal;
	}

	public void setValorMensal(BigDecimal valorMensal) {
		this.valorMensal = valorMensal;
	}

	public BigDecimal getValorAnual() {
		return valorAnual;
	}

	public void setValorAnual(BigDecimal valorAnual) {
		this.valorAnual = valorAnual;
	}

	public String getPeriodoCobranca() {
		return periodoCobranca;
	}

	public void setPeriodoCobranca(String periodoCobranca) {
		this.periodoCobranca = periodoCobranca;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getLimiteAssociados() {
		return limiteAssociados;
	}

	public void setLimiteAssociados(Integer limiteAssociados) {
		this.limiteAssociados = limiteAssociados;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
}