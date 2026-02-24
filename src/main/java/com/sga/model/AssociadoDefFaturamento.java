package com.sga.model;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_ASSOCIADO_DEF_FATURAMENTO")
public class AssociadoDefFaturamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDASSOCIADO")
	private Associado associado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ID_PLANO")
	private Planos plano;

	@Column(name = "VALORDEF", precision = 15, scale = 2)
	private BigDecimal valorDef;

	@Column(name = "DIA_EMISSAO")
	private Integer diaEmissao;

	@Column(name = "DIA_VENCIMENTO")
	private Integer diaVencimento;

	@Lob
	@Column(name = "OBSERVACAO")
	private String observacao;

	public AssociadoDefFaturamento(Long id, Associado associado, Planos plano, BigDecimal valorDef, Integer diaEmissao,
			Integer diaVencimento, String observacao) {
		super();
		this.id = id;
		this.associado = associado;
		this.plano = plano;
		this.valorDef = valorDef;
		this.diaEmissao = diaEmissao;
		this.diaVencimento = diaVencimento;
		this.observacao = observacao;
	}

	public AssociadoDefFaturamento() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Associado getAssociado() {
		return associado;
	}

	public void setAssociado(Associado associado) {
		this.associado = associado;
	}

	public Planos getPlano() {
		return plano;
	}

	public void setPlano(Planos plano) {
		this.plano = plano;
	}

	public BigDecimal getValorDef() {
		return valorDef;
	}

	public void setValorDef(BigDecimal valorDef) {
		this.valorDef = valorDef;
	}

	public Integer getDiaEmissao() {
		return diaEmissao;
	}

	public void setDiaEmissao(Integer diaEmissao) {
		this.diaEmissao = diaEmissao;
	}

	public Integer getDiaVencimento() {
		return diaVencimento;
	}

	public void setDiaVencimento(Integer diaVencimento) {
		this.diaVencimento = diaVencimento;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssociadoDefFaturamento other = (AssociadoDefFaturamento) obj;
		return Objects.equals(id, other.id);
	}

}
