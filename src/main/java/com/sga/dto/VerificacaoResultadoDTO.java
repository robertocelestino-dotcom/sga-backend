package com.sga.dto;

import java.math.BigDecimal;
import java.util.Map;

public class VerificacaoResultadoDTO {
	
	private String categoria;
	private Long quantidadeArquivo;
	private Long quantidadeBanco;
	private Long diferenca;
	private BigDecimal valorArquivo;
	private BigDecimal valorBanco;
	private BigDecimal diferencaValor;
	private boolean possuiDivergencia;
	private Map<String, Object> detalhes;

	public VerificacaoResultadoDTO() {
	}

	public VerificacaoResultadoDTO(String categoria) {
		this.categoria = categoria;
		this.quantidadeArquivo = 0L;
		this.quantidadeBanco = 0L;
		this.diferenca = 0L;
		this.valorArquivo = BigDecimal.ZERO;
		this.valorBanco = BigDecimal.ZERO;
		this.diferencaValor = BigDecimal.ZERO;
		this.possuiDivergencia = false;
	}

	// Getters e Setters
	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public Long getQuantidadeArquivo() {
		return quantidadeArquivo;
	}

	public void setQuantidadeArquivo(Long quantidadeArquivo) {
		this.quantidadeArquivo = quantidadeArquivo;
	}

	public Long getQuantidadeBanco() {
		return quantidadeBanco;
	}

	public void setQuantidadeBanco(Long quantidadeBanco) {
		this.quantidadeBanco = quantidadeBanco;
	}

	public Long getDiferenca() {
		return diferenca;
	}

	public void setDiferenca(Long diferenca) {
		this.diferenca = diferenca;
	}

	public BigDecimal getValorArquivo() {
		return valorArquivo;
	}

	public void setValorArquivo(BigDecimal valorArquivo) {
		this.valorArquivo = valorArquivo;
	}

	public BigDecimal getValorBanco() {
		return valorBanco;
	}

	public void setValorBanco(BigDecimal valorBanco) {
		this.valorBanco = valorBanco;
	}

	public BigDecimal getDiferencaValor() {
		return diferencaValor;
	}

	public void setDiferencaValor(BigDecimal diferencaValor) {
		this.diferencaValor = diferencaValor;
	}

	public boolean isPossuiDivergencia() {
		return possuiDivergencia;
	}

	public void setPossuiDivergencia(boolean possuiDivergencia) {
		this.possuiDivergencia = possuiDivergencia;
	}

	public Map<String, Object> getDetalhes() {
		return detalhes;
	}

	public void setDetalhes(Map<String, Object> detalhes) {
		this.detalhes = detalhes;
	}
	
}