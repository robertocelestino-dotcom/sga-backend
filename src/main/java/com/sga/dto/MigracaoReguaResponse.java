// src/main/java/com/sga/dto/MigracaoReguaResponse.java

package com.sga.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MigracaoReguaResponse {

    private Long associadoId;
    private String associadoNome;
    private Long reguaOrigemId;
    private String reguaOrigemNome;
    private Long reguaDestinoId;
    private String reguaDestinoNome;
    private LocalDateTime dataMigracao;
    private LocalDate dataInicio;
    private String status; // SUCESSO, ERRO, TEM_FATURAS_PENDENTES, MESMA_REGUA, PODE_MIGRAR
    private String mensagem;
    private Long historicoId;
    private boolean faturasPendentes;
    private int totalFaturasPendentes;
    private boolean migracaoForcada;
    private List<FaturaResumoDTO> faturasPendentesList = new ArrayList<>();

    // ========== CONSTRUTORES ==========

    public MigracaoReguaResponse() {}

    // ========== GETTERS E SETTERS ==========

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

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getHistoricoId() {
        return historicoId;
    }

    public void setHistoricoId(Long historicoId) {
        this.historicoId = historicoId;
    }

    public boolean isFaturasPendentes() {
        return faturasPendentes;
    }

    public void setFaturasPendentes(boolean faturasPendentes) {
        this.faturasPendentes = faturasPendentes;
    }

    public int getTotalFaturasPendentes() {
        return totalFaturasPendentes;
    }

    public void setTotalFaturasPendentes(int totalFaturasPendentes) {
        this.totalFaturasPendentes = totalFaturasPendentes;
    }

    public boolean isMigracaoForcada() {
        return migracaoForcada;
    }

    public void setMigracaoForcada(boolean migracaoForcada) {
        this.migracaoForcada = migracaoForcada;
    }

    public List<FaturaResumoDTO> getFaturasPendentesList() {
        return faturasPendentesList;
    }

    public void setFaturasPendentesList(List<FaturaResumoDTO> faturasPendentesList) {
        this.faturasPendentesList = faturasPendentesList != null ? faturasPendentesList : new ArrayList<>();
    }
}