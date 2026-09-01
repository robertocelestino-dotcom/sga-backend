package com.sga.model;

import java.util.Objects;
import java.time.LocalDateTime;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TB_LOG_ACESSO")
public class LogAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USUARIO_ID")
    private Usuario usuario;

    @Column(length = 50)
    private String username;

    @Column(name = "DATA_HORA")
    private LocalDateTime dataHora = LocalDateTime.now();

    @Column(length = 45)
    private String ip;

    @Column(name = "USER_AGENT", columnDefinition = "TEXT")
    private String userAgent;

    @Column(length = 50)
    private String acao;

    @Column(length = 20)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String mensagem;

    // Construtores
    public LogAcesso() {
        super();
    }

    public LogAcesso(Usuario usuario, String username, String ip, String userAgent, 
                     String acao, String status, String mensagem) {
        this.usuario = usuario;
        this.username = username;
        this.ip = ip;
        this.userAgent = userAgent;
        this.acao = acao;
        this.status = status;
        this.mensagem = mensagem;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (usuario != null) {
            this.username = usuario.getUsername();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
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

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogAcesso other = (LogAcesso) obj;
        return Objects.equals(id, other.id);
    }
}