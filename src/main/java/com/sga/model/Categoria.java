package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "TB_CATEGORIA")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "DESCRICAO", nullable = false, length = 100)
	private String descricao;

	// ========== NOVOS CAMPOS PARA FATURAMENTO MÍNIMO ==========

	@Column(name = "FATURAMENTO_MINIMO")
	private Boolean faturamentoMinimo = false;

	@Column(name = "VALOR_MINIMO", precision = 15, scale = 2)
	private BigDecimal valorMinimo = BigDecimal.ZERO;

	@Column(name = "CODIGO", length = 50)
	private String codigo;

	@Column(name = "ATIVO")
	private Boolean ativo = true;

	@Column(name = "OBSERVACAO", length = 500)
	private String observacao;

	// ========== CAMPOS DE AUDITORIA ==========

	@CreationTimestamp
	@Column(name = "CRIADO_EM", updatable = false)
	private LocalDateTime criadoEm;

	@UpdateTimestamp
	@Column(name = "ATUALIZADO_EM")
	private LocalDateTime atualizadoEm;

	@Column(name = "CRIADO_POR", length = 100)
	private String criadoPor;

	@Column(name = "ATUALIZADO_POR", length = 100)
	private String atualizadoPor;

	// ========== CONSTRUTORES ==========

	public Categoria() {
		this.faturamentoMinimo = false;
		this.ativo = true;
		this.valorMinimo = BigDecimal.ZERO;
	}

	public Categoria(String descricao) {
		this();
		this.descricao = descricao;
		this.codigo = "CAT-" + System.currentTimeMillis();
	}

	public Categoria(String descricao, Boolean faturamentoMinimo, BigDecimal valorMinimo) {
		this(descricao);
		this.faturamentoMinimo = faturamentoMinimo != null ? faturamentoMinimo : false;
		this.valorMinimo = valorMinimo != null ? valorMinimo : BigDecimal.ZERO;
	}

	// ========== GETTERS E SETTERS ==========

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	// 🔥 GETTER E SETTER PARA faturamentoMinimo
	public Boolean getFaturamentoMinimo() {
		return faturamentoMinimo != null ? faturamentoMinimo : false;
	}

	public void setFaturamentoMinimo(Boolean faturamentoMinimo) {
		this.faturamentoMinimo = faturamentoMinimo;
	}

	public BigDecimal getValorMinimo() {
		return valorMinimo != null ? valorMinimo : BigDecimal.ZERO;
	}

	public void setValorMinimo(BigDecimal valorMinimo) {
		this.valorMinimo = valorMinimo;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Boolean getAtivo() {
		return ativo != null ? ativo : true;
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

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public String getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(String criadoPor) {
		this.criadoPor = criadoPor;
	}

	public String getAtualizadoPor() {
		return atualizadoPor;
	}

	public void setAtualizadoPor(String atualizadoPor) {
		this.atualizadoPor = atualizadoPor;
	}

	// ========== MÉTODOS AUXILIARES ==========

	@PrePersist
	protected void onCreate() {
		criadoEm = LocalDateTime.now();
		if (faturamentoMinimo == null)
			faturamentoMinimo = false;
		if (ativo == null)
			ativo = true;
		if (valorMinimo == null)
			valorMinimo = BigDecimal.ZERO;
	}

	@PreUpdate
	protected void onUpdate() {
		atualizadoEm = LocalDateTime.now();
	}

	/**
	 * Verifica se a categoria tem faturamento mínimo configurado
	 */
	public boolean temFaturamentoMinimo() {
		return getFaturamentoMinimo() && getValorMinimo().compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * Retorna o valor mínimo para faturamento
	 */
	public BigDecimal getValorMinimoFaturamento() {
		return temFaturamentoMinimo() ? getValorMinimo() : BigDecimal.ZERO;
	}

	/**
	 * Verifica se a categoria está ativa
	 */
	public boolean isAtivo() {
		return getAtivo();
	}

	// ========== hashCode e equals ==========

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Categoria categoria = (Categoria) o;
		return id != null && id.equals(categoria.id);
	}

	@Override
	public String toString() {
		return "Categoria{" + "id=" + id + ", descricao='" + descricao + '\'' + ", faturamentoMinimo="
				+ faturamentoMinimo + ", valorMinimo=" + valorMinimo + ", ativo=" + ativo + '}';
	}
}