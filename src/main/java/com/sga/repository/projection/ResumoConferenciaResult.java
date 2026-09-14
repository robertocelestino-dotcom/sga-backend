package com.sga.repository.projection;

import java.math.BigDecimal;

/**
 * DTO tipado para o resumo agregado da conferência.
 *
 * @author SGA
 */
public class ResumoConferenciaResult {

    private final BigDecimal somaDiferencas;
    private final Integer totalItensFatura;
    private final Integer totalItensNota;
    private final Integer totalFranquiasFatura;
    private final Integer totalFranquiasNota;
    private final BigDecimal somaValorNotas;
    private final BigDecimal somaValorFaturas;

    public ResumoConferenciaResult(
            BigDecimal somaDiferencas,
            Integer totalItensFatura,
            Integer totalItensNota,
            Integer totalFranquiasFatura,
            Integer totalFranquiasNota,
            BigDecimal somaValorNotas,
            BigDecimal somaValorFaturas) {
        this.somaDiferencas = somaDiferencas;
        this.totalItensFatura = totalItensFatura;
        this.totalItensNota = totalItensNota;
        this.totalFranquiasFatura = totalFranquiasFatura;
        this.totalFranquiasNota = totalFranquiasNota;
        this.somaValorNotas = somaValorNotas;
        this.somaValorFaturas = somaValorFaturas;
    }

    public BigDecimal getSomaDiferencas() { return somaDiferencas; }
    public Integer getTotalItensFatura() { return totalItensFatura; }
    public Integer getTotalItensNota() { return totalItensNota; }
    public Integer getTotalFranquiasFatura() { return totalFranquiasFatura; }
    public Integer getTotalFranquiasNota() { return totalFranquiasNota; }
    public BigDecimal getSomaValorNotas() { return somaValorNotas; }
    public BigDecimal getSomaValorFaturas() { return somaValorFaturas; }
}