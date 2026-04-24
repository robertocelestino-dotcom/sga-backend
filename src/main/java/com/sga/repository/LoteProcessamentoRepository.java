package com.sga.repository;

import com.sga.model.LoteProcessamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteProcessamentoRepository extends JpaRepository<LoteProcessamento, Long> {

    List<LoteProcessamento> findByStatus(String status);
    
    @Query("SELECT l FROM LoteProcessamento l WHERE l.status = 'EM_PROCESSAMENTO'")
    List<LoteProcessamento> findEmProcessamento();
    
    @Query("SELECT l FROM LoteProcessamento l WHERE l.regua.id = :reguaId AND l.mesReferencia = :mesReferencia")
    Optional<LoteProcessamento> findByReguaAndMesReferencia(@Param("reguaId") Long reguaId, @Param("mesReferencia") LocalDate mesReferencia);
    
    @Query("SELECT l FROM LoteProcessamento l ORDER BY l.criadoEm DESC")
    Page<LoteProcessamento> findAllOrderByCriadoEmDesc(Pageable pageable);
}