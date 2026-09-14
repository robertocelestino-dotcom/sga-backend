package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ConferenciaFaturaDetalheDTO {

	// Dados da Fatura
	private Long faturaId;
	private String numeroFatura;
	private LocalDate dataEmissao;
	private String statusFatura;
	private BigDecimal valorFatura;

	// Dados da Nota
	private Long notaId;
	private String numeroNotaDebito;
	private LocalDate dataVencimento;
	private BigDecimal valorNota;

	// Associado
	private Long associadoId;
	private String codigoSpc;
	private String codigoRm;
	private String nomeRazao;

	// Resumo da Comparação
	private BigDecimal diferencaValor;
	private Integer diferencaItens;
	private String statusConferencia;
	private String observacao;

	// Listas de Itens
	private List<ItemComparacaoDTO> itensNota = new ArrayList<>();
	private List<ItemComparacaoDTO> itensFatura = new ArrayList<>();
	private List<ItemComparacaoDTO> itensRemovidos = new ArrayList<>();
	private List<ItemComparacaoDTO> itensAdicionados = new ArrayList<>();
	private List<ItemComparacaoDTO> itensModificados = new ArrayList<>();

	// Getters e Setters
	public Long getFaturaId() {
		return faturaId;
	}

	public void setFaturaId(Long faturaId) {
		this.faturaId = faturaId;
	}

	public String getNumeroFatura() {
		return numeroFatura;
	}

	public void setNumeroFatura(String numeroFatura) {
		this.numeroFatura = numeroFatura;
	}

	public LocalDate getDataEmissao() {
		return dataEmissao;
	}

	public void setDataEmissao(LocalDate dataEmissao) {
		this.dataEmissao = dataEmissao;
	}

	public String getStatusFatura() {
		return statusFatura;
	}

	public void setStatusFatura(String statusFatura) {
		this.statusFatura = statusFatura;
	}

	public BigDecimal getValorFatura() {
		return valorFatura;
	}

	public void setValorFatura(BigDecimal valorFatura) {
		this.valorFatura = valorFatura;
	}

	public Long getNotaId() {
		return notaId;
	}

	public void setNotaId(Long notaId) {
		this.notaId = notaId;
	}

	public String getNumeroNotaDebito() {
		return numeroNotaDebito;
	}

	public void setNumeroNotaDebito(String numeroNotaDebito) {
		this.numeroNotaDebito = numeroNotaDebito;
	}

	public LocalDate getDataVencimento() {
		return dataVencimento;
	}

	public void setDataVencimento(LocalDate dataVencimento) {
		this.dataVencimento = dataVencimento;
	}

	public BigDecimal getValorNota() {
		return valorNota;
	}

	public void setValorNota(BigDecimal valorNota) {
		this.valorNota = valorNota;
	}

	public Long getAssociadoId() {
		return associadoId;
	}

	public void setAssociadoId(Long associadoId) {
		this.associadoId = associadoId;
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

	public String getNomeRazao() {
		return nomeRazao;
	}

	public void setNomeRazao(String nomeRazao) {
		this.nomeRazao = nomeRazao;
	}

	public BigDecimal getDiferencaValor() {
		return diferencaValor;
	}

	public void setDiferencaValor(BigDecimal diferencaValor) {
		this.diferencaValor = diferencaValor;
	}

	public Integer getDiferencaItens() {
		return diferencaItens;
	}

	public void setDiferencaItens(Integer diferencaItens) {
		this.diferencaItens = diferencaItens;
	}

	public String getStatusConferencia() {
		return statusConferencia;
	}

	public void setStatusConferencia(String statusConferencia) {
		this.statusConferencia = statusConferencia;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public List<ItemComparacaoDTO> getItensNota() {
		return itensNota;
	}

	public void setItensNota(List<ItemComparacaoDTO> itensNota) {
		this.itensNota = itensNota;
	}

	public List<ItemComparacaoDTO> getItensFatura() {
		return itensFatura;
	}

	public void setItensFatura(List<ItemComparacaoDTO> itensFatura) {
		this.itensFatura = itensFatura;
	}

	public List<ItemComparacaoDTO> getItensRemovidos() {
		return itensRemovidos;
	}

	public void setItensRemovidos(List<ItemComparacaoDTO> itensRemovidos) {
		this.itensRemovidos = itensRemovidos;
	}

	public List<ItemComparacaoDTO> getItensAdicionados() {
		return itensAdicionados;
	}

	public void setItensAdicionados(List<ItemComparacaoDTO> itensAdicionados) {
		this.itensAdicionados = itensAdicionados;
	}

	public List<ItemComparacaoDTO> getItensModificados() {
		return itensModificados;
	}

	public void setItensModificados(List<ItemComparacaoDTO> itensModificados) {
		this.itensModificados = itensModificados;
	}

	// Inner Class para Comparação de Itens
	public static class ItemComparacaoDTO {
		private String codigo;
		private String descricao;
		private BigDecimal quantidade;
		private BigDecimal valorUnitario;
		private BigDecimal valorTotal;
		private String tipo; // NOTA, FATURA, AMBOS
		private String status; // OK, REMOVIDO, ADICIONADO, MODIFICADO

		// Getters e Setters
		public String getCodigo() {
			return codigo;
		}

		public void setCodigo(String codigo) {
			this.codigo = codigo;
		}

		public String getDescricao() {
			return descricao;
		}

		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}

		public BigDecimal getQuantidade() {
			return quantidade;
		}

		public void setQuantidade(BigDecimal quantidade) {
			this.quantidade = quantidade;
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

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}
}