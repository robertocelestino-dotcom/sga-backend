// src/main/java/com/sga/dto/FaturaItemDTO.java

package com.sga.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO para transferência de dados de itens de fatura
 * Utilizado para adicionar, atualizar e listar itens de uma fatura
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FaturaItemDTO {

    private Long id; // 🔥 ADICIONADO
    private String codigoProduto;
    private String descricao;
    private BigDecimal quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private String tipoLancamento;

    public FaturaItemDTO() {
    }

    public FaturaItemDTO(Long id, String codigoProduto, String descricao, BigDecimal quantidade,
                         BigDecimal valorUnitario, BigDecimal valorTotal, String tipoLancamento) {
        this.id = id;
        this.codigoProduto = codigoProduto;
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.valorTotal = valorTotal;
        this.tipoLancamento = tipoLancamento;
    }

    // ========== GETTERS E SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getTipoLancamento() {
        return tipoLancamento;
    }

    public void setTipoLancamento(String tipoLancamento) {
        this.tipoLancamento = tipoLancamento;
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Verifica se o DTO tem todos os campos obrigatórios preenchidos
     */
    public boolean isValid() {
        return codigoProduto != null && !codigoProduto.trim().isEmpty()
                && descricao != null && !descricao.trim().isEmpty()
                && quantidade != null && quantidade.compareTo(BigDecimal.ZERO) > 0
                && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Calcula o valor total do item (quantidade * valor unitário)
     */
    public void calcularValorTotal() {
        if (quantidade != null && valorUnitario != null) {
            this.valorTotal = quantidade.multiply(valorUnitario);
        }
    }

    @Override
    public String toString() {
        return "FaturaItemDTO{" +
                "id=" + id +
                ", codigoProduto='" + codigoProduto + '\'' +
                ", descricao='" + descricao + '\'' +
                ", quantidade=" + quantidade +
                ", valorUnitario=" + valorUnitario +
                ", valorTotal=" + valorTotal +
                ", tipoLancamento='" + tipoLancamento + '\'' +
                '}';
    }
}