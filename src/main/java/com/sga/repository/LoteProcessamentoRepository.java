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
    
    // 🔥 MÉTODO PARA BUSCAR LOTE POR MÊS REFERÊNCIA E RÉGUA
    @Query("SELECT l FROM LoteProcessamento l WHERE l.mesReferencia = :mesReferencia AND l.regua.id = :reguaId")
    List<LoteProcessamento> findByMesReferenciaAndReguaId(
            @Param("mesReferencia") LocalDate mesReferencia,
            @Param("reguaId") Long reguaId);
    
 // 🔥 BUSCAR POR MÊS/ANO APENAS (ignorando o dia)
    @Query("SELECT l FROM LoteProcessamento l " +
           "WHERE FUNCTION('YEAR', l.mesReferencia) = :ano " +
           "AND FUNCTION('MONTH', l.mesReferencia) = :mes " +
           "AND l.regua.id = :reguaId " +
           "AND l.status != 'ERRO'")
    List<LoteProcessamento> findByMesAnoAndReguaId(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            @Param("reguaId") Long reguaId);
    
    @Query("SELECT l FROM LoteProcessamento l " +
           "WHERE FUNCTION('YEAR', l.mesReferencia) = :ano " +
           "AND FUNCTION('MONTH', l.mesReferencia) = :mes " +
           "AND l.status != 'ERRO'")
    List<LoteProcessamento> findByMesAno(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);
}