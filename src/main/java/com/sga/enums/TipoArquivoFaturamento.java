package com.sga.enums;

/**
 * Enum para tipos de arquivo de faturamento SPC
 */
public enum TipoArquivoFaturamento {
    
    CONSOLIDADO("CONSOLIDADO", "Arquivo Consolidado"),
    PREVIA_ANTERIOR("PREVIA_ANTERIOR", "Prévia do Mês Anterior"),
    PREVIA_CORRENTE("PREVIA_CORRENTE", "Prévia do Mês Corrente");
    
    private final String codigo;
    private final String descricao;
    
    /**
     * Construtor do enum
     * @param codigo Código do tipo de arquivo
     * @param descricao Descrição do tipo de arquivo
     */
    TipoArquivoFaturamento(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public String getDescricao() {
        return descricao;
    }
    
    /**
     * Busca um enum pelo código
     * @param codigo Código do tipo de arquivo
     * @return Enum correspondente ou null
     */
    public static TipoArquivoFaturamento fromCodigo(String codigo) {
        for (TipoArquivoFaturamento tipo : values()) {
            if (tipo.codigo.equals(codigo)) {
                return tipo;
            }
        }
        return null;
    }
    
    /**
     * Verifica se é um arquivo de prévia
     */
    public boolean isPrevia() {
        return this == PREVIA_ANTERIOR || this == PREVIA_CORRENTE;
    }
    
    /**
     * Verifica se é arquivo consolidado
     */
    public boolean isConsolidado() {
        return this == CONSOLIDADO;
    }
    
    @Override
    public String toString() {
        return descricao;
    }
}