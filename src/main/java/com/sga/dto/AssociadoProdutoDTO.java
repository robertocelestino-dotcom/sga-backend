package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssociadoProdutoDTO {
	private Long id;

	@NotNull(message = "ID do Associado é obrigatório")
	private Long associadoId;

	@NotNull(message = "ID do Produto é obrigatório")
	private Long produtoId;

	private Long tipoEnvioId;

	private BigDecimal valorDefinido;

	private LocalDate dataAdesao;

	private LocalDate dataInicio;

	private LocalDate dataFim;

	private LocalDate dataReinicio;

	private Boolean envioPadrao = false;

	private Boolean utilizaEnriquecimento = false;

	private Boolean deduzirDoPlano = false;

	private String statusNoProcesso = "A";

	private String tipoProduto;

	private String observacao;

	private String usuario;

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

	public Long getProdutoId() {
		return produtoId;
	}

	public void setProdutoId(Long produtoId) {
		this.produtoId = produtoId;
	}

	public Long getTipoEnvioId() {
		return tipoEnvioId;
	}

	public void setTipoEnvioId(Long tipoEnvioId) {
		this.tipoEnvioId = tipoEnvioId;
	}

	public BigDecimal getValorDefinido() {
		return valorDefinido;
	}

	public void setValorDefinido(BigDecimal valorDefinido) {
		this.valorDefinido = valorDefinido;
	}

	public LocalDate getDataAdesao() {
		return dataAdesao;
	}

	public void setDataAdesao(LocalDate dataAdesao) {
		this.dataAdesao = dataAdesao;
	}

	public LocalDate getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDate dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDate getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDate dataFim) {
		this.dataFim = dataFim;
	}

	public LocalDate getDataReinicio() {
		return dataReinicio;
	}

	public void setDataReinicio(LocalDate dataReinicio) {
		this.dataReinicio = dataReinicio;
	}

	public Boolean getEnvioPadrao() {
		return envioPadrao;
	}

	public void setEnvioPadrao(Boolean envioPadrao) {
		this.envioPadrao = envioPadrao;
	}

	public Boolean getUtilizaEnriquecimento() {
		return utilizaEnriquecimento;
	}

	public void setUtilizaEnriquecimento(Boolean utilizaEnriquecimento) {
		this.utilizaEnriquecimento = utilizaEnriquecimento;
	}

	public Boolean getDeduzirDoPlano() {
		return deduzirDoPlano;
	}

	public void setDeduzirDoPlano(Boolean deduzirDoPlano) {
		this.deduzirDoPlano = deduzirDoPlano;
	}

	public String getStatusNoProcesso() {
		return statusNoProcesso;
	}

	public void setStatusNoProcesso(String statusNoProcesso) {
		this.statusNoProcesso = statusNoProcesso;
	}

	public String getTipoProduto() {
		return tipoProduto;
	}

	public void setTipoProduto(String tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

}