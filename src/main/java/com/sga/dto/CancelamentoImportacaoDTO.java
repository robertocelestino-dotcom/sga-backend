// src/main/java/com/sga/dto/CancelamentoImportacaoDTO.java

package com.sga.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CancelamentoImportacaoDTO {

	private String codigoAssociado;
	private String descricaoProduto;
	private String produtoPersonalizado;
	private BigDecimal valorCancelamento;
	private Integer quantidadeServicos;
	private List<String> servicos = new ArrayList<>();
	private String status;
	private Integer linha;
	private String erro;

	// Getters e Setters
	public String getCodigoAssociado() {
		return codigoAssociado;
	}

	public void setCodigoAssociado(String codigoAssociado) {
		this.codigoAssociado = codigoAssociado;
	}

	public String getDescricaoProduto() {
		return descricaoProduto;
	}

	public void setDescricaoProduto(String descricaoProduto) {
		this.descricaoProduto = descricaoProduto;
	}

	public String getProdutoPersonalizado() {
		return produtoPersonalizado;
	}

	public void setProdutoPersonalizado(String produtoPersonalizado) {
		this.produtoPersonalizado = produtoPersonalizado;
	}

	public BigDecimal getValorCancelamento() {
		return valorCancelamento;
	}

	public void setValorCancelamento(BigDecimal valorCancelamento) {
		this.valorCancelamento = valorCancelamento;
	}

	public Integer getQuantidadeServicos() {
		return quantidadeServicos;
	}

	public void setQuantidadeServicos(Integer quantidadeServicos) {
		this.quantidadeServicos = quantidadeServicos;
	}

	public List<String> getServicos() {
		return servicos;
	}

	public void setServicos(List<String> servicos) {
		this.servicos = servicos;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getLinha() {
		return linha;
	}

	public void setLinha(Integer linha) {
		this.linha = linha;
	}

	public String getErro() {
		return erro;
	}

	public void setErro(String erro) {
		this.erro = erro;
	}
}