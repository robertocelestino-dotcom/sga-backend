package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ConsumoFranquiaDTO {
	private Long id;
	private Long associadoId;
	private String associadoNome;
	private Long planoId;
	private String planoNome;
	private Long produtoId;
	private String produtoNome;
	private Long franquiaId;
	private String franquiaNome;
	private Integer ano;
	private Integer mes;
	private Integer utilizado;
	private Integer limite;
	private Integer disponivel;
	private Integer excedente;
	private BigDecimal valorExcedente;
	private Double percentualUtilizado;
	private String status;
	private LocalDateTime dataUltimoConsumo;

	// Construtores
	public ConsumoFranquiaDTO() {
	}

	// Método para calcular percentual
	public Double getPercentualUtilizado() {
		if (limite == null || limite == 0)
			return 0.0;
		return Double.parseDouble(String.format("%.1f", (utilizado * 100.0) / limite).replace(",", "."));
	}

	// Método para determinar status baseado no consumo
	public String getStatus() {
		if (limite == null || limite == 0)
			return "SEM_LIMITE";
		double percentual = getPercentualUtilizado();
		if (percentual >= 100)
			return "EXCEDIDO";
		if (percentual >= 90)
			return "CRITICO";
		if (percentual >= 70)
			return "ATENCAO";
		return "NORMAL";
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAssociadoId() {
		return associadoId;
	}

	public void setAssociadoId(Long associadoId) {
		this.associadoId = associadoId;
	}

	public String getAssociadoNome() {
		return associadoNome;
	}

	public void setAssociadoNome(String associadoNome) {
		this.associadoNome = associadoNome;
	}

	public Long getPlanoId() {
		return planoId;
	}

	public void setPlanoId(Long planoId) {
		this.planoId = planoId;
	}

	public String getPlanoNome() {
		return planoNome;
	}

	public void setPlanoNome(String planoNome) {
		this.planoNome = planoNome;
	}

	public Long getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(Long produtoId) {
		this.produtoId = produtoId;
	}

	public String getProdutoNome() {
		return produtoNome;
	}

	public void setProdutoNome(String produtoNome) {
		this.produtoNome = produtoNome;
	}

	public Long getFranquiaId() {
		return franquiaId;
	}

	public void setFranquiaId(Long franquiaId) {
		this.franquiaId = franquiaId;
	}

	public String getFranquiaNome() {
		return franquiaNome;
	}

	public void setFranquiaNome(String franquiaNome) {
		this.franquiaNome = franquiaNome;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getMes() {
		return mes;
	}

	public void setMes(Integer mes) {
		this.mes = mes;
	}

	public Integer getUtilizado() {
		return utilizado;
	}

	public void setUtilizado(Integer utilizado) {
		this.utilizado = utilizado;
	}

	public Integer getLimite() {
		return limite;
	}

	public void setLimite(Integer limite) {
		this.limite = limite;
	}

	public Integer getDisponivel() {
		if (limite == null)
			return null;
		return Math.max(0, limite - (utilizado != null ? utilizado : 0));
	}

	public void setDisponivel(Integer disponivel) {
		this.disponivel = disponivel;
	}

	public Integer getExcedente() {
		return excedente;
	}

	public void setExcedente(Integer excedente) {
		this.excedente = excedente;
	}

	public BigDecimal getValorExcedente() {
		return valorExcedente;
	}

	public void setValorExcedente(BigDecimal valorExcedente) {
		this.valorExcedente = valorExcedente;
	}

	public LocalDateTime getDataUltimoConsumo() {
		return dataUltimoConsumo;
	}

	public void setDataUltimoConsumo(LocalDateTime dataUltimoConsumo) {
		this.dataUltimoConsumo = dataUltimoConsumo;
	}
}