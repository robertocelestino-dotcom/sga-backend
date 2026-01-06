package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

import lombok.Data;

@Data
@Entity
@Table(name = "TB_ASSOCIADO")
public class Associado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDVENDEDOR")
	private Vendedor vendedor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDPLANO")
	private Planos plano;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDCATEGORIA")
	private Categoria categoria;

	@Column(name = "CODIGOSPC", length = 50)
	private String codigoSpc;

	@Column(name = "CODIGORM", length = 50)
	private String codigoRm;

	@Column(name = "TIPOPESSOA", nullable = false, length = 1)
	private String tipoPessoa;

	@Column(name = "CNPJCPF", nullable = false, length = 20)
	private String cnpjCpf;

	@Column(name = "NOMERAZAO", nullable = false)
	private String nomeRazao;

	@Column(name = "NOMEFANTASIA")
	private String nomeFantasia;

	@Column(name = "STATUS", length = 1)
	private String status = "A";

	@Column(name = "FATURAMENTO_MINIMO", precision = 15, scale = 2)
	private BigDecimal faturamentoMinimo;

	@Column(name = "DATA_CADASTRO")
	private LocalDateTime dataCadastro;// = LocalDateTime.now();

	@OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Endereco> enderecos;

	@OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Email> emails;

	@OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Telefone> telefones;

	@OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<AssociadoDefNotificacao> definicoesNotificacao;

	@OneToMany(mappedBy = "associado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<AssociadoDefFaturamento> definicoesFaturamento;

	public Associado(Long id) {
		
	}

	public Associado() {
	
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public Planos getPlano() {
		return plano;
	}

	public void setPlano(Planos plano) {
		this.plano = plano;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public String getCodigoSpc() {
		return codigoSpc;
	}

	public void setCodigoSpc(String codigoSpc) {
		this.codigoSpc = codigoSpc;
	}

	public String getCodigoRm() {
		return codigoRm;
	}

	public void setCodigoRm(String codigoRm) {
		this.codigoRm = codigoRm;
	}

	public String getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	public String getCnpjCpf() {
		return cnpjCpf;
	}

	public void setCnpjCpf(String cnpjCpf) {
		this.cnpjCpf = cnpjCpf;
	}

	public String getNomeRazao() {
		return nomeRazao;
	}

	public void setNomeRazao(String nomeRazao) {
		this.nomeRazao = nomeRazao;
	}

	public String getNomeFantasia() {
		return nomeFantasia;
	}

	public void setNomeFantasia(String nomeFantasia) {
		this.nomeFantasia = nomeFantasia;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BigDecimal getFaturamentoMinimo() {
		return faturamentoMinimo;
	}

	public void setFaturamentoMinimo(BigDecimal faturamentoMinimo) {
		this.faturamentoMinimo = faturamentoMinimo;
	}

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public List<Endereco> getEnderecos() {
		return enderecos;
	}

	public void setEnderecos(List<Endereco> enderecos) {
		this.enderecos = enderecos;
	}

	public List<Email> getEmails() {
		return emails;
	}

	public void setEmails(List<Email> emails) {
		this.emails = emails;
	}

	public List<Telefone> getTelefones() {
		return telefones;
	}

	public void setTelefones(List<Telefone> telefones) {
		this.telefones = telefones;
	}

	public List<AssociadoDefNotificacao> getDefinicoesNotificacao() {
		return definicoesNotificacao;
	}

	public void setDefinicoesNotificacao(List<AssociadoDefNotificacao> definicoesNotificacao) {
		this.definicoesNotificacao = definicoesNotificacao;
	}

	public List<AssociadoDefFaturamento> getDefinicoesFaturamento() {
		return definicoesFaturamento;
	}

	public void setDefinicoesFaturamento(List<AssociadoDefFaturamento> definicoesFaturamento) {
		this.definicoesFaturamento = definicoesFaturamento;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Associado other = (Associado) obj;
		return Objects.equals(id, other.id);
	}

}
