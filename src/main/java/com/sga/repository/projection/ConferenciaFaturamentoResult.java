package com.sga.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO tipado que recebe o resultado da query nativa {@code findConferenciaFaturamento}.
 *
 * <p>Os valores são injetados via construtor pelo {@code @SqlResultSetMapping}.
 * Isso garante mapeamento correto mesmo em queries nativas, onde o mapeamento
 * automático de aliases → getters do Spring Data JPA não funciona em todas as versões.</p>
 *
 * @author SGA
 */
public class ConferenciaFaturamentoResult {

    // ===== Fatura =====
    private final Long faturaId;
    private final String numeroFatura;
    private final LocalDate dataEmissao;
    private final String statusFatura;
    private final BigDecimal valorFatura;

    // ===== Nota de Débito =====
    private final Long notaId;
    private final String numeroNotaDebito;
    private final LocalDate dataVencimento;
    private final BigDecimal valorNota;
    private final String tipoArquivo;

    // ===== Associado =====
    private final Long associadoId;
    private final String codigoSpc;
    private final String codigoRm;
    private final String nomeRazao;

    // ===== Contadores =====
    private final Integer qtdItensNota;
    private final Integer qtdItensFatura;
    private final Integer qtdFranquiasNota;
    private final Integer qtdFranquiasFatura;

    /**
     * Construtor usado pelo {@code @SqlResultSetMapping}.
     * A ordem dos parâmetros DEVE casar com a ordem dos {@code @ColumnResult}.
     */
    public ConferenciaFaturamentoResult(
            Long faturaId,
            String numeroFatura,
            LocalDate dataEmissao,
            String statusFatura,
            BigDecimal valorFatura,
            Long notaId,
            String numeroNotaDebito,
            LocalDate dataVencimento,
            BigDecimal valorNota,
            String tipoArquivo,
            Long associadoId,
            String codigoSpc,
            String codigoRm,
            String nomeRazao,
            Integer qtdItensNota,
            Integer qtdItensFatura,
            Integer qtdFranquiasNota,
            Integer qtdFranquiasFatura) {
        this.faturaId = faturaId;
        this.numeroFatura = numeroFatura;
        this.dataEmissao = dataEmissao;
        this.statusFatura = statusFatura;
        this.valorFatura = valorFatura;
        this.notaId = notaId;
        this.numeroNotaDebito = numeroNotaDebito;
        this.dataVencimento = dataVencimento;
        this.valorNota = valorNota;
        this.tipoArquivo = tipoArquivo;
        this.associadoId = associadoId;
        this.codigoSpc = codigoSpc;
        this.codigoRm = codigoRm;
        this.nomeRazao = nomeRazao;
        this.qtdItensNota = qtdItensNota;
        this.qtdItensFatura = qtdItensFatura;
        this.qtdFranquiasNota = qtdFranquiasNota;
        this.qtdFranquiasFatura = qtdFranquiasFatura;
    }

    // ===== Getters =====
    public Long getFaturaId() { return faturaId; }
    public String getNumeroFatura() { return numeroFatura; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public String getStatusFatura() { return statusFatura; }
    public BigDecimal getValorFatura() { return valorFatura; }
    public Long getNotaId() { return notaId; }
    public String getNumeroNotaDebito() { return numeroNotaDebito; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public BigDecimal getValorNota() { return valorNota; }
    public String getTipoArquivo() { return tipoArquivo; }
    public Long getAssociadoId() { return associadoId; }
    public String getCodigoSpc() { return codigoSpc; }
    public String getCodigoRm() { return codigoRm; }
    public String getNomeRazao() { return nomeRazao; }
    public Integer getQtdItensNota() { return qtdItensNota; }
    public Integer getQtdItensFatura() { return qtdItensFatura; }
    public Integer getQtdFranquiasNota() { return qtdFranquiasNota; }
    public Integer getQtdFranquiasFatura() { return qtdFranquiasFatura; }
}