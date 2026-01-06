package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlanoResumoDTO {

    private Long id;
    private String codigo;
    private String plano;
    private String descricao;
    private BigDecimal valorMensal;
    private String status;
    private LocalDateTime dataCadastro;

    // Construtor padrão (OBRIGATÓRIO)
    public PlanoResumoDTO() {
    }

    // Construtor com parâmetros (opcional)
    public PlanoResumoDTO(Long id, String codigo, String plano, BigDecimal valorMensal) {
        this.id = id;
        this.codigo = codigo;
        this.plano = plano;
        this.valorMensal = valorMensal;
        this.status = "ATIVO";
        this.dataCadastro = LocalDateTime.now();
    }

    // Getters e Setters (mantenha todos que já tem)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getPlano() {
        return plano;
    }

    public void setPlano(String plano) {
        this.plano = plano;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorMensal() {
        return valorMensal;
    }

    public void setValorMensal(BigDecimal valorMensal) {
        this.valorMensal = valorMensal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}