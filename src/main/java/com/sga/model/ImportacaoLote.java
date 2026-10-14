package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Entity
@Table(name = "tb_importacao_lote")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ImportacaoLote {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_inicio", nullable = false)
	private LocalDateTime dataInicio;

	@Column(name = "data_fim")
	private LocalDateTime dataFim;

	@Column(name = "arquivo_nome", length = 255)
	private String arquivoNome;

	@Column(name = "usuario", length = 100)
	private String usuario;

	@Column(name = "total_linhas")
	private Integer totalLinhas = 0;

	@Column(name = "total_criados")
	private Integer totalCriados = 0;

	@Column(name = "total_atualizados")
	private Integer totalAtualizados = 0;

	@Column(name = "total_inativados")
	private Integer totalInativados = 0;

	@Column(name = "total_erros")
	private Integer totalErros = 0;

	@Column(name = "status", length = 20)
	private String status = "EM_ANDAMENTO";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDateTime dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDateTime getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDateTime dataFim) {
		this.dataFim = dataFim;
	}

	public String getArquivoNome() {
		return arquivoNome;
	}

	public void setArquivoNome(String arquivoNome) {
		this.arquivoNome = arquivoNome;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public Integer getTotalLinhas() {
		return totalLinhas;
	}

	public void setTotalLinhas(Integer totalLinhas) {
		this.totalLinhas = totalLinhas;
	}

	public Integer getTotalCriados() {
		return totalCriados;
	}

	public void setTotalCriados(Integer totalCriados) {
		this.totalCriados = totalCriados;
	}

	public Integer getTotalAtualizados() {
		return totalAtualizados;
	}

	public void setTotalAtualizados(Integer totalAtualizados) {
		this.totalAtualizados = totalAtualizados;
	}

	public Integer getTotalInativados() {
		return totalInativados;
	}

	public void setTotalInativados(Integer totalInativados) {
		this.totalInativados = totalInativados;
	}

	public Integer getTotalErros() {
		return totalErros;
	}

	public void setTotalErros(Integer totalErros) {
		this.totalErros = totalErros;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}