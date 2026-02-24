package com.sga.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociadoDefFaturamentoResumoDTO {

	private Long id;
	private Long associadoId;
	private String associadoNome;
	private Long planoId;
	private String planoNome;
	private BigDecimal valorDef;
	private Integer diaEmissao;
	private Integer diaVencimento;
	private String observacao;

	public AssociadoDefFaturamentoResumoDTO(Long id, Long associadoId, String associadoNome, Long planoId,
			String planoNome, BigDecimal valorDef, Integer diaEmissao, Integer diaVencimento, String observacao) {
		super();
		this.id = id;
		this.associadoId = associadoId;
		this.associadoNome = associadoNome;
		this.planoId = planoId;
		this.planoNome = planoNome;
		this.valorDef = valorDef;
		this.diaEmissao = diaEmissao;
		this.diaVencimento = diaVencimento;
		this.observacao = observacao;
	}

	public AssociadoDefFaturamentoResumoDTO() {

	}

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

	public Long getPlanoId() {
		return planoId;
	}

	public void setPlanoId(Long planoId) {
		this.planoId = planoId;
	}

	public String getPlanoNome() {
		return planoNome;
	}

	public void setPlanoNome(String planoNome) {
		this.planoNome = planoNome;
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

}