package com.sga.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.NotaDebitoSPC;

@Repository
public interface NotaDebitoSPCRepository extends JpaRepository<NotaDebitoSPC, Long> {

    int countByImportacao_Id(Long importacaoId);

    List<NotaDebitoSPC> findByImportacao_Id(Long importacaoId);

    Page<NotaDebitoSPC> findByImportacao_Id(Long importacaoId, Pageable pageable);

    @Query("select n from NotaDebitoSPC n where n.importacao.id = :importacaoId and "
            + "(lower(n.numeroNotaDebito) like lower(concat('%', :filtro, '%')) or "
            + "lower(n.codigoSocio) like lower(concat('%', :filtro, '%')) or "
            + "lower(n.nomeAssociado) like lower(concat('%', :filtro, '%')) )")
    Page<NotaDebitoSPC> findByImportacaoIdAndFiltro(@Param("importacaoId") Long importacaoId,
            @Param("filtro") String filtro, Pageable pageable);

    @Query(value = " SELECT " + "   nd.id, " + "   nd.numero_nota_debito AS numeroNota, "
            + "   nd.codigo_socio, " + "   nd.nome_associado, "
            + "   COALESCE(SUM(CASE WHEN ni.credito_debito = 'D' THEN ni.valor_total ELSE 0 END), 0) AS total_debitos, "
            + "   COALESCE(SUM(CASE WHEN ni.credito_debito = 'C' THEN ni.valor_total ELSE 0 END), 0) AS total_creditos, "
            + "   COALESCE(SUM( " + "       CASE " + "           WHEN ni.credito_debito = 'D' THEN ni.valor_total "
            + "           WHEN ni.credito_debito = 'C' THEN -ni.valor_total " + "           ELSE 0 " + "       END "
            + "   ), 0) AS valor_faturado " + " FROM tb_nota_debito_spc nd "
            + " LEFT JOIN tb_nota_itens_spc ni ON ni.nota_debito_id = nd.id "
            + " WHERE nd.importacao_id = :importacaoId "
            + " GROUP BY nd.id, nd.numero_nota_debito, nd.codigo_socio, nd.nome_associado ", 
            countQuery = " SELECT COUNT(*) FROM tb_nota_debito_spc WHERE importacao_id = :importacaoId ", 
            nativeQuery = true)
    Page<Object[]> listarNotasResumo(@Param("importacaoId") Long importacaoId, Pageable pageable);

    @Query(value = "SELECT " + "  nd.id, " + "  nd.numero_nota_debito AS numeroNota, "
            + "  nd.codigo_socio AS codigoSocio, " + "  nd.nome_associado AS nomeAssociado, "
            + "  COALESCE(SUM(CASE WHEN ni.credito_debito = 'D' THEN ni.valor_total ELSE 0 END), 0) AS totalDebitos, "
            + "  COALESCE(SUM(CASE WHEN ni.credito_debito = 'C' THEN ni.valor_total ELSE 0 END), 0) AS totalCreditos, "
            + "  COALESCE(SUM( " + "      CASE " + "          WHEN ni.credito_debito = 'D' THEN ni.valor_total "
            + "          WHEN ni.credito_debito = 'C' THEN -ni.valor_total " + "          ELSE 0 " + "      END "
            + "  ), 0) AS valorFaturado " + "FROM tb_nota_debito_spc nd "
            + "LEFT JOIN tb_nota_itens_spc ni ON ni.nota_debito_id = nd.id " + "WHERE nd.importacao_id = :importacaoId "
            + "  AND (:filtro IS NULL OR :filtro = '' OR " + "       nd.codigo_socio LIKE CONCAT('%', :filtro, '%') OR "
            + "       nd.nome_associado LIKE CONCAT('%', :filtro, '%') OR "
            + "       nd.numero_nota_debito LIKE CONCAT('%', :filtro, '%')) " + 
            "GROUP BY nd.id, nd.numero_nota_debito, nd.codigo_socio, nd.nome_associado", 
            countQuery = "SELECT COUNT(DISTINCT nd.id) "
                    + "FROM tb_nota_debito_spc nd " + "WHERE nd.importacao_id = :importacaoId "
                    + "  AND (:filtro IS NULL OR :filtro = '' OR "
                    + "       nd.codigo_socio LIKE CONCAT('%', :filtro, '%') OR "
                    + "       nd.nome_associado LIKE CONCAT('%', :filtro, '%') OR "
                    + "       nd.numero_nota_debito LIKE CONCAT('%', :filtro, '%'))", 
            nativeQuery = true)
    Page<Object[]> listarNotasResumoComFiltro(@Param("importacaoId") Long importacaoId, @Param("filtro") String filtro,
            Pageable pageable);

    @Query("SELECT new map(" + "nd.id as id, " + "nd.numeroNotaDebito as numeroNota, "
            + "nd.codigoSocio as codigoSocio, " + "nd.nomeAssociado as nomeAssociado, " + "nd.cnpjCic as cnpjCic, "
            + "nd.enderecoCobranca as endereco, " + "nd.cidadeCobranca as cidade, " + "nd.ufCobranca as uf, "
            + "nd.cepCobranca as cep, " + "nd.dataVencimento as dataVencimento, " + "nd.valorNota as valorNota, "
            + "nd.importacao.id as importacaoId) " + "FROM NotaDebitoSPC nd " + "WHERE nd.id = :notaId")
    Optional<Map<String, Object>> findDetalhesBasicosById(@Param("notaId") Long notaId);

    @Query("SELECT new map(" + "i.id as id, " + "i.codigoProduto as codigo, " + "i.descricaoServico as descricao, "
            + "i.quantidadeServicos as quantidade, " + "i.valorUnitario as valorUnitario, "
            + "i.valorTotal as valorTotal, " + "i.creditoDebito as tipoLancamento) " + "FROM ItemSPC i "
            + "WHERE i.notaDebito.id = :notaId " + "ORDER BY i.id")
    List<Map<String, Object>> findItensByNotaId(@Param("notaId") Long notaId);

    @Query(value = "SELECT " + "  nd.id, " + "  nd.numero_nota_debito AS numeroNota, "
            + "  nd.codigo_socio AS codigoSocio, " + "  nd.nome_associado AS nomeAssociado, "
            + "  nd.cnpj_cic AS cnpjCic, " + "  nd.endereco_cobranca AS endereco, " + "  nd.cidade_cobranca AS cidade, "
            + "  nd.uf_cobranca AS uf, " + "  nd.cep_cobranca AS cep, " + "  nd.data_vencimento AS dataVencimento, "
            + "  nd.valor_nota AS valorNota, " + "  nd.importacao_id AS importacaoId " + "FROM tb_nota_debito_spc nd "
            + "WHERE nd.id = :notaId", nativeQuery = true)
    Map<String, Object> findDetalhesBasicosByIdNative(@Param("notaId") Long notaId);

    @Query(value = "SELECT " + "  ni.id, " + "  ni.codigo_produto AS codigo, " + "  ni.descricao_servico AS descricao, "
            + "  ni.quantidade_servicos AS quantidade, " + "  ni.valor_unitario AS valorUnitario, "
            + "  ni.valor_total AS valorTotal, " + "  ni.credito_debito AS tipoLancamento "
            + "FROM tb_nota_itens_spc ni " + "WHERE ni.nota_debito_id = :notaId "
            + "ORDER BY ni.id", nativeQuery = true)
    List<Map<String, Object>> findItensByNotaIdNative(@Param("notaId") Long notaId);

    // ========== MÉTODOS PARA FATURAMENTO ==========
    
    /**
     * Busca notas por código do sócio (código SPC)
     */
    @Query("SELECT n FROM NotaDebitoSPC n WHERE n.codigoSocio = :codigoSocio")
    List<NotaDebitoSPC> findByCodigoSocio(@Param("codigoSocio") String codigoSocio);
    
    /**
     * Busca notas por código do sócio (com paginação)
     */
    Page<NotaDebitoSPC> findByCodigoSocio(String codigoSocio, Pageable pageable);
    
    /**
     * Busca notas por nome do associado (busca parcial)
     */
    List<NotaDebitoSPC> findByNomeAssociadoContainingIgnoreCase(String nomeAssociado);
    
    List<NotaDebitoSPC> findByImportacaoId(Long importacaoId);
    
    // 🔥 MÉTODO CORRIGIDO - Usando Native Query do PostgreSQL
    @Query(value = "SELECT * FROM tb_nota_debito_spc WHERE " +
           "CAST(CAST(codigo_socio AS INTEGER) AS VARCHAR) = CAST(CAST(:codigoSpc AS INTEGER) AS VARCHAR) " +
           "OR codigo_socio = :codigoSpc " +
           "OR LPAD(codigo_socio, 8, '0') = LPAD(:codigoSpc, 8, '0')", 
           nativeQuery = true)
    List<NotaDebitoSPC> findByCodigoSocioNormalized(@Param("codigoSpc") String codigoSpc);
    
    // 🔥 MÉTODO PARA BUSCAR NOTAS POR CÓDIGO E PERÍODO (NATIVE QUERY)
    @Query(value = "SELECT * FROM tb_nota_debito_spc WHERE " +
           "(CAST(CAST(codigo_socio AS INTEGER) AS VARCHAR) = CAST(CAST(:codigoSpc AS INTEGER) AS VARCHAR) " +
           "OR codigo_socio = :codigoSpc " +
           "OR LPAD(codigo_socio, 8, '0') = LPAD(:codigoSpc, 8, '0')) " +
           "AND mes_referencia = :mes AND ano_referencia = :ano", 
           nativeQuery = true)
    List<NotaDebitoSPC> findByCodigoSocioAndPeriodo(
            @Param("codigoSpc") String codigoSpc,
            @Param("mes") Integer mes,
            @Param("ano") Integer ano);
}