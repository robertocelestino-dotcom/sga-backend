package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_log_fatura")
public class LogFatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fatura_id", nullable = false)
    private Fatura fatura;

    @Column(name = "nivel", nullable = false, length = 10)
    private String nivel;

    @Column(name = "mensagem", nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    @Column(name = "passo", length = 50)
    private String passo;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    // ========== CONSTRUTORES ==========

    public LogFatura() {}

    public LogFatura(Fatura fatura, String nivel, String mensagem, String passo) {
        this.fatura = fatura;
        this.nivel = nivel;
        this.mensagem = mensagem;
        this.passo = passo;
        this.dataHora = LocalDateTime.now();
    }

    // ========== GETTERS E SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Fatura getFatura() {
        return fatura;
    }

    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
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

    // ========== MÉTODOS AUXILIARES ==========

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s", 
            dataHora != null ? dataHora.toString() : "N/A",
            nivel != null ? nivel : "N/A",
            passo != null ? passo : "N/A",
            mensagem != null ? mensagem : "N/A"
        );
    }
}