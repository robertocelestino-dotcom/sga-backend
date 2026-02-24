package com.sga.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoEnvioResumoDTO {
	
	private Long id;
	private Integer codigo;
	private String descricao;
	private Boolean ativo;

	public TipoEnvioResumoDTO(Long id, Integer codigo, String descricao, Boolean ativo) {
		super();
		this.id = id;
		this.codigo = codigo;
		this.descricao = descricao;
		this.ativo = ativo;
	}

	public TipoEnvioResumoDTO() {

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

}