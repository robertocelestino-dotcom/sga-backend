// src/main/java/com/sga/model/VendedorTipo.java
package com.sga.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Entity
@Table(name = "tb_vendedor_tipo")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class VendedorTipo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "descricao", length = 100, nullable = false)
	private String descricao;

	// Construtor padrão
	public VendedorTipo() {
	}

	// Construtor com parâmetros
	public VendedorTipo(String descricao) {
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