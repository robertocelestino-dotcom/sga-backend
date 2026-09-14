package com.sga.model;

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
@Table(name = "tb_franquia_servico_coberto")
public class FranquiaServicoCoberto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "franquia_nome_base", nullable = false, length = 255)
	private String franquiaNomeBase;

	@Column(name = "servico_nome_base", nullable = false, length = 255)
	private String servicoNomeBase;

	@Column(nullable = false)
	private Integer prioridade = 0;

	@Column(nullable = false)
	private Boolean ativo = true;

	@Column(length = 500)
	private String observacao;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private LocalDateTime criadoEm;

	@Column(name = "criado_por", length = 100)
	private String criadoPor;

	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@Column(name = "atualizado_por", length = 100)
	private String atualizadoPor;

	@PrePersist
	protected void onCreate() {
		if (criadoEm == null)
			criadoEm = LocalDateTime.now();
		if (atualizadoEm == null)
			atualizadoEm = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		atualizadoEm = LocalDateTime.now();
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFranquiaNomeBase() {
		return franquiaNomeBase;
	}

	public void setFranquiaNomeBase(String franquiaNomeBase) {
		this.franquiaNomeBase = franquiaNomeBase;
	}

	public String getServicoNomeBase() {
		return servicoNomeBase;
	}

	public void setServicoNomeBase(String servicoNomeBase) {
		this.servicoNomeBase = servicoNomeBase;
	}

	public Integer getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(Integer prioridade) {
		this.prioridade = prioridade;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
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

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public String getAtualizadoPor() {
		return atualizadoPor;
	}

	public void setAtualizadoPor(String atualizadoPor) {
		this.atualizadoPor = atualizadoPor;
	}
}