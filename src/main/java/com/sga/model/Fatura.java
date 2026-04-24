package com.sga.model;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_fatura")
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nota_debito_id")
    private Long notaDebitoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associado_id", nullable = false)
    private Associado associado;

    @Column(name = "numero_fatura", nullable = false, length = 50)
    private String numeroFatura;

    @Column(name = "numero_nota_debito", length = 50)
    private String numeroNotaDebito;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(length = 20)
    private String status = "PENDENTE";

    @Column(name = "processado_rm")
    private Boolean processadoRm = false;

    @Column(name = "lote_processamento_id")
    private Long loteProcessamentoId;

    @Column(name = "data_processamento")
    private LocalDateTime dataProcessamento;

    @Column(name = "mes_referencia")
    private Integer mesReferencia;

    @Column(name = "ano_referencia")
    private Integer anoReferencia;

    private String observacao;

    @Column(name = "usuario_criacao", length = 100)
    private String usuarioCriacao;

    @CreationTimestamp
    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @UpdateTimestamp
    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @OneToMany(mappedBy = "fatura", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<FaturaItem> itens = new ArrayList<>();

    // ==================== CONSTRUTORES ====================
    public Fatura() {
    }

    public Fatura(Associado associado, String numeroFatura, LocalDate dataEmissao, LocalDate dataVencimento) {
        this.associado = associado;
        this.numeroFatura = numeroFatura;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
    }

    // ==================== GETTERS E SETTERS ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNotaDebitoId() {
        return notaDebitoId;
    }

    public void setNotaDebitoId(Long notaDebitoId) {
        this.notaDebitoId = notaDebitoId;
    }

    public Associado getAssociado() {
        return associado;
    }

    public void setAssociado(Associado associado) {
        this.associado = associado;
    }

    public String getNumeroFatura() {
        return numeroFatura;
    }

    public void setNumeroFatura(String numeroFatura) {
        this.numeroFatura = numeroFatura;
    }

    public String getNumeroNotaDebito() {
        return numeroNotaDebito;
    }

    public void setNumeroNotaDebito(String numeroNotaDebito) {
        this.numeroNotaDebito = numeroNotaDebito;
    }

    public LocalDate getDataEmissao() {
        return dataEmissao;
    }

    public void setDataEmissao(LocalDate dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getProcessadoRm() {
        return processadoRm;
    }

    public void setProcessadoRm(Boolean processadoRm) {
        this.processadoRm = processadoRm;
    }

    public Long getLoteProcessamentoId() {
        return loteProcessamentoId;
    }

    public void setLoteProcessamentoId(Long loteProcessamentoId) {
        this.loteProcessamentoId = loteProcessamentoId;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(LocalDateTime dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

    public Integer getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(Integer mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public Integer getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(Integer anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getUsuarioCriacao() {
        return usuarioCriacao;
    }

    public void setUsuarioCriacao(String usuarioCriacao) {
        this.usuarioCriacao = usuarioCriacao;
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

    public List<FaturaItem> getItens() {
        return itens;
    }

    public void setItens(List<FaturaItem> itens) {
        this.itens = itens;
        recalcularTotal();
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Adiciona um item à fatura
     */
    public void addItem(FaturaItem item) {
        if (item != null) {
            itens.add(item);
            item.setFatura(this);
            recalcularTotal();
        }
    }

    /**
     * Remove um item da fatura
     */
    public void removeItem(FaturaItem item) {
        if (item != null) {
            itens.remove(item);
            item.setFatura(null);
            recalcularTotal();
        }
    }

    /**
     * Remove item por índice
     */
    public void removeItemPorIndice(int index) {
        if (index >= 0 && index < itens.size()) {
            FaturaItem item = itens.get(index);
            itens.remove(index);
            item.setFatura(null);
            recalcularTotal();
        }
    }

    /**
     * Recalcula o valor total da fatura somando todos os itens
     */
    public void recalcularTotal() {
        if (itens != null && !itens.isEmpty()) {
            this.valorTotal = itens.stream()
                    .map(item -> {
                        if (item.getValorTotal() != null) {
                            return item.getValorTotal();
                        }
                        return BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }

    /**
     * Recalcula o valor total (alias para recalcularTotal)
     */
    public void recalcularValor() {
        recalcularTotal();
    }

    /**
     * Calcula o total de débitos da fatura
     */
    public BigDecimal getTotalDebitos() {
        if (itens == null || itens.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return itens.stream()
                .filter(item -> {
                    String tipo = item.getTipoLancamento();
                    return tipo != null && "D".equals(tipo);
                })
                .map(item -> {
                    if (item.getValorTotal() != null) {
                        return item.getValorTotal();
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula o total de créditos da fatura
     */
    public BigDecimal getTotalCreditos() {
        if (itens == null || itens.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return itens.stream()
                .filter(item -> {
                    String tipo = item.getTipoLancamento();
                    return tipo != null && "C".equals(tipo);
                })
                .map(item -> {
                    if (item.getValorTotal() != null) {
                        return item.getValorTotal();
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcula o valor líquido (débitos - créditos)
     */
    public BigDecimal getValorLiquido() {
        return getTotalDebitos().subtract(getTotalCreditos());
    }

    /**
     * Verifica se a fatura está paga
     */
    public boolean isPaga() {
        return "PAGA".equals(status) || "PAGO".equals(status);
    }

    /**
     * Verifica se a fatura está cancelada
     */
    public boolean isCancelada() {
        return "CANCELADA".equals(status) || "CANCELADO".equals(status);
    }

    /**
     * Verifica se a fatura está pendente
     */
    public boolean isPendente() {
        return "PENDENTE".equals(status);
    }
}