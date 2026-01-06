// src/main/java/com/sga/repository/VendedorRepository.java
package com.sga.repository;

import com.sga.model.Vendedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
    
    // Buscar por nome/razão (contendo, ignore case)
    Page<Vendedor> findByNomeRazaoContainingIgnoreCase(String nomeRazao, Pageable pageable);
    List<Vendedor> findByNomeRazaoContainingIgnoreCase(String nomeRazao);
    
    // Buscar por status
    Page<Vendedor> findByStatus(String status, Pageable pageable);
    List<Vendedor> findByStatus(String status);
    
    // Buscar por tipo de vendedor
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = :vendedorTipoId")
    Page<Vendedor> findByVendedorTipoId(@Param("vendedorTipoId") Long vendedorTipoId, Pageable pageable);
    
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = :vendedorTipoId")
    List<Vendedor> findByVendedorTipoId(@Param("vendedorTipoId") Long vendedorTipoId);
    
    // Buscar por tipo de vendedor e status
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = :vendedorTipoId AND v.status = :status")
    Page<Vendedor> findByVendedorTipoIdAndStatus(@Param("vendedorTipoId") Long vendedorTipoId, 
                                                @Param("status") String status, 
                                                Pageable pageable);
    
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = :vendedorTipoId AND v.status = :status")
    List<Vendedor> findByVendedorTipoIdAndStatus(@Param("vendedorTipoId") Long vendedorTipoId, 
                                                @Param("status") String status);
    
    // Buscar vendedores do tipo 1 (ID específico)
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = 1")
    List<Vendedor> findVendedoresTipo1();
    
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = 1")
    Page<Vendedor> findVendedoresTipo1(Pageable pageable);
    
    // Buscar vendedores do tipo 2 (ID específico)
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = 2")
    List<Vendedor> findVendedoresTipo2();
    
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = 2")
    Page<Vendedor> findVendedoresTipo2(Pageable pageable);
    
    // Buscar vendedores do tipo 1 ativos
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = 1 AND v.status = 'A'")
    List<Vendedor> findVendedoresTipo1Ativos();
    
    // Buscar vendedores do tipo 2 ativos
    @Query("SELECT v FROM Vendedor v WHERE v.vendedorTipo.id = 2 AND v.status = 'A'")
    List<Vendedor> findVendedoresTipo2Ativos();
    
    // Contar vendedores por tipo
    @Query("SELECT COUNT(v) FROM Vendedor v WHERE v.vendedorTipo.id = :tipoId")
    Long countByVendedorTipoId(@Param("tipoId") Long tipoId);
    
    // Contar vendedores ativos por tipo
    @Query("SELECT COUNT(v) FROM Vendedor v WHERE v.vendedorTipo.id = :tipoId AND v.status = 'A'")
    Long countAtivosByVendedorTipoId(@Param("tipoId") Long tipoId);
    
    // Buscar todos ordenados por nome
    List<Vendedor> findAllByOrderByNomeRazaoAsc();
    
    // Buscar ativos ordenados por nome
    List<Vendedor> findByStatusOrderByNomeRazaoAsc(String status);
    
    // Verificar se existe vendedor com mesmo nome no mesmo tipo (exceto o próprio)
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Vendedor v WHERE LOWER(v.nomeRazao) = LOWER(:nomeRazao) AND v.vendedorTipo.id = :tipoId AND v.id <> :id")
    boolean existsByNomeRazaoAndTipoAndNotId(@Param("nomeRazao") String nomeRazao, 
                                             @Param("tipoId") Long tipoId, 
                                             @Param("id") Long id);
}