package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_alerta_franquia_sem_cobertura")
public class AlertaFranquiaSemCobertura {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "fatura_id")
	private Long faturaId;

	@Column(name = "associado_id", nullable = false)
	private Long associadoId;

	@Column(name = "codigo_spc", length = 50)
	private String codigoSpc;

	@Column(name = "franquia_descricao", nullable = false, length = 500)
	private String franquiaDescricao;

	@Column(name = "franquia_nome_base", length = 255)
	private String franquiaNomeBase;

	@Column(name = "itens_potencialmente_cobertos", columnDefinition = "TEXT")
	private String itensPotencialmenteCobertos;

	@Column(name = "valor_potencial_afetado", precision = 15, scale = 2)
	private BigDecimal valorPotencialAfetado;

	@Column(nullable = false, length = 20)
	private String status = "PENDENTE";

	@Column(columnDefinition = "TEXT")
	private String observacao;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm = LocalDateTime.now();

	@Column(name = "resolvido_em")
	private LocalDateTime resolvidoEm;

	@Column(name = "resolvido_por", length = 100)
	private String resolvidoPor;

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getFaturaId() {
		return faturaId;
	}

	public void setFaturaId(Long faturaId) {
		this.faturaId = faturaId;
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

	public String getFranquiaDescricao() {
		return franquiaDescricao;
	}

	public void setFranquiaDescricao(String franquiaDescricao) {
		this.franquiaDescricao = franquiaDescricao;
	}

	public String getFranquiaNomeBase() {
		return franquiaNomeBase;
	}

	public void setFranquiaNomeBase(String franquiaNomeBase) {
		this.franquiaNomeBase = franquiaNomeBase;
	}

	public String getItensPotencialmenteCobertos() {
		return itensPotencialmenteCobertos;
	}

	public void setItensPotencialmenteCobertos(String itensPotencialmenteCobertos) {
		this.itensPotencialmenteCobertos = itensPotencialmenteCobertos;
	}

	public BigDecimal getValorPotencialAfetado() {
		return valorPotencialAfetado;
	}

	public void setValorPotencialAfetado(BigDecimal valorPotencialAfetado) {
		this.valorPotencialAfetado = valorPotencialAfetado;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getResolvidoEm() {
		return resolvidoEm;
	}

	public void setResolvidoEm(LocalDateTime resolvidoEm) {
		this.resolvidoEm = resolvidoEm;
	}

	public String getResolvidoPor() {
		return resolvidoPor;
	}

	public void setResolvidoPor(String resolvidoPor) {
		this.resolvidoPor = resolvidoPor;
	}
}