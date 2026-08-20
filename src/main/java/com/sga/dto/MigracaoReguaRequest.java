// src/main/java/com/sga/dto/MigracaoReguaRequest.java

package com.sga.dto;

import java.time.LocalDate;

public class MigracaoReguaRequest {

    private Long associadoId;
    private Long novaReguaId;
    private String motivo;
    private boolean forcarMigracao;
    private LocalDate dataMigracao;

    // ========== CONSTRUTORES ==========

    public MigracaoReguaRequest() {}

    public MigracaoReguaRequest(Long associadoId, Long novaReguaId, String motivo, 
                                 boolean forcarMigracao, LocalDate dataMigracao) {
        this.associadoId = associadoId;
        this.novaReguaId = novaReguaId;
        this.motivo = motivo;
        this.forcarMigracao = forcarMigracao;
        this.dataMigracao = dataMigracao;
    }

    // ========== GETTERS E SETTERS ==========

    public Long getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(Long associadoId) {
        this.associadoId = associadoId;
    }

    public Long getNovaReguaId() {
        return novaReguaId;
    }

    public void setNovaReguaId(Long novaReguaId) {
        this.novaReguaId = novaReguaId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isForcarMigracao() {
        return forcarMigracao;
    }

    public void setForcarMigracao(boolean forcarMigracao) {
        this.forcarMigracao = forcarMigracao;
    }

    public LocalDate getDataMigracao() {
        return dataMigracao;
    }

    public void setDataMigracao(LocalDate dataMigracao) {
        this.dataMigracao = dataMigracao;
    }
}