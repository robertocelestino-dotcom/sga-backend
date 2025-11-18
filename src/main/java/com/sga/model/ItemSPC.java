package com.sga.model;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_nota_itens_spc")
public class ItemSPC {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tipo_registro", length = 1)
	private String tipoRegistro;

	@Column(name = "quantidade_servicos")
	private Integer quantidadeServicos;

	@Column(name = "descricao_servico", length = 50)
	private String descricaoServico;

	@Column(name = "valor_unitario", precision = 15, scale = 2)
	private BigDecimal valorUnitario;

	@Column(name = "valor_total", precision = 15, scale = 2)
	private BigDecimal valorTotal;

	@Column(name = "credito_debito", length = 1)
	private String creditoDebito;

	@Column(name = "tipo_produto", length = 2)
	private String tipoProduto;

	@Column(name = "codigo_produto_comercial", length = 10)
	private String codigoProdutoComercial;

	@Column(name = "codigo_contabil", length = 20)
	private String codigoContabil;

	@Column(name = "numero_nota_debito", length = 10)
	private String numeroNotaDebito;

	@Column(name = "sequencia_nota_debito", length = 5)
	private String sequenciaNotaDebito;

	@Column(name = "codigo_produto", length = 4)
	private String codigoProduto;

	@Column(name = "codigo_meio_acesso", length = 5)
	private String codigoMeioAcesso;

	@Column(name = "tipo_produto_detalhe", length = 1)
	private String tipoProdutoDetalhe;

	@Column(name = "incide_iss", length = 1)
	private String incideISS;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nota_debito_id", nullable = false)
	private NotaDebitoSPC notaDebito;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "importacao_id", nullable = false)
	private ImportacaoSPC importacao;

	public ItemSPC() {
	}

	// Getters e Setters (manter todos)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTipoRegistro() {
		return tipoRegistro;
	}

	public void setTipoRegistro(String tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}

	public Integer getQuantidadeServicos() {
		return quantidadeServicos;
	}

	public void setQuantidadeServicos(Integer quantidadeServicos) {
		this.quantidadeServicos = quantidadeServicos;
	}

	public String getDescricaoServico() {
		return descricaoServico;
	}

	public void setDescricaoServico(String descricaoServico) {
		this.descricaoServico = descricaoServico;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getCreditoDebito() {
		return creditoDebito;
	}

	public void setCreditoDebito(String creditoDebito) {
		this.creditoDebito = creditoDebito;
	}

	public String getTipoProduto() {
		return tipoProduto;
	}

	public void setTipoProduto(String tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	public String getCodigoProdutoComercial() {
		return codigoProdutoComercial;
	}

	public void setCodigoProdutoComercial(String codigoProdutoComercial) {
		this.codigoProdutoComercial = codigoProdutoComercial;
	}

	public String getCodigoContabil() {
		return codigoContabil;
	}

	public void setCodigoContabil(String codigoContabil) {
		this.codigoContabil = codigoContabil;
	}

	public String getNumeroNotaDebito() {
		return numeroNotaDebito;
	}

	public void setNumeroNotaDebito(String numeroNotaDebito) {
		this.numeroNotaDebito = numeroNotaDebito;
	}

	public String getSequenciaNotaDebito() {
		return sequenciaNotaDebito;
	}

	public void setSequenciaNotaDebito(String sequenciaNotaDebito) {
		this.sequenciaNotaDebito = sequenciaNotaDebito;
	}

	public String getCodigoProduto() {
		return codigoProduto;
	}

	public void setCodigoProduto(String codigoProduto) {
		this.codigoProduto = codigoProduto;
	}

	public String getCodigoMeioAcesso() {
		return codigoMeioAcesso;
	}

	public void setCodigoMeioAcesso(String codigoMeioAcesso) {
		this.codigoMeioAcesso = codigoMeioAcesso;
	}

	public String getTipoProdutoDetalhe() {
		return tipoProdutoDetalhe;
	}

	public void setTipoProdutoDetalhe(String tipoProdutoDetalhe) {
		this.tipoProdutoDetalhe = tipoProdutoDetalhe;
	}

	public String getIncideISS() {
		return incideISS;
	}

	public void setIncideISS(String incideISS) {
		this.incideISS = incideISS;
	}

	public NotaDebitoSPC getNotaDebito() {
		return notaDebito;
	}

	public void setNotaDebito(NotaDebitoSPC notaDebito) {
		this.notaDebito = notaDebito;
	}

	public ImportacaoSPC getImportacao() {
		return importacao;
	}

	public void setImportacao(ImportacaoSPC importacao) {
		this.importacao = importacao;
	}
}