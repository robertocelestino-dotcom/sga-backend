// src/main/java/com/sga/repository/SincronizacaoNotificacaoRepository.java

package com.sga.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.SincronizacaoNotificacao;

@Repository
public interface SincronizacaoNotificacaoRepository extends JpaRepository<SincronizacaoNotificacao, Long> {

    /**
     * Verifica se já existe sincronização para o período e código do associado
     */
    @Query("SELECT COUNT(s) > 0 FROM SincronizacaoNotificacao s " +
           "WHERE s.dataInicio = :dataInicio " +
           "AND s.dataFim = :dataFim " +
           "AND (:codigoAssociado IS NULL OR s.codigoAssociado = :codigoAssociado) " +
           "AND s.status = 'CONCLUIDO'")
    boolean existsByPeriodoAndAssociado(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoAssociado") String codigoAssociado);

    /**
     * Busca histórico de sincronizações com paginação
     */
    Page<SincronizacaoNotificacao> findAllByOrderByDataSincronizacaoDesc(Pageable pageable);

    /**
     * Busca sincronizações por período
     */
    @Query("SELECT s FROM SincronizacaoNotificacao s " +
           "WHERE s.dataInicio >= :dataInicio " +
           "AND s.dataFim <= :dataFim " +
           "ORDER BY s.dataSincronizacao DESC")
    List<SincronizacaoNotificacao> findByPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);
    
}