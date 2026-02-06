package com.sga.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssociadoDTO {

	private Long id;
	private String codigoSpc;
	private String codigoRm;
	private String cnpjCpf;
	private String nomeRazao;
	private String nomeFantasia;
	private String tipoPessoa;
	private String status = "A";

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime dataCadastro;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFiliacao;

	// NOVOS CAMPOS DE STATUS
	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInativacao;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataInicioSuspensao;

	@JsonFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataFimSuspensao;

	private String motivoInativacao;
	private String motivoSuspensao;

	private BigDecimal faturamentoMinimo;

	private Long vendedorId;
	private String vendedorNome;

	private Integer vendedorExternoId;
	private String vendedorExternoNome;

	private Long planoId;
	private Long categoriaId;

	private List<EnderecoDTO> enderecos;
	private List<EmailDTO> emails;
	private List<TelefoneDTO> telefones;

	// Construtor
	public AssociadoDTO() {
	}

	// Getters e Setters (adicionar os novos)
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

	// NOVOS GETTERS E SETTERS
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

	public BigDecimal getFaturamentoMinimo() {
		return faturamentoMinimo;
	}

	public void setFaturamentoMinimo(BigDecimal faturamentoMinimo) {
		this.faturamentoMinimo = faturamentoMinimo;
	}

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

	public Integer getVendedorExternoId() {
		return vendedorExternoId;
	}

	public void setVendedorExternoId(Integer vendedorExternoId) {
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

	public Long getCategoriaId() {
		return categoriaId;
	}

	public void setCategoriaId(Long categoriaId) {
		this.categoriaId = categoriaId;
	}

	public List<EnderecoDTO> getEnderecos() {
		return enderecos;
	}

	public void setEnderecos(List<EnderecoDTO> enderecos) {
		this.enderecos = enderecos;
	}

	public List<EmailDTO> getEmails() {
		return emails;
	}

	public void setEmails(List<EmailDTO> emails) {
		this.emails = emails;
	}

	public List<TelefoneDTO> getTelefones() {
		return telefones;
	}

	public void setTelefones(List<TelefoneDTO> telefones) {
		this.telefones = telefones;
	}

}