package com.sga.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoReguaDTO;
import com.sga.model.AssociadoRegua;

@Repository
public interface AssociadoReguaRepository extends JpaRepository<AssociadoRegua, Long> {

    // ========== BUSCAS COM DTO ==========
    
    @Query("SELECT new com.sga.dto.AssociadoReguaDTO(" +
           "ar.id, " +
           "ar.associado.id, " +
           "ar.associado.nomeRazao, " +
           "ar.associado.codigoSpc, " +
           "ar.regua.id, " +
           "ar.regua.nome, " +
           "ar.dataInicio, " +
           "ar.dataFim, " +
           "ar.ativo, " +
           "ar.motivoMigracao, " +
           "ar.observacao, " +
           "ar.criadoEm, " +
           "ar.criadoPor) " +
           "FROM AssociadoRegua ar " +
           "WHERE ar.regua.id = :reguaId AND ar.ativo = true")
    List<AssociadoReguaDTO> findAtivosByReguaIdDTO(@Param("reguaId") Long reguaId);
    
    // ========== BUSCAS POR RÉGUA ==========
    
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.regua.id = :reguaId AND ar.ativo = true")
    List<AssociadoRegua> findByReguaIdAndAtivoTrue(@Param("reguaId") Long reguaId);
    
    Page<AssociadoRegua> findByReguaIdAndAtivoTrue(Long reguaId, Pageable pageable);
    
    // ========== BUSCAS POR ASSOCIADO ==========
    
    Optional<AssociadoRegua> findByAssociadoIdAndAtivoTrue(Long associadoId);
    
    List<AssociadoRegua> findByAssociadoId(Long associadoId);
    
    // 🔥 MÉTODO PARA VERIFICAR SE EXISTE ASSOCIAÇÃO ATIVA
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.ativo = true")
    boolean existsByAssociadoIdAndAtivoTrue(@Param("associadoId") Long associadoId);
    
    // 🔥 MÉTODO PARA VERIFICAR SE EXISTE ASSOCIAÇÃO ATIVA COM UMA RÉGUA ESPECÍFICA
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.regua.id = :reguaId AND ar.ativo = true")
    boolean existsByAssociadoIdAndReguaIdAndAtivoTrue(@Param("associadoId") Long associadoId, @Param("reguaId") Long reguaId);
    
    // ========== ATUALIZAÇÕES ==========
    
    @Modifying
    @Transactional
    @Query("UPDATE AssociadoRegua ar SET ar.ativo = false, ar.dataFim = :dataFim WHERE ar.associado.id = :associadoId AND ar.ativo = true")
    void inativarAssociadoAtual(@Param("associadoId") Long associadoId, @Param("dataFim") LocalDate dataFim);
    
    // ========== BUSCAS POR PERÍODO ==========
    
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.regua.id = :reguaId AND ar.ativo = true AND ar.dataInicio <= :dataReferencia")
    List<AssociadoRegua> findAtivosPorReguaNaData(@Param("reguaId") Long reguaId, @Param("dataReferencia") LocalDate dataReferencia);
    
    
}