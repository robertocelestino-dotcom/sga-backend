// src/main/java/com/sga/repository/FaturaRepository.java

package com.sga.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Fatura;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Long> {

    // ========== BUSCAS BÁSICAS ==========

    Optional<Fatura> findByNumeroFatura(String numeroFatura);

    List<Fatura> findByStatus(String status);

    List<Fatura> findByStatusIn(List<String> statusList);

    List<Fatura> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);

    List<Fatura> findByDataEmissaoBetween(LocalDate inicio, LocalDate fim);

    // ========== BUSCAS POR ASSOCIADO ==========

    Page<Fatura> findByAssociadoId(Long associadoId, Pageable pageable);

    List<Fatura> findByAssociadoId(Long associadoId);

    Page<Fatura> findByAssociadoIdAndStatus(Long associadoId, String status, Pageable pageable);

    List<Fatura> findByAssociadoIdAndStatus(Long associadoId, String status);

    // ========== BUSCAS POR PERÍODO ==========

    @Query("SELECT f FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano")
    List<Fatura> findByPeriodo(@Param("mes") Integer mes, @Param("ano") Integer ano);

    @Query("SELECT f FROM Fatura f WHERE f.associado.id = :associadoId "
            + "AND f.mesReferencia = :mesReferencia "
            + "AND f.anoReferencia = :anoReferencia")
    List<Fatura> findByAssociadoIdAndMesReferenciaAndAnoReferencia(
            @Param("associadoId") Long associadoId,
            @Param("mesReferencia") Integer mesReferencia,
            @Param("anoReferencia") Integer anoReferencia);

    @Query("SELECT f FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano AND f.status = :status")
    List<Fatura> findByPeriodoAndStatus(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            @Param("status") String status);

    // ========== BUSCAS PARA INTEGRAÇÃO RM ==========

    @Query("SELECT f FROM Fatura f WHERE f.processadoRm = false OR f.processadoRm IS NULL")
    List<Fatura> findByProcessadoRmFalse();

    @Query("SELECT f FROM Fatura f WHERE f.processadoRm = false AND f.status = 'PENDENTE'")
    List<Fatura> findNaoProcessadasRm();

    @Query("SELECT f FROM Fatura f WHERE f.processadoRm = false AND f.status = 'PENDENTE' AND f.dataVencimento <= :dataLimite")
    List<Fatura> findNaoProcessadasRmComVencimentoAte(@Param("dataLimite") LocalDate dataLimite);

    // ========== BUSCAS POR LOTE ==========

    List<Fatura> findByLoteProcessamentoId(Long loteProcessamentoId);

    @Query("SELECT f FROM Fatura f WHERE f.loteProcessamentoId = :loteId AND f.processadoRm = false")
    List<Fatura> findNaoProcessadasPorLote(@Param("loteId") Long loteId);

    Page<Fatura> findByLoteProcessamentoId(Long loteProcessamentoId, Pageable pageable);

    // ========== BUSCAS POR DATA ==========

    List<Fatura> findByDataVencimento(LocalDate dataVencimento);

    List<Fatura> findByDataVencimentoBefore(LocalDate data);

    List<Fatura> findByDataVencimentoAfter(LocalDate data);

    // ========== ESTATÍSTICAS ==========

    @Query("SELECT COUNT(f) FROM Fatura f WHERE f.status = :status")
    Long countByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(f.valorTotal), 0) FROM Fatura f WHERE f.status = :status")
    BigDecimal sumValorTotalByStatus(@Param("status") String status);

    @Query("SELECT COALESCE(SUM(f.valorTotal), 0) FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano")
    BigDecimal sumValorTotalByPeriodo(@Param("mes") Integer mes, @Param("ano") Integer ano);

    // ========== BUSCAS COM FILTROS ==========

    @Query("SELECT f FROM Fatura f WHERE "
            + "(:associadoId IS NULL OR f.associado.id = :associadoId) AND "
            + "(:status IS NULL OR f.status = :status) AND "
            + "(:mes IS NULL OR f.mesReferencia = :mes) AND "
            + "(:ano IS NULL OR f.anoReferencia = :ano)")
    Page<Fatura> findByFiltros(
            @Param("associadoId") Long associadoId,
            @Param("status") String status,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            Pageable pageable);

    @Query("SELECT f FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano")
    Page<Fatura> findByMesReferenciaAndAnoReferencia(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            Pageable pageable);

    @Query("SELECT DISTINCT f.associado.id FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano")
    List<Long> findAssociadosComFaturaNoPeriodo(
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);

    // ========== 🔥 BUSCA COM FILTROS - COM @EntityGraph ==========
    @Query("SELECT DISTINCT f FROM Fatura f "
            + "LEFT JOIN f.associado a "
            + "LEFT JOIN LoteProcessamento l ON f.loteProcessamentoId = l.id "
            + "WHERE (COALESCE(:numeroFatura, '') = '' OR f.numeroFatura LIKE CONCAT('%', :numeroFatura, '%')) "
            + "AND (COALESCE(:associadoNome, '') = '' OR LOWER(a.nomeRazao) LIKE LOWER(CONCAT('%', :associadoNome, '%'))) "
            + "AND (COALESCE(:status, '') = '' OR f.status = :status) "
            + "AND (:mes IS NULL OR f.mesReferencia = :mes) "
            + "AND (:ano IS NULL OR f.anoReferencia = :ano) "
            + "AND (:associadoId IS NULL OR a.id = :associadoId) "
            + "AND (:reguaId IS NULL OR l.regua.id = :reguaId)")
    Page<Fatura> findFaturasComFiltros(
            @Param("numeroFatura") String numeroFatura,
            @Param("associadoNome") String associadoNome,
            @Param("status") String status,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano,
            @Param("associadoId") Long associadoId,
            @Param("reguaId") Long reguaId,
            Pageable pageable);

    // ============================================================
    // 🔥 MÉTODOS PARA INTEGRAÇÃO RM API
    // ============================================================

    Optional<Fatura> findByNotaDebitoId(Long notaDebitoId);

    List<Fatura> findByNotaDebitoIdIn(List<Long> notaDebitoIds);

    Optional<Fatura> findByNotaDebitoIdAndStatus(Long notaDebitoId, String status);

    List<Fatura> findByProcessadoRmTrue();

    @Query("SELECT f FROM Fatura f LEFT JOIN FETCH f.itens WHERE f.notaDebitoId = :notaDebitoId")
    Optional<Fatura> findByNotaDebitoIdWithItens(@Param("notaDebitoId") Long notaDebitoId);


    // ============================================================
    // 🔥 MÉTODOS PARA CONFERÊNCIA DE FATURAMENTO
    // ============================================================

    /**
     * Lista faturas com dados para conferência.
     * Retorna Object[] — usar CastUtils no service para converter.
     *
     * Aliases únicos obrigatórios: o Hibernate faz auto-discovery dos nomes
     * de colunas em queries nativas e rejeita colunas com mesmo nome (ex: "id").
     */
    @Query(value =
        "SELECT " +
        "   ft.id AS fatura_id, " +
        "   ft.numero_fatura AS numero_fatura, " +
        "   ft.data_emissao AS data_emissao, " +
        "   ft.status AS status_fatura, " +
        "   ft.valor_total AS valor_fatura, " +
        "   nd.id AS nota_id, " +
        "   nd.numero_nota_debito AS numero_nota_debito, " +
        "   nd.data_vencimento AS data_vencimento, " +
        "   nd.valor_nota AS valor_nota, " +
        "   nd.tipo_arquivo AS tipo_arquivo, " +
        "   a.id AS associado_id, " +
        "   a.codigospc AS codigo_spc, " +
        "   a.codigorm AS codigo_rm, " +
        "   a.nomerazao AS nome_razao, " +
        "   COALESCE((SELECT COUNT(*) FROM tb_nota_itens_spc ni WHERE ni.nota_debito_id = nd.id), 0) AS qtd_itens_nota, " +
        "   COALESCE((SELECT COUNT(*) FROM tb_fatura_itens fi WHERE fi.fatura_id = ft.id), 0) AS qtd_itens_fatura, " +
        "   COALESCE((SELECT COUNT(*) FROM tb_nota_itens_spc ni WHERE ni.nota_debito_id = nd.id AND UPPER(ni.descricao_servico) LIKE '%FRANQUIA%'), 0) AS qtd_franquias_nota, " +
        "   COALESCE((SELECT COUNT(*) FROM tb_fatura_itens fi WHERE fi.fatura_id = ft.id AND UPPER(fi.descricao) LIKE '%FRANQUIA%'), 0) AS qtd_franquias_fatura " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "LEFT JOIN tb_associado_regua ar ON a.id = ar.associado_id " +
        "WHERE ft.status != 'CANCELADA' " +
        "  AND (ar.regua_id = COALESCE(:reguaId, ar.regua_id)) " +
        "  AND (ft.data_emissao >= COALESCE(:dataInicio, ft.data_emissao)) " +
        "  AND (ft.data_emissao <= COALESCE(:dataFim, ft.data_emissao)) " +
        "  AND (a.codigospc = COALESCE(:codigoSpc, a.codigospc)) " +
        "ORDER BY ft.data_emissao DESC",
        nativeQuery = true)
    List<Object[]> findConferenciaFaturamento(
            @Param("reguaId") Long reguaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoSpc") String codigoSpc);

    /**
     * Busca dados detalhados de uma fatura específica.
     */
    @Query(value =
        "SELECT " +
        "   ft.id AS fatura_id, " +
        "   ft.numero_fatura AS numero_fatura, " +
        "   ft.data_emissao AS data_emissao, " +
        "   ft.status AS status_fatura, " +
        "   ft.valor_total AS valor_fatura, " +
        "   nd.id AS nota_id, " +
        "   nd.numero_nota_debito AS numero_nota_debito, " +
        "   nd.data_vencimento AS data_vencimento, " +
        "   nd.valor_nota AS valor_nota, " +
        "   a.id AS associado_id, " +
        "   a.codigospc AS codigo_spc, " +
        "   a.codigorm AS codigo_rm, " +
        "   a.nomerazao AS nome_razao " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "WHERE ft.id = :faturaId",
        nativeQuery = true)
    List<Object[]> findFaturaDetalheConferencia(@Param("faturaId") Long faturaId);

    /**
     * Busca itens da nota de débito.
     */
    @Query(value =
        "SELECT " +
        "   ni.codigo_produto_comercial AS codigo, " +
        "   ni.descricao_servico AS descricao, " +
        "   ni.quantidade_servicos AS quantidade, " +
        "   ni.valor_unitario AS valor_unitario, " +
        "   ni.valor_total AS valor_total " +
        "FROM tb_nota_itens_spc ni " +
        "WHERE ni.nota_debito_id = (SELECT nota_debito_id FROM tb_fatura WHERE id = :faturaId) " +
        "ORDER BY ni.descricao_servico",
        nativeQuery = true)
    List<Object[]> findItensNotaByFaturaId(@Param("faturaId") Long faturaId);

    /**
     * Busca itens da fatura.
     */
    @Query(value =
        "SELECT " +
        "   fi.codigo_produto AS codigo, " +
        "   fi.descricao AS descricao, " +
        "   fi.quantidade AS quantidade, " +
        "   fi.valor_unitario AS valor_unitario, " +
        "   fi.valor_total AS valor_total " +
        "FROM tb_fatura_itens fi " +
        "WHERE fi.fatura_id = :faturaId " +
        "ORDER BY fi.descricao",
        nativeQuery = true)
    List<Object[]> findItensFaturaByFaturaId(@Param("faturaId") Long faturaId);

    /**
     * Resumo da conferência.
     */
    @Query(value =
        "SELECT " +
        "   COALESCE(SUM(ft.valor_total - nd.valor_nota), 0) AS soma_diferencas, " +
        "   COALESCE(SUM((SELECT COUNT(*) FROM tb_fatura_itens fi WHERE fi.fatura_id = ft.id)), 0) AS total_itens_fatura, " +
        "   COALESCE(SUM((SELECT COUNT(*) FROM tb_nota_itens_spc ni WHERE ni.nota_debito_id = nd.id)), 0) AS total_itens_nota, " +
        "   COALESCE(SUM((SELECT COUNT(*) FROM tb_fatura_itens fi WHERE fi.fatura_id = ft.id AND UPPER(fi.descricao) LIKE '%FRANQUIA%')), 0) AS total_franquias_fatura, " +
        "   COALESCE(SUM((SELECT COUNT(*) FROM tb_nota_itens_spc ni WHERE ni.nota_debito_id = nd.id AND UPPER(ni.descricao_servico) LIKE '%FRANQUIA%')), 0) AS total_franquias_nota, " +
        "   COALESCE(SUM(nd.valor_nota), 0) AS soma_valor_notas, " +
        "   COALESCE(SUM(ft.valor_total), 0) AS soma_valor_faturas " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "LEFT JOIN tb_associado_regua ar ON a.id = ar.associado_id " +
        "WHERE ft.status != 'CANCELADA' " +
        "  AND (ar.regua_id = COALESCE(:reguaId, ar.regua_id)) " +
        "  AND (ft.data_emissao >= COALESCE(:dataInicio, ft.data_emissao)) " +
        "  AND (ft.data_emissao <= COALESCE(:dataFim, ft.data_emissao))",
        nativeQuery = true)
    List<Object[]> findResumoConferencia(
            @Param("reguaId") Long reguaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim);

    /**
     * Conta faturas por status da conferência.
     * Retorna: [0] = qtd OK, [1] = qtd DIFERENCA
     */
    @Query(value =
        "SELECT " +
        "   SUM(CASE WHEN ABS(ft.valor_total - nd.valor_nota) <= 0.01 AND " +
        "       (SELECT COUNT(*) FROM tb_fatura_itens fi WHERE fi.fatura_id = ft.id) = " +
        "       (SELECT COUNT(*) FROM tb_nota_itens_spc ni WHERE ni.nota_debito_id = nd.id) " +
        "   THEN 1 ELSE 0 END) AS qtd_ok, " +
        "   SUM(CASE WHEN ABS(ft.valor_total - nd.valor_nota) > 0.01 OR " +
        "       (SELECT COUNT(*) FROM tb_fatura_itens fi WHERE fi.fatura_id = ft.id) != " +
        "       (SELECT COUNT(*) FROM tb_nota_itens_spc ni WHERE ni.nota_debito_id = nd.id) " +
        "   THEN 1 ELSE 0 END) AS qtd_diferenca " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "LEFT JOIN tb_associado_regua ar ON a.id = ar.associado_id " +
        "WHERE ft.status != 'CANCELADA' " +
        "  AND (ar.regua_id = COALESCE(:reguaId, ar.regua_id)) " +
        "  AND (ft.data_emissao >= COALESCE(:dataInicio, ft.data_emissao)) " +
        "  AND (ft.data_emissao <= COALESCE(:dataFim, ft.data_emissao)) " +
        "  AND (a.codigospc = COALESCE(:codigoSpc, a.codigospc))",
        nativeQuery = true)
    List<Object[]> contarFaturasPorStatusConferencia(
            @Param("reguaId") Long reguaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoSpc") String codigoSpc);

    /**
     * Soma das diferenças (todos os registros filtrados).
     */
    @Query(value =
        "SELECT " +
        "   COALESCE(SUM(ft.valor_total - nd.valor_nota), 0) AS soma_diferencas, " +
        "   COUNT(*) AS total_faturas " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "LEFT JOIN tb_associado_regua ar ON a.id = ar.associado_id " +
        "WHERE ft.status != 'CANCELADA' " +
        "  AND (ar.regua_id = COALESCE(:reguaId, ar.regua_id)) " +
        "  AND (ft.data_emissao >= COALESCE(:dataInicio, ft.data_emissao)) " +
        "  AND (ft.data_emissao <= COALESCE(:dataFim, ft.data_emissao)) " +
        "  AND (a.codigospc = COALESCE(:codigoSpc, a.codigospc))",
        nativeQuery = true)
    List<Object[]> somarDiferencasConferencia(
            @Param("reguaId") Long reguaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoSpc") String codigoSpc);

    // ============================================================
    // 🔥 MÉTODOS PARA EXPORTAÇÃO CSV
    // ============================================================

    /**
     * Busca faturas para exportação CSV (sem paginação).
     * Retorna todos os dados necessários em uma linha.
     */
    @Query(value =
        "SELECT " +
        "   ft.id AS fatura_id, " +
        "   ft.numero_fatura AS numero_fatura, " +
        "   ft.data_emissao AS data_emissao, " +
        "   ft.data_vencimento AS data_vencimento, " +
        "   ft.status AS status_fatura, " +
        "   ft.valor_total AS valor_fatura, " +
        "   nd.id AS nota_id, " +
        "   nd.numero_nota_debito AS numero_nota_debito, " +
        "   nd.valor_nota AS valor_nota, " +
        "   a.id AS associado_id, " +
        "   a.codigospc AS codigo_spc, " +
        "   a.codigorm AS codigo_rm, " +
        "   a.nomerazao AS nome_razao, " +
        "   a.cnpjcpf AS cnpj_cpf " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "LEFT JOIN tb_associado_regua ar ON a.id = ar.associado_id " +
        "WHERE ft.status != 'CANCELADA' " +
        "  AND (ar.regua_id = COALESCE(:reguaId, ar.regua_id)) " +
        "  AND (ft.data_emissao >= COALESCE(:dataInicio, ft.data_emissao)) " +
        "  AND (ft.data_emissao <= COALESCE(:dataFim, ft.data_emissao)) " +
        "  AND (a.codigospc = COALESCE(:codigoSpc, a.codigospc)) " +
        "ORDER BY ft.data_emissao DESC",
        nativeQuery = true)
    List<Object[]> findConferenciaParaExportacao(
            @Param("reguaId") Long reguaId,
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim,
            @Param("codigoSpc") String codigoSpc);

    /**
     * Busca faturas por lista de IDs para exportação CSV.
     */
    @Query(value =
        "SELECT " +
        "   ft.id AS fatura_id, " +
        "   ft.numero_fatura AS numero_fatura, " +
        "   ft.data_emissao AS data_emissao, " +
        "   ft.data_vencimento AS data_vencimento, " +
        "   ft.status AS status_fatura, " +
        "   ft.valor_total AS valor_fatura, " +
        "   nd.id AS nota_id, " +
        "   nd.numero_nota_debito AS numero_nota_debito, " +
        "   nd.valor_nota AS valor_nota, " +
        "   a.id AS associado_id, " +
        "   a.codigospc AS codigo_spc, " +
        "   a.codigorm AS codigo_rm, " +
        "   a.nomerazao AS nome_razao, " +
        "   a.cnpjcpf AS cnpj_cpf " +
        "FROM tb_fatura ft " +
        "INNER JOIN tb_associado a ON ft.associado_id = a.id " +
        "INNER JOIN tb_nota_debito_spc nd ON ft.nota_debito_id = nd.id " +
        "WHERE ft.id IN (:faturaIds) " +
        "ORDER BY ft.data_emissao DESC",
        nativeQuery = true)
    List<Object[]> findFaturasParaExportacaoPorIds(@Param("faturaIds") List<Long> faturaIds);
    
}