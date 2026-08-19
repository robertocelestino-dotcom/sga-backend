package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.LogFatura;

@Repository
public interface LogFaturaRepository extends JpaRepository<LogFatura, Long> {

    /**
     * Busca logs por ID da fatura, ordenados por data/hora (crescente)
     */
    List<LogFatura> findByFaturaIdOrderByDataHoraAsc(Long faturaId);

    /**
     * Busca logs por ID da fatura e nível
     */
    List<LogFatura> findByFaturaIdAndNivelOrderByDataHoraAsc(Long faturaId, String nivel);

    /**
     * Busca logs por ID da fatura e passo
     */
    List<LogFatura> findByFaturaIdAndPassoOrderByDataHoraAsc(Long faturaId, String passo);

    /**
     * Deleta todos os logs de uma fatura
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM LogFatura l WHERE l.fatura.id = :faturaId")
    void deleteByFaturaId(@Param("faturaId") Long faturaId);

    /**
     * Conta logs por fatura
     */
    long countByFaturaId(Long faturaId);

    /**
     * Busca logs com erro (WARN ou ERROR)
     */
    @Query("SELECT l FROM LogFatura l WHERE l.fatura.id = :faturaId AND l.nivel IN ('WARN', 'ERROR') ORDER BY l.dataHora ASC")
    List<LogFatura> findErrosByFaturaId(@Param("faturaId") Long faturaId);
}