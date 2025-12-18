// 
package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class NotaFaturamentoGridDTO {
	private Long idNota;
	private String codigoAssociado;
	private String nomeAssociado;
	private BigDecimal totalDebitos;
	private BigDecimal totalCredito;
	private BigDecimal valorFaturado;
	private LocalDateTime dataImportacao;
	private String status;

	// Construtor padrão
	public NotaFaturamentoGridDTO() {
		this.status = "PROCESSADO";
	}

	public Long getIdNota() {
		return idNota;
	}

	public void setIdNota(Long idNota) {
		this.idNota = idNota;
	}

	public String getCodigoAssociado() {
		return codigoAssociado;
	}

	public void setCodigoAssociado(String codigoAssociado) {
		this.codigoAssociado = codigoAssociado;
	}

	public String getNomeAssociado() {
		return nomeAssociado;
	}

	public void setNomeAssociado(String nomeAssociado) {
		this.nomeAssociado = nomeAssociado;
	}

	public BigDecimal getTotalDebitos() {
		return totalDebitos;
	}

	public void setTotalDebitos(BigDecimal totalDebitos) {
		this.totalDebitos = totalDebitos;
	}

	public BigDecimal getTotalCredito() {
		return totalCredito;
	}

	public void setTotalCredito(BigDecimal totalCredito) {
		this.totalCredito = totalCredito;
	}

	public BigDecimal getValorFaturado() {
		return valorFaturado;
	}

	public void setValorFaturado(BigDecimal valorFaturado) {
		this.valorFaturado = valorFaturado;
	}

	public LocalDateTime getDataImportacao() {
		return dataImportacao;
	}

	public void setDataImportacao(LocalDateTime dataImportacao) {
		this.dataImportacao = dataImportacao;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	
}