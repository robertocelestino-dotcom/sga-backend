package com.sga.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tb_importacao_spc")
public class ImportacaoSPC {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "nome_arquivo")
	private String nomeArquivo;

	@Column(name = "data_importacao")
	private LocalDateTime dataImportacao;

	@Column(name = "status")
	private String status; // IMPORTADO, PROCESSADO, ERRO

	// One-to-Many relationships
	@OneToMany(mappedBy = "importacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<HeaderSPC> headers = new ArrayList<>();

	@OneToMany(mappedBy = "importacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ParametrosSPC> parametros = new ArrayList<>();

	@OneToMany(mappedBy = "importacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<NotaDebitoSPC> notasDebito = new ArrayList<>();

	@OneToMany(mappedBy = "importacao", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TraillerSPC> traillers = new ArrayList<>();

	// Construtor padrão
	public ImportacaoSPC() {
	}

	public ImportacaoSPC(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
		this.dataImportacao = LocalDateTime.now();
		this.status = "IMPORTADO";
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
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

	public List<HeaderSPC> getHeaders() {
		return headers;
	}

	public void setHeaders(List<HeaderSPC> headers) {
		this.headers = headers;
	}

	public List<ParametrosSPC> getParametros() {
		return parametros;
	}

	public void setParametros(List<ParametrosSPC> parametros) {
		this.parametros = parametros;
	}

	public List<NotaDebitoSPC> getNotasDebito() {
		return notasDebito;
	}

	public void setNotasDebito(List<NotaDebitoSPC> notasDebito) {
		this.notasDebito = notasDebito;
	}

	public List<TraillerSPC> getTraillers() {
		return traillers;
	}

	public void setTraillers(List<TraillerSPC> traillers) {
		this.traillers = traillers;
	}
}