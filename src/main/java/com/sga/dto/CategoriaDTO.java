// src/main/java/com/sga/dto/CategoriaDTO.java
package com.sga.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CategoriaDTO {

	private Long id;
	private String descricao;
	private String tipo; // ASSOCIADO, PRODUTO, GERAL
	private String status; // ATIVO, INATIVO
	private Integer nivel;
	private Long subcategoriaDe;
	private String corHex;
	private String icone;
	private String observacoes;
	private LocalDateTime dataCadastro;

	public CategoriaDTO() {

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

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}

	public Long getSubcategoriaDe() {
		return subcategoriaDe;
	}

	public void setSubcategoriaDe(Long subcategoriaDe) {
		this.subcategoriaDe = subcategoriaDe;
	}

	public String getCorHex() {
		return corHex;
	}

	public void setCorHex(String corHex) {
		this.corHex = corHex;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

}
