package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import javax.persistence.Transient;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_ASSOCIADO")
public class Associado {

	public static final String STATUS_ATIVO = "A";
	public static final String STATUS_INATIVO = "I";
	public static final String STATUS_SUSPENSO = "S";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDVENDEDOR")
	private Vendedor vendedor;

	// Coluna no banco para armazenar o ID do vendedor externo
	@Column(name = "idvendedor_externo", insertable = false, updatable = false)
	private Integer idVendedorExterno;

	// Relacionamento com vendedor externo
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idvendedor_externo", referencedColumnName = "id")
	private Vendedor vendedorExterno;

	@Column(name = "data_filiacao")
	private LocalDate dataFiliacao;

	@Column(name = "data_inativacao") // NOVO CAMPO
	private LocalDate dataInativacao;

	@Column(name = "data_inicio_suspensao") // NOVO CAMPO
	private LocalDate dataInicioSuspensao;

	@Column(name = "data_fim_suspensao") // NOVO CAMPO
	private LocalDate dataFimSuspensao;

	@Column(name = "motivo_inativacao", length = 500) // NOVO CAMPO
	private String motivoInativacao;

	@Column(name = "motivo_suspensao", length = 500) // NOVO CAMPO
	private String motivoSuspensao;

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
	private String status = "A"; // A=Ativo, I=Inativo, S=Suspenso

	@Column(name = "FATURAMENTO_MINIMO", precision = 15, scale = 2)
	private BigDecimal faturamentoMinimo;

	@Column(name = "DATA_CADASTRO")
	private LocalDateTime dataCadastro;

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

	// Campos transientes para controle
	@Transient
	private boolean statusChanged = false;

	@Transient
	private String statusAnterior;

	// Adicionar métodos auxiliares
	public boolean isAtivo() {
		return STATUS_ATIVO.equals(status);
	}

	public boolean isInativo() {
		return STATUS_INATIVO.equals(status);
	}

	public boolean isSuspenso() {
		return STATUS_SUSPENSO.equals(status);
	}

	// Método para mudar status com validação
	public void mudarStatus(String novoStatus, String motivo, LocalDate dataFimSuspensao) {
		this.statusAnterior = this.status;
		this.status = novoStatus;
		this.statusChanged = true;

		if (STATUS_INATIVO.equals(novoStatus)) {
			this.dataInativacao = LocalDate.now();
			this.motivoInativacao = motivo;
			// Limpar suspensão
			this.dataInicioSuspensao = null;
			this.dataFimSuspensao = null;
			this.motivoSuspensao = null;
		} else if (STATUS_SUSPENSO.equals(novoStatus)) {
			this.dataInicioSuspensao = LocalDate.now();
			this.dataFimSuspensao = dataFimSuspensao;
			this.motivoSuspensao = motivo;
			// Limpar inativação
			this.dataInativacao = null;
			this.motivoInativacao = null;
		} else if (STATUS_ATIVO.equals(novoStatus)) {
			// Reativar - limpar tudo
			this.dataInativacao = null;
			this.dataInicioSuspensao = null;
			this.dataFimSuspensao = null;
			this.motivoInativacao = null;
			this.motivoSuspensao = null;
		}
	}

	// Validar se pode mudar para o novo status
	public boolean podeMudarPara(String novoStatus) {
		if (status == null || novoStatus == null)
			return true;

		// Se for o mesmo status, pode "mudar" (atualizar motivo/data)
		if (status.equals(novoStatus))
			return true;

		// Regras específicas
		if (STATUS_INATIVO.equals(status)) {
			// De inativo só pode voltar para ativo
			return STATUS_ATIVO.equals(novoStatus);
		}

		// Demais transições são permitidas
		return true;
	}

	// Verificar se suspensão expirou
	@Transient
	public boolean isSuspensaoExpirada() {
		if (!isSuspenso() || dataFimSuspensao == null)
			return false;
		return LocalDate.now().isAfter(dataFimSuspensao);
	}

	// Verificar status considerando datas
	@Transient
	public String getStatusCompleto() {
		if (isSuspenso() && isSuspensaoExpirada()) {
			return "Suspensão Expirada";
		}
		if (isAtivo())
			return "Ativo";
		if (isInativo())
			return "Inativo";
		if (isSuspenso())
			return "Suspenso";
		return "Desconhecido";
	}

	public Associado(Long id) {
		this.id = id;
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

	public Integer getIdVendedorExterno() {
		return idVendedorExterno;
	}

	public void setIdVendedorExterno(Integer idVendedorExterno) {
		this.idVendedorExterno = idVendedorExterno;
	}

	public Vendedor getVendedorExterno() {
		return vendedorExterno;
	}

	public void setVendedorExterno(Vendedor vendedorExterno) {
		this.vendedorExterno = vendedorExterno;
	}

	public LocalDate getDataFiliacao() {
		return dataFiliacao;
	}

	public void setDataFiliacao(LocalDate dataFiliacao) {
		this.dataFiliacao = dataFiliacao;
	}

	public LocalDate getDataInativacao() {
		return dataInativacao;
	}

	public void setDataInativacao(LocalDate dataInativacao) {
		this.dataInativacao = dataInativacao;
	}

	public LocalDate getDataInicioSuspensao() {
		return dataInicioSuspensao;
	}

	public void setDataInicioSuspensao(LocalDate dataInicioSuspensao) {
		this.dataInicioSuspensao = dataInicioSuspensao;
	}

	public LocalDate getDataFimSuspensao() {
		return dataFimSuspensao;
	}

	public void setDataFimSuspensao(LocalDate dataFimSuspensao) {
		this.dataFimSuspensao = dataFimSuspensao;
	}

	public String getMotivoInativacao() {
		return motivoInativacao;
	}

	public void setMotivoInativacao(String motivoInativacao) {
		this.motivoInativacao = motivoInativacao;
	}

	public String getMotivoSuspensao() {
		return motivoSuspensao;
	}

	public void setMotivoSuspensao(String motivoSuspensao) {
		this.motivoSuspensao = motivoSuspensao;
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

	public boolean isStatusChanged() {
		return statusChanged;
	}

	public void setStatusChanged(boolean statusChanged) {
		this.statusChanged = statusChanged;
	}

	public String getStatusAnterior() {
		return statusAnterior;
	}

	public void setStatusAnterior(String statusAnterior) {
		this.statusAnterior = statusAnterior;
	}

	// Métodos auxiliares
	public Integer getIdVendedorExternoValue() {
		if (vendedorExterno != null) {
			return vendedorExterno.getId() != null ? vendedorExterno.getId().intValue() : null;
		}
		return idVendedorExterno;
	}

	public void setVendedorExternoById(Integer id) {
		if (id != null) {
			this.vendedorExterno = new Vendedor();
			this.vendedorExterno.setId(id.longValue());
		} else {
			this.vendedorExterno = null;
		}
	}

	// Método para validar status
	public void validarStatus() {
		if ("I".equals(status)) {
			// Se status é Inativo, deve ter data de inativação
			if (dataInativacao == null) {
				dataInativacao = LocalDate.now();
			}
			// Limpar datas de suspensão se existirem
			dataInicioSuspensao = null;
			dataFimSuspensao = null;
			motivoSuspensao = null;
		} else if ("S".equals(status)) {
			// Se status é Suspenso, deve ter datas de início e fim
			if (dataInicioSuspensao == null) {
				dataInicioSuspensao = LocalDate.now();
			}
			if (dataFimSuspensao == null) {
				dataFimSuspensao = dataInicioSuspensao.plusMonths(1); // Padrão: 1 mês
			}
			// Limpar data de inativação se existir
			dataInativacao = null;
			motivoInativacao = null;
		} else if ("A".equals(status)) {
			// Se status é Ativo, limpar todas as datas
			dataInativacao = null;
			dataInicioSuspensao = null;
			dataFimSuspensao = null;
			motivoInativacao = null;
			motivoSuspensao = null;
		}
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