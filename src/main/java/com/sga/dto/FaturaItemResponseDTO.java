package com.sga.dto;

import java.math.BigDecimal;

import com.sga.model.FaturaItem;

public class FaturaItemResponseDTO {
	private Long id;
	private String codigoProduto;
	private String descricao;
	private BigDecimal quantidade;
	private BigDecimal valorUnitario;
	private BigDecimal valorTotal;
	private String tipoLancamento;

	public FaturaItemResponseDTO() {
	}

	public FaturaItemResponseDTO(FaturaItem item) {
		if (item != null) {
			this.id = item.getId();
			this.codigoProduto = item.getCodigoProduto();
			this.descricao = item.getDescricao();
			this.quantidade = item.getQuantidade();
			this.valorUnitario = item.getValorUnitario();
			this.valorTotal = item.getValorTotal();
			this.tipoLancamento = item.getTipoLancamento();
		}
	}

	public FaturaItemResponseDTO(Long id, String codigoProduto, String descricao, BigDecimal quantidade,
			BigDecimal valorUnitario, BigDecimal valorTotal, String tipoLancamento) {
		this.id = id;
		this.codigoProduto = codigoProduto;
		this.descricao = descricao;
		this.quantidade = quantidade;
		this.valorUnitario = valorUnitario;
		this.valorTotal = valorTotal;
		this.tipoLancamento = tipoLancamento;
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigoProduto() {
		return codigoProduto;
	}

	public void setCodigoProduto(String codigoProduto) {
		this.codigoProduto = codigoProduto;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(BigDecimal quantidade) {
		this.quantidade = quantidade;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getTipoLancamento() {
		return tipoLancamento;
	}

	public void setTipoLancamento(String tipoLancamento) {
		this.tipoLancamento = tipoLancamento;
	}

	@Override
	public String toString() {
		return "FaturaItemResponseDTO{" + "id=" + id + ", codigoProduto='" + codigoProduto + '\'' + ", descricao='"
				+ descricao + '\'' + ", quantidade=" + quantidade + ", valorUnitario=" + valorUnitario + ", valorTotal="
				+ valorTotal + ", tipoLancamento='" + tipoLancamento + '\'' + '}';
	}
}