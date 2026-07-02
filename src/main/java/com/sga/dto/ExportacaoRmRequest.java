// src/main/java/com/sga/dto/ExportacaoRmRequest.java

package com.sga.dto;

import java.util.List;

public class ExportacaoRmRequest {
    private List<Long> faturaIds;
    private Integer ultimoNumeroRps;
    private Long reguaId;
    private String mesReferencia;
    
    // Getters e Setters
    public List<Long> getFaturaIds() {
        return faturaIds;
    }
    
    public void setFaturaIds(List<Long> faturaIds) {
        this.faturaIds = faturaIds;
    }
    
    public Integer getUltimoNumeroRps() {
        return ultimoNumeroRps;
    }
    
    public void setUltimoNumeroRps(Integer ultimoNumeroRps) {
        this.ultimoNumeroRps = ultimoNumeroRps;
    }
    
    public Long getReguaId() {
        return reguaId;
    }
    
    public void setReguaId(Long reguaId) {
        this.reguaId = reguaId;
    }
    
    public String getMesReferencia() {
        return mesReferencia;
    }
    
    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }
}