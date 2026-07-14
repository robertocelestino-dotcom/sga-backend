// src/main/java/com/sga/repository/NotificacaoAssociadoRepository.java
package com.sga.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.NotificacaoAssociado;

@Repository
public interface NotificacaoAssociadoRepository extends JpaRepository<NotificacaoAssociado, Long> {

    Optional<NotificacaoAssociado> findByAssociadoIdAndMesReferenciaAndAnoReferencia(
            Long associadoId, Integer mes, Integer ano);

    List<NotificacaoAssociado> findByMesReferenciaAndAnoReferencia(Integer mes, Integer ano);

    List<NotificacaoAssociado> findByAssociadoIdAndProcessadoFaturaFalse(Long associadoId);

    @Query("SELECT n FROM NotificacaoAssociado n WHERE n.mesReferencia = :mes AND n.anoReferencia = :ano AND n.processadoFatura = false")
    List<NotificacaoAssociado> findNaoProcessados(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Modifying
    @Query("UPDATE NotificacaoAssociado n SET n.processadoFatura = true, n.faturaId = :faturaId, n.dataProcessamento = CURRENT_TIMESTAMP WHERE n.id = :id")
    void marcarComoProcessado(@Param("id") Long id, @Param("faturaId") Long faturaId);
    
    /**
     * 🔥 Buscar notificações por código SPC e período
     */
    @Query("SELECT n FROM NotificacaoAssociado n WHERE n.codigoSpc = :codigoSpc AND n.mesReferencia = :mes AND n.anoReferencia = :ano")
    Optional<NotificacaoAssociado> findByCodigoSpcAndMesReferenciaAndAnoReferencia(
            @Param("codigoSpc") String codigoSpc,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);
    
    /**
     * 🔥 REMOVER NOTIFICAÇÕES POR PERÍODO
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificacaoAssociado n " +
           "WHERE n.periodoInicio = :dataInicio " +
           "AND n.periodoFim = :dataFim " +
           "AND (:codigoAssociado IS NULL OR n.codigoSpc = :codigoAssociado)")
    int deleteByPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoAssociado") String codigoAssociado);

    /**
     * 🔥 VERIFICAR SE EXISTE NOTIFICAÇÃO FATURADA NO PERÍODO
     */
    @Query("SELECT COUNT(n) > 0 FROM NotificacaoAssociado n " +
           "WHERE n.periodoInicio = :dataInicio " +
           "AND n.periodoFim = :dataFim " +
           "AND (:codigoAssociado IS NULL OR n.codigoSpc = :codigoAssociado) " +
           "AND n.processadoFatura = true")
    boolean existsFaturadoByPeriodo(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoAssociado") String codigoAssociado);
    
}