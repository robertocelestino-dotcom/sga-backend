// src/main/java/com/sga/dto/ReguaFaturamentoDTO.java

package com.sga.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReguaFaturamentoDTO {

    private Long id;
    private String nome;
    private String descricao;
    private Integer diaEmissao;
    private String periodo;
    private Integer sequencia;
    private String tipoArquivo;
    private Integer ordemImportacao;
    private Boolean ehPadrao;
    private Boolean ativo;
    private String cor;
    private String icone;
    
    // 🔥 TIPOS DE ARQUIVO (lista)
    private List<TipoArquivoReguaDTO> tiposArquivo = new ArrayList<>();
    
    // Campos de auditoria
    private LocalDateTime criadoEm;
    private String criadoPor;
    private LocalDateTime atualizadoEm;
    private String atualizadoPor;
    
    // Construtores
    public ReguaFaturamentoDTO() {
        this.tiposArquivo = new ArrayList<>();
        this.ativo = true;
        this.ehPadrao = false;
    }
    
    public ReguaFaturamentoDTO(Long id, String nome, String descricao, Integer diaEmissao, 
                               String periodo, Integer sequencia, String tipoArquivo, 
                               Integer ordemImportacao, Boolean ehPadrao, Boolean ativo,
                               String cor, String icone, LocalDateTime criadoEm, 
                               String criadoPor, LocalDateTime atualizadoEm, String atualizadoPor) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.diaEmissao = diaEmissao;
        this.periodo = periodo;
        this.sequencia = sequencia;
        this.tipoArquivo = tipoArquivo;
        this.ordemImportacao = ordemImportacao;
        this.ehPadrao = ehPadrao;
        this.ativo = ativo;
        this.cor = cor;
        this.icone = icone;
        this.criadoEm = criadoEm;
        this.criadoPor = criadoPor;
        this.atualizadoEm = atualizadoEm;
        this.atualizadoPor = atualizadoPor;
        this.tiposArquivo = new ArrayList<>();
    }
    
    // Getters e Setters
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
    
    public List<TipoArquivoReguaDTO> getTiposArquivo() {
        return tiposArquivo;
    }
    
    public void setTiposArquivo(List<TipoArquivoReguaDTO> tiposArquivo) {
        this.tiposArquivo = tiposArquivo != null ? tiposArquivo : new ArrayList<>();
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public String getCriadoPor() {
        return criadoPor;
    }
    
    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
    
    public String getAtualizadoPor() {
        return atualizadoPor;
    }
    
    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Adiciona um tipo de arquivo à lista
     */
    public void addTipoArquivo(TipoArquivoReguaDTO tipo) {
        if (this.tiposArquivo == null) {
            this.tiposArquivo = new ArrayList<>();
        }
        this.tiposArquivo.add(tipo);
    }
    
    /**
     * Remove um tipo de arquivo da lista
     */
    public void removeTipoArquivo(TipoArquivoReguaDTO tipo) {
        if (this.tiposArquivo != null) {
            this.tiposArquivo.remove(tipo);
        }
    }
    
    /**
     * Verifica se a régua é do primeiro período
     */
    public boolean isPrimeiroPeriodo() {
        return diaEmissao != null && (diaEmissao == 1 || diaEmissao == 2);
    }
    
    /**
     * Verifica se a régua é do segundo período
     */
    public boolean isSegundoPeriodo() {
        return diaEmissao != null && diaEmissao == 16;
    }
    
    /**
     * Verifica se a régua é do terceiro período
     */
    public boolean isTerceiroPeriodo() {
        return diaEmissao != null && diaEmissao == 26;
    }
    
    /**
     * Verifica se é régua padrão
     */
    public boolean isPadrao() {
        return ehPadrao != null && ehPadrao;
    }
    
    /**
     * Verifica se está ativa
     */
    public boolean isAtiva() {
        return ativo != null && ativo;
    }
    
    /**
     * Retorna a descrição do período para exibição
     */
    public String getPeriodoDescricao() {
        if (isPrimeiroPeriodo()) {
            return "Primeiro Período (Dia " + diaEmissao + ")";
        } else if (isSegundoPeriodo()) {
            return "Segundo Período (Dia 16)";
        } else if (isTerceiroPeriodo()) {
            return "Terceiro Período (Dia 26 - Padrão)";
        }
        return periodo != null ? periodo : "Período não definido";
    }
    
    @Override
    public String toString() {
        return "ReguaFaturamentoDTO{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", diaEmissao=" + diaEmissao +
                ", periodo='" + periodo + '\'' +
                ", ehPadrao=" + ehPadrao +
                ", ativo=" + ativo +
                ", tiposArquivo=" + tiposArquivo +
                '}';
    }
}