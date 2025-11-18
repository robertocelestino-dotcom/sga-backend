// model/Faturamento.java
package com.sga.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "tb_faturamento")
public class Faturamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_faturamento")
	@Temporal(TemporalType.DATE)
	private Date dataFaturamento;

	@Column(name = "mes")
	private Integer mes;

	@Column(name = "ano")
	private Integer ano;

	@Column(name = "valor_total", precision = 15, scale = 2)
	private BigDecimal valorTotal;

	@Column(name = "quantidade_notas")
	private Integer quantidadeNotas;

	@Column(name = "status")
	private String status;

	// Construtores
	public Faturamento() {
	}

	public Faturamento(Date dataFaturamento, Integer mes, Integer ano, BigDecimal valorTotal) {
		this.dataFaturamento = dataFaturamento;
		this.mes = mes;
		this.ano = ano;
		this.valorTotal = valorTotal;
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDataFaturamento() {
		return dataFaturamento;
	}

	public void setDataFaturamento(Date dataFaturamento) {
		this.dataFaturamento = dataFaturamento;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public Integer getQuantidadeNotas() {
		return quantidadeNotas;
	}

	public void setQuantidadeNotas(Integer quantidadeNotas) {
		this.quantidadeNotas = quantidadeNotas;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}