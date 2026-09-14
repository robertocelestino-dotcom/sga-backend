package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ConferenciaFaturamentoDTO {

	// Identificação da Fatura
	private Long faturaId;
	private String numeroFatura;
	private LocalDate dataEmissao;
	private String statusFatura;

	// Identificação da Nota
	private Long notaId;
	private String numeroNotaDebito;
	private LocalDate dataVencimento;

	// Associado
	private Long associadoId;
	private String codigoSpc;
	private String codigoRm;
	private String nomeRazao;

	// Valores
	private BigDecimal valorNota;
	private BigDecimal valorFatura;
	private BigDecimal diferencaValor;

	// Quantidades
	private Integer qtdItensNota;
	private Integer qtdItensFatura;
	private Integer diferencaItens;

	// Franquias
	private Integer qtdFranquiasNota;
	private Integer qtdFranquiasFatura;
	private Integer diferencaFranquias;

	// Status da Conferência
	private String statusConferencia; // OK, DIFERENCA, ERRO, ATENCAO
	private String observacao;
	private String tipoArquivo;

	// Getters e Setters
	public Long getFaturaId() {
		return faturaId;
	}

	public void setFaturaId(Long faturaId) {
		this.faturaId = faturaId;
	}

	public String getNumeroFatura() {
		return numeroFatura;
	}

	public void setNumeroFatura(String numeroFatura) {
		this.numeroFatura = numeroFatura;
	}

	public LocalDate getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(LocalDate dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	public String getStatusFatura() {
		return statusFatura;
	}

	public void setStatusFatura(String statusFatura) {
		this.statusFatura = statusFatura;
	}

	public Long getNotaId() {
		return notaId;
	}

	public void setNotaId(Long notaId) {
		this.notaId = notaId;
	}

	public String getNumeroNotaDebito() {
		return numeroNotaDebito;
	}

	public void setNumeroNotaDebito(String numeroNotaDebito) {
		this.numeroNotaDebito = numeroNotaDebito;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public Long getAssociadoId() {
		return associadoId;
	}

	public void setAssociadoId(Long associadoId) {
		this.associadoId = associadoId;
	}

	public String getCodigoSpc() {
		return codigoSpc;
	}

	public void setCodigoSpc(String codigoSpc) {
		this.codigoSpc = codigoSpc;
	}

	public String getCodigoRm() {
		return codigoRm;
	}

	public void setCodigoRm(String codigoRm) {
		this.codigoRm = codigoRm;
	}

	public String getNomeRazao() {
		return nomeRazao;
	}

	public void setNomeRazao(String nomeRazao) {
		this.nomeRazao = nomeRazao;
	}

	public BigDecimal getValorNota() {
		return valorNota;
	}

	public void setValorNota(BigDecimal valorNota) {
		this.valorNota = valorNota;
	}

	public BigDecimal getValorFatura() {
		return valorFatura;
	}

	public void setValorFatura(BigDecimal valorFatura) {
		this.valorFatura = valorFatura;
	}

	public BigDecimal getDiferencaValor() {
		return diferencaValor;
	}

	public void setDiferencaValor(BigDecimal diferencaValor) {
		this.diferencaValor = diferencaValor;
	}

	public Integer getQtdItensNota() {
		return qtdItensNota;
	}

	public void setQtdItensNota(Integer qtdItensNota) {
		this.qtdItensNota = qtdItensNota;
	}

	public Integer getQtdItensFatura() {
		return qtdItensFatura;
	}

	public void setQtdItensFatura(Integer qtdItensFatura) {
		this.qtdItensFatura = qtdItensFatura;
	}

	public Integer getDiferencaItens() {
		return diferencaItens;
	}

	public void setDiferencaItens(Integer diferencaItens) {
		this.diferencaItens = diferencaItens;
	}

	public Integer getQtdFranquiasNota() {
		return qtdFranquiasNota;
	}

	public void setQtdFranquiasNota(Integer qtdFranquiasNota) {
		this.qtdFranquiasNota = qtdFranquiasNota;
	}

	public Integer getQtdFranquiasFatura() {
		return qtdFranquiasFatura;
	}

	public void setQtdFranquiasFatura(Integer qtdFranquiasFatura) {
		this.qtdFranquiasFatura = qtdFranquiasFatura;
	}

	public Integer getDiferencaFranquias() {
		return diferencaFranquias;
	}

	public void setDiferencaFranquias(Integer diferencaFranquias) {
		this.diferencaFranquias = diferencaFranquias;
	}

	public String getStatusConferencia() {
		return statusConferencia;
	}

	public void setStatusConferencia(String statusConferencia) {
		this.statusConferencia = statusConferencia;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getTipoArquivo() {
		return tipoArquivo;
	}

	public void setTipoArquivo(String tipoArquivo) {
		this.tipoArquivo = tipoArquivo;
	}
}
