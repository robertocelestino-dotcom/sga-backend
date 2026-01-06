// src/main/java/com/sga/model/VendedorTipo.java
package com.sga.model;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "tb_vendedor_tipo")
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