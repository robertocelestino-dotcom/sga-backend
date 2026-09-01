package com.sga.model;

import java.util.Objects;
import java.time.LocalDateTime;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TB_SESSAO_USUARIO")
public class SessaoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USUARIO_ID", nullable = false)
    private Usuario usuario;

    @Column(length = 500)
    private String token;

    @Column(name = "DATA_CRIACAO")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "DATA_EXPIRACAO")
    private LocalDateTime dataExpiracao;

    @Column(length = 45)
    private String ip;

    @Column(name = "USER_AGENT", columnDefinition = "TEXT")
    private String userAgent;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Construtores
    public SessaoUsuario() {
        super();
    }

    public SessaoUsuario(Usuario usuario, String token, LocalDateTime dataExpiracao, 
                         String ip, String userAgent) {
        this.usuario = usuario;
        this.token = token;
        this.dataExpiracao = dataExpiracao;
        this.ip = ip;
        this.userAgent = userAgent;
        this.ativo = true;
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
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }

    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
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
        SessaoUsuario other = (SessaoUsuario) obj;
        return Objects.equals(id, other.id);
    }
}