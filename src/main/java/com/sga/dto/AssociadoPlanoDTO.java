package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AssociadoPlanoDTO {
	private Long id;
	private Long associadoId;
	private String associadoNome;
	private Long planoId;
	private String planoNome;
	private BigDecimal planoValor;
	private LocalDate dataAdesao;
	private LocalDate dataCancelamento;
	private String status;
	private String observacao;
	private List<PlanoProdutoFranquiaDTO> produtosDoPlano;

	// Construtores
	public AssociadoPlanoDTO() {
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAssociadoId() {
		return associadoId;
	}

	public void setAssociadoId(Long associadoId) {
		this.associadoId = associadoId;
	}

	public String getAssociadoNome() {
		return associadoNome;
	}

	public void setAssociadoNome(String associadoNome) {
		this.associadoNome = associadoNome;
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

	public BigDecimal getPlanoValor() {
		return planoValor;
	}

	public void setPlanoValor(BigDecimal planoValor) {
		this.planoValor = planoValor;
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

	public List<PlanoProdutoFranquiaDTO> getProdutosDoPlano() {
		return produtosDoPlano;
	}

	public void setProdutosDoPlano(List<PlanoProdutoFranquiaDTO> produtosDoPlano) {
		this.produtosDoPlano = produtosDoPlano;
	}
}