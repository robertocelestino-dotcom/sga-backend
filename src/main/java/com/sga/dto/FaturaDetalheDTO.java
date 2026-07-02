// src/main/java/com/sga/dto/FaturaDetalheDTO.java

package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FaturaDetalheDTO {

	private Long id;
	private String numeroFatura;
	private BigDecimal valorTotal;
	private LocalDate dataEmissao;
	private LocalDate dataVencimento;
	private String status;
	private Boolean processadoRm;
	private Integer mesReferencia;
	private Integer anoReferencia;
	private LocalDateTime criadoEm;
	private String observacao;

	// Dados do associado
	private Long associadoId;
	private String associadoNome;
	private String cnpjCpf;
	private String codigoSpc;

	// Itens da fatura
	private List<FaturaItemDTO> itens;

	private Integer numeroRps;

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

	public Boolean getProcessadoRm() {
		return processadoRm;
	}

	public void setProcessadoRm(Boolean processadoRm) {
		this.processadoRm = processadoRm;
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

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Long getAssociadoId() {
		return associadoId;
	}

	public void setAssociadoId(Long associadoId) {
		this.associadoId = associadoId;
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

	public List<FaturaItemDTO> getItens() {
		return itens;
	}

	public void setItens(List<FaturaItemDTO> itens) {
		this.itens = itens;
	}

	public Integer getNumeroRps() {
		return numeroRps;
	}

	public void setNumeroRps(Integer numeroRps) {
		this.numeroRps = numeroRps;
	}

}