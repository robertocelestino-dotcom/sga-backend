package com.sga.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VerificacaoAssociadosCompletoDTO {

    private int quantidadeArquivo;   // Trailler
    private int quantidadeBanco;     // Notas Debito
    private int diferenca;

    private List<DivergenciaAssociadoDTO> divergencias = new ArrayList<>();
    private List<NotaBasicaDTO> notasSomenteNoArquivo = new ArrayList<>();
    private List<NotaBasicaDTO> notasSomenteNoBanco = new ArrayList<>();

    // Getters e Setters
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

    public int getDiferenca() {
        return diferenca;
    }

    public void setDiferenca(int diferenca) {
        this.diferenca = diferenca;
    }

    public List<DivergenciaAssociadoDTO> getDivergencias() {
        return divergencias;
    }

    public void setDivergencias(List<DivergenciaAssociadoDTO> divergencias) {
        this.divergencias = divergencias;
    }

    public List<NotaBasicaDTO> getNotasSomenteNoArquivo() {
        return notasSomenteNoArquivo;
    }

    public void setNotasSomenteNoArquivo(List<NotaBasicaDTO> notasSomenteNoArquivo) {
        this.notasSomenteNoArquivo = notasSomenteNoArquivo;
    }

    public List<NotaBasicaDTO> getNotasSomenteNoBanco() {
        return notasSomenteNoBanco;
    }

    public void setNotasSomenteNoBanco(List<NotaBasicaDTO> notasSomenteNoBanco) {
        this.notasSomenteNoBanco = notasSomenteNoBanco;
    }

    // ----------------------
    // DTO INTERNO
    // ----------------------
    public static class NotaBasicaDTO {
        public String numeroNota;
        public String codigoSocio;
        public String nomeAssociado;
        public BigDecimal valorNota;
        public int totalItens;
    }

    public static class DivergenciaAssociadoDTO {
        public String numeroNota;
        public String codigoSocio;

        public String nomeArquivo;
        public String nomeBanco;

        public BigDecimal valorArquivo;
        public BigDecimal valorBanco;

        public int itensArquivo;
        public int itensBanco;

        public boolean nomeDivergente;
        public boolean valorDivergente;
        public boolean itensDivergentes;
    }
}
