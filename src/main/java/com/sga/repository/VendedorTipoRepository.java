// src/main/java/com/sga/repository/VendedorTipoRepository.java
package com.sga.repository;

import com.sga.model.VendedorTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendedorTipoRepository extends JpaRepository<VendedorTipo, Long> {
    
    // Buscar por descrição (exata)
    Optional<VendedorTipo> findByDescricao(String descricao);
    
    // Buscar por descrição contendo (ignore case)
    List<VendedorTipo> findByDescricaoContainingIgnoreCase(String descricao);
    
    // Verificar se existe por descrição
    boolean existsByDescricao(String descricao);
    
    // Buscar todos ordenados por descrição
    List<VendedorTipo> findAllByOrderByDescricaoAsc();
    
    // Contar total de tipos de vendedor
    @Query("SELECT COUNT(vt) FROM VendedorTipo vt")
    Long countTotal();
}