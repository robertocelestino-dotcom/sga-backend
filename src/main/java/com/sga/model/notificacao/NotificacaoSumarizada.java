// src/main/java/com/sga/model/notificacao/NotificacaoSumarizada.java
package com.sga.model.notificacao;

import java.time.LocalDate;

public class NotificacaoSumarizada {

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

	// ====== CONSTRUTORES ======
	public NotificacaoSumarizada() {
	}

	public NotificacaoSumarizada(Long idRemessa, String tipoEnvio, String competencia, LocalDate dataMovimento,
			Integer codigoAssociado, String nomeAssociado, Integer totalRegistrosDigital, Integer smsSemEnriquecimento,
			Integer smsComEnriquecimento, Integer totalSms, Integer emailsSemEnriquecimento,
			Integer emailsComEnriquecimento, Integer totalEmail, Integer cartasEnviadas, Integer naoEnviada) {
		this.idRemessa = idRemessa;
		this.tipoEnvio = tipoEnvio;
		this.competencia = competencia;
		this.dataMovimento = dataMovimento;
		this.codigoAssociado = codigoAssociado;
		this.nomeAssociado = nomeAssociado;
		this.totalRegistrosDigital = totalRegistrosDigital;
		this.smsSemEnriquecimento = smsSemEnriquecimento;
		this.smsComEnriquecimento = smsComEnriquecimento;
		this.totalSms = totalSms;
		this.emailsSemEnriquecimento = emailsSemEnriquecimento;
		this.emailsComEnriquecimento = emailsComEnriquecimento;
		this.totalEmail = totalEmail;
		this.cartasEnviadas = cartasEnviadas;
		this.naoEnviada = naoEnviada;
	}

	// ====== GETTERS E SETTERS ======
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