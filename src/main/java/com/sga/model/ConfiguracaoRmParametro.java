package com.sga.model;

import java.time.LocalDateTime;
import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tb_configuracao_rm_parametro")
public class ConfiguracaoRmParametro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "configuracao_rm_id", insertable = false, updatable = false)
    private Long configuracaoRmId;

    @Column(nullable = false, length = 50)
    private String chave;

    @Column(nullable = false, length = 100)
    private String valor;

    @Column(length = 200)
    private String descricao;

    private Integer ordem = 0;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "configuracao_rm_id", nullable = false)
    @JsonIgnore
    private ConfiguracaoRm configuracaoRm;

    // Construtores
    public ConfiguracaoRmParametro() {
    }

    public ConfiguracaoRmParametro(String chave, String valor, String descricao, Integer ordem) {
        this.chave = chave;
        this.valor = valor;
        this.descricao = descricao;
        this.ordem = ordem;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConfiguracaoRmId() {
        return configuracaoRmId;
    }

    public void setConfiguracaoRmId(Long configuracaoRmId) {
        this.configuracaoRmId = configuracaoRmId;
    }

    public String getChave() {
        return chave;
    }

    public void setChave(String chave) {
        this.chave = chave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public ConfiguracaoRm getConfiguracaoRm() {
        return configuracaoRm;
    }

    public void setConfiguracaoRm(ConfiguracaoRm configuracaoRm) {
        this.configuracaoRm = configuracaoRm;
    }
}