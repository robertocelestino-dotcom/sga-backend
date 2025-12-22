package com.sga.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VendedorDTO {
	private Long id;
	private String nomeRazao;
	private String nomeFantasia;
	private String cargoFuncao;
	private String status; // "A" = Ativo, "I" = Inativo
	private String observacoes; // No modelo é "observacao"
	private LocalDateTime dataCadastro;

	// Construtor vazio
	public VendedorDTO() {}

	// Construtor útil
	public VendedorDTO(String nomeRazao, String status) {
		this.nomeRazao = nomeRazao;
		this.status = status;
		this.dataCadastro = LocalDateTime.now();
	}

	// Getters e Setters (mantidos por compatibilidade)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeRazao() {
		return nomeRazao;
	}

	public void setNomeRazao(String nomeRazao) {
		this.nomeRazao = nomeRazao;
	}

	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	public String getCargoFuncao() {
		return cargoFuncao;
	}

	public void setCargoFuncao(String cargoFuncao) {
		this.cargoFuncao = cargoFuncao;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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