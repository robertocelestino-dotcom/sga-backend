// src/main/java/com/sga/model/NotificacaoAssociado.java
package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "notificacao_associado")
public class NotificacaoAssociado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "associado_id", nullable = false)
	private Long associadoId;

	@Column(name = "regua_id")
	private Long reguaId;

	@Column(name = "mes_referencia", nullable = false)
	private Integer mesReferencia;

	@Column(name = "ano_referencia", nullable = false)
	private Integer anoReferencia;

	@Column(name = "periodo_inicio")
	private LocalDate periodoInicio;

	@Column(name = "periodo_fim")
	private LocalDate periodoFim;

	@Column(name = "codigo_spc")
	private String codigoSpc;

	@Column(name = "total_registros")
	private Integer totalRegistros = 0;

	@Column(name = "sms_sem_enriquecimento")
	private Integer smsSemEnriquecimento = 0;

	@Column(name = "sms_com_enriquecimento")
	private Integer smsComEnriquecimento = 0;

	@Column(name = "sms_total")
	private Integer smsTotal = 0;

	@Column(name = "emails_sem_enriquecimento")
	private Integer emailsSemEnriquecimento = 0;

	@Column(name = "emails_com_enriquecimento")
	private Integer emailsComEnriquecimento = 0;

	@Column(name = "emails_total")
	private Integer emailsTotal = 0;

	@Column(name = "cartas_total")
	private Integer cartasTotal = 0;

	@Column(name = "nao_enviadas")
	private Integer naoEnviadas = 0;

	@Column(name = "valor_unitario_sms_sem_enriquecimento")
	private BigDecimal valorUnitarioSmsSemEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_unitario_sms_com_enriquecimento")
	private BigDecimal valorUnitarioSmsComEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_unitario_email_sem_enriquecimento")
	private BigDecimal valorUnitarioEmailSemEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_unitario_email_com_enriquecimento")
	private BigDecimal valorUnitarioEmailComEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_unitario_carta")
	private BigDecimal valorUnitarioCarta = BigDecimal.ZERO;

	@Column(name = "valor_total_sms_sem_enriquecimento")
	private BigDecimal valorTotalSmsSemEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_total_sms_com_enriquecimento")
	private BigDecimal valorTotalSmsComEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_total_emails_sem_enriquecimento")
	private BigDecimal valorTotalEmailsSemEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_total_emails_com_enriquecimento")
	private BigDecimal valorTotalEmailsComEnriquecimento = BigDecimal.ZERO;

	@Column(name = "valor_total_cartas")
	private BigDecimal valorTotalCartas = BigDecimal.ZERO;

	@Column(name = "valor_total")
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@Column(name = "processado_fatura")
	private Boolean processadoFatura = false;

	@Column(name = "fatura_id")
	private Long faturaId;

	@Column(name = "data_processamento")
	private LocalDateTime dataProcessamento;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@PrePersist
	protected void onCreate() {
		this.criadoEm = LocalDateTime.now();
		this.atualizadoEm = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		this.atualizadoEm = LocalDateTime.now();
	}

	// ====== GETTERS E SETTERS ======
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

	public BigDecimal getValorUnitarioSmsSemEnriquecimento() {
		return valorUnitarioSmsSemEnriquecimento;
	}

	public void setValorUnitarioSmsSemEnriquecimento(BigDecimal valorUnitarioSmsSemEnriquecimento) {
		this.valorUnitarioSmsSemEnriquecimento = valorUnitarioSmsSemEnriquecimento;
	}

	public BigDecimal getValorUnitarioSmsComEnriquecimento() {
		return valorUnitarioSmsComEnriquecimento;
	}

	public void setValorUnitarioSmsComEnriquecimento(BigDecimal valorUnitarioSmsComEnriquecimento) {
		this.valorUnitarioSmsComEnriquecimento = valorUnitarioSmsComEnriquecimento;
	}

	public BigDecimal getValorUnitarioEmailSemEnriquecimento() {
		return valorUnitarioEmailSemEnriquecimento;
	}

	public void setValorUnitarioEmailSemEnriquecimento(BigDecimal valorUnitarioEmailSemEnriquecimento) {
		this.valorUnitarioEmailSemEnriquecimento = valorUnitarioEmailSemEnriquecimento;
	}

	public BigDecimal getValorUnitarioEmailComEnriquecimento() {
		return valorUnitarioEmailComEnriquecimento;
	}

	public void setValorUnitarioEmailComEnriquecimento(BigDecimal valorUnitarioEmailComEnriquecimento) {
		this.valorUnitarioEmailComEnriquecimento = valorUnitarioEmailComEnriquecimento;
	}

	public BigDecimal getValorUnitarioCarta() {
		return valorUnitarioCarta;
	}

	public void setValorUnitarioCarta(BigDecimal valorUnitarioCarta) {
		this.valorUnitarioCarta = valorUnitarioCarta;
	}

	public BigDecimal getValorTotalSmsSemEnriquecimento() {
		return valorTotalSmsSemEnriquecimento;
	}

	public void setValorTotalSmsSemEnriquecimento(BigDecimal valorTotalSmsSemEnriquecimento) {
		this.valorTotalSmsSemEnriquecimento = valorTotalSmsSemEnriquecimento;
	}

	public BigDecimal getValorTotalSmsComEnriquecimento() {
		return valorTotalSmsComEnriquecimento;
	}

	public void setValorTotalSmsComEnriquecimento(BigDecimal valorTotalSmsComEnriquecimento) {
		this.valorTotalSmsComEnriquecimento = valorTotalSmsComEnriquecimento;
	}

	public BigDecimal getValorTotalEmailsSemEnriquecimento() {
		return valorTotalEmailsSemEnriquecimento;
	}

	public void setValorTotalEmailsSemEnriquecimento(BigDecimal valorTotalEmailsSemEnriquecimento) {
		this.valorTotalEmailsSemEnriquecimento = valorTotalEmailsSemEnriquecimento;
	}

	public BigDecimal getValorTotalEmailsComEnriquecimento() {
		return valorTotalEmailsComEnriquecimento;
	}

	public void setValorTotalEmailsComEnriquecimento(BigDecimal valorTotalEmailsComEnriquecimento) {
		this.valorTotalEmailsComEnriquecimento = valorTotalEmailsComEnriquecimento;
	}

	public BigDecimal getValorTotalCartas() {
		return valorTotalCartas;
	}

	public void setValorTotalCartas(BigDecimal valorTotalCartas) {
		this.valorTotalCartas = valorTotalCartas;
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

	public LocalDateTime getDataProcessamento() {
		return dataProcessamento;
	}

	public void setDataProcessamento(LocalDateTime dataProcessamento) {
		this.dataProcessamento = dataProcessamento;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	// ====== MÉTODOS DE CÁLCULO ======
	public void calcularTotais() {
		this.smsTotal = (smsSemEnriquecimento != null ? smsSemEnriquecimento : 0)
				+ (smsComEnriquecimento != null ? smsComEnriquecimento : 0);
		this.emailsTotal = (emailsSemEnriquecimento != null ? emailsSemEnriquecimento : 0)
				+ (emailsComEnriquecimento != null ? emailsComEnriquecimento : 0);
		this.totalRegistros = (smsTotal != null ? smsTotal : 0) + (emailsTotal != null ? emailsTotal : 0)
				+ (cartasTotal != null ? cartasTotal : 0) + (naoEnviadas != null ? naoEnviadas : 0);
		calcularValoresTotais();
	}

	public void calcularValoresTotais() {
		BigDecimal smsSem = BigDecimal.valueOf(smsSemEnriquecimento != null ? smsSemEnriquecimento : 0).multiply(
				valorUnitarioSmsSemEnriquecimento != null ? valorUnitarioSmsSemEnriquecimento : BigDecimal.ZERO);
		BigDecimal smsCom = BigDecimal.valueOf(smsComEnriquecimento != null ? smsComEnriquecimento : 0).multiply(
				valorUnitarioSmsComEnriquecimento != null ? valorUnitarioSmsComEnriquecimento : BigDecimal.ZERO);
		BigDecimal emailSem = BigDecimal.valueOf(emailsSemEnriquecimento != null ? emailsSemEnriquecimento : 0)
				.multiply(valorUnitarioEmailSemEnriquecimento != null ? valorUnitarioEmailSemEnriquecimento
						: BigDecimal.ZERO);
		BigDecimal emailCom = BigDecimal.valueOf(emailsComEnriquecimento != null ? emailsComEnriquecimento : 0)
				.multiply(valorUnitarioEmailComEnriquecimento != null ? valorUnitarioEmailComEnriquecimento
						: BigDecimal.ZERO);
		BigDecimal cartas = BigDecimal.valueOf(cartasTotal != null ? cartasTotal : 0)
				.multiply(valorUnitarioCarta != null ? valorUnitarioCarta : BigDecimal.ZERO);

		this.valorTotalSmsSemEnriquecimento = smsSem;
		this.valorTotalSmsComEnriquecimento = smsCom;
		this.valorTotalEmailsSemEnriquecimento = emailSem;
		this.valorTotalEmailsComEnriquecimento = emailCom;
		this.valorTotalCartas = cartas;

		this.valorTotal = smsSem.add(smsCom).add(emailSem).add(emailCom).add(cartas);
	}
}