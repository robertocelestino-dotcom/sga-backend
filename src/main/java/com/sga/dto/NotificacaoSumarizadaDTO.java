// src/main/java/com/sga/dto/NotificacaoSumarizadaDTO.java
package com.sga.dto;

import java.time.LocalDate;

public class NotificacaoSumarizadaDTO {
	private Long idRemessa;
	private String tipoEnvio;
	private String competencia;
	private LocalDate dataMovimento;
	private Integer codigoAssociado;
	private String nomeAssociado;
	private Integer totalRegistrosDigital;
	private Integer smsSemEnriquecimento;
	private Integer smsComEnriquecimento;
	private Integer totalSms;
	private Integer emailsSemEnriquecimento;
	private Integer emailsComEnriquecimento;
	private Integer totalEmail;
	private Integer cartasEnviadas;
	private Integer naoEnviada;

	// Construtores
	public NotificacaoSumarizadaDTO() {
	}

	// Getters e Setters
	public Long getIdRemessa() {
		return idRemessa;
	}

	public void setIdRemessa(Long idRemessa) {
		this.idRemessa = idRemessa;
	}

	public String getTipoEnvio() {
		return tipoEnvio;
	}

	public void setTipoEnvio(String tipoEnvio) {
		this.tipoEnvio = tipoEnvio;
	}

	public String getCompetencia() {
		return competencia;
	}

	public void setCompetencia(String competencia) {
		this.competencia = competencia;
	}

	public LocalDate getDataMovimento() {
		return dataMovimento;
	}

	public void setDataMovimento(LocalDate dataMovimento) {
		this.dataMovimento = dataMovimento;
	}

	public Integer getCodigoAssociado() {
		return codigoAssociado;
	}

	public void setCodigoAssociado(Integer codigoAssociado) {
		this.codigoAssociado = codigoAssociado;
	}

	public String getNomeAssociado() {
		return nomeAssociado;
	}

	public void setNomeAssociado(String nomeAssociado) {
		this.nomeAssociado = nomeAssociado;
	}

	public Integer getTotalRegistrosDigital() {
		return totalRegistrosDigital;
	}

	public void setTotalRegistrosDigital(Integer totalRegistrosDigital) {
		this.totalRegistrosDigital = totalRegistrosDigital;
	}

	public Integer getSmsSemEnriquecimento() {
		return smsSemEnriquecimento;
	}

	public void setSmsSemEnriquecimento(Integer smsSemEnriquecimento) {
		this.smsSemEnriquecimento = smsSemEnriquecimento;
	}

	public Integer getSmsComEnriquecimento() {
		return smsComEnriquecimento;
	}

	public void setSmsComEnriquecimento(Integer smsComEnriquecimento) {
		this.smsComEnriquecimento = smsComEnriquecimento;
	}

	public Integer getTotalSms() {
		return totalSms;
	}

	public void setTotalSms(Integer totalSms) {
		this.totalSms = totalSms;
	}

	public Integer getEmailsSemEnriquecimento() {
		return emailsSemEnriquecimento;
	}

	public void setEmailsSemEnriquecimento(Integer emailsSemEnriquecimento) {
		this.emailsSemEnriquecimento = emailsSemEnriquecimento;
	}

	public Integer getEmailsComEnriquecimento() {
		return emailsComEnriquecimento;
	}

	public void setEmailsComEnriquecimento(Integer emailsComEnriquecimento) {
		this.emailsComEnriquecimento = emailsComEnriquecimento;
	}

	public Integer getTotalEmail() {
		return totalEmail;
	}

	public void setTotalEmail(Integer totalEmail) {
		this.totalEmail = totalEmail;
	}

	public Integer getCartasEnviadas() {
		return cartasEnviadas;
	}

	public void setCartasEnviadas(Integer cartasEnviadas) {
		this.cartasEnviadas = cartasEnviadas;
	}

	public Integer getNaoEnviada() {
		return naoEnviada;
	}

	public void setNaoEnviada(Integer naoEnviada) {
		this.naoEnviada = naoEnviada;
	}
}