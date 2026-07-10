package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_cancelamento_processado")
public class CancelamentoProcessado {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "cancelamento_importacao_id", nullable = false)
	private CancelamentoImportacao cancelamentoImportacao;

	@ManyToOne
	@JoinColumn(name = "fatura_id")
	private Fatura fatura;

	@ManyToOne
	@JoinColumn(name = "fatura_item_id")
	private FaturaItem faturaItem;

	@ManyToOne
	@JoinColumn(name = "item_spc_id")
	private ItemSPC itemSPC;

	@Column(name = "processado_em")
	private LocalDateTime processadoEm;

	@Column(name = "observacao", columnDefinition = "TEXT")
	private String observacao;

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CancelamentoImportacao getCancelamentoImportacao() {
		return cancelamentoImportacao;
	}

	public void setCancelamentoImportacao(CancelamentoImportacao cancelamentoImportacao) {
		this.cancelamentoImportacao = cancelamentoImportacao;
	}

	public Fatura getFatura() {
		return fatura;
	}

	public void setFatura(Fatura fatura) {
		this.fatura = fatura;
	}

	public FaturaItem getFaturaItem() {
		return faturaItem;
	}

	public void setFaturaItem(FaturaItem faturaItem) {
		this.faturaItem = faturaItem;
	}

	public ItemSPC getItemSPC() {
		return itemSPC;
	}

	public void setItemSPC(ItemSPC itemSPC) {
		this.itemSPC = itemSPC;
	}

	public LocalDateTime getProcessadoEm() {
		return processadoEm;
	}

	public void setProcessadoEm(LocalDateTime processadoEm) {
		this.processadoEm = processadoEm;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}
}