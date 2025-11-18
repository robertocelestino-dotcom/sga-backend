package com.sga.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tb_nota_parametros_spc")
public class ParametrosSPC {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tipo_registro", length = 1)
	private String tipoRegistro;

	@Column(name = "data_referencia", length = 8)
	private String dataReferencia;

	@Column(name = "valor_fat_vencto_1", precision = 15, scale = 2)
	private BigDecimal valorFatVencto1;

	@Column(name = "data_1o_vencimento", length = 8)
	private String data1oVencimento;

	@Column(name = "data_2o_vencimento", length = 8)
	private String data2oVencimento;

	@Column(name = "quant_dias_atraso")
	private Integer quantDiasAtraso;

	@Column(name = "perc_juros_apos_vencto", precision = 5, scale = 2)
	private BigDecimal percJurosAposVencto;

	@Column(name = "valor_multa_apos_vencto", precision = 15, scale = 2)
	private BigDecimal valorMultaAposVencto;

	@Column(name = "data_inicio_periodo_ref", length = 8)
	private String dataInicioPeriodoRef;

	@Column(name = "data_fim_periodo_ref", length = 8)
	private String dataFimPeriodoRef;

	@Column(name = "valor_faturamento_minimo", precision = 15, scale = 2)
	private BigDecimal valorFaturamentoMinimo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "importacao_id")
	private ImportacaoSPC importacao;

	public ParametrosSPC() {
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

	public String getDataReferencia() {
		return dataReferencia;
	}

	public void setDataReferencia(String dataReferencia) {
		this.dataReferencia = dataReferencia;
	}

	public BigDecimal getValorFatVencto1() {
		return valorFatVencto1;
	}

	public void setValorFatVencto1(BigDecimal valorFatVencto1) {
		this.valorFatVencto1 = valorFatVencto1;
	}

	public String getData1oVencimento() {
		return data1oVencimento;
	}

	public void setData1oVencimento(String data1oVencimento) {
		this.data1oVencimento = data1oVencimento;
	}

	public String getData2oVencimento() {
		return data2oVencimento;
	}

	public void setData2oVencimento(String data2oVencimento) {
		this.data2oVencimento = data2oVencimento;
	}

	public Integer getQuantDiasAtraso() {
		return quantDiasAtraso;
	}

	public void setQuantDiasAtraso(Integer quantDiasAtraso) {
		this.quantDiasAtraso = quantDiasAtraso;
	}

	public BigDecimal getPercJurosAposVencto() {
		return percJurosAposVencto;
	}

	public void setPercJurosAposVencto(BigDecimal percJurosAposVencto) {
		this.percJurosAposVencto = percJurosAposVencto;
	}

	public BigDecimal getValorMultaAposVencto() {
		return valorMultaAposVencto;
	}

	public void setValorMultaAposVencto(BigDecimal valorMultaAposVencto) {
		this.valorMultaAposVencto = valorMultaAposVencto;
	}

	public String getDataInicioPeriodoRef() {
		return dataInicioPeriodoRef;
	}

	public void setDataInicioPeriodoRef(String dataInicioPeriodoRef) {
		this.dataInicioPeriodoRef = dataInicioPeriodoRef;
	}

	public String getDataFimPeriodoRef() {
		return dataFimPeriodoRef;
	}

	public void setDataFimPeriodoRef(String dataFimPeriodoRef) {
		this.dataFimPeriodoRef = dataFimPeriodoRef;
	}

	public BigDecimal getValorFaturamentoMinimo() {
		return valorFaturamentoMinimo;
	}

	public void setValorFaturamentoMinimo(BigDecimal valorFaturamentoMinimo) {
		this.valorFaturamentoMinimo = valorFaturamentoMinimo;
	}

	public ImportacaoSPC getImportacao() {
		return importacao;
	}

	public void setImportacao(ImportacaoSPC importacao) {
		this.importacao = importacao;
	}
}