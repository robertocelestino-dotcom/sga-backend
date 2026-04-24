// src/main/java/com/sga/dto/TipoArquivoReguaDTO.java

package com.sga.dto;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class TipoArquivoReguaDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    @NotBlank(message = "Tipo de arquivo é obrigatório")
    private String tipo;  // CONSOLIDACAO, PREVIA_CORRENTE, PREVIA_ANTERIOR
    
    @NotNull(message = "Ordem é obrigatória")
    private Integer ordem;  // Ordem de importação
    
    // Construtores
    public TipoArquivoReguaDTO() {
    }
    
    public TipoArquivoReguaDTO(Long id, String tipo, Integer ordem) {
        this.id = id;
        this.tipo = tipo;
        this.ordem = ordem;
    }
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public Integer getOrdem() {
        return ordem;
    }
    
    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
    
    /**
     * Retorna o label do tipo para exibição
     */
    public String getTipoLabel() {
        if (tipo == null) return "";
        switch (tipo) {
            case "CONSOLIDACAO":
                return "Consolidação";
            case "PREVIA_CORRENTE":
                return "Prévia Corrente";
            case "PREVIA_ANTERIOR":
                return "Prévia Anterior";
            default:
                return tipo;
        }
    }
    
    /**
     * Verifica se é consolidação
     */
    public boolean isConsolidacao() {
        return "CONSOLIDACAO".equals(tipo);
    }
    
    /**
     * Verifica se é prévia corrente
     */
    public boolean isPreviaCorrente() {
        return "PREVIA_CORRENTE".equals(tipo);
    }
    
    /**
     * Verifica se é prévia anterior
     */
    public boolean isPreviaAnterior() {
        return "PREVIA_ANTERIOR".equals(tipo);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoArquivoReguaDTO that = (TipoArquivoReguaDTO) o;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        return tipo != null && ordem != null && tipo.equals(that.tipo) && ordem.equals(that.ordem);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : (tipo != null ? tipo.hashCode() : 0);
    }
    
    @Override
    public String toString() {
        return "TipoArquivoReguaDTO{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", ordem=" + ordem +
                '}';
    }
}