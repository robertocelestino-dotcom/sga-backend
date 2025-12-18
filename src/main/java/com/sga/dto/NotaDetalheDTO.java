package com.sga.dto;

public class NotaDetalheDTO {

    private Long id;
    private String codigoItem;
    private String descricaoItem;
    private Integer quantidade;
    private Double valorUnitario;
    private Double valorTotal;
    private String tipo; // C ou D
    private String observacao;

    public NotaDetalheDTO() {}

    public NotaDetalheDTO(
            Long id,
            String codigoItem,
            String descricaoItem,
            Integer quantidade,
            Double valorUnitario,
            Double valorTotal,
            String tipo,
            String observacao
    ) {
        this.id = id;
        this.codigoItem = codigoItem;
        this.descricaoItem = descricaoItem;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.tipo = tipo;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }

    public String getCodigoItem() {
        return codigoItem;
    }

    public String getDescricaoItem() {
        return descricaoItem;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public Double getValorTotal() {
        return valorTotal;
    }

    public String getTipo() {
        return tipo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCodigoItem(String codigoItem) {
        this.codigoItem = codigoItem;
    }

    public void setDescricaoItem(String descricaoItem) {
        this.descricaoItem = descricaoItem;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
