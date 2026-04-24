package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ResultadoProcessamento {

	private Long totalAssociados;
	private Long associadosProcessados;
	private Long associadosComErro;
	private Long totalNotasGeradas;
	private BigDecimal valorTotalDebito;
	private LocalDateTime dataProcessamento;
	private List<AssociadoProcessamentoDTO> detalhes = new ArrayList<>();
	private List<String> erros = new ArrayList<>();

	// Getters e Setters
	public Long getTotalAssociados() {
		return totalAssociados;
	}

	public void setTotalAssociados(Long totalAssociados) {
		this.totalAssociados = totalAssociados;
	}

	public Long getAssociadosProcessados() {
		return associadosProcessados;
	}

	public void setAssociadosProcessados(Long associadosProcessados) {
		this.associadosProcessados = associadosProcessados;
	}

	public Long getAssociadosComErro() {
		return associadosComErro;
	}

	public void setAssociadosComErro(Long associadosComErro) {
		this.associadosComErro = associadosComErro;
	}

	public Long getTotalNotasGeradas() {
		return totalNotasGeradas;
	}

	public void setTotalNotasGeradas(Long totalNotasGeradas) {
		this.totalNotasGeradas = totalNotasGeradas;
	}

	public BigDecimal getValorTotalDebito() {
		return valorTotalDebito;
	}

	public void setValorTotalDebito(BigDecimal valorTotalDebito) {
		this.valorTotalDebito = valorTotalDebito;
	}

	public LocalDateTime getDataProcessamento() {
		return dataProcessamento;
	}

	public void setDataProcessamento(LocalDateTime dataProcessamento) {
		this.dataProcessamento = dataProcessamento;
	}

	public List<AssociadoProcessamentoDTO> getDetalhes() {
		return detalhes;
	}

	public void setDetalhes(List<AssociadoProcessamentoDTO> detalhes) {
		this.detalhes = detalhes;
	}

	public List<String> getErros() {
		return erros;
	}

	public void setErros(List<String> erros) {
		this.erros = erros;
	}
	
	public void addErro(String erro) {
	    if (this.erros == null) {
	        this.erros = new ArrayList<>();
	    }
	    this.erros.add(erro);
	}
}