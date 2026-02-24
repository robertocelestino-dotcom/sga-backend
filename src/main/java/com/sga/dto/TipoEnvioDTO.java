package com.sga.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoEnvioDTO {
	
	private Long id;

	@NotNull(message = "Código é obrigatório")
	private Integer codigo;

	@NotBlank(message = "Descrição é obrigatória")
	@Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
	private String descricao;

	private Boolean ativo;

	private String observacao;

	public TipoEnvioDTO(Long id, @NotNull(message = "Código é obrigatório") Integer codigo,
			@NotBlank(message = "Descrição é obrigatória") @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres") String descricao,
			Boolean ativo, String observacao) {
		super();
		this.id = id;
		this.codigo = codigo;
		this.descricao = descricao;
		this.ativo = ativo;
		this.observacao = observacao;
	}

	public TipoEnvioDTO() {
	
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
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

}