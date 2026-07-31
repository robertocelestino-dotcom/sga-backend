// src/main/java/com/sga/dto/RmApiIntegracaoResponse.java

package com.sga.dto;

import java.util.ArrayList;
import java.util.List;

public class RmApiIntegracaoResponse {

	private boolean sucesso;
	private String mensagem;
	private Integer totalProcessados;
	private Integer totalSucessos;
	private Integer totalErros;
	private List<ItemResponse> itens = new ArrayList<>();

	public static class ItemResponse {
		private Long notaId;
		private Long faturaId;
		private boolean sucesso;
		private Long idMov;
		private String mensagem;

		// Getters e Setters
		public Long getNotaId() {
			return notaId;
		}

		public void setNotaId(Long notaId) {
			this.notaId = notaId;
		}

		public Long getFaturaId() {
			return faturaId;
		}

		public void setFaturaId(Long faturaId) {
			this.faturaId = faturaId;
		}

		public boolean isSucesso() {
			return sucesso;
		}

		public void setSucesso(boolean sucesso) {
			this.sucesso = sucesso;
		}

		public Long getIdMov() {
			return idMov;
		}

		public void setIdMov(Long idMov) {
			this.idMov = idMov;
		}

		public String getMensagem() {
			return mensagem;
		}

		public void setMensagem(String mensagem) {
			this.mensagem = mensagem;
		}
	}

	// Getters e Setters
	public boolean isSucesso() {
		return sucesso;
	}

	public void setSucesso(boolean sucesso) {
		this.sucesso = sucesso;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public Integer getTotalProcessados() {
		return totalProcessados;
	}

	public void setTotalProcessados(Integer totalProcessados) {
		this.totalProcessados = totalProcessados;
	}

	public Integer getTotalSucessos() {
		return totalSucessos;
	}

	public void setTotalSucessos(Integer totalSucessos) {
		this.totalSucessos = totalSucessos;
	}

	public Integer getTotalErros() {
		return totalErros;
	}

	public void setTotalErros(Integer totalErros) {
		this.totalErros = totalErros;
	}

	public List<ItemResponse> getItens() {
		return itens;
	}

	public void setItens(List<ItemResponse> itens) {
		this.itens = itens;
	}
}