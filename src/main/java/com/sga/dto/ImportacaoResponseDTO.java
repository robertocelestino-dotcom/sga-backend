package com.sga.dto;

import java.util.Date;

public class ImportacaoResponseDTO {
    private Long id;
    private String nomeArquivo;
    private String status;
    private Integer quantidadeRegistros; // Nome mais genérico
    private Integer registrosProcessados; // Nome mais genérico  
    private Double totalValor; // Nome mais genérico
    private Date dataImportacao;
    
    // Construtor padrão
    public ImportacaoResponseDTO() {}
    
    // Construtor com parâmetros
    public ImportacaoResponseDTO(Long id, String nomeArquivo, String status, 
                                Integer quantidadeRegistros, Integer registrosProcessados,
                                Double totalValor, Date dataImportacao) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.status = status;
        this.quantidadeRegistros = quantidadeRegistros;
        this.registrosProcessados = registrosProcessados;
        this.totalValor = totalValor;
        this.dataImportacao = dataImportacao;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNomeArquivo() {
        return nomeArquivo;
    }
    
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getQuantidadeRegistros() {
        return quantidadeRegistros;
    }
    
    public void setQuantidadeRegistros(Integer quantidadeRegistros) {
        this.quantidadeRegistros = quantidadeRegistros;
    }
    
    public Integer getRegistrosProcessados() {
        return registrosProcessados;
    }
    
    public void setRegistrosProcessados(Integer registrosProcessados) {
        this.registrosProcessados = registrosProcessados;
    }
    
    public Double getTotalValor() {
        return totalValor;
    }
    
    public void setTotalValor(Double totalValor) {
        this.totalValor = totalValor;
    }
    
    public Date getDataImportacao() {
        return dataImportacao;
    }
    
    public void setDataImportacao(Date dataImportacao) {
        this.dataImportacao = dataImportacao;
    }
    
    @Override
    public String toString() {
        return "ImportacaoResponseDTO{" +
                "id=" + id +
                ", nomeArquivo='" + nomeArquivo + '\'' +
                ", status='" + status + '\'' +
                ", quantidadeRegistros=" + quantidadeRegistros +
                ", registrosProcessados=" + registrosProcessados +
                ", totalValor=" + totalValor +
                ", dataImportacao=" + dataImportacao +
                '}';
    }
}