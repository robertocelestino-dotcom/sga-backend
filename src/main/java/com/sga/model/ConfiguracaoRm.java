package com.sga.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "tb_configuracao_rm")
public class ConfiguracaoRm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String descricao;  // ← este é o campo descricao_faturamento

    private Boolean ativo = true;

    @Column(name = "tipo_movimento", nullable = false, length = 50)
    private String tipoMovimento;

    @Column(name = "codigo_tmv", nullable = false, length = 20)
    private String codigoTmv;

    @Column(name = "centro_custo", length = 20)
    private String centroCusto = "01.01";

    @Column(name = "condicao_pagamento", length = 10)
    private String condicaoPagamento = "99";

    @Column(length = 10)
    private String serie = "RPS";

    @Column(name = "conta_caixa", length = 10)
    private String contaCaixa = "78";

    @Column(name = "codigo_servico", length = 20)
    private String codigoServico = "5.949.01";

    @Column(name = "municipio_servico", length = 10)
    private String municipioServico = "04400";

    @Column(name = "uf_servico", length = 2)
    private String ufServico = "CE";

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    // 🔥 Parâmetros adicionais (flexíveis)
    @OneToMany(mappedBy = "configuracaoRm", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ConfiguracaoRmParametro> parametros = new ArrayList<>();

    // Construtores
    public ConfiguracaoRm() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getTipoMovimento() {
        return tipoMovimento;
    }

    public void setTipoMovimento(String tipoMovimento) {
        this.tipoMovimento = tipoMovimento;
    }

    public String getCodigoTmv() {
        return codigoTmv;
    }

    public void setCodigoTmv(String codigoTmv) {
        this.codigoTmv = codigoTmv;
    }

    public String getCentroCusto() {
        return centroCusto;
    }

    public void setCentroCusto(String centroCusto) {
        this.centroCusto = centroCusto;
    }

    public String getCondicaoPagamento() {
        return condicaoPagamento;
    }

    public void setCondicaoPagamento(String condicaoPagamento) {
        this.condicaoPagamento = condicaoPagamento;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getContaCaixa() {
        return contaCaixa;
    }

    public void setContaCaixa(String contaCaixa) {
        this.contaCaixa = contaCaixa;
    }

    public String getCodigoServico() {
        return codigoServico;
    }

    public void setCodigoServico(String codigoServico) {
        this.codigoServico = codigoServico;
    }

    public String getMunicipioServico() {
        return municipioServico;
    }

    public void setMunicipioServico(String municipioServico) {
        this.municipioServico = municipioServico;
    }

    public String getUfServico() {
        return ufServico;
    }

    public void setUfServico(String ufServico) {
        this.ufServico = ufServico;
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

    public List<ConfiguracaoRmParametro> getParametros() {
        return parametros;
    }

    public void setParametros(List<ConfiguracaoRmParametro> parametros) {
        this.parametros = parametros;
    }
}