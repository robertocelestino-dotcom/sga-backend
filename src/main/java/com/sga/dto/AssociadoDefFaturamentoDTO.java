package com.sga.dto;

import java.math.BigDecimal;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociadoDefFaturamentoDTO {

	private Long id;

	@NotNull(message = "ID do Associado é obrigatório")
	private Long associadoId;

	private Long planoId;

	private BigDecimal valorDef;

	@NotNull(message = "Dia de emissão é obrigatório")
	@Min(value = 1, message = "Dia de emissão deve ser entre 1 e 31")
	@Max(value = 31, message = "Dia de emissão deve ser entre 1 e 31")
	private Integer diaEmissao;

	@NotNull(message = "Dia de vencimento é obrigatório")
	@Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 31")
	@Max(value = 31, message = "Dia de vencimento deve ser entre 1 e 31")
	private Integer diaVencimento;

	private String observacao;

	private String usuario;

	public AssociadoDefFaturamentoDTO(Long id, @NotNull(message = "ID do Associado é obrigatório") Long associadoId,
			Long planoId, BigDecimal valorDef,
			@NotNull(message = "Dia de emissão é obrigatório") @Min(value = 1, message = "Dia de emissão deve ser entre 1 e 31") @Max(value = 31, message = "Dia de emissão deve ser entre 1 e 31") Integer diaEmissao,
			@NotNull(message = "Dia de vencimento é obrigatório") @Min(value = 1, message = "Dia de vencimento deve ser entre 1 e 31") @Max(value = 31, message = "Dia de vencimento deve ser entre 1 e 31") Integer diaVencimento,
			String observacao, String usuario) {
		super();
		this.id = id;
		this.associadoId = associadoId;
		this.planoId = planoId;
		this.valorDef = valorDef;
		this.diaEmissao = diaEmissao;
		this.diaVencimento = diaVencimento;
		this.observacao = observacao;
		this.usuario = usuario;
	}

	public AssociadoDefFaturamentoDTO() {

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

	public Long getPlanoId() {
		return planoId;
	}

	public void setPlanoId(Long planoId) {
		this.planoId = planoId;
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

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

}