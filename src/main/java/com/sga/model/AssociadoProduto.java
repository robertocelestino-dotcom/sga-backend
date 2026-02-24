package com.sga.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TB_ASSOCIADO_PRODUTO")
public class AssociadoProduto {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idassociado", nullable = false)
	private Associado associado;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idproduto", nullable = false)
	private Produto produto;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "idtipo_envio")
	private TipoEnvio tipoEnvio;

	@Column(name = "valor_definido", precision = 15, scale = 4)
	private BigDecimal valorDefinido;

	@Column(name = "data_adesao")
	private LocalDate dataAdesao;

	@Column(name = "data_inicio")
	private LocalDate dataInicio;

	@Column(name = "data_fim")
	private LocalDate dataFim;

	@Column(name = "data_reinicio")
	private LocalDate dataReinicio;

	@Column(name = "envio_padrao", length = 1)
	private String envioPadrao = "N";

	@Column(name = "utiliza_enriquecimento", length = 1)
	private String utilizaEnriquecimento = "N";

	@Column(name = "deduzir_do_plano", length = 1)
	private String deduzirDoPlano = "N";

	@Column(name = "status_no_processo", length = 1)
	private String statusNoProcesso = "A";

	@Column(name = "tipo_produto", length = 50)
	private String tipoProduto;

	@Lob
	@Column(name = "observacao")
	private String observacao;

	@Column(name = "data_criacao")
	private LocalDateTime dataCriacao;

	@Column(name = "data_alteracao")
	private LocalDateTime dataAlteracao;

	@Column(name = "usuario_criacao", length = 100)
	private String usuarioCriacao;

	@Column(name = "usuario_alteracao", length = 100)
	private String usuarioAlteracao;

	@PrePersist
	protected void onCreate() {
		dataCriacao = LocalDateTime.now();
		if (envioPadrao == null)
			envioPadrao = "N";
		if (utilizaEnriquecimento == null)
			utilizaEnriquecimento = "N";
		if (deduzirDoPlano == null)
			deduzirDoPlano = "N";
		if (statusNoProcesso == null)
			statusNoProcesso = "A";
	}

	@PreUpdate
	protected void onUpdate() {
		dataAlteracao = LocalDateTime.now();
	}

	// Métodos utilitários
	public boolean isEnvioPadrao() {
		return "S".equalsIgnoreCase(envioPadrao);
	}

	public boolean isUtilizaEnriquecimento() {
		return "S".equalsIgnoreCase(utilizaEnriquecimento);
	}

	public boolean isDeduzirDoPlano() {
		return "S".equalsIgnoreCase(deduzirDoPlano);
	}

	public boolean isAtivo() {
		return "A".equalsIgnoreCase(statusNoProcesso);
	}

	public BigDecimal getValorEfetivo() {
		return valorDefinido != null ? valorDefinido : produto.getValorUnitario();
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

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public TipoEnvio getTipoEnvio() {
		return tipoEnvio;
	}

	public void setTipoEnvio(TipoEnvio tipoEnvio) {
		this.tipoEnvio = tipoEnvio;
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

	public LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	public LocalDateTime getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(LocalDateTime dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}

	public String getUsuarioCriacao() {
		return usuarioCriacao;
	}

	public void setUsuarioCriacao(String usuarioCriacao) {
		this.usuarioCriacao = usuarioCriacao;
	}

	public String getUsuarioAlteracao() {
		return usuarioAlteracao;
	}

	public void setUsuarioAlteracao(String usuarioAlteracao) {
		this.usuarioAlteracao = usuarioAlteracao;
	}

}