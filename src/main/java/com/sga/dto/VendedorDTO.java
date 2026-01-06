// src/main/java/com/sga/dto/VendedorDTO.java
package com.sga.dto;

import java.time.LocalDateTime;

public class VendedorDTO {
    private Long id;
    private String nomeRazao;
    private String nomeFantasia;
    private String cargoFuncao;
    private String status = "A";
    private String observacao;
    private LocalDateTime dataCadastro;
    
    // Relacionamento com VendedorTipo
    private Long vendedorTipoId;
    private String vendedorTipoDescricao;
    
    public VendedorDTO() {}
    
    // Getters e Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNomeRazao() {
        return nomeRazao;
    }
    
    public void setNomeRazao(String nomeRazao) {
        this.nomeRazao = nomeRazao;
    }
    
    public String getNomeFantasia() {
        return nomeFantasia;
    }
    
    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }
    
    public String getCargoFuncao() {
        return cargoFuncao;
    }
    
    public void setCargoFuncao(String cargoFuncao) {
        this.cargoFuncao = cargoFuncao;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getObservacao() {
        return observacao;
    }
    
    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
    
    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
    
    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
    
    public Long getVendedorTipoId() {
        return vendedorTipoId;
    }
    
    public void setVendedorTipoId(Long vendedorTipoId) {
        this.vendedorTipoId = vendedorTipoId;
    }
    
    public String getVendedorTipoDescricao() {
        return vendedorTipoDescricao;
    }
    
    public void setVendedorTipoDescricao(String vendedorTipoDescricao) {
        this.vendedorTipoDescricao = vendedorTipoDescricao;
    }
}