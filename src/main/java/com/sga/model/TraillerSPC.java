package com.sga.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_nota_trailler_spc")
public class TraillerSPC {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tipo_registro", length = 1)
	private String tipoRegistro;

	@Column(name = "qtde_total_registros")
	private Integer qtdeTotalRegistros;

	@Column(name = "qtde_total_boletos")
	private Integer qtdeTotalBoletos;

	@Column(name = "valor_total_boletos", precision = 15, scale = 2)
	private BigDecimal valorTotalBoletos;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "importacao_id")
	private ImportacaoSPC importacao;

	public TraillerSPC() {
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

	public Integer getQtdeTotalRegistros() {
		return qtdeTotalRegistros;
	}

	public void setQtdeTotalRegistros(Integer qtdeTotalRegistros) {
		this.qtdeTotalRegistros = qtdeTotalRegistros;
	}

	public Integer getQtdeTotalBoletos() {
		return qtdeTotalBoletos;
	}

	public void setQtdeTotalBoletos(Integer qtdeTotalBoletos) {
		this.qtdeTotalBoletos = qtdeTotalBoletos;
	}

	public BigDecimal getValorTotalBoletos() {
		return valorTotalBoletos;
	}

	public void setValorTotalBoletos(BigDecimal valorTotalBoletos) {
		this.valorTotalBoletos = valorTotalBoletos;
	}

	public ImportacaoSPC getImportacao() {
		return importacao;
	}

	public void setImportacao(ImportacaoSPC importacao) {
		this.importacao = importacao;
	}
}