package com.sga.repository;

import com.sga.model.ConsumoFranquia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsumoFranquiaRepository extends JpaRepository<ConsumoFranquia, Long> {
    
    Optional<ConsumoFranquia> findByAssociadoIdAndProdutoIdAndAnoAndMes(
        Long associadoId, Long produtoId, Integer ano, Integer mes);
    
    List<ConsumoFranquia> findByAssociadoIdAndAnoAndMes(
        Long associadoId, Integer ano, Integer mes);
    
    // ✅ CORRIGIDO: Usando o nome da entidade "ConsumoFranquia" em vez do nome da tabela
    @Query("SELECT COALESCE(SUM(cf.quantidadeUtilizada), 0) FROM ConsumoFranquia cf " +
           "WHERE cf.associado.id = :associadoId " +
           "AND cf.produto.id = :produtoId " +
           "AND cf.ano = :ano AND cf.mes = :mes")
    Integer sumQuantidadeUtilizada(@Param("associadoId") Long associadoId,
                                   @Param("produtoId") Long produtoId,
                                   @Param("ano") Integer ano,
                                   @Param("mes") Integer mes);
    
    @Modifying
    @Transactional
    @Query("UPDATE ConsumoFranquia cf SET cf.quantidadeUtilizada = cf.quantidadeUtilizada + :quantidade, " +
           "cf.quantidadeExcedente = CASE WHEN (cf.quantidadeUtilizada + :quantidade) > cf.limiteFranquia " +
           "THEN (cf.quantidadeUtilizada + :quantidade - cf.limiteFranquia) ELSE cf.quantidadeExcedente END, " +
           "cf.dataUltimoConsumo = CURRENT_TIMESTAMP " +
           "WHERE cf.associado.id = :associadoId AND cf.produto.id = :produtoId " +
           "AND cf.ano = :ano AND cf.mes = :mes")
    int incrementarConsumo(@Param("associadoId") Long associadoId,
                           @Param("produtoId") Long produtoId,
                           @Param("ano") Integer ano,
                           @Param("mes") Integer mes,
                           @Param("quantidade") Integer quantidade);
    
    @Query("SELECT cf FROM ConsumoFranquia cf " +
           "WHERE cf.associado.id = :associadoId " +
           "AND cf.ano = :ano AND cf.mes = :mes " +
           "AND cf.quantidadeUtilizada > cf.limiteFranquia")
    List<ConsumoFranquia> findConsumosComExcedente(
        @Param("associadoId") Long associadoId,
        @Param("ano") Integer ano,
        @Param("mes") Integer mes);
    
    @Query("SELECT cf FROM ConsumoFranquia cf " +
           "WHERE cf.associado.id = :associadoId " +
           "AND cf.produto.id = :produtoId " +
           "ORDER BY cf.ano DESC, cf.mes DESC")
    List<ConsumoFranquia> findHistoricoConsumo(
        @Param("associadoId") Long associadoId,
        @Param("produtoId") Long produtoId);
    
    @Query("SELECT cf FROM ConsumoFranquia cf " +
           "WHERE cf.dataUltimoConsumo >= :dataInicio")
    List<ConsumoFranquia> findConsumosDesde(@Param("dataInicio") LocalDateTime dataInicio);
}