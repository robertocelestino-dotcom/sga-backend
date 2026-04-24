package com.sga.dto;

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
    
    private Boolean processarCancelamentos = true;      // Se deve processar cancelamentos
    private Boolean aplicarFranquia = true;             // Se deve aplicar regra de franquia
    private Boolean aplicarFaturamentoMinimo = false;   // Se deve aplicar faturamento mínimo
    private String observacao;                          // Observação do processamento
    private String usuario;                             // Usuário que está processando
    private Long loteId;                                // ID do lote de processamento (opcional)
    
    // ========== GETTERS E SETTERS (EXISTENTES) ==========
    
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

    // ========== NOVOS GETTERS E SETTERS ==========
    
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
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Verifica se o processamento é para período extemporâneo (dia 16 ou 1/2)
     */
    public boolean isPeriodoExtemporaneo() {
        return getExtemporaneo();
    }
    
    /**
     * Retorna a descrição do período para logs
     */
    public String getPeriodoDescricao() {
        if (getExtemporaneo()) {
            return "Extemporâneo - " + mes + "/" + ano;
        }
        return "Padrão - " + mes + "/" + ano;
    }
    
    /**
     * Valida se a requisição tem dados mínimos para processamento
     */
    public boolean isValid() {
        return (associadosIds != null && !associadosIds.isEmpty()) 
                && mes != null && ano != null;
    }
    
    /**
     * Retorna o número de associados a processar
     */
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
                '}';
    }
}