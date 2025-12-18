// src/main/java/com/sga/dto/ProdutoResumoDTO.java
package com.sga.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProdutoResumoDTO {
	private Long id;
	private String codigo;
	private String nome;
	private String nomeCompleto;
	private BigDecimal valorUnitario;
	private String status;
	private String tipoProduto;
	private String categoria;
	private String modalidade;
	private Boolean temFranquia;
	private Integer totalFranquias;

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

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNomeCompleto() {
		return nomeCompleto;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nomeCompleto = nomeCompleto;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTipoProduto() {
		return tipoProduto;
	}

	public void setTipoProduto(String tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getModalidade() {
		return modalidade;
	}

	public void setModalidade(String modalidade) {
		this.modalidade = modalidade;
	}

	public Boolean getTemFranquia() {
		return temFranquia;
	}

	public void setTemFranquia(Boolean temFranquia) {
		this.temFranquia = temFranquia;
	}

	public Integer getTotalFranquias() {
		return totalFranquias;
	}

	public void setTotalFranquias(Integer totalFranquias) {
		this.totalFranquias = totalFranquias;
	}

	// Para exibição formatada
	public String getValorFormatado() {
		if (valorUnitario == null)
			return "R$ 0,00";
		return String.format("R$ %,.2f", valorUnitario);
	}

	public String getStatusColor() {
		return "ATIVO".equals(status) ? "success" : "error";
	}

	public String getTipoProdutoLabel() {
		switch (tipoProduto) {
		case "FRANQUIA":
			return "Franquia";
		case "SERVICO":
			return "Serviço";
		case "PRODUTO":
			return "Produto";
		case "ASSINATURA":
			return "Assinatura";
		default:
			return tipoProduto;
		}
	}
}