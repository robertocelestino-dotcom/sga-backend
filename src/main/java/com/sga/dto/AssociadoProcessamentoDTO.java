package com.sga.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AssociadoProcessamentoDTO {

	private Long associadoId;
	private String associadoNome;
	private String cnpjCpf;
	private String codigoSpc;
	private String codigoRm;

	// Valores do faturamento
	private BigDecimal faturamentoMinimo;
	private BigDecimal faturamentoReal;
	private BigDecimal valorFranquia;
	private BigDecimal consumoFranquia;
	private BigDecimal saldoFranquia;
	private BigDecimal valorDebito;
	private BigDecimal valorNota;

	// Configuração da régua aplicada
	private Long reguaId;
	private String reguaNome;
	private Integer diaEmissao;
	private Integer diaVencimento;

	// Status do processamento
	private Boolean processado;
	private Boolean gerouNota;
	private Boolean enviadoRM;
	private String mensagemErro;

	private List<NotaDebitoResumoDTO> notasGeradas = new ArrayList<>();
	private List<ConsumoFranquiaDTO> consumos = new ArrayList<>();
	
	// 🔥 NOVO - Itens da fatura gerada
	private List<FaturaItemDTO> itensFatura = new ArrayList<>();

	// getters e setters
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

	public String getCodigoRm() {
		return codigoRm;
	}

	public void setCodigoRm(String codigoRm) {
		this.codigoRm = codigoRm;
	}

	public BigDecimal getFaturamentoMinimo() {
		return faturamentoMinimo;
	}

	public void setFaturamentoMinimo(BigDecimal faturamentoMinimo) {
		this.faturamentoMinimo = faturamentoMinimo;
	}

	public BigDecimal getFaturamentoReal() {
		return faturamentoReal;
	}

	public void setFaturamentoReal(BigDecimal faturamentoReal) {
		this.faturamentoReal = faturamentoReal;
	}

	public BigDecimal getValorFranquia() {
		return valorFranquia;
	}

	public void setValorFranquia(BigDecimal valorFranquia) {
		this.valorFranquia = valorFranquia;
	}

	public BigDecimal getConsumoFranquia() {
		return consumoFranquia;
	}

	public void setConsumoFranquia(BigDecimal consumoFranquia) {
		this.consumoFranquia = consumoFranquia;
	}

	public BigDecimal getSaldoFranquia() {
		return saldoFranquia;
	}

	public void setSaldoFranquia(BigDecimal saldoFranquia) {
		this.saldoFranquia = saldoFranquia;
	}

	public BigDecimal getValorDebito() {
		return valorDebito;
	}

	public void setValorDebito(BigDecimal valorDebito) {
		this.valorDebito = valorDebito;
	}

	public BigDecimal getValorNota() {
		return valorNota;
	}

	public void setValorNota(BigDecimal valorNota) {
		this.valorNota = valorNota;
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

	public Integer getDiaEmissao() {
		return diaEmissao;
	}

	public void setDiaEmissao(Integer diaEmissao) {
		this.diaEmissao = diaEmissao;
	}

	public Integer getDiaVencimento() {
		return diaVencimento;
	}

	public void setDiaVencimento(Integer diaVencimento) {
		this.diaVencimento = diaVencimento;
	}

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}

	public Boolean getGerouNota() {
		return gerouNota;
	}

	public void setGerouNota(Boolean gerouNota) {
		this.gerouNota = gerouNota;
	}

	public Boolean getEnviadoRM() {
		return enviadoRM;
	}

	public void setEnviadoRM(Boolean enviadoRM) {
		this.enviadoRM = enviadoRM;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public List<NotaDebitoResumoDTO> getNotasGeradas() {
		return notasGeradas;
	}

	public void setNotasGeradas(List<NotaDebitoResumoDTO> notasGeradas) {
		this.notasGeradas = notasGeradas;
	}

	public List<ConsumoFranquiaDTO> getConsumos() {
		return consumos;
	}

	public void setConsumos(List<ConsumoFranquiaDTO> consumos) {
		this.consumos = consumos;
	}
	
	public List<FaturaItemDTO> getItensFatura() {
		return itensFatura;
	}
	
	public void setItensFatura(List<FaturaItemDTO> itensFatura) {
		this.itensFatura = itensFatura;
	}
	
	public void addItemFatura(FaturaItemDTO item) {
		if (this.itensFatura == null) {
			this.itensFatura = new ArrayList<>();
		}
		this.itensFatura.add(item);
	}
}