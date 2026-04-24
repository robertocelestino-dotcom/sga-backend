package com.sga.repository;

import com.sga.model.ReguaFaturamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReguaFaturamentoRepository extends JpaRepository<ReguaFaturamento, Long> {

    @Query("SELECT r FROM ReguaFaturamento r WHERE r.ativo = true ORDER BY r.sequencia ASC")
    List<ReguaFaturamento> findReguasAtivas();
    
    @Query("SELECT r FROM ReguaFaturamento r WHERE r.ativo = true")
    List<ReguaFaturamento> findAllAtivas();

    List<ReguaFaturamento> findByPeriodo(String periodo);

    @Query("SELECT r FROM ReguaFaturamento r WHERE r.ehPadrao = true")
    Optional<ReguaFaturamento> findReguaPadrao();

    @Query("SELECT r FROM ReguaFaturamento r WHERE r.diaEmissao = :dia AND r.tipoArquivo = :tipoArquivo")
    List<ReguaFaturamento> findByDiaEmissaoAndTipoArquivo(@Param("dia") Integer dia, 
                                                           @Param("tipoArquivo") String tipoArquivo);

    @Query("SELECT r FROM ReguaFaturamento r WHERE r.sequencia = :sequencia ORDER BY r.ordemImportacao")
    List<ReguaFaturamento> findBySequenciaOrderByOrdemImportacao(@Param("sequencia") Integer sequencia);
    
    // 🔥 MÉTODO PARA BUSCAR POR ID COM OS TIPOS DE ARQUIVO
    @Query("SELECT DISTINCT r FROM ReguaFaturamento r LEFT JOIN FETCH r.tiposArquivo WHERE r.id = :id")
    Optional<ReguaFaturamento> findByIdWithTiposArquivo(@Param("id") Long id);
    
    
}