package com.sga.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociadoProdutoResumoDTO {
	private Long id;
	private Long associadoId;
	private String associadoNome;
	private Long produtoId;
	private String produtoCodigo;
	private String produtoNome;
	private String tipoProduto;
	private BigDecimal valorDefinido;
	private BigDecimal valorEfetivo;
	private String statusNoProcesso;
	private Boolean ativo;
	private String tipoEnvioDescricao;

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

	public Long getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(Long produtoId) {
		this.produtoId = produtoId;
	}

	public String getProdutoCodigo() {
		return produtoCodigo;
	}

	public void setProdutoCodigo(String produtoCodigo) {
		this.produtoCodigo = produtoCodigo;
	}

	public String getProdutoNome() {
		return produtoNome;
	}

	public void setProdutoNome(String produtoNome) {
		this.produtoNome = produtoNome;
	}

	public String getTipoProduto() {
		return tipoProduto;
	}

	public void setTipoProduto(String tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	public BigDecimal getValorDefinido() {
		return valorDefinido;
	}

	public void setValorDefinido(BigDecimal valorDefinido) {
		this.valorDefinido = valorDefinido;
	}

	public BigDecimal getValorEfetivo() {
		return valorEfetivo;
	}

	public void setValorEfetivo(BigDecimal valorEfetivo) {
		this.valorEfetivo = valorEfetivo;
	}

	public String getStatusNoProcesso() {
		return statusNoProcesso;
	}

	public void setStatusNoProcesso(String statusNoProcesso) {
		this.statusNoProcesso = statusNoProcesso;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getTipoEnvioDescricao() {
		return tipoEnvioDescricao;
	}

	public void setTipoEnvioDescricao(String tipoEnvioDescricao) {
		this.tipoEnvioDescricao = tipoEnvioDescricao;
	}

}