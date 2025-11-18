package com.sga.model;

import javax.persistence.*;

@Entity
@Table(name = "tb_nota_header_spc")
public class HeaderSPC {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tipo_registro", length = 1)
	private String tipoRegistro;

	@Column(name = "data_gravacao", length = 8)
	private String dataGravacao;

	@Column(name = "nome_arquivo", length = 30)
	private String nomeArquivo;

	@Column(name = "descricao_arquivo", length = 20)
	private String descricaoArquivo;

	@Column(name = "data_ref_faturamento", length = 8)
	private String dataRefFaturamento;

	@Column(name = "codigo_entidade", length = 8)
	private String codigoEntidade;

	@Column(name = "nome_entidade", length = 50)
	private String nomeEntidade;

	@Column(name = "endereco_entidade", length = 50)
	private String enderecoEntidade;

	@Column(name = "bairro_entidade", length = 30)
	private String bairroEntidade;

	@Column(name = "cep_entidade", length = 9)
	private String cepEntidade;

	@Column(name = "cidade_entidade", length = 30)
	private String cidadeEntidade;

	@Column(name = "uf_entidade", length = 2)
	private String ufEntidade;

	@Column(name = "telefone_entidade", length = 15)
	private String telefoneEntidade;

	@Column(name = "fax_entidade", length = 15)
	private String faxEntidade;

	@Column(name = "cnpj_entidade", length = 19)
	private String cnpjEntidade;

	@Column(name = "inscricao_estadual", length = 14)
	private String inscricaoEstadual;

	@Column(name = "inscricao_municipal", length = 20)
	private String inscricaoMunicipal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "importacao_id")
	private ImportacaoSPC importacao;

	public HeaderSPC() {
	}

	// Getters e Setters
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

	public String getDataGravacao() {
		return dataGravacao;
	}

	public void setDataGravacao(String dataGravacao) {
		this.dataGravacao = dataGravacao;
	}

	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	public String getDescricaoArquivo() {
		return descricaoArquivo;
	}

	public void setDescricaoArquivo(String descricaoArquivo) {
		this.descricaoArquivo = descricaoArquivo;
	}

	public String getDataRefFaturamento() {
		return dataRefFaturamento;
	}

	public void setDataRefFaturamento(String dataRefFaturamento) {
		this.dataRefFaturamento = dataRefFaturamento;
	}

	public String getCodigoEntidade() {
		return codigoEntidade;
	}

	public void setCodigoEntidade(String codigoEntidade) {
		this.codigoEntidade = codigoEntidade;
	}

	public String getNomeEntidade() {
		return nomeEntidade;
	}

	public void setNomeEntidade(String nomeEntidade) {
		this.nomeEntidade = nomeEntidade;
	}

	public String getEnderecoEntidade() {
		return enderecoEntidade;
	}

	public void setEnderecoEntidade(String enderecoEntidade) {
		this.enderecoEntidade = enderecoEntidade;
	}

	public String getBairroEntidade() {
		return bairroEntidade;
	}

	public void setBairroEntidade(String bairroEntidade) {
		this.bairroEntidade = bairroEntidade;
	}

	public String getCepEntidade() {
		return cepEntidade;
	}

	public void setCepEntidade(String cepEntidade) {
		this.cepEntidade = cepEntidade;
	}

	public String getCidadeEntidade() {
		return cidadeEntidade;
	}

	public void setCidadeEntidade(String cidadeEntidade) {
		this.cidadeEntidade = cidadeEntidade;
	}

	public String getUfEntidade() {
		return ufEntidade;
	}

	public void setUfEntidade(String ufEntidade) {
		this.ufEntidade = ufEntidade;
	}

	public String getTelefoneEntidade() {
		return telefoneEntidade;
	}

	public void setTelefoneEntidade(String telefoneEntidade) {
		this.telefoneEntidade = telefoneEntidade;
	}

	public String getFaxEntidade() {
		return faxEntidade;
	}

	public void setFaxEntidade(String faxEntidade) {
		this.faxEntidade = faxEntidade;
	}

	public String getCnpjEntidade() {
		return cnpjEntidade;
	}

	public void setCnpjEntidade(String cnpjEntidade) {
		this.cnpjEntidade = cnpjEntidade;
	}

	public String getInscricaoEstadual() {
		return inscricaoEstadual;
	}

	public void setInscricaoEstadual(String inscricaoEstadual) {
		this.inscricaoEstadual = inscricaoEstadual;
	}

	public String getInscricaoMunicipal() {
		return inscricaoMunicipal;
	}

	public void setInscricaoMunicipal(String inscricaoMunicipal) {
		this.inscricaoMunicipal = inscricaoMunicipal;
	}

	public ImportacaoSPC getImportacao() {
		return importacao;
	}

	public void setImportacao(ImportacaoSPC importacao) {
		this.importacao = importacao;
	}
}