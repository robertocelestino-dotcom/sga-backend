// src/main/java/com/sga/dto/RmApiIntegracaoRequest.java

package com.sga.dto;

import java.util.List;

public class RmApiIntegracaoRequest {

	private List<Long> notaIds;
	private Long configuracaoId;
	private String usuario;

	public List<Long> getNotaIds() {
		return notaIds;
	}

	public void setNotaIds(List<Long> notaIds) {
		this.notaIds = notaIds;
	}

	public Long getConfiguracaoId() {
		return configuracaoId;
	}

	public void setConfiguracaoId(Long configuracaoId) {
		this.configuracaoId = configuracaoId;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
}