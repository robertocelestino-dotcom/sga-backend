package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "tb_sistema_log")
public class SistemaLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tabela_afetada", nullable = false, length = 100)
	private String tabelaAfetada;

	@Column(name = "id_registro")
	private Long idRegistro;

	@Column(name = "acao", nullable = false, length = 50)
	private String acao; // CREATE, UPDATE, DELETE, READ, IMPORT, EXPORT, LOGIN, LOGOUT

	@Column(name = "usuario_id")
	private Long usuarioId;

	@Column(name = "usuario_nome", length = 255)
	private String usuarioNome;

	@Column(name = "endereco_ip", length = 50)
	private String enderecoIp;

	@Column(name = "user_agent", length = 500)
	private String userAgent;

	@Column(name = "dados_anteriores", columnDefinition = "TEXT")
	private String dadosAnteriores;

	@Column(name = "dados_novos", columnDefinition = "TEXT")
	private String dadosNovos;

	@Column(name = "diferencas", columnDefinition = "TEXT")
	private String diferencas;

	@Column(name = "observacao", columnDefinition = "TEXT")
	private String observacao;

	@Column(name = "data_hora", nullable = false)
	private LocalDateTime dataHora;

	@Column(name = "sucesso")
	private Boolean sucesso = true;

	@Column(name = "mensagem_erro", columnDefinition = "TEXT")
	private String mensagemErro;

	@Column(name = "tempo_execucao_ms")
	private Long tempoExecucaoMs;

	@Column(name = "modulo", length = 100)
	private String modulo; // ASSOCIADOS, PRODUTOS, VENDEDORES, FINANCEIRO, ETC

	@Column(name = "operacao", length = 200)
	private String operacao; // Método/endpoint executado

	// Construtor padrão
	public SistemaLog() {
		this.dataHora = LocalDateTime.now();
	}

	// Construtor simplificado
	public SistemaLog(String tabelaAfetada, Long idRegistro, String acao, String modulo) {
		this();
		this.tabelaAfetada = tabelaAfetada;
		this.idRegistro = idRegistro;
		this.acao = acao;
		this.modulo = modulo;
	}

	// Getters e Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTabelaAfetada() {
		return tabelaAfetada;
	}

	public void setTabelaAfetada(String tabelaAfetada) {
		this.tabelaAfetada = tabelaAfetada;
	}

	public Long getIdRegistro() {
		return idRegistro;
	}

	public void setIdRegistro(Long idRegistro) {
		this.idRegistro = idRegistro;
	}

	public String getAcao() {
		return acao;
	}

	public void setAcao(String acao) {
		this.acao = acao;
	}

	public Long getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(Long usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getUsuarioNome() {
		return usuarioNome;
	}

	public void setUsuarioNome(String usuarioNome) {
		this.usuarioNome = usuarioNome;
	}

	public String getEnderecoIp() {
		return enderecoIp;
	}

	public void setEnderecoIp(String enderecoIp) {
		this.enderecoIp = enderecoIp;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getDadosAnteriores() {
		return dadosAnteriores;
	}

	public void setDadosAnteriores(String dadosAnteriores) {
		this.dadosAnteriores = dadosAnteriores;
	}

	public String getDadosNovos() {
		return dadosNovos;
	}

	public void setDadosNovos(String dadosNovos) {
		this.dadosNovos = dadosNovos;
	}

	public String getDiferencas() {
		return diferencas;
	}

	public void setDiferencas(String diferencas) {
		this.diferencas = diferencas;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}

	public Boolean getSucesso() {
		return sucesso;
	}

	public void setSucesso(Boolean sucesso) {
		this.sucesso = sucesso;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public Long getTempoExecucaoMs() {
		return tempoExecucaoMs;
	}

	public void setTempoExecucaoMs(Long tempoExecucaoMs) {
		this.tempoExecucaoMs = tempoExecucaoMs;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public String getOperacao() {
		return operacao;
	}

	public void setOperacao(String operacao) {
		this.operacao = operacao;
	}
}