// src/main/java/com/sga/dto/HistoricoMigracaoDTO.java

package com.sga.dto;

import java.time.LocalDateTime;

public class HistoricoMigracaoDTO {

    private Long id;
    private Long associadoId;
    private String associadoNome;
    private Long reguaOrigemId;
    private String reguaOrigemNome;
    private Long reguaDestinoId;
    private String reguaDestinoNome;
    private LocalDateTime dataMigracao;
    private String usuario;
    private String motivo;
    private String status;
    private String observacao;
    private Integer faturasPendentes;
    private Boolean migracaoForcada;

    // ========== CONSTRUTORES ==========

    public HistoricoMigracaoDTO() {}

    public HistoricoMigracaoDTO(Long id, Long associadoId, String associadoNome,
                                 Long reguaOrigemId, String reguaOrigemNome,
                                 Long reguaDestinoId, String reguaDestinoNome,
                                 LocalDateTime dataMigracao, String usuario,
                                 String motivo, String status, String observacao,
                                 Integer faturasPendentes, Boolean migracaoForcada) {
        this.id = id;
        this.associadoId = associadoId;
        this.associadoNome = associadoNome;
        this.reguaOrigemId = reguaOrigemId;
        this.reguaOrigemNome = reguaOrigemNome;
        this.reguaDestinoId = reguaDestinoId;
        this.reguaDestinoNome = reguaDestinoNome;
        this.dataMigracao = dataMigracao;
        this.usuario = usuario;
        this.motivo = motivo;
        this.status = status;
        this.observacao = observacao;
        this.faturasPendentes = faturasPendentes;
        this.migracaoForcada = migracaoForcada;
    }

    // ========== GETTERS E SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAssociadoId() {
        return associadoId;
    }

    public void setAssociadoId(Long associadoId) {
        this.associadoId = associadoId;
    }

    public String getAssociadoNome() {
        return associadoNome;
    }

    public void setAssociadoNome(String associadoNome) {
        this.associadoNome = associadoNome;
    }

    public Long getReguaOrigemId() {
        return reguaOrigemId;
    }

    public void setReguaOrigemId(Long reguaOrigemId) {
        this.reguaOrigemId = reguaOrigemId;
    }

    public String getReguaOrigemNome() {
        return reguaOrigemNome;
    }

    public void setReguaOrigemNome(String reguaOrigemNome) {
        this.reguaOrigemNome = reguaOrigemNome;
    }

    public Long getReguaDestinoId() {
        return reguaDestinoId;
    }

    public void setReguaDestinoId(Long reguaDestinoId) {
        this.reguaDestinoId = reguaDestinoId;
    }

    public String getReguaDestinoNome() {
        return reguaDestinoNome;
    }

    public void setReguaDestinoNome(String reguaDestinoNome) {
        this.reguaDestinoNome = reguaDestinoNome;
    }

    public LocalDateTime getDataMigracao() {
        return dataMigracao;
    }

    public void setDataMigracao(LocalDateTime dataMigracao) {
        this.dataMigracao = dataMigracao;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Integer getFaturasPendentes() {
        return faturasPendentes;
    }

    public void setFaturasPendentes(Integer faturasPendentes) {
        this.faturasPendentes = faturasPendentes;
    }

    public Boolean getMigracaoForcada() {
        return migracaoForcada;
    }

    public void setMigracaoForcada(Boolean migracaoForcada) {
        this.migracaoForcada = migracaoForcada;
    }
}