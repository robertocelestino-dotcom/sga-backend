// src/main/java/com/sga/model/TipoArquivoRegua.java

package com.sga.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tb_regua_tipo_arquivo")
public class TipoArquivoRegua implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;  // CONSOLIDACAO, PREVIA_CORRENTE, PREVIA_ANTERIOR
    
    @Column(name = "ordem", nullable = false)
    private Integer ordem;
    
    // 🔥 RELACIONAMENTO BIDIRECIONAL COM RÉGUA
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regua_id")
    private ReguaFaturamento regua;
    
    // Construtores
    public TipoArquivoRegua() {
    }
    
    public TipoArquivoRegua(String tipo, Integer ordem, ReguaFaturamento regua) {
        this.tipo = tipo;
        this.ordem = ordem;
        this.regua = regua;
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
    
    public ReguaFaturamento getRegua() {
        return regua;
    }
    
    public void setRegua(ReguaFaturamento regua) {
        this.regua = regua;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TipoArquivoRegua that = (TipoArquivoRegua) o;
        return id != null && id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "TipoArquivoRegua{" +
                "id=" + id +
                ", tipo='" + tipo + '\'' +
                ", ordem=" + ordem +
                '}';
    }
}