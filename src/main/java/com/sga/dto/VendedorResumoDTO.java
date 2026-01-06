// src/main/java/com/sga/dto/VendedorResumoDTO.java
package com.sga.dto;

public class VendedorResumoDTO {
    private Long id;
    private String nomeRazao;
    private String nomeFantasia;
    private String cargoFuncao;
    private String status;
    
    // Relacionamento com VendedorTipo
    private Long vendedorTipoId;
    private String vendedorTipoDescricao;
    
    public VendedorResumoDTO() {}
    
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