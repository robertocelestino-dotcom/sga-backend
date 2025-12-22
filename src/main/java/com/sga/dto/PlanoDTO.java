// src/main/java/com/sga/dto/PlanoDTO.java
package com.sga.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PlanoDTO {

	private Long id;
	private String codigo;
	private String plano;
	private String descricao;
	private BigDecimal valorMensal;
	private BigDecimal valorAnual;
	private String periodicidade; // MENSAL, ANUAL, SEMESTRAL, TRIMESTRAL
	private Integer limiteAssociados;
	private Integer limiteConsultas;
	private Boolean incluiSuporte;
	private Integer diasCarencia;
	private String status; // ATIVO, INATIVO
	private String observacoes;
	private LocalDateTime dataCadastro;

	public PlanoDTO() {

	}

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

	public String getPeriodicidade() {
		return periodicidade;
	}

	public void setPeriodicidade(String periodicidade) {
		this.periodicidade = periodicidade;
	}

	public Integer getLimiteAssociados() {
		return limiteAssociados;
	}

	public void setLimiteAssociados(Integer limiteAssociados) {
		this.limiteAssociados = limiteAssociados;
	}

	public Integer getLimiteConsultas() {
		return limiteConsultas;
	}

	public void setLimiteConsultas(Integer limiteConsultas) {
		this.limiteConsultas = limiteConsultas;
	}

	public Boolean getIncluiSuporte() {
		return incluiSuporte;
	}

	public void setIncluiSuporte(Boolean incluiSuporte) {
		this.incluiSuporte = incluiSuporte;
	}

	public Integer getDiasCarencia() {
		return diasCarencia;
	}

	public void setDiasCarencia(Integer diasCarencia) {
		this.diasCarencia = diasCarencia;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
