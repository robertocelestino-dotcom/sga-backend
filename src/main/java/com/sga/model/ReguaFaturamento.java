package com.sga.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "tb_regua_faturamento")
public class ReguaFaturamento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(columnDefinition = "TEXT")
	private String descricao;

	@Column(name = "dia_emissao", nullable = false)
	private Integer diaEmissao;

	@Column(nullable = false, length = 20)
	private String periodo;

	@Column(nullable = false)
	private Integer sequencia;

	@Column(name = "tipo_arquivo", nullable = false, length = 50)
	private String tipoArquivo;

	@Column(name = "ordem_importacao", nullable = false)
	private Integer ordemImportacao;

	@Column(name = "eh_padrao")
	private Boolean ehPadrao = false;

	private Boolean ativo = true;

	@Column(length = 20)
	private String cor;

	@Column(length = 50)
	private String icone;

	// ========== NOVOS CAMPOS ==========

	@Column(name = "dia_vencimento")
	private Integer diaVencimento;

	@Column(name = "permite_migracao")
	private Boolean permiteMigracao = true;

	@Column(name = "regua_destino_migracao_id")
	private Long reguaDestinoMigracaoId;

	@Column(name = "tipo_processamento", length = 50)
	private String tipoProcessamento;

	@Column(name = "sequencia_arquivos_json", columnDefinition = "TEXT")
	private String sequenciaArquivosJson;

	@Column(name = "aplicar_franquia")
	private Boolean aplicarFranquia = true;

	@Column(name = "aplicar_faturamento_minimo")
	private Boolean aplicarFaturamentoMinimo = false;

	@Column(name = "aplicar_cancelamentos")
	private Boolean aplicarCancelamentos = true;

	// ========== CAMPOS DE AUDITORIA ==========

	@CreationTimestamp
	@Column(name = "criado_em", updatable = false)
	private LocalDateTime criadoEm;

	@UpdateTimestamp
	@Column(name = "atualizado_em")
	private LocalDateTime atualizadoEm;

	@Column(name = "criado_por", length = 100)
	private String criadoPor;

	@Column(name = "atualizado_por", length = 100)
	private String atualizadoPor;

	@Column(name = "tipos_arquivo_json", columnDefinition = "TEXT")
	private String tiposArquivoJson; // JSON com a lista de tipos de arquivo
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "regua_id")
	private List<TipoArquivoRegua> tiposArquivo = new ArrayList<>();
	
	

	// ========== CONSTRUTORES ==========

	public ReguaFaturamento() {
		this.ativo = true;
		this.ehPadrao = false;
		this.permiteMigracao = true;
		this.aplicarFranquia = true;
		this.aplicarCancelamentos = true;
	}

	// ========== GETTERS E SETTERS ==========

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Integer getDiaEmissao() {
		return diaEmissao;
	}

	public void setDiaEmissao(Integer diaEmissao) {
		this.diaEmissao = diaEmissao;
	}

	public String getPeriodo() {
		return periodo;
	}

	public void setPeriodo(String periodo) {
		this.periodo = periodo;
	}

	public Integer getSequencia() {
		return sequencia;
	}

	public void setSequencia(Integer sequencia) {
		this.sequencia = sequencia;
	}

	public String getTipoArquivo() {
		return tipoArquivo;
	}

	public void setTipoArquivo(String tipoArquivo) {
		this.tipoArquivo = tipoArquivo;
	}

	public Integer getOrdemImportacao() {
		return ordemImportacao;
	}

	public void setOrdemImportacao(Integer ordemImportacao) {
		this.ordemImportacao = ordemImportacao;
	}

	public Boolean getEhPadrao() {
		return ehPadrao;
	}

	public void setEhPadrao(Boolean ehPadrao) {
		this.ehPadrao = ehPadrao;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getCor() {
		return cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public String getIcone() {
		return icone;
	}

	public void setIcone(String icone) {
		this.icone = icone;
	}

	public Integer getDiaVencimento() {
		return diaVencimento;
	}

	public void setDiaVencimento(Integer diaVencimento) {
		this.diaVencimento = diaVencimento;
	}

	public Boolean getPermiteMigracao() {
		return permiteMigracao;
	}

	public void setPermiteMigracao(Boolean permiteMigracao) {
		this.permiteMigracao = permiteMigracao;
	}

	public Long getReguaDestinoMigracaoId() {
		return reguaDestinoMigracaoId;
	}

	public void setReguaDestinoMigracaoId(Long reguaDestinoMigracaoId) {
		this.reguaDestinoMigracaoId = reguaDestinoMigracaoId;
	}

	public String getTipoProcessamento() {
		return tipoProcessamento;
	}

	public void setTipoProcessamento(String tipoProcessamento) {
		this.tipoProcessamento = tipoProcessamento;
	}

	public String getSequenciaArquivosJson() {
		return sequenciaArquivosJson;
	}

	public void setSequenciaArquivosJson(String sequenciaArquivosJson) {
		this.sequenciaArquivosJson = sequenciaArquivosJson;
	}

	public Boolean getAplicarFranquia() {
		return aplicarFranquia != null ? aplicarFranquia : true;
	}

	public void setAplicarFranquia(Boolean aplicarFranquia) {
		this.aplicarFranquia = aplicarFranquia;
	}

	public Boolean getAplicarFaturamentoMinimo() {
		return aplicarFaturamentoMinimo != null ? aplicarFaturamentoMinimo : false;
	}

	public void setAplicarFaturamentoMinimo(Boolean aplicarFaturamentoMinimo) {
		this.aplicarFaturamentoMinimo = aplicarFaturamentoMinimo;
	}

	public Boolean getAplicarCancelamentos() {
		return aplicarCancelamentos != null ? aplicarCancelamentos : true;
	}

	public void setAplicarCancelamentos(Boolean aplicarCancelamentos) {
		this.aplicarCancelamentos = aplicarCancelamentos;
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

	public String getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(String criadoPor) {
		this.criadoPor = criadoPor;
	}

	public String getAtualizadoPor() {
		return atualizadoPor;
	}

	public void setAtualizadoPor(String atualizadoPor) {
		this.atualizadoPor = atualizadoPor;
	}

	public String getTiposArquivoJson() {
		return tiposArquivoJson;
	}

	public void setTiposArquivoJson(String tiposArquivoJson) {
		this.tiposArquivoJson = tiposArquivoJson;
	}

	
	// ========== MÉTODOS AUXILIARES ==========

	// Métodos auxiliares para converter lista para JSON e vice-versa
	@Transient
	public List<TipoArquivoRegua> getTiposArquivo() {
		if (tiposArquivoJson == null || tiposArquivoJson.isEmpty()) {
			return new ArrayList<>();
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(tiposArquivoJson, new TypeReference<List<TipoArquivoRegua>>() {
			});
		} catch (JsonProcessingException e) {
			return new ArrayList<>();
		}
	}

	@Transient
	public void setTiposArquivo(List<TipoArquivoRegua> tiposArquivo) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			this.tiposArquivoJson = mapper.writeValueAsString(tiposArquivo);
		} catch (JsonProcessingException e) {
			this.tiposArquivoJson = "[]";
		}
	}

	@Transient
	public List<String> getSequenciaArquivosList() {
		if (sequenciaArquivosJson == null || sequenciaArquivosJson.isEmpty()) {
			return getSequenciaArquivosPadrao();
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(sequenciaArquivosJson, new TypeReference<List<String>>() {
			});
		} catch (JsonProcessingException e) {
			return getSequenciaArquivosPadrao();
		}
	}

	@Transient
	public void setSequenciaArquivosList(List<String> sequenciaArquivos) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			this.sequenciaArquivosJson = mapper.writeValueAsString(sequenciaArquivos);
		} catch (JsonProcessingException e) {
			this.sequenciaArquivosJson = "[]";
		}
	}

	@Transient
	public List<String> getSequenciaArquivosPadrao() {
		List<String> sequencia = new ArrayList<>();

		if (diaEmissao == null)
			return sequencia;

		if (diaEmissao == 1 || diaEmissao == 2) {
			sequencia.add("CONSOLIDADO");
			sequencia.add("PREVIA_ANTERIOR");
			sequencia.add("PREVIA_CORRENTE");
		} else if (diaEmissao == 16) {
			sequencia.add("PREVIA_ANTERIOR");
			sequencia.add("CONSOLIDADO");
			sequencia.add("PREVIA_CORRENTE");
		} else if (diaEmissao == 26) {
			sequencia.add("CONSOLIDADO");
		}

		return sequencia;
	}

	@Transient
	public boolean isExtemporaneo() {
		return diaEmissao != null && (diaEmissao == 1 || diaEmissao == 2 || diaEmissao == 16);
	}

	@Transient
	public boolean isPrimeiroPeriodo() {
		return diaEmissao != null && (diaEmissao == 1 || diaEmissao == 2);
	}

	@Transient
	public boolean isSegundoPeriodo() {
		return diaEmissao != null && diaEmissao == 16;
	}

	@Transient
	public boolean isTerceiroPeriodo() {
		return diaEmissao != null && diaEmissao == 26;
	}

	@Transient
	public String getPeriodoDescricao() {
		if (isPrimeiroPeriodo()) {
			return "1º Período (Dia " + diaEmissao + ")";
		} else if (isSegundoPeriodo()) {
			return "2º Período (Dia 16)";
		} else if (isTerceiroPeriodo()) {
			return "3º Período (Dia 26 - Padrão)";
		}
		return "Período " + periodo;
	}
}