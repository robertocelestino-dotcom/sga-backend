package com.sga.dto;

import java.math.BigDecimal;

public class NotaDebitoResumoDTO {
    
    private Long id;
    private String numeroNota;
    private String codigoSocio;
    private String nomeAssociado;
    private BigDecimal totalDebitos;
    private BigDecimal totalCreditos;
    private BigDecimal valorFaturado;
    
    // Construtores
    public NotaDebitoResumoDTO() {
        this.totalDebitos = BigDecimal.ZERO;
        this.totalCreditos = BigDecimal.ZERO;
        this.valorFaturado = BigDecimal.ZERO;
    }
    
    public NotaDebitoResumoDTO(Long id, String numeroNota, String codigoSocio, 
            String nomeAssociado, BigDecimal totalDebitos, BigDecimal totalCreditos, 
            BigDecimal valorFaturado) {
        this.id = id;
        this.numeroNota = numeroNota;
        this.codigoSocio = codigoSocio;
        this.nomeAssociado = nomeAssociado;
        this.totalDebitos = totalDebitos != null ? totalDebitos : BigDecimal.ZERO;
        this.totalCreditos = totalCreditos != null ? totalCreditos : BigDecimal.ZERO;
        this.valorFaturado = valorFaturado != null ? valorFaturado : BigDecimal.ZERO;
    }
    
    // Getters e Setters
    public Long getId() { 
        return id; 
    }
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getNumeroNota() { 
        return numeroNota; 
    }
    public void setNumeroNota(String numeroNota) { 
        this.numeroNota = numeroNota; 
    }
    
    public String getCodigoSocio() { 
        return codigoSocio; 
    }
    public void setCodigoSocio(String codigoSocio) { 
        this.codigoSocio = codigoSocio; 
    }
    
    public String getNomeAssociado() { 
        return nomeAssociado; 
    }
    public void setNomeAssociado(String nomeAssociado) { 
        this.nomeAssociado = nomeAssociado; 
    }
    
    public BigDecimal getTotalDebitos() { 
        return totalDebitos; 
    }
    public void setTotalDebitos(BigDecimal totalDebitos) { 
        this.totalDebitos = totalDebitos != null ? totalDebitos : BigDecimal.ZERO; 
    }
    // Método de conveniência para Double (se necessário)
    public void setTotalDebitos(Double totalDebitos) { 
        this.totalDebitos = totalDebitos != null ? 
            BigDecimal.valueOf(totalDebitos) : BigDecimal.ZERO; 
    }
    
    public BigDecimal getTotalCreditos() { 
        return totalCreditos; 
    }
    public void setTotalCreditos(BigDecimal totalCreditos) { 
        this.totalCreditos = totalCreditos != null ? totalCreditos : BigDecimal.ZERO; 
    }
    // Método de conveniência para Double
    public void setTotalCreditos(Double totalCreditos) { 
        this.totalCreditos = totalCreditos != null ? 
            BigDecimal.valueOf(totalCreditos) : BigDecimal.ZERO; 
    }
    
    public BigDecimal getValorFaturado() { 
        return valorFaturado; 
    }
    public void setValorFaturado(BigDecimal valorFaturado) { 
        this.valorFaturado = valorFaturado != null ? valorFaturado : BigDecimal.ZERO; 
    }
    // Método de conveniência para Double
    public void setValorFaturado(Double valorFaturado) { 
        this.valorFaturado = valorFaturado != null ? 
            BigDecimal.valueOf(valorFaturado) : BigDecimal.ZERO; 
    }
    
    // Métodos para compatibilidade com Double (se o frontend esperar Double)
    public Double getTotalDebitosAsDouble() {
        return totalDebitos != null ? totalDebitos.doubleValue() : 0.0;
    }
    
    public Double getTotalCreditosAsDouble() {
        return totalCreditos != null ? totalCreditos.doubleValue() : 0.0;
    }
    
    public Double getValorFaturadoAsDouble() {
        return valorFaturado != null ? valorFaturado.doubleValue() : 0.0;
    }
}