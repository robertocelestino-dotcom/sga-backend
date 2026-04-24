package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tb_fatura_itens")
public class FaturaItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "fatura_id", nullable = false)
	private Fatura fatura;

	@Column(name = "item_spc_id")
	private Long itemSpcId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "produto_id")
	private Produto produto;

	@Column(name = "codigo_produto", length = 50)
	private String codigoProduto;

	@Column(nullable = false, length = 500)
	private String descricao;

	@Column(nullable = false, precision = 15, scale = 4)
	private BigDecimal quantidade = BigDecimal.ONE;

	@Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
	private BigDecimal valorUnitario = BigDecimal.ZERO;

	@Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
	private BigDecimal valorTotal = BigDecimal.ZERO;

	@Column(name = "tipo_lancamento", nullable = false, length = 1)
	private String tipoLancamento; // D = Débito, C = Crédito

	@Column(name = "aplicou_regra_franquia")
	private Boolean aplicouRegraFranquia = false;

	@Column(name = "aplicou_regra_minimo")
	private Boolean aplicouRegraMinimo = false;

	private Boolean cancelado = false;

	@Column(name = "cancelamento_importacao_id")
	private Long cancelamentoImportacaoId;

	@CreationTimestamp
	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	// ==================== CONSTRUTORES ====================
	public FaturaItem() {
	}

	public FaturaItem(String descricao, BigDecimal quantidade, BigDecimal valorUnitario, String tipoLancamento) {
		this.descricao = descricao;
		this.quantidade = quantidade;
		this.valorUnitario = valorUnitario;
		this.tipoLancamento = tipoLancamento;
		calcularValorTotal();
	}

	public FaturaItem(String descricao, Integer quantidade, BigDecimal valorUnitario, String tipoLancamento) {
		this.descricao = descricao;
		this.quantidade = BigDecimal.valueOf(quantidade);
		this.valorUnitario = valorUnitario;
		this.tipoLancamento = tipoLancamento;
		calcularValorTotal();
	}

	// ==================== GETTERS E SETTERS ====================
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Fatura getFatura() {
		return fatura;
	}

	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}

	public Long getItemSpcId() {
		return itemSpcId;
	}

	public void setItemSpcId(Long itemSpcId) {
		this.itemSpcId = itemSpcId;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public String getCodigoProduto() {
		return codigoProduto;
	}

	public void setCodigoProduto(String codigoProduto) {
		this.codigoProduto = codigoProduto;
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

	// 🔥 MÉTODO PARA SETAR QUANTIDADE COM INTEGER (usado na regra de franquia)
	public void setQuantidade(Integer quantidade) {
		if (quantidade != null) {
			this.quantidade = BigDecimal.valueOf(quantidade);
			calcularValorTotal();
			if (fatura != null) {
				fatura.recalcularTotal();
			}
		}
	}

	public void setQuantidade(BigDecimal quantidade) {
		this.quantidade = quantidade;
		calcularValorTotal();
		if (fatura != null) {
			fatura.recalcularTotal();
		}
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
		calcularValorTotal();
		if (fatura != null) {
			fatura.recalcularTotal();
		}
	}

	public BigDecimal getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(BigDecimal valorTotal) {
		this.valorTotal = valorTotal;
	}

	public String getTipoLancamento() {
		return tipoLancamento;
	}

	public void setTipoLancamento(String tipoLancamento) {
		this.tipoLancamento = tipoLancamento;
	}

	public Boolean getAplicouRegraFranquia() {
		return aplicouRegraFranquia;
	}

	public void setAplicouRegraFranquia(Boolean aplicouRegraFranquia) {
		this.aplicouRegraFranquia = aplicouRegraFranquia;
	}

	public Boolean getAplicouRegraMinimo() {
		return aplicouRegraMinimo;
	}

	public void setAplicouRegraMinimo(Boolean aplicouRegraMinimo) {
		this.aplicouRegraMinimo = aplicouRegraMinimo;
	}

	public Boolean getCancelado() {
		return cancelado;
	}

	public void setCancelado(Boolean cancelado) {
		this.cancelado = cancelado;
	}

	public Long getCancelamentoImportacaoId() {
		return cancelamentoImportacaoId;
	}

	public void setCancelamentoImportacaoId(Long cancelamentoImportacaoId) {
		this.cancelamentoImportacaoId = cancelamentoImportacaoId;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	// ==================== MÉTODOS AUXILIARES ====================

	@PrePersist
	@PreUpdate
	public void calcularValorTotal() {
		if (quantidade != null && valorUnitario != null) {
			this.valorTotal = quantidade.multiply(valorUnitario);
		}
	}

	public void atualizarValorTotal() {
		calcularValorTotal();
		if (fatura != null) {
			fatura.recalcularTotal();
		}
	}

	public boolean isDebito() {
		return "D".equals(tipoLancamento);
	}

	public boolean isCredito() {
		return "C".equals(tipoLancamento);
	}

	/**
	 * Converte para Integer a quantidade (usado na regra de franquia)
	 */
	public Integer getQuantidadeAsInteger() {
		return quantidade != null ? quantidade.intValue() : 0;
	}

	/**
	 * Verifica se o item é do tipo franquia
	 */
	public boolean isItemFranquia() {
		return descricao != null && descricao.toUpperCase().contains("FRANQUIA DE CONSULTA");
	}

	/**
	 * Verifica se o item é do tipo complemento faturamento mínimo
	 */
	public boolean isItemComplementoMinimo() {
		return descricao != null && descricao.toUpperCase().contains("COMPLEMENTO FATURAMENTO MINIMO");
	}
}