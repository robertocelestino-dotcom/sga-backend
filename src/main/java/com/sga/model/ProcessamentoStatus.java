// src/main/java/com/sga/model/ProcessamentoStatus.java

package com.sga.model;

import java.time.LocalDateTime;

public class ProcessamentoStatus {

	private String taskId;
	private String status; // EM_PROCESSAMENTO, CONCLUIDO, ERRO, CANCELADO
	private int progresso;
	private String mensagem;
	private LocalDateTime dataInicio;
	private LocalDateTime dataFim;
	private Object resultado;
	private int totalAssociados;
	private String usuario;
	private int totalProcessados;
	private int totalFaturasGeradas;

	// Getters e Setters
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getProgresso() {
		return progresso;
	}

	public void setProgresso(int progresso) {
		this.progresso = progresso;
	}

	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	public LocalDateTime getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDateTime dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDateTime getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDateTime dataFim) {
		this.dataFim = dataFim;
	}

	public Object getResultado() {
		return resultado;
	}

	public void setResultado(Object resultado) {
		this.resultado = resultado;
	}

	public int getTotalAssociados() {
		return totalAssociados;
	}

	public void setTotalAssociados(int totalAssociados) {
		this.totalAssociados = totalAssociados;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public int getTotalProcessados() {
		return totalProcessados;
	}

	public void setTotalProcessados(int totalProcessados) {
		this.totalProcessados = totalProcessados;
	}

	public int getTotalFaturasGeradas() {
		return totalFaturasGeradas;
	}

	public void setTotalFaturasGeradas(int totalFaturasGeradas) {
		this.totalFaturasGeradas = totalFaturasGeradas;
	}
}