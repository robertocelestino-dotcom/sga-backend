// src/main/java/com/sga/dto/AssociadoResumoDTO.java
package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AssociadoResumoDTO {
	private Long id;
	private String codigoSpc;
	private String codigoRm;
	private String cnpjCpf;
	private String nomeRazao;
	private String nomeFantasia;
	private String tipoPessoa; // "F" ou "J"
	private String status; // "A", "I", "S"
	private BigDecimal faturamentoMinimo;
	private LocalDateTime dataCadastro;

	// Relacionamentos (apenas nomes para resumo)
	private Long vendedorId;
	private String vendedorNome;
	private Long planoId;
	private String planoNome;
	private Long categoriaId;
	private String categoriaNome;

	public AssociadoResumoDTO() {

	}

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

	public Long getPlanoId() {
		return planoId;
	}

	public void setPlanoId(Long planoId) {
		this.planoId = planoId;
	}

	public String getPlanoNome() {
		return planoNome;
	}

	public void setPlanoNome(String planoNome) {
		this.planoNome = planoNome;
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