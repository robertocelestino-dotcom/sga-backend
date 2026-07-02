package com.sga.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para requisição de processamento de faturamento
 */
public class ProcessamentoRequest {

    // ========== CAMPOS EXISTENTES ==========
    
    private List<Long> associadosIds = new ArrayList<>();
    private Integer mes;
    private Integer ano;
    private Boolean extemporaneo = false;
    private Boolean integrarRm = false;
    private Boolean gerarNotas = true;
    private Boolean simular = false;
    private Long reguaId;

    // ========== NOVOS CAMPOS ==========
    
    private Boolean processarCancelamentos = true;
    private Boolean aplicarFranquia = true;
    private Boolean aplicarFaturamentoMinimo = false;
    private String observacao;
    private String usuario;
    private Long loteId;
    
    // 🔥 CAMPOS DE DATA ADICIONADOS
    private LocalDate dataEmissao;
    private LocalDate dataVencimento;
    
    // ========== GETTERS E SETTERS ==========
    
    public List<Long> getAssociadosIds() {
        return associadosIds;
    }

    public void setAssociadosIds(List<Long> associadosIds) {
        this.associadosIds = associadosIds;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Boolean getExtemporaneo() {
        return extemporaneo != null ? extemporaneo : false;
    }

    public void setExtemporaneo(Boolean extemporaneo) {
        this.extemporaneo = extemporaneo;
    }

    public Boolean getIntegrarRm() {
        return integrarRm != null ? integrarRm : false;
    }

    public void setIntegrarRm(Boolean integrarRm) {
        this.integrarRm = integrarRm;
    }

    public Boolean getGerarNotas() {
        return gerarNotas != null ? gerarNotas : true;
    }

    public void setGerarNotas(Boolean gerarNotas) {
        this.gerarNotas = gerarNotas;
    }

    public Boolean getSimular() {
        return simular != null ? simular : false;
    }

    public void setSimular(Boolean simular) {
        this.simular = simular;
    }

    public Long getReguaId() {
        return reguaId;
    }

    public void setReguaId(Long reguaId) {
        this.reguaId = reguaId;
    }

    public Boolean getProcessarCancelamentos() {
        return processarCancelamentos != null ? processarCancelamentos : true;
    }

    public void setProcessarCancelamentos(Boolean processarCancelamentos) {
        this.processarCancelamentos = processarCancelamentos;
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

    public Long getLoteId() {
        return loteId;
    }

    public void setLoteId(Long loteId) {
        this.loteId = loteId;
    }
    
    // 🔥 GETTERS E SETTERS PARA DATAS
    public LocalDate getDataEmissao() {
        return dataEmissao;
    }
    
    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }
    
    public LocalDate getDataVencimento() {
        return dataVencimento;
    }
    
    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    public boolean isPeriodoExtemporaneo() {
        return getExtemporaneo();
    }
    
    public String getPeriodoDescricao() {
        if (getExtemporaneo()) {
            return "Extemporâneo - " + mes + "/" + ano;
        }
        return "Padrão - " + mes + "/" + ano;
    }
    
    public boolean isValid() {
        return (associadosIds != null && !associadosIds.isEmpty()) 
                && mes != null && ano != null;
    }
    
    public int getQuantidadeAssociados() {
        return associadosIds != null ? associadosIds.size() : 0;
    }
    
    @Override
    public String toString() {
        return "ProcessamentoRequest{" +
                "quantidadeAssociados=" + getQuantidadeAssociados() +
                ", mes=" + mes +
                ", ano=" + ano +
                ", extemporaneo=" + extemporaneo +
                ", integrarRm=" + integrarRm +
                ", gerarNotas=" + gerarNotas +
                ", simular=" + simular +
                ", reguaId=" + reguaId +
                ", dataEmissao=" + dataEmissao +
                ", dataVencimento=" + dataVencimento +
                '}';
    }
}