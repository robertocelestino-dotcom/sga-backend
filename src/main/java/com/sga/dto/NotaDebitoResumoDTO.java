package com.sga.dto;

import java.math.BigDecimal;

public class NotaDebitoResumoDTO {
	private Long id;
	private String numeroNota;
	private String codigoSocio;
	private String nomeAssociado;
	private BigDecimal totalDebitos;
	private BigDecimal totalCreditos;
	private BigDecimal valorFaturado; // debitos - credits

	public NotaDebitoResumoDTO() {
	}

	public NotaDebitoResumoDTO(Long id, String numeroNota, String codigoSocio, String nomeAssociado,
			BigDecimal totalDebitos, BigDecimal totalCreditos, BigDecimal valorFaturado) {
		this.id = id;
		this.numeroNota = numeroNota;
		this.codigoSocio = codigoSocio;
		this.nomeAssociado = nomeAssociado;
		this.totalDebitos = totalDebitos;
		this.totalCreditos = totalCreditos;
		this.valorFaturado = valorFaturado;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumeroNota() {
		return numeroNota;
	}

	public void setNumeroNota(String numeroNota) {
		this.numeroNota = numeroNota;
	}

	public String getCodigoSocio() {
		return codigoSocio;
	}

	public void setCodigoSocio(String codigoSocio) {
		this.codigoSocio = codigoSocio;
	}

	public String getNomeAssociado() {
		return nomeAssociado;
	}

	public void setNomeAssociado(String nomeAssociado) {
		this.nomeAssociado = nomeAssociado;
	}

	public BigDecimal getTotalDebitos() {
		return totalDebitos;
	}

	public void setTotalDebitos(BigDecimal totalDebitos) {
		this.totalDebitos = totalDebitos;
	}

	public BigDecimal getTotalCreditos() {
		return totalCreditos;
	}

	public void setTotalCreditos(BigDecimal totalCreditos) {
		this.totalCreditos = totalCreditos;
	}

	public BigDecimal getValorFaturado() {
		return valorFaturado;
	}

	public void setValorFaturado(BigDecimal valorFaturado) {
		this.valorFaturado = valorFaturado;
	}
}
