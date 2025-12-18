package com.sga.dto;

import java.math.BigDecimal;
import java.util.List;

import com.sga.model.ItemSPC;

public class NotaDebitoDetalheDTO {
	public Long id;
	public String numeroNota;
	public String codigoSocio;
	public String nomeAssociado;
	public BigDecimal totalDebitos;
	public BigDecimal totalCreditos;
	public BigDecimal valorFaturado;
	public List<ItemSPC> itens; // reuso do model para enviar detalhes

	public NotaDebitoDetalheDTO() {

	}

	// getters/setters se preferir
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

	public List<ItemSPC> getItens() {
		return itens;
	}

	public void setItens(List<ItemSPC> itens) {
		this.itens = itens;
	}

}
