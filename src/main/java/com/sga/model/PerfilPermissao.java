package com.sga.model;

import java.util.Objects;
import java.time.LocalDateTime;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TB_PERFIL_PERMISSAO")
public class PerfilPermissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PERFIL_ID", nullable = false)
    private Perfil perfil;

    @ManyToOne
    @JoinColumn(name = "PERMISSAO_ID", nullable = false)
    private Permissao permissao;

    @Column(name = "DATA_CRIACAO")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    // Construtores
    public PerfilPermissao() {
        super();
    }

    public PerfilPermissao(Perfil perfil, Permissao permissao) {
        this.perfil = perfil;
        this.permissao = permissao;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Permissao getPermissao() {
        return permissao;
    }

    public void setPermissao(Permissao permissao) {
        this.permissao = permissao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
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
        PerfilPermissao other = (PerfilPermissao) obj;
        return Objects.equals(id, other.id);
    }
}