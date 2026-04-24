package com.sga.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Data
@Entity
@Table(name = "tb_lote_processamento")
public class LoteProcessamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "regua_id")
	private ReguaFaturamento regua;

	@Column(name = "mes_referencia", nullable = false)
	private LocalDate mesReferencia;

	@Column(name = "data_inicio_processamento", nullable = false)
	private LocalDateTime dataInicioProcessamento;

	@Column(name = "data_fim_processamento")
	private LocalDateTime dataFimProcessamento;

	@Column(length = 20)
	private String status = "EM_PROCESSAMENTO";

	@Column(name = "total_notas_processadas")
	private Integer totalNotasProcessadas = 0;

	@Column(name = "total_faturas_geradas")
	private Integer totalFaturasGeradas = 0;

	@Column(name = "total_cancelamentos_aplicados")
	private Integer totalCancelamentosAplicados = 0;

	@Column(name = "total_erros")
	private Integer totalErros = 0;

	private String observacao;

	@Column(name = "usuario_processamento", length = 100)
	private String usuarioProcessamento;

	@CreationTimestamp
	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	public LoteProcessamento() {
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ReguaFaturamento getRegua() {
		return regua;
	}

	public void setRegua(ReguaFaturamento regua) {
		this.regua = regua;
	}

	public LocalDate getMesReferencia() {
		return mesReferencia;
	}

	public void setMesReferencia(LocalDate mesReferencia) {
		this.mesReferencia = mesReferencia;
	}

	public LocalDateTime getDataInicioProcessamento() {
		return dataInicioProcessamento;
	}

	public void setDataInicioProcessamento(LocalDateTime dataInicioProcessamento) {
		this.dataInicioProcessamento = dataInicioProcessamento;
	}

	public LocalDateTime getDataFimProcessamento() {
		return dataFimProcessamento;
	}

	public void setDataFimProcessamento(LocalDateTime dataFimProcessamento) {
		this.dataFimProcessamento = dataFimProcessamento;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getTotalNotasProcessadas() {
		return totalNotasProcessadas;
	}

	public void setTotalNotasProcessadas(Integer totalNotasProcessadas) {
		this.totalNotasProcessadas = totalNotasProcessadas;
	}

	public Integer getTotalFaturasGeradas() {
		return totalFaturasGeradas;
	}

	public void setTotalFaturasGeradas(Integer totalFaturasGeradas) {
		this.totalFaturasGeradas = totalFaturasGeradas;
	}

	public Integer getTotalCancelamentosAplicados() {
		return totalCancelamentosAplicados;
	}

	public void setTotalCancelamentosAplicados(Integer totalCancelamentosAplicados) {
		this.totalCancelamentosAplicados = totalCancelamentosAplicados;
	}

	public Integer getTotalErros() {
		return totalErros;
	}

	public void setTotalErros(Integer totalErros) {
		this.totalErros = totalErros;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getUsuarioProcessamento() {
		return usuarioProcessamento;
	}

	public void setUsuarioProcessamento(String usuarioProcessamento) {
		this.usuarioProcessamento = usuarioProcessamento;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

}