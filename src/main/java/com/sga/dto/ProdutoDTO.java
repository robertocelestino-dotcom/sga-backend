// src/main/java/com/sga/dto/ProdutoDTO.java
package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ProdutoDTO {
	private Long id;
	private String codigo;
	private String codigoRm;
	private String nome;
	private String descricao;
	private BigDecimal valorUnitario;
	private String status = "ATIVO";
	private String tipoProduto = "SERVICO";
	private String unidadeMedida = "UNIDADE";
	private String categoria;
	private String modalidade;
	private Boolean temFranquia = false;
	private Integer limiteFranquia;
	private String periodoFranquia;

	// Relacionamentos
	private Set<Long> franquiasIds = new HashSet<>();
	private Set<Long> produtosRelacionadosIds = new HashSet<>();

	// Regras de faturamento
	private Boolean geraCobrancaAutomatica = true;
	private Boolean cobrancaPeriodica = false;
	private String periodicidadeCobranca;
	private Integer diaCobranca;
	private Boolean permiteDesconto = true;
	private BigDecimal descontoMaximo = BigDecimal.ZERO;
	private Boolean exigeAutorizacao = false;
	private Integer nivelAutorizacao;

	// Auditoria
	private LocalDateTime criadoEm;
	private LocalDateTime atualizadoEm;
	private String usuarioCriacao;
	private String usuarioAtualizacao;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigoRm() {
		return codigoRm;
	}

	public void setCodigoRm(String codigoRm) {
		this.codigoRm = codigoRm;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public BigDecimal getValorUnitario() {
		return valorUnitario;
	}

	public void setValorUnitario(BigDecimal valorUnitario) {
		this.valorUnitario = valorUnitario;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTipoProduto() {
		return tipoProduto;
	}

	public void setTipoProduto(String tipoProduto) {
		this.tipoProduto = tipoProduto;
	}

	public String getUnidadeMedida() {
		return unidadeMedida;
	}

	public void setUnidadeMedida(String unidadeMedida) {
		this.unidadeMedida = unidadeMedida;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getModalidade() {
		return modalidade;
	}

	public void setModalidade(String modalidade) {
		this.modalidade = modalidade;
	}

	public Boolean getTemFranquia() {
		return temFranquia;
	}

	public void setTemFranquia(Boolean temFranquia) {
		this.temFranquia = temFranquia;
	}

	public Integer getLimiteFranquia() {
		return limiteFranquia;
	}

	public void setLimiteFranquia(Integer limiteFranquia) {
		this.limiteFranquia = limiteFranquia;
	}

	public String getPeriodoFranquia() {
		return periodoFranquia;
	}

	public void setPeriodoFranquia(String periodoFranquia) {
		this.periodoFranquia = periodoFranquia;
	}

	public Set<Long> getFranquiasIds() {
		return franquiasIds;
	}

	public void setFranquiasIds(Set<Long> franquiasIds) {
		this.franquiasIds = franquiasIds;
	}

	public Set<Long> getProdutosRelacionadosIds() {
		return produtosRelacionadosIds;
	}

	public void setProdutosRelacionadosIds(Set<Long> produtosRelacionadosIds) {
		this.produtosRelacionadosIds = produtosRelacionadosIds;
	}

	public Boolean getGeraCobrancaAutomatica() {
		return geraCobrancaAutomatica;
	}

	public void setGeraCobrancaAutomatica(Boolean geraCobrancaAutomatica) {
		this.geraCobrancaAutomatica = geraCobrancaAutomatica;
	}

	public Boolean getCobrancaPeriodica() {
		return cobrancaPeriodica;
	}

	public void setCobrancaPeriodica(Boolean cobrancaPeriodica) {
		this.cobrancaPeriodica = cobrancaPeriodica;
	}

	public String getPeriodicidadeCobranca() {
		return periodicidadeCobranca;
	}

	public void setPeriodicidadeCobranca(String periodicidadeCobranca) {
		this.periodicidadeCobranca = periodicidadeCobranca;
	}

	public Integer getDiaCobranca() {
		return diaCobranca;
	}

	public void setDiaCobranca(Integer diaCobranca) {
		this.diaCobranca = diaCobranca;
	}

	public Boolean getPermiteDesconto() {
		return permiteDesconto;
	}

	public void setPermiteDesconto(Boolean permiteDesconto) {
		this.permiteDesconto = permiteDesconto;
	}

	public BigDecimal getDescontoMaximo() {
		return descontoMaximo;
	}

	public void setDescontoMaximo(BigDecimal descontoMaximo) {
		this.descontoMaximo = descontoMaximo;
	}

	public Boolean getExigeAutorizacao() {
		return exigeAutorizacao;
	}

	public void setExigeAutorizacao(Boolean exigeAutorizacao) {
		this.exigeAutorizacao = exigeAutorizacao;
	}

	public Integer getNivelAutorizacao() {
		return nivelAutorizacao;
	}

	public void setNivelAutorizacao(Integer nivelAutorizacao) {
		this.nivelAutorizacao = nivelAutorizacao;
	}

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public String getUsuarioCriacao() {
		return usuarioCriacao;
	}

	public void setUsuarioCriacao(String usuarioCriacao) {
		this.usuarioCriacao = usuarioCriacao;
	}

	public String getUsuarioAtualizacao() {
		return usuarioAtualizacao;
	}

	public void setUsuarioAtualizacao(String usuarioAtualizacao) {
		this.usuarioAtualizacao = usuarioAtualizacao;
	}

}