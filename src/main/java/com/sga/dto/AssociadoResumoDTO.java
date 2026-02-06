package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AssociadoResumoDTO {
	private Long id;
	private String codigoSpc;
	private String codigoRm;
	private String cnpjCpf;
	private String nomeRazao;
	private String nomeFantasia;
	private String tipoPessoa;
	private String status;
	private BigDecimal faturamentoMinimo; // Adicionado

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime dataCadastro;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFiliacao;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInativacao;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInicioSuspensao;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFimSuspensao;

	private String motivoInativacao;
	private String motivoSuspensao;

	// Campos de relacionamento ADICIONADOS
	private Long vendedorId;
	private String vendedorNome;
	
	private Long vendedorExternoId;
	private String vendedorExternoNome;
	
	private Long planoId;
	private String planoTitulo; // Alterado para corresponder ao seu modelo
	
	private Long categoriaId;
	private String categoriaNome;

	// Getters e Setters existentes (mantenha os que já tem)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigoSpc() {
		return codigoSpc;
	}

	public void setCodigoSpc(String codigoSpc) {
		this.codigoSpc = codigoSpc;
	}

	public String getCodigoRm() {
		return codigoRm;
	}

	public void setCodigoRm(String codigoRm) {
		this.codigoRm = codigoRm;
	}

	public String getCnpjCpf() {
		return cnpjCpf;
	}

	public void setCnpjCpf(String cnpjCpf) {
		this.cnpjCpf = cnpjCpf;
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

	public String getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BigDecimal getFaturamentoMinimo() {
		return faturamentoMinimo;
	}

	public void setFaturamentoMinimo(BigDecimal faturamentoMinimo) {
		this.faturamentoMinimo = faturamentoMinimo;
	}

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public LocalDate getDataFiliacao() {
		return dataFiliacao;
	}

	public void setDataFiliacao(LocalDate dataFiliacao) {
		this.dataFiliacao = dataFiliacao;
	}

	public LocalDate getDataInativacao() {
		return dataInativacao;
	}

	public void setDataInativacao(LocalDate dataInativacao) {
		this.dataInativacao = dataInativacao;
	}

	public LocalDate getDataInicioSuspensao() {
		return dataInicioSuspensao;
	}

	public void setDataInicioSuspensao(LocalDate dataInicioSuspensao) {
		this.dataInicioSuspensao = dataInicioSuspensao;
	}

	public LocalDate getDataFimSuspensao() {
		return dataFimSuspensao;
	}

	public void setDataFimSuspensao(LocalDate dataFimSuspensao) {
		this.dataFimSuspensao = dataFimSuspensao;
	}

	public String getMotivoInativacao() {
		return motivoInativacao;
	}

	public void setMotivoInativacao(String motivoInativacao) {
		this.motivoInativacao = motivoInativacao;
	}

	public String getMotivoSuspensao() {
		return motivoSuspensao;
	}

	public void setMotivoSuspensao(String motivoSuspensao) {
		this.motivoSuspensao = motivoSuspensao;
	}

	// NOVOS Getters e Setters para relacionamentos
	public Long getVendedorId() {
		return vendedorId;
	}

	public void setVendedorId(Long vendedorId) {
		this.vendedorId = vendedorId;
	}

	public String getVendedorNome() {
		return vendedorNome;
	}

	public void setVendedorNome(String vendedorNome) {
		this.vendedorNome = vendedorNome;
	}

	public Long getVendedorExternoId() {
		return vendedorExternoId;
	}

	public void setVendedorExternoId(Long vendedorExternoId) {
		this.vendedorExternoId = vendedorExternoId;
	}

	public String getVendedorExternoNome() {
		return vendedorExternoNome;
	}

	public void setVendedorExternoNome(String vendedorExternoNome) {
		this.vendedorExternoNome = vendedorExternoNome;
	}

	public Long getPlanoId() {
		return planoId;
	}

	public void setPlanoId(Long planoId) {
		this.planoId = planoId;
	}

	public String getPlanoTitulo() {
		return planoTitulo;
	}

	public void setPlanoTitulo(String planoTitulo) {
		this.planoTitulo = planoTitulo;
	}

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public String getCategoriaNome() {
		return categoriaNome;
	}

	public void setCategoriaNome(String categoriaNome) {
		this.categoriaNome = categoriaNome;
	}
}