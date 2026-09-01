package com.sga.dto;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PerfilRequest {
	@NotBlank(message = "Nome do perfil é obrigatório")
	private String nome;

	private String descricao;

	@NotNull(message = "Status ativo é obrigatório")
	private Boolean ativo = true;

	private List<Long> menusIds;
	private List<Long> permissoesIds;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
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

	public List<Long> getMenusIds() {
		return menusIds;
	}

	public void setMenusIds(List<Long> menusIds) {
		this.menusIds = menusIds;
	}

	public List<Long> getPermissoesIds() {
		return permissoesIds;
	}

	public void setPermissoesIds(List<Long> permissoesIds) {
		this.permissoesIds = permissoesIds;
	}

}