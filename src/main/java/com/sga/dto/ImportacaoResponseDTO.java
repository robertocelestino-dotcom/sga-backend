package com.sga.dto;

import java.time.ZoneId;
import java.util.Date;

public class ImportacaoResponseDTO {

    private Long id;
    private String nomeArquivo;
    private String status;
    private Date dataImportacao;

    private Integer quantidadeRegistros;
    private Integer registrosProcessados;
    private Double totalValor;

    public ImportacaoResponseDTO() {
    }

    // ============================== NOVO MÉTODO ==============================
    // Constrói o DTO diretamente a partir da entidade ImportacaoSPC
    public static ImportacaoResponseDTO from(com.sga.model.ImportacaoSPC importacao) {
        ImportacaoResponseDTO dto = new ImportacaoResponseDTO();
        dto.setId(importacao.getId());
        dto.setNomeArquivo(importacao.getNomeArquivo());
        dto.setStatus(importacao.getStatus());
        dto.setDataImportacao(Date.from(importacao.getDataImportacao().atZone(ZoneId.systemDefault()).toInstant()));

        try {
            int total = importacao.getNotasDebito().stream()
                    .mapToInt(n -> n.getItens().size())
                    .sum();
            dto.setQuantidadeRegistros(total);
            dto.setRegistrosProcessados(total);
        } catch (Exception e) {
            dto.setQuantidadeRegistros(0);
            dto.setRegistrosProcessados(0);
        }

        try {
            double totalValor = importacao.getNotasDebito().stream()
                    .flatMap(n -> n.getItens().stream())
                    .mapToDouble(i -> i.getValorTotal().doubleValue())
                    .sum();
            dto.setTotalValor(totalValor);
        } catch (Exception e) {
            dto.setTotalValor(0.0);
        }

        return dto;
    }
    // =======================================================================

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

    public Date getDataImportacao() {
        return dataImportacao;
    }

    public void setDataImportacao(Date dataImportacao) {
        this.dataImportacao = dataImportacao;
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
}
