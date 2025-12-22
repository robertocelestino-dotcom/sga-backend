package com.sga.dto;

import java.time.LocalDateTime;

public class VendedorResumoDTO {
	private Long id;
	private String nomeRazao;
	private String cargoFuncao;
	private String status;
	private LocalDateTime dataCadastro;

	public VendedorResumoDTO() {}

	public VendedorResumoDTO(Long id, String nomeRazao, String cargoFuncao, String status, LocalDateTime dataCadastro) {
		this.id = id;
		this.nomeRazao = nomeRazao;
		this.cargoFuncao = cargoFuncao;
		this.status = status;
		this.dataCadastro = dataCadastro;
	}

	// Getters e Setters
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

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
}