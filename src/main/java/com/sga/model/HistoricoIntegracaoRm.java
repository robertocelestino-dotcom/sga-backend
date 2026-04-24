// src/main/java/com/sga/model/HistoricoIntegracaoRm.java

package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "tb_historico_integracao_rm")
public class HistoricoIntegracaoRm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "data_integracao", nullable = false)
	private LocalDateTime dataIntegracao;

	@Column(name = "mes_referencia")
	private Integer mesReferencia;

	@Column(name = "ano_referencia")
	private Integer anoReferencia;

	@Column(name = "quantidade_faturas")
	private Integer quantidadeFaturas;

	@Column(name = "valor_total")
	private Double valorTotal;

	@Column(name = "nome_arquivo", length = 255)
	private String nomeArquivo;

	@Column(name = "caminho_arquivo", length = 500)
	private String caminhoArquivo;

	@Column(name = "status", length = 20)
	private String status; // SUCESSO, ERRO, PENDENTE

	@Column(name = "mensagem_erro", columnDefinition = "TEXT")
	private String mensagemErro;

	@Column(name = "usuario", length = 100)
	private String usuario;

	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	@PrePersist
	protected void onCreate() {
		criadoEm = LocalDateTime.now();
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDataIntegracao() {
		return dataIntegracao;
	}

	public void setDataIntegracao(LocalDateTime dataIntegracao) {
		this.dataIntegracao = dataIntegracao;
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

	public Integer getQuantidadeFaturas() {
		return quantidadeFaturas;
	}

	public void setQuantidadeFaturas(Integer quantidadeFaturas) {
		this.quantidadeFaturas = quantidadeFaturas;
	}

	public Double getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(Double valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public String getCaminhoArquivo() {
		return caminhoArquivo;
	}

	public void setCaminhoArquivo(String caminhoArquivo) {
		this.caminhoArquivo = caminhoArquivo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}
}