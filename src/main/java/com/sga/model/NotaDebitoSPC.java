package com.sga.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tb_nota_debito_spc")
public class NotaDebitoSPC {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tipo_registro", length = 1)
	private String tipoRegistro;

	@Column(name = "data_vencimento", length = 8)
	private String dataVencimento;

	@Column(name = "numero_fatura", length = 10)
	private String numeroFatura;

	@Column(name = "numero_nota_debito", length = 10)
	private String numeroNotaDebito;

	@Column(name = "valor_nota", precision = 15, scale = 2)
	private BigDecimal valorNota;

	@Column(name = "codigo_socio", length = 8)
	private String codigoSocio;

	@Column(name = "nome_associado", length = 50)
	private String nomeAssociado;

	@Column(name = "endereco_cobranca", length = 62)
	private String enderecoCobranca;

	@Column(name = "bairro_cobranca", length = 30)
	private String bairroCobranca;

	@Column(name = "cep_cobranca", length = 9)
	private String cepCobranca;

	@Column(name = "cidade_cobranca", length = 30)
	private String cidadeCobranca;

	@Column(name = "uf_cobranca", length = 2)
	private String ufCobranca;

	@Column(name = "telefone_cobranca", length = 15)
	private String telefoneCobranca;

	@Column(name = "tipo_pessoa", length = 1)
	private String tipoPessoa;

	@Column(name = "cnpj_cic", length = 19)
	private String cnpjCic;

	@Column(name = "inscricao_estadual", length = 14)
	private String inscricaoEstadual;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "importacao_id", nullable = false)
	private ImportacaoSPC importacao;

	@OneToMany(mappedBy = "notaDebito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ItemSPC> itens = new ArrayList<>();

	public NotaDebitoSPC() {
	}

	// Getters e Setters (manter todos os existentes e adicionar)
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

	public String getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(String dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public String getNumeroFatura() {
		return numeroFatura;
	}

	public void setNumeroFatura(String numeroFatura) {
		this.numeroFatura = numeroFatura;
	}

	public String getNumeroNotaDebito() {
		return numeroNotaDebito;
	}

	public void setNumeroNotaDebito(String numeroNotaDebito) {
		this.numeroNotaDebito = numeroNotaDebito;
	}

	public BigDecimal getValorNota() {
		return valorNota;
	}

	public void setValorNota(BigDecimal valorNota) {
		this.valorNota = valorNota;
	}

	public String getCodigoSocio() {
		return codigoSocio;
	}

	public void setCodigoSocio(String codigoSocio) {
		this.codigoSocio = codigoSocio;
	}

	public String getNomeAssociado() {
		return nomeAssociado;
	}

	public void setNomeAssociado(String nomeAssociado) {
		this.nomeAssociado = nomeAssociado;
	}

	public String getEnderecoCobranca() {
		return enderecoCobranca;
	}

	public void setEnderecoCobranca(String enderecoCobranca) {
		this.enderecoCobranca = enderecoCobranca;
	}

	public String getBairroCobranca() {
		return bairroCobranca;
	}

	public void setBairroCobranca(String bairroCobranca) {
		this.bairroCobranca = bairroCobranca;
	}

	public String getCepCobranca() {
		return cepCobranca;
	}

	public void setCepCobranca(String cepCobranca) {
		this.cepCobranca = cepCobranca;
	}

	public String getCidadeCobranca() {
		return cidadeCobranca;
	}

	public void setCidadeCobranca(String cidadeCobranca) {
		this.cidadeCobranca = cidadeCobranca;
	}

	public String getUfCobranca() {
		return ufCobranca;
	}

	public void setUfCobranca(String ufCobranca) {
		this.ufCobranca = ufCobranca;
	}

	public String getTelefoneCobranca() {
		return telefoneCobranca;
	}

	public void setTelefoneCobranca(String telefoneCobranca) {
		this.telefoneCobranca = telefoneCobranca;
	}

	public String getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public String getCnpjCic() {
		return cnpjCic;
	}

	public void setCnpjCic(String cnpjCic) {
		this.cnpjCic = cnpjCic;
	}

	public String getInscricaoEstadual() {
		return inscricaoEstadual;
	}

	public void setInscricaoEstadual(String inscricaoEstadual) {
		this.inscricaoEstadual = inscricaoEstadual;
	}

	public ImportacaoSPC getImportacao() {
		return importacao;
	}

	public void setImportacao(ImportacaoSPC importacao) {
		this.importacao = importacao;
	}

	public List<ItemSPC> getItens() {
		return itens;
	}

	public void setItens(List<ItemSPC> itens) {
		this.itens = itens;
	}
}