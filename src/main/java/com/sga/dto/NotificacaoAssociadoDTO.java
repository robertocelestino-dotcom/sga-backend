// src/main/java/com/sga/dto/NotificacaoAssociadoDTO.java
package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificacaoAssociadoDTO {
	private Long id;
	private Long associadoId;
	private Long reguaId;
	private Integer mesReferencia;
	private Integer anoReferencia;
	private LocalDate periodoInicio;
	private LocalDate periodoFim;
	private String codigoSpc;
	private Integer totalRegistros;
	private Integer smsSemEnriquecimento;
	private Integer smsComEnriquecimento;
	private Integer smsTotal;
	private Integer emailsSemEnriquecimento;
	private Integer emailsComEnriquecimento;
	private Integer emailsTotal;
	private Integer cartasTotal;
	private Integer naoEnviadas;
	private BigDecimal valorTotal;
	private Boolean processadoFatura;
	private Long faturaId;

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

	public Long getReguaId() {
		return reguaId;
	}

	public void setReguaId(Long reguaId) {
		this.reguaId = reguaId;
	}

	public Integer getMesReferencia() {
		return mesReferencia;
	}

	public void setMesReferencia(Integer mesReferencia) {
		this.mesReferencia = mesReferencia;
	}

	public Integer getAnoReferencia() {
		return anoReferencia;
	}

	public void setAnoReferencia(Integer anoReferencia) {
		this.anoReferencia = anoReferencia;
	}

	public LocalDate getPeriodoInicio() {
		return periodoInicio;
	}

	public void setPeriodoInicio(LocalDate periodoInicio) {
		this.periodoInicio = periodoInicio;
	}

	public LocalDate getPeriodoFim() {
		return periodoFim;
	}

	public void setPeriodoFim(LocalDate periodoFim) {
		this.periodoFim = periodoFim;
	}

	public String getCodigoSpc() {
		return codigoSpc;
	}

	public void setCodigoSpc(String codigoSpc) {
		this.codigoSpc = codigoSpc;
	}

	public Integer getTotalRegistros() {
		return totalRegistros;
	}

	public void setTotalRegistros(Integer totalRegistros) {
		this.totalRegistros = totalRegistros;
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

	public Integer getSmsTotal() {
		return smsTotal;
	}

	public void setSmsTotal(Integer smsTotal) {
		this.smsTotal = smsTotal;
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

	public Integer getEmailsTotal() {
		return emailsTotal;
	}

	public void setEmailsTotal(Integer emailsTotal) {
		this.emailsTotal = emailsTotal;
	}

	public Integer getCartasTotal() {
		return cartasTotal;
	}

	public void setCartasTotal(Integer cartasTotal) {
		this.cartasTotal = cartasTotal;
	}

	public Integer getNaoEnviadas() {
		return naoEnviadas;
	}

	public void setNaoEnviadas(Integer naoEnviadas) {
		this.naoEnviadas = naoEnviadas;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public Boolean getProcessadoFatura() {
		return processadoFatura;
	}

	public void setProcessadoFatura(Boolean processadoFatura) {
		this.processadoFatura = processadoFatura;
	}

	public Long getFaturaId() {
		return faturaId;
	}

	public void setFaturaId(Long faturaId) {
		this.faturaId = faturaId;
	}

}