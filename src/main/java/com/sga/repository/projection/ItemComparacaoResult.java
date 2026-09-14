package com.sga.repository.projection;

import java.math.BigDecimal;

/**
 * DTO tipado para os itens da nota/fatura na conferência.
 *
 * @author SGA
 */
public class ItemComparacaoResult {

    private final String codigo;
    private final String descricao;
    private final BigDecimal quantidade;
    private final BigDecimal valorUnitario;
    private final BigDecimal valorTotal;

    public ItemComparacaoResult(
            String codigo,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            BigDecimal valorTotal) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
    }

    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public BigDecimal getValorUnitario() { return valorUnitario; }
    public BigDecimal getValorTotal() { return valorTotal; }
}