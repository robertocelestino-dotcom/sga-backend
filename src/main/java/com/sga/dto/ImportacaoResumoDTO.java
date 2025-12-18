package com.sga.dto;

import java.time.LocalDateTime;

public class ImportacaoResumoDTO {

    private Long id;
    private String nomeArquivo;
    private String status;
    private LocalDateTime dataImportacao;
    private int quantidadeRegistros;
    private double totalValor;

    public ImportacaoResumoDTO() {
    }

    public ImportacaoResumoDTO(
            Long id,
            String nomeArquivo,
            String status,
            LocalDateTime dataImportacao,
            int quantidadeRegistros,
            double totalValor
    ) {
        this.id = id;
        this.nomeArquivo = nomeArquivo;
        this.status = status;
        this.dataImportacao = dataImportacao;
        this.quantidadeRegistros = quantidadeRegistros;
        this.totalValor = totalValor;
    }

    // GETTERS E SETTERS
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

    public LocalDateTime getDataImportacao() {
        return dataImportacao;
    }

    public void setDataImportacao(LocalDateTime dataImportacao) {
        this.dataImportacao = dataImportacao;
    }

    public int getQuantidadeRegistros() {
        return quantidadeRegistros;
    }

    public void setQuantidadeRegistros(int quantidadeRegistros) {
        this.quantidadeRegistros = quantidadeRegistros;
    }

    public double getTotalValor() {
        return totalValor;
    }

    public void setTotalValor(double totalValor) {
        this.totalValor = totalValor;
    }
}
