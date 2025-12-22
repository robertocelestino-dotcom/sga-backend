// src/main/java/com/sga/dto/EmailDTO.java
package com.sga.dto;

import lombok.Data;

@Data
public class EmailDTO {
	private Long id;
	private String email;
	private String tipoEmail; // "PESSOAL", "COMERCIAL", "COBRANCA"
	private Boolean ativo = true;

	public EmailDTO() {
	
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTipoEmail() {
		return tipoEmail;
	}

	public void setTipoEmail(String tipoEmail) {
		this.tipoEmail = tipoEmail;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

}