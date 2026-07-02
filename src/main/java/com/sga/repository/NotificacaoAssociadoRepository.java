// src/main/java/com/sga/repository/NotificacaoAssociadoRepository.java
package com.sga.repository;

import com.sga.model.NotificacaoAssociado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}