// src/main/java/com/sga/dto/VendedorTipoDTO.java
package com.sga.dto;

import lombok.Data;

@Data
public class VendedorTipoDTO {
	private Long id;
	private String descricao;

	// Construtor padrão
	public VendedorTipoDTO() {
	}

	// Construtor com parâmetros
	public VendedorTipoDTO(Long id, String descricao) {
		this.id = id;
		this.descricao = descricao;
	}

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

}