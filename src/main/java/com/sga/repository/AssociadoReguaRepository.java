// src/main/java/com/sga/repository/AssociadoReguaRepository.java

package com.sga.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoReguaDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.model.AssociadoRegua;

@Repository
public interface AssociadoReguaRepository extends JpaRepository<AssociadoRegua, Long> {

    // ========== BUSCAS COM DTO ==========
    @Query("SELECT new com.sga.dto.AssociadoReguaDTO(" +
           "ar.id, ar.associado.id, ar.associado.nomeRazao, ar.associado.codigoSpc, " +
           "ar.regua.id, ar.regua.nome, ar.dataInicio, ar.dataFim, ar.ativo, " +
           "ar.motivoMigracao, ar.observacao, ar.criadoEm, ar.criadoPor) " +
           "FROM AssociadoRegua ar " +
           "WHERE ar.regua.id = :reguaId AND ar.ativo = true")
    List<AssociadoReguaDTO> findAtivosByReguaIdDTO(@Param("reguaId") Long reguaId);

    // ========== BUSCAS POR RÉGUA ==========
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.regua.id = :reguaId AND ar.ativo = true")
    List<AssociadoRegua> findByReguaIdAndAtivoTrue(@Param("reguaId") Long reguaId);
    
    Page<AssociadoRegua> findByReguaIdAndAtivoTrue(Long reguaId, Pageable pageable);
    
    List<AssociadoRegua> findByReguaId(Long reguaId);

    // ========== BUSCAS POR ASSOCIADO ==========
    
    // 🔥 MANTIDO PARA COMPATIBILIDADE (pode lançar exceção se houver duplicatas)
    Optional<AssociadoRegua> findByAssociadoIdAndAtivoTrue(Long associadoId);
    
    List<AssociadoRegua> findByAssociadoId(Long associadoId);

    // 🔥 NOVO MÉTODO: BUSCA TODOS OS REGISTROS ATIVOS (PARA LIDAR COM DUPLICATAS)
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.ativo = true ORDER BY ar.id DESC")
    List<AssociadoRegua> findAllByAssociadoIdAndAtivoTrue(@Param("associadoId") Long associadoId);

    // 🔥 NOVO MÉTODO: BUSCA O PRIMEIRO REGISTRO ATIVO (MAIS RECENTE)
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.ativo = true ORDER BY ar.id DESC")
    Optional<AssociadoRegua> findFirstByAssociadoIdAndAtivoTrueOrderByIdDesc(@Param("associadoId") Long associadoId);

    // 🔥 NOVO MÉTODO: BUSCA O PRIMEIRO REGISTRO ATIVO (MAIS ANTIGO)
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.ativo = true ORDER BY ar.id ASC")
    Optional<AssociadoRegua> findFirstByAssociadoIdAndAtivoTrueOrderByIdAsc(@Param("associadoId") Long associadoId);

    // 🔥 NOVO MÉTODO: BUSCA TODOS OS REGISTROS (ORDENADO POR ID DESC)
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId ORDER BY ar.id DESC")
    List<AssociadoRegua> findByAssociadoIdOrderByIdDesc(@Param("associadoId") Long associadoId);

    // 🔥 NOVO MÉTODO: BUSCA O ÚLTIMO REGISTRO (MAIS RECENTE)
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId ORDER BY ar.id DESC")
    Optional<AssociadoRegua> findFirstByAssociadoIdOrderByIdDesc(@Param("associadoId") Long associadoId);

    // 🔥 NOVO MÉTODO: CONTA REGISTROS ATIVOS
    @Query("SELECT COUNT(ar) FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.ativo = true")
    long countActiveByAssociadoId(@Param("associadoId") Long associadoId);

    // ========== MÉTODOS DE VERIFICAÇÃO ==========
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
           "FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId AND ar.ativo = true")
    boolean existsByAssociadoIdAndAtivoTrue(@Param("associadoId") Long associadoId);

    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
           "FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId " +
           "AND ar.regua.id = :reguaId AND ar.ativo = true")
    boolean existsByAssociadoIdAndReguaIdAndAtivoTrue(@Param("associadoId") Long associadoId,
                                                       @Param("reguaId") Long reguaId);

    // ========== ATUALIZAÇÕES ==========
    @Modifying
    @Transactional
    @Query("UPDATE AssociadoRegua ar SET ar.ativo = false, ar.dataFim = :dataFim " +
           "WHERE ar.associado.id = :associadoId AND ar.ativo = true")
    void inativarAssociadoAtual(@Param("associadoId") Long associadoId,
                                @Param("dataFim") LocalDate dataFim);

    // ========== BUSCAS POR PERÍODO ==========
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.regua.id = :reguaId " +
           "AND ar.ativo = true AND ar.dataInicio <= :dataReferencia")
    List<AssociadoRegua> findAtivosPorReguaNaData(@Param("reguaId") Long reguaId,
                                                   @Param("dataReferencia") LocalDate dataReferencia);

    @Query("SELECT ar.associado.id FROM AssociadoRegua ar WHERE ar.regua.id = :reguaId AND ar.ativo = true")
    List<Long> findAssociadoIdsByReguaId(@Param("reguaId") Long reguaId);

    // ========== BUSCA CONSOLIDADA (NATIVE QUERY) ==========
    @Query(value = 
        "SELECT DISTINCT " +
        "   a.id, " +
        "   CAST(a.nomerazao AS VARCHAR) AS nomerazao, " +
        "   CAST(a.cnpjcpf AS VARCHAR) AS cnpjcpf, " +
        "   CAST(a.codigospc AS VARCHAR) AS codigospc, " +
        "   CAST(a.status AS VARCHAR) AS status " +
        "FROM tb_associado_regua ar " +
        "INNER JOIN tb_associado a ON ar.associado_id = a.id " +
        "INNER JOIN tb_regua_faturamento r ON ar.regua_id = r.id " +
        "WHERE r.id = :reguaId " +
        "AND ar.data_fim IS NULL " +
        "AND EXISTS ( " +
        "   SELECT 1 FROM tb_nota_debito_spc nd " +
        "   WHERE CAST(nd.codigo_socio AS INTEGER) = CAST(a.codigospc AS INTEGER) " +
        ") " +
        "AND (:nome IS NULL OR LOWER(CAST(a.nomerazao AS VARCHAR)) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
        "AND (:cnpjCpf IS NULL OR LOWER(CAST(a.cnpjcpf AS VARCHAR)) LIKE LOWER(CONCAT('%', :cnpjCpf, '%'))) " +
        "ORDER BY CAST(a.nomerazao AS VARCHAR) ASC",
        countQuery = 
        "SELECT COUNT(DISTINCT a.id) " +
        "FROM tb_associado_regua ar " +
        "INNER JOIN tb_associado a ON ar.associado_id = a.id " +
        "INNER JOIN tb_regua_faturamento r ON ar.regua_id = r.id " +
        "WHERE r.id = :reguaId " +
        "AND ar.data_fim IS NULL " +
        "AND EXISTS ( " +
        "   SELECT 1 FROM tb_nota_debito_spc nd " +
        "   WHERE CAST(nd.codigo_socio AS INTEGER) = CAST(a.codigospc AS INTEGER) " +
        ") " +
        "AND (:nome IS NULL OR LOWER(CAST(a.nomerazao AS VARCHAR)) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
        "AND (:cnpjCpf IS NULL OR LOWER(CAST(a.cnpjcpf AS VARCHAR)) LIKE LOWER(CONCAT('%', :cnpjCpf, '%'))) ",
        nativeQuery = true)
    Page<Object[]> findAssociadosConsolidadoPorReguaNative(
            @Param("reguaId") Long reguaId,
            @Param("nome") String nome,
            @Param("cnpjCpf") String cnpjCpf,
            Pageable pageable);
    
    /**
     * 🔥 VERIFICA SE EXISTE UM REGISTRO COM A MESMA COMBINAÇÃO (INDEPENDENTE DO STATUS)
     */
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
           "FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId " +
           "AND ar.regua.id = :reguaId AND ar.dataInicio = :dataInicio")
    boolean existsByAssociadoIdAndReguaIdAndDataInicio(@Param("associadoId") Long associadoId,
                                                        @Param("reguaId") Long reguaId,
                                                        @Param("dataInicio") LocalDate dataInicio);
    
    /**
     * 🔥 VERIFICA SE EXISTE UM REGISTRO ATIVO COM A MESMA COMBINAÇÃO
     */
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
           "FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId " +
           "AND ar.regua.id = :reguaId AND ar.dataInicio = :dataInicio AND ar.ativo = true")
    boolean existsByAssociadoIdAndReguaIdAndDataInicioAndAtivoTrue(@Param("associadoId") Long associadoId,
                                                                    @Param("reguaId") Long reguaId,
                                                                    @Param("dataInicio") LocalDate dataInicio);
    
    /**
     * 🔥 VERIFICA SE EXISTE UM REGISTRO INATIVO COM A MESMA COMBINAÇÃO
     */
    @Query("SELECT CASE WHEN COUNT(ar) > 0 THEN true ELSE false END " +
           "FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId " +
           "AND ar.regua.id = :reguaId AND ar.dataInicio = :dataInicio AND ar.ativo = false")
    boolean existsByAssociadoIdAndReguaIdAndDataInicioAndAtivoFalse(@Param("associadoId") Long associadoId,
                                                                     @Param("reguaId") Long reguaId,
                                                                     @Param("dataInicio") LocalDate dataInicio);

    /**
     * 🔥 BUSCA UM REGISTRO PELA COMBINAÇÃO
     */
    @Query("SELECT ar FROM AssociadoRegua ar WHERE ar.associado.id = :associadoId " +
           "AND ar.regua.id = :reguaId AND ar.dataInicio = :dataInicio")
    Optional<AssociadoRegua> findByAssociadoIdAndReguaIdAndDataInicio(@Param("associadoId") Long associadoId,
                                                                       @Param("reguaId") Long reguaId,
                                                                       @Param("dataInicio") LocalDate dataInicio);    
    
}

