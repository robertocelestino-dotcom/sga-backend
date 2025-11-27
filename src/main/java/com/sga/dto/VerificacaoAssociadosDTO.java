package com.sga.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO principal para retorno da verificação de associados / notas entre
 * arquivo SPC e banco.
 */
public class VerificacaoAssociadosDTO {

    // ----------------------------------------------------------
    // RESUMO GERAL
    // ----------------------------------------------------------
    private int quantidadeArquivo;
    private int quantidadeBanco;
    private int diferenca;

    // Lista detalhada de divergências
    private List<AssociadoDivergenteDTO> associadosDivergentes = new ArrayList<>();

    // Notas presentes apenas no arquivo
    private List<String> notasSomenteNoArquivo = new ArrayList<>();

    // Notas presentes apenas no banco
    private List<String> notasSomenteNoBanco = new ArrayList<>();


    // ==========================================================
    // GETTERS / SETTERS
    // ==========================================================

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

    public List<AssociadoDivergenteDTO> getAssociadosDivergentes() {
        return associadosDivergentes;
    }

    public void setAssociadosDivergentes(List<AssociadoDivergenteDTO> associadosDivergentes) {
        this.associadosDivergentes = associadosDivergentes;
    }

    public List<String> getNotasSomenteNoArquivo() {
        return notasSomenteNoArquivo;
    }

    public void setNotasSomenteNoArquivo(List<String> notasSomenteNoArquivo) {
        this.notasSomenteNoArquivo = notasSomenteNoArquivo;
    }

    public List<String> getNotasSomenteNoBanco() {
        return notasSomenteNoBanco;
    }

    public void setNotasSomenteNoBanco(List<String> notasSomenteNoBanco) {
        this.notasSomenteNoBanco = notasSomenteNoBanco;
    }


    // ========================================================================
    // INNER DTO — REPRESENTA UMA NOTA / ASSOCIADO COM ALGUM TIPO DE DIVERGÊNCIA
    // ========================================================================
    public static class AssociadoDivergenteDTO {

        // Dados básicos
        private String codigoSocio;
        private String nomeAssociado;

        // Dados completos da nota
        private BigDecimal valorNota;      // Valor da nota (arq)
        private int totalItens;            // Total de itens (arq)
        private BigDecimal valorTotalItens; // Soma dos itens (arq)

        // Flags de divergência
        private boolean codigoDivergente;
        private boolean nomeDivergente;
        private boolean valorDivergente;
        private boolean qtdItensDivergente;

        // Divergência de itens
        private List<String> itensFaltantes = new ArrayList<>();
        private List<String> itensExtras = new ArrayList<>();

        // Status especial
        private String status; // ex.: "NOTA_PRESENTE_NO_ARQUIVO_AUSENTE_NO_BANCO"


        // ==========================================================
        // GETTERS / SETTERS
        // ==========================================================

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

        public BigDecimal getValorNota() {
            return valorNota;
        }

        public void setValorNota(BigDecimal valorNota) {
            this.valorNota = valorNota;
        }

        public int getTotalItens() {
            return totalItens;
        }

        public void setTotalItens(int totalItens) {
            this.totalItens = totalItens;
        }

        public BigDecimal getValorTotalItens() {
            return valorTotalItens;
        }

        public void setValorTotalItens(BigDecimal valorTotalItens) {
            this.valorTotalItens = valorTotalItens;
        }

        public boolean isCodigoDivergente() {
            return codigoDivergente;
        }

        public void setCodigoDivergente(boolean codigoDivergente) {
            this.codigoDivergente = codigoDivergente;
        }

        public boolean isNomeDivergente() {
            return nomeDivergente;
        }

        public void setNomeDivergente(boolean nomeDivergente) {
            this.nomeDivergente = nomeDivergente;
        }

        public boolean isValorDivergente() {
            return valorDivergente;
        }

        public void setValorDivergente(boolean valorDivergente) {
            this.valorDivergente = valorDivergente;
        }

        public boolean isQtdItensDivergente() {
            return qtdItensDivergente;
        }

        public void setQtdItensDivergente(boolean qtdItensDivergente) {
            this.qtdItensDivergente = qtdItensDivergente;
        }

        public List<String> getItensFaltantes() {
            return itensFaltantes;
        }

        public void setItensFaltantes(List<String> itensFaltantes) {
            this.itensFaltantes = itensFaltantes;
        }

        public List<String> getItensExtras() {
            return itensExtras;
        }

        public void setItensExtras(List<String> itensExtras) {
            this.itensExtras = itensExtras;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}