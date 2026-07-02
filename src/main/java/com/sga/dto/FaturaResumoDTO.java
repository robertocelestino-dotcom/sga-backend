// FaturaResumoDTO.java
package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FaturaResumoDTO {
	private Long id;
	private String numeroFatura;
	private BigDecimal valorTotal;
	private LocalDate dataEmissao;
	private LocalDate dataVencimento;
	private String status;
	private Integer mesReferencia;
	private Integer anoReferencia;
	private String associadoNome;

	private String cnpjCpf;
	private String codigoSpc;
	private Long associadoId;

	private Long reguaId;
	private String reguaNome;
	private String reguaCor; // Opcional: cor da régua para destacar

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNumeroFatura() {
		return numeroFatura;
	}

	public void setNumeroFatura(String numeroFatura) {
		this.numeroFatura = numeroFatura;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public LocalDate getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(LocalDate dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getMesReferencia() {
		return mesReferencia;
	}

	public void setMesReferencia(Integer mesReferencia) {
		this.mesReferencia = mesReferencia;
	}

	public Integer getAnoReferencia() {
		return anoReferencia;
	}

	public void setAnoReferencia(Integer anoReferencia) {
		this.anoReferencia = anoReferencia;
	}

	public String getAssociadoNome() {
		return associadoNome;
	}

	public void setAssociadoNome(String associadoNome) {
		this.associadoNome = associadoNome;
	}

	public String getCnpjCpf() {
		return cnpjCpf;
	}

	public void setCnpjCpf(String cnpjCpf) {
		this.cnpjCpf = cnpjCpf;
	}

	public String getCodigoSpc() {
		return codigoSpc;
	}

	public void setCodigoSpc(String codigoSpc) {
		this.codigoSpc = codigoSpc;
	}

	public Long getAssociadoId() {
		return associadoId;
	}

	public void setAssociadoId(Long associadoId) {
		this.associadoId = associadoId;
	}

	public Long getReguaId() {
		return reguaId;
	}

	public void setReguaId(Long reguaId) {
		this.reguaId = reguaId;
	}

	public String getReguaNome() {
		return reguaNome;
	}

	public void setReguaNome(String reguaNome) {
		this.reguaNome = reguaNome;
	}

	public String getReguaCor() {
		return reguaCor;
	}

	public void setReguaCor(String reguaCor) {
		this.reguaCor = reguaCor;
	}
	
}