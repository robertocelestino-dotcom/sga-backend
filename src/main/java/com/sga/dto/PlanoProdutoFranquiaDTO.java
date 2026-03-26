package com.sga.dto;

import java.math.BigDecimal;

public class PlanoProdutoFranquiaDTO {
	private Long id;
	private Long planoId;
	private String planoNome;
	private Long produtoId;
	private String produtoNome;
	private String produtoCodigo;  // ✅ NOVO CAMPO
	private Long franquiaId;
	private String franquiaNome;
	private String franquiaCodigo;  // ✅ NOVO CAMPO
	private Integer limiteFranquia;
	private String periodoFranquia;
	private BigDecimal valorExcedente;
	private Boolean permiteExcedente;
	private Boolean ativo;

	// Construtores
	public PlanoProdutoFranquiaDTO() {
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getPlanoId() {
		return planoId;
	}

	public void setPlanoId(Long planoId) {
		this.planoId = planoId;
	}

	public String getPlanoNome() {
		return planoNome;
	}

	public void setPlanoNome(String planoNome) {
		this.planoNome = planoNome;
	}

	public Long getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(Long produtoId) {
		this.produtoId = produtoId;
	}

	public String getProdutoNome() {
		return produtoNome;
	}

	public void setProdutoNome(String produtoNome) {
		this.produtoNome = produtoNome;
	}

	// ✅ NOVO GETTER E SETTER
	public String getProdutoCodigo() {
		return produtoCodigo;
	}

	public void setProdutoCodigo(String produtoCodigo) {
		this.produtoCodigo = produtoCodigo;
	}

	public Long getFranquiaId() {
		return franquiaId;
	}

	public void setFranquiaId(Long franquiaId) {
		this.franquiaId = franquiaId;
	}

	public String getFranquiaNome() {
		return franquiaNome;
	}

	public void setFranquiaNome(String franquiaNome) {
		this.franquiaNome = franquiaNome;
	}

	// ✅ NOVO GETTER E SETTER
	public String getFranquiaCodigo() {
		return franquiaCodigo;
	}

	public void setFranquiaCodigo(String franquiaCodigo) {
		this.franquiaCodigo = franquiaCodigo;
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
	
}