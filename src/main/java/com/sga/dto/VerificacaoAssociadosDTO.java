package com.sga.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VerificacaoAssociadosDTO {

    private int quantidadeArquivo;
    private int quantidadeBanco;
    private int quantidadeTrailler;

    private int diferencaTraillerArquivo;
    private int diferencaArquivoBanco;

    private List<AssociadoDivergenteDTO> divergentes = new ArrayList<>();
    private List<AssociadoDivergenteDTO> somenteArquivo = new ArrayList<>();
    private List<AssociadoDivergenteDTO> somenteBanco = new ArrayList<>();
    private List<AssociadoDivergenteDTO> faltandoPorTrailler = new ArrayList<>();


    // GETTERS E SETTERS
    public int getQuantidadeArquivo() {
        return quantidadeArquivo;
    }

    public void setQuantidadeArquivo(int quantidadeArquivo) {
        this.quantidadeArquivo = quantidadeArquivo;
    }

    public int getQuantidadeBanco() {
        return quantidadeBanco;
    }

    public void setQuantidadeBanco(int quantidadeBanco) {
        this.quantidadeBanco = quantidadeBanco;
    }

    public int getQuantidadeTrailler() {
        return quantidadeTrailler;
    }

    public void setQuantidadeTrailler(int quantidadeTrailler) {
        this.quantidadeTrailler = quantidadeTrailler;
    }

    public int getDiferencaTraillerArquivo() {
        return diferencaTraillerArquivo;
    }

    public void setDiferencaTraillerArquivo(int diferencaTraillerArquivo) {
        this.diferencaTraillerArquivo = diferencaTraillerArquivo;
    }

    public int getDiferencaArquivoBanco() {
        return diferencaArquivoBanco;
    }

    public void setDiferencaArquivoBanco(int diferencaArquivoBanco) {
        this.diferencaArquivoBanco = diferencaArquivoBanco;
    }

    public List<AssociadoDivergenteDTO> getDivergentes() {
        return divergentes;
    }

    public void setDivergentes(List<AssociadoDivergenteDTO> divergentes) {
        this.divergentes = divergentes;
    }

    public List<AssociadoDivergenteDTO> getSomenteArquivo() {
        return somenteArquivo;
    }

    public void setSomenteArquivo(List<AssociadoDivergenteDTO> somenteArquivo) {
        this.somenteArquivo = somenteArquivo;
    }

    public List<AssociadoDivergenteDTO> getSomenteBanco() {
        return somenteBanco;
    }

    public void setSomenteBanco(List<AssociadoDivergenteDTO> somenteBanco) {
        this.somenteBanco = somenteBanco;
    }

    public List<AssociadoDivergenteDTO> getFaltandoPorTrailler() {
        return faltandoPorTrailler;
    }

    public void setFaltandoPorTrailler(List<AssociadoDivergenteDTO> faltandoPorTrailler) {
        this.faltandoPorTrailler = faltandoPorTrailler;
    }


    // ============================
    // DTO INTERNO
    // ============================
    public static class AssociadoDivergenteDTO {

        private String status;

        private String numeroNota;
        private String codigoSocio;
        private String nomeAssociado;

        private BigDecimal valorNota;
        private int totalItens;
        private BigDecimal valorTotalItens;

        private boolean nomeDivergente;
        private boolean valorDivergente;
        private boolean qtdItensDivergente;

        private List<String> itensFaltantes = new ArrayList<>();
        private List<String> itensExtras = new ArrayList<>();


        // GETTERS E SETTERS
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getNumeroNota() { return numeroNota; }
        public void setNumeroNota(String numeroNota) { this.numeroNota = numeroNota; }

        public String getCodigoSocio() { return codigoSocio; }
        public void setCodigoSocio(String codigoSocio) { this.codigoSocio = codigoSocio; }

        public String getNomeAssociado() { return nomeAssociado; }
        public void setNomeAssociado(String nomeAssociado) { this.nomeAssociado = nomeAssociado; }

        public BigDecimal getValorNota() { return valorNota; }
        public void setValorNota(BigDecimal valorNota) { this.valorNota = valorNota; }

        public int getTotalItens() { return totalItens; }
        public void setTotalItens(int totalItens) { this.totalItens = totalItens; }

        public BigDecimal getValorTotalItens() { return valorTotalItens; }
        public void setValorTotalItens(BigDecimal valorTotalItens) { this.valorTotalItens = valorTotalItens; }

        public boolean isNomeDivergente() { return nomeDivergente; }
        public void setNomeDivergente(boolean nomeDivergente) { this.nomeDivergente = nomeDivergente; }

        public boolean isValorDivergente() { return valorDivergente; }
        public void setValorDivergente(boolean valorDivergente) { this.valorDivergente = valorDivergente; }

        public boolean isQtdItensDivergente() { return qtdItensDivergente; }
        public void setQtdItensDivergente(boolean qtdItensDivergente) { this.qtdItensDivergente = qtdItensDivergente; }

        public List<String> getItensFaltantes() { return itensFaltantes; }
        public void setItensFaltantes(List<String> itensFaltantes) { this.itensFaltantes = itensFaltantes; }

        public List<String> getItensExtras() { return itensExtras; }
        public void setItensExtras(List<String> itensExtras) { this.itensExtras = itensExtras; }
    }
}
