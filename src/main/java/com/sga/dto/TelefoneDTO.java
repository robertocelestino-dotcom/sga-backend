// src/main/java/com/sga/dto/TelefoneDTO.java
package com.sga.dto;

import lombok.Data;

@Data
public class TelefoneDTO {
	private Long id;
	private String ddd;
	private String numero;
	private String tipoTelefone; // "CELULAR", "COMERCIAL", "RESIDENCIAL", "FAX"
	private Boolean whatsapp = false;
	private Boolean ativo = true;

	public TelefoneDTO() {
	
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDdd() {
		return ddd;
	}

	public void setDdd(String ddd) {
		this.ddd = ddd;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public String getTipoTelefone() {
		return tipoTelefone;
	}

	public void setTipoTelefone(String tipoTelefone) {
		this.tipoTelefone = tipoTelefone;
	}

	public Boolean getWhatsapp() {
		return whatsapp;
	}

	public void setWhatsapp(Boolean whatsapp) {
		this.whatsapp = whatsapp;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

}