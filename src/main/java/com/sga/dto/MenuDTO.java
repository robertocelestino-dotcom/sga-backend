package com.sga.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuDTO {
	private Long id;
	private String nome;
	private String caminho;
	private String icone;
	private Integer ordem;
	private Boolean ativo;
	private Long menuPaiId;
	private String menuPaiNome;
	private List<MenuDTO> subMenus;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCaminho() {
		return caminho;
	}

	public void setCaminho(String caminho) {
		this.caminho = caminho;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	public Integer getOrdem() {
		return ordem;
	}

	public void setOrdem(Integer ordem) {
		this.ordem = ordem;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Long getMenuPaiId() {
		return menuPaiId;
	}

	public void setMenuPaiId(Long menuPaiId) {
		this.menuPaiId = menuPaiId;
	}

	public String getMenuPaiNome() {
		return menuPaiNome;
	}

	public void setMenuPaiNome(String menuPaiNome) {
		this.menuPaiNome = menuPaiNome;
	}

	public List<MenuDTO> getSubMenus() {
		return subMenus;
	}

	public void setSubMenus(List<MenuDTO> subMenus) {
		this.subMenus = subMenus;
	}

}