// src/main/java/com/sga/dto/ProdutoFiltroDTO.java
package com.sga.dto;

import org.springframework.data.domain.Pageable;

import lombok.Data;

@Data
public class ProdutoFiltroDTO {
    private String codigo;
    private String nome;
    private String tipoProduto;
    private String categoria;
    private String modalidade;
    private String status;
    private Boolean temFranquia;
    private Pageable pageable;
    
    public boolean hasFiltros() {
        return codigo != null || nome != null || tipoProduto != null || 
               categoria != null || modalidade != null || status != null || temFranquia != null;
    }
}