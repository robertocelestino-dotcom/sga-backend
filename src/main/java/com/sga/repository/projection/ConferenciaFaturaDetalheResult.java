package com.sga.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO tipado para o detalhamento de uma fatura na conferência.
 *
 * @author SGA
 */
public class ConferenciaFaturaDetalheResult {

    private final Long faturaId;
    private final String numeroFatura;
    private final LocalDate dataEmissao;
    private final String statusFatura;
    private final BigDecimal valorFatura;

    private final Long notaId;
    private final String numeroNotaDebito;
    private final LocalDate dataVencimento;
    private final BigDecimal valorNota;

    private final Long associadoId;
    private final String codigoSpc;
    private final String codigoRm;
    private final String nomeRazao;

    /**
     * Construtor usado pelo {@code @SqlResultSetMapping}.
     */
    public ConferenciaFaturaDetalheResult(
            Long faturaId,
            String numeroFatura,
            LocalDate dataEmissao,
            String statusFatura,
            BigDecimal valorFatura,
            Long notaId,
            String numeroNotaDebito,
            LocalDate dataVencimento,
            BigDecimal valorNota,
            Long associadoId,
            String codigoSpc,
            String codigoRm,
            String nomeRazao) {
        this.faturaId = faturaId;
        this.numeroFatura = numeroFatura;
        this.dataEmissao = dataEmissao;
        this.statusFatura = statusFatura;
        this.valorFatura = valorFatura;
        this.notaId = notaId;
        this.numeroNotaDebito = numeroNotaDebito;
        this.dataVencimento = dataVencimento;
        this.valorNota = valorNota;
        this.associadoId = associadoId;
        this.codigoSpc = codigoSpc;
        this.codigoRm = codigoRm;
        this.nomeRazao = nomeRazao;
    }

    public Long getFaturaId() { return faturaId; }
    public String getNumeroFatura() { return numeroFatura; }
    public LocalDate getDataEmissao() { return dataEmissao; }
    public String getStatusFatura() { return statusFatura; }
    public BigDecimal getValorFatura() { return valorFatura; }
    public Long getNotaId() { return notaId; }
    public String getNumeroNotaDebito() { return numeroNotaDebito; }
    public LocalDate getDataVencimento() { return dataVencimento; }
    public BigDecimal getValorNota() { return valorNota; }
    public Long getAssociadoId() { return associadoId; }
    public String getCodigoSpc() { return codigoSpc; }
    public String getCodigoRm() { return codigoRm; }
    public String getNomeRazao() { return nomeRazao; }
}