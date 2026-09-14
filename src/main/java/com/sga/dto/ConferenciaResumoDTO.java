package com.sga.dto;

import java.math.BigDecimal;

public class ConferenciaResumoDTO {

	private Integer totalFaturas;
	private Integer totalComDiferenca;
	private Integer totalSemDiferenca;

	private BigDecimal somaDiferencas;
	private BigDecimal maiorDiferenca;
	private BigDecimal menorDiferenca;
	private BigDecimal mediaDiferenca;

	private Integer totalItensNota;
	private Integer totalItensFatura;
	private Integer totalFranquiasNota;
	private Integer totalFranquiasFatura;
	private Integer totalFranquiasRemovidas;

	private Integer totalAssociados;
	private BigDecimal somaValorNotas;
	private BigDecimal somaValorFaturas;

	// Getters e Setters
	public Integer getTotalFaturas() {
		return totalFaturas;
	}

	public void setTotalFaturas(Integer totalFaturas) {
		this.totalFaturas = totalFaturas;
	}

	public Integer getTotalComDiferenca() {
		return totalComDiferenca;
	}

	public void setTotalComDiferenca(Integer totalComDiferenca) {
		this.totalComDiferenca = totalComDiferenca;
	}

	public Integer getTotalSemDiferenca() {
		return totalSemDiferenca;
	}

	public void setTotalSemDiferenca(Integer totalSemDiferenca) {
		this.totalSemDiferenca = totalSemDiferenca;
	}

	public BigDecimal getSomaDiferencas() {
		return somaDiferencas;
	}

	public void setSomaDiferencas(BigDecimal somaDiferencas) {
		this.somaDiferencas = somaDiferencas;
	}

	public BigDecimal getMaiorDiferenca() {
		return maiorDiferenca;
	}

	public void setMaiorDiferenca(BigDecimal maiorDiferenca) {
		this.maiorDiferenca = maiorDiferenca;
	}

	public BigDecimal getMenorDiferenca() {
		return menorDiferenca;
	}

	public void setMenorDiferenca(BigDecimal menorDiferenca) {
		this.menorDiferenca = menorDiferenca;
	}

	public BigDecimal getMediaDiferenca() {
		return mediaDiferenca;
	}

	public void setMediaDiferenca(BigDecimal mediaDiferenca) {
		this.mediaDiferenca = mediaDiferenca;
	}

	public Integer getTotalItensNota() {
		return totalItensNota;
	}

	public void setTotalItensNota(Integer totalItensNota) {
		this.totalItensNota = totalItensNota;
	}

	public Integer getTotalItensFatura() {
		return totalItensFatura;
	}

	public void setTotalItensFatura(Integer totalItensFatura) {
		this.totalItensFatura = totalItensFatura;
	}

	public Integer getTotalFranquiasNota() {
		return totalFranquiasNota;
	}

	public void setTotalFranquiasNota(Integer totalFranquiasNota) {
		this.totalFranquiasNota = totalFranquiasNota;
	}

	public Integer getTotalFranquiasFatura() {
		return totalFranquiasFatura;
	}

	public void setTotalFranquiasFatura(Integer totalFranquiasFatura) {
		this.totalFranquiasFatura = totalFranquiasFatura;
	}

	public Integer getTotalFranquiasRemovidas() {
		return totalFranquiasRemovidas;
	}

	public void setTotalFranquiasRemovidas(Integer totalFranquiasRemovidas) {
		this.totalFranquiasRemovidas = totalFranquiasRemovidas;
	}

	public Integer getTotalAssociados() {
		return totalAssociados;
	}

	public void setTotalAssociados(Integer totalAssociados) {
		this.totalAssociados = totalAssociados;
	}

	public BigDecimal getSomaValorNotas() {
		return somaValorNotas;
	}

	public void setSomaValorNotas(BigDecimal somaValorNotas) {
		this.somaValorNotas = somaValorNotas;
	}

	public BigDecimal getSomaValorFaturas() {
		return somaValorFaturas;
	}

	public void setSomaValorFaturas(BigDecimal somaValorFaturas) {
		this.somaValorFaturas = somaValorFaturas;
	}
}