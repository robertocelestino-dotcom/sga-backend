// src/main/java/com/sga/repository/HistoricoMigracaoReguaRepository.java

package com.sga.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.HistoricoMigracaoRegua;

@Repository
public interface HistoricoMigracaoReguaRepository 
        extends JpaRepository<HistoricoMigracaoRegua, Long> {

    /**
     * Busca histórico de migrações de um associado (mais recente primeiro)
     */
    List<HistoricoMigracaoRegua> findByAssociadoIdOrderByDataMigracaoDesc(Long associadoId);

    /**
     * Busca histórico de migrações de um associado por status
     */
    List<HistoricoMigracaoRegua> findByAssociadoIdAndStatus(Long associadoId, String status);

    /**
     * Busca histórico de migrações de um associado por período
     */
    @Query("SELECT h FROM HistoricoMigracaoRegua h " +
           "WHERE h.associado.id = :associadoId " +
           "AND h.dataMigracao BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY h.dataMigracao DESC")
    List<HistoricoMigracaoRegua> findByAssociadoIdAndPeriodo(
            @Param("associadoId") Long associadoId,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    /**
     * Conta migrações de um associado
     */
    long countByAssociadoId(Long associadoId);

    /**
     * Verifica se o associado já foi migrado
     */
    boolean existsByAssociadoId(Long associadoId);

    /**
     * Busca última migração de um associado
     */
    @Query("SELECT h FROM HistoricoMigracaoRegua h " +
           "WHERE h.associado.id = :associadoId " +
           "ORDER BY h.dataMigracao DESC")
    List<HistoricoMigracaoRegua> findTopByAssociadoIdOrderByDataMigracaoDesc(
            @Param("associadoId") Long associadoId);

    /**
     * Busca migrações por usuário
     */
    List<HistoricoMigracaoRegua> findByUsuarioOrderByDataMigracaoDesc(String usuario);

    /**
     * Busca migrações por período
     */
    @Query("SELECT h FROM HistoricoMigracaoRegua h " +
           "WHERE h.dataMigracao BETWEEN :dataInicio AND :dataFim " +
           "ORDER BY h.dataMigracao DESC")
    List<HistoricoMigracaoRegua> findByPeriodo(
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);

    /**
     * Busca migrações forçadas
     */
    @Query("SELECT h FROM HistoricoMigracaoRegua h " +
           "WHERE h.migracaoForcada = true " +
           "ORDER BY h.dataMigracao DESC")
    List<HistoricoMigracaoRegua> findMigracoesForcadas();
}
