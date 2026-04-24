package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity
@Table(name = "tb_cancelamento_importacao")
public class CancelamentoImportacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "codigo_associado", nullable = false, length = 50)
	private String codigoAssociado;

	// 🔥 NOVO CAMPO - Código do serviço cancelado
	@Column(name = "codigo_servico", length = 50)
	private String codigoServico;

	@Column(name = "descricao_produto", length = 500)
	private String descricaoProduto;

	@Column(name = "produto_hsm")
	private Boolean produtoHsm = false;

	@Column(name = "produto_spc_avisa")
	private Boolean produtoSpcAvisa = false;

	@Column(name = "produto_nfe")
	private Boolean produtoNfe = false;

	@Column(name = "produto_personalizado", length = 100)
	private String produtoPersonalizado;

	@Column(name = "valor_cancelamento", precision = 15, scale = 2)
	private BigDecimal valorCancelamento = BigDecimal.ZERO;

	@Column(name = "mes_referencia")
	private Integer mesReferencia;

	@Column(name = "ano_referencia")
	private Integer anoReferencia;

	@Column(name = "processado")
	private Boolean processado = false;

	@Column(name = "status", length = 20)
	private String status = "PENDENTE";

	@Column(name = "data_importacao")
	private LocalDateTime dataImportacao;

	@Column(name = "data_processamento")
	private LocalDateTime dataProcessamento;

	@Column(name = "linha_arquivo")
	private Integer linhaArquivo;

	@Column(name = "mensagem_erro", columnDefinition = "TEXT")
	private String mensagemErro;

	@Column(name = "importacao_id")
	private Long importacaoId;

	@Column(name = "nome_arquivo", length = 255)
	private String nomeArquivo;

	@Column(name = "usuario_importacao", length = 100)
	private String usuarioImportacao;

	@Column(name = "criado_em")
	private LocalDateTime criadoEm;

	@PrePersist
	protected void onCreate() {
		if (dataImportacao == null) {
			dataImportacao = LocalDateTime.now();
		}
		if (criadoEm == null) {
			criadoEm = LocalDateTime.now();
		}
		if (processado == null) {
			processado = false;
		}
		if (status == null) {
			status = "PENDENTE";
		}
		if (produtoHsm == null) {
			produtoHsm = false;
		}
		if (produtoSpcAvisa == null) {
			produtoSpcAvisa = false;
		}
		if (produtoNfe == null) {
			produtoNfe = false;
		}
		if (valorCancelamento == null) {
			valorCancelamento = BigDecimal.ZERO;
		}
	}

	// ========== GETTERS E SETTERS ==========

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigoAssociado() {
		return codigoAssociado;
	}

	public void setCodigoAssociado(String codigoAssociado) {
		this.codigoAssociado = codigoAssociado;
	}

	// 🔥 GETTER E SETTER DO NOVO CAMPO
	public String getCodigoServico() {
		return codigoServico;
	}

	public void setCodigoServico(String codigoServico) {
		this.codigoServico = codigoServico;
	}

	public String getDescricaoProduto() {
		return descricaoProduto;
	}

	public void setDescricaoProduto(String descricaoProduto) {
		this.descricaoProduto = descricaoProduto;
	}

	public Boolean getProdutoHsm() {
		return produtoHsm;
	}

	public void setProdutoHsm(Boolean produtoHsm) {
		this.produtoHsm = produtoHsm;
	}

	public Boolean getProdutoSpcAvisa() {
		return produtoSpcAvisa;
	}

	public void setProdutoSpcAvisa(Boolean produtoSpcAvisa) {
		this.produtoSpcAvisa = produtoSpcAvisa;
	}

	public Boolean getProdutoNfe() {
		return produtoNfe;
	}

	public void setProdutoNfe(Boolean produtoNfe) {
		this.produtoNfe = produtoNfe;
	}

	public String getProdutoPersonalizado() {
		return produtoPersonalizado;
	}

	public void setProdutoPersonalizado(String produtoPersonalizado) {
		this.produtoPersonalizado = produtoPersonalizado;
	}

	public BigDecimal getValorCancelamento() {
		return valorCancelamento;
	}

	public void setValorCancelamento(BigDecimal valorCancelamento) {
		this.valorCancelamento = valorCancelamento;
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

	public Boolean getProcessado() {
		return processado;
	}

	public void setProcessado(Boolean processado) {
		this.processado = processado;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public LocalDateTime getDataImportacao() {
		return dataImportacao;
	}

	public void setDataImportacao(LocalDateTime dataImportacao) {
		this.dataImportacao = dataImportacao;
	}

	public LocalDateTime getDataProcessamento() {
		return dataProcessamento;
	}

	public void setDataProcessamento(LocalDateTime dataProcessamento) {
		this.dataProcessamento = dataProcessamento;
	}

	public Integer getLinhaArquivo() {
		return linhaArquivo;
	}

	public void setLinhaArquivo(Integer linhaArquivo) {
		this.linhaArquivo = linhaArquivo;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public Long getImportacaoId() {
		return importacaoId;
	}

	public void setImportacaoId(Long importacaoId) {
		this.importacaoId = importacaoId;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public String getUsuarioImportacao() {
		return usuarioImportacao;
	}

	public void setUsuarioImportacao(String usuarioImportacao) {
		this.usuarioImportacao = usuarioImportacao;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}
}