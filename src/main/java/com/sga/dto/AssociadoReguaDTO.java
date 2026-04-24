package com.sga.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssociadoReguaDTO {

	private Long id;
	private Long associadoId;
	private String associadoNome;
	private String associadoCodigoSpc;
	private Long reguaId;
	private String reguaNome;
	private LocalDate dataInicio;
	private LocalDate dataFim;
	private Boolean ativo;
	private String motivoMigracao;
	private String observacao;
	private LocalDateTime criadoEm;
	private String criadoPor;

	// Construtores
	public AssociadoReguaDTO() {
	}

	public AssociadoReguaDTO(Long id, Long associadoId, String associadoNome, String associadoCodigoSpc, Long reguaId,
			String reguaNome, LocalDate dataInicio, LocalDate dataFim, Boolean ativo, String motivoMigracao,
			String observacao, LocalDateTime criadoEm, String criadoPor) {
		this.id = id;
		this.associadoId = associadoId;
		this.associadoNome = associadoNome;
		this.associadoCodigoSpc = associadoCodigoSpc;
		this.reguaId = reguaId;
		this.reguaNome = reguaNome;
		this.dataInicio = dataInicio;
		this.dataFim = dataFim;
		this.ativo = ativo;
		this.motivoMigracao = motivoMigracao;
		this.observacao = observacao;
		this.criadoEm = criadoEm;
		this.criadoPor = criadoPor;
	}

	// Getters e Setters
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

	public String getAssociadoNome() {
		return associadoNome;
	}

	public void setAssociadoNome(String associadoNome) {
		this.associadoNome = associadoNome;
	}

	public String getAssociadoCodigoSpc() {
		return associadoCodigoSpc;
	}

	public void setAssociadoCodigoSpc(String associadoCodigoSpc) {
		this.associadoCodigoSpc = associadoCodigoSpc;
	}

	public Long getReguaId() {
		return reguaId;
	}

	public void setReguaId(Long reguaId) {
		this.reguaId = reguaId;
	}

	public String getReguaNome() {
		return reguaNome;
	}

	public void setReguaNome(String reguaNome) {
		this.reguaNome = reguaNome;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDate getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getMotivoMigracao() {
		return motivoMigracao;
	}

	public void setMotivoMigracao(String motivoMigracao) {
		this.motivoMigracao = motivoMigracao;
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

	public String getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(String criadoPor) {
		this.criadoPor = criadoPor;
	}
}