// src/main/java/com/sga/dto/LogFaturaDTO.java

package com.sga.dto;

import java.time.LocalDateTime;

public class LogFaturaDTO {
    private Long id;
    private String nivel;
    private String mensagem;
    private String passo;
    private LocalDateTime dataHora;
    private Long faturaId;
    private String numeroFatura;

    // Construtores
    public LogFaturaDTO() {}

    public LogFaturaDTO(Long id, String nivel, String mensagem, String passo, 
                        LocalDateTime dataHora, Long faturaId, String numeroFatura) {
        this.id = id;
        this.nivel = nivel;
        this.mensagem = mensagem;
        this.passo = passo;
        this.dataHora = dataHora;
        this.faturaId = faturaId;
        this.numeroFatura = numeroFatura;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getPasso() {
        return passo;
    }

    public void setPasso(String passo) {
        this.passo = passo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Long getFaturaId() {
        return faturaId;
    }

    public void setFaturaId(Long faturaId) {
        this.faturaId = faturaId;
    }

    public String getNumeroFatura() {
        return numeroFatura;
    }

    public void setNumeroFatura(String numeroFatura) {
        this.numeroFatura = numeroFatura;
    }
}