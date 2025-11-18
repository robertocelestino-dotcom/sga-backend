package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_ASSOCIADO_DEF_NOTIFICACAO")
public class AssociadoDefNotificacao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDASSOCIADO")
	private Associado associado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDPRODUTO")
	private ProdutoFaixa produto;

	@Column(name = "VALORDEFINIDO", precision = 15, scale = 4)
	private BigDecimal valorDefinido;

	@Column(name = "DATAADESAO")
	private LocalDateTime dataAdesao;

	@Column(name = "DATAINICIO")
	private LocalDateTime dataInicio;

	@Column(name = "DATAFIM")
	private LocalDateTime dataFim;

	@Column(name = "DATAREINICIO")
	private LocalDateTime dataReinicio;

	@Column(name = "IDTIPOENVIO")
	private Integer idTipoEnvio;

	@Column(name = "ENVIOPADRAO", length = 1)
	private String envioPadrao = "N";

	@Column(name = "UTILIZAENRIQUECIMENTO", length = 1)
	private String utilizaEnriquecimento = "N";

	@Column(name = "DEDUZIRDOPLANO", length = 1)
	private String deduzirDoPlano = "N";

	@Column(name = "STATUSNOPROCESSO", length = 1)
	private String statusNoProcesso = "A";

	@Lob
	@Column(name = "OBSERVACAO")
	private String observacao;

	public AssociadoDefNotificacao() {

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Associado getAssociado() {
		return associado;
	}

	public void setAssociado(Associado associado) {
		this.associado = associado;
	}

	public ProdutoFaixa getProduto() {
		return produto;
	}

	public void setProduto(ProdutoFaixa produto) {
		this.produto = produto;
	}

	public BigDecimal getValorDefinido() {
		return valorDefinido;
	}

	public void setValorDefinido(BigDecimal valorDefinido) {
		this.valorDefinido = valorDefinido;
	}

	public LocalDateTime getDataAdesao() {
		return dataAdesao;
	}

	public void setDataAdesao(LocalDateTime dataAdesao) {
		this.dataAdesao = dataAdesao;
	}

	public LocalDateTime getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(LocalDateTime dataInicio) {
		this.dataInicio = dataInicio;
	}

	public LocalDateTime getDataFim() {
		return dataFim;
	}

	public void setDataFim(LocalDateTime dataFim) {
		this.dataFim = dataFim;
	}

	public LocalDateTime getDataReinicio() {
		return dataReinicio;
	}

	public void setDataReinicio(LocalDateTime dataReinicio) {
		this.dataReinicio = dataReinicio;
	}

	public Integer getIdTipoEnvio() {
		return idTipoEnvio;
	}

	public void setIdTipoEnvio(Integer idTipoEnvio) {
		this.idTipoEnvio = idTipoEnvio;
	}

	public String getEnvioPadrao() {
		return envioPadrao;
	}

	public void setEnvioPadrao(String envioPadrao) {
		this.envioPadrao = envioPadrao;
	}

	public String getUtilizaEnriquecimento() {
		return utilizaEnriquecimento;
	}

	public void setUtilizaEnriquecimento(String utilizaEnriquecimento) {
		this.utilizaEnriquecimento = utilizaEnriquecimento;
	}

	public String getDeduzirDoPlano() {
		return deduzirDoPlano;
	}

	public void setDeduzirDoPlano(String deduzirDoPlano) {
		this.deduzirDoPlano = deduzirDoPlano;
	}

	public String getStatusNoProcesso() {
		return statusNoProcesso;
	}

	public void setStatusNoProcesso(String statusNoProcesso) {
		this.statusNoProcesso = statusNoProcesso;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
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
		AssociadoDefNotificacao other = (AssociadoDefNotificacao) obj;
		return Objects.equals(id, other.id);
	}

}
