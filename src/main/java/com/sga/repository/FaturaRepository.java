// src/main/java/com/sga/repository/FaturaRepository.java

package com.sga.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

	@Query("SELECT f FROM Fatura f WHERE f.associado.id = :associadoId " +
	       "AND f.mesReferencia = :mesReferencia " +
	       "AND f.anoReferencia = :anoReferencia")
	List<Fatura> findByAssociadoIdAndMesReferenciaAndAnoReferencia(@Param("associadoId") Long associadoId,
			@Param("mesReferencia") Integer mesReferencia, @Param("anoReferencia") Integer anoReferencia);

	@Query("SELECT f FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano AND f.status = :status")
	List<Fatura> findByPeriodoAndStatus(@Param("mes") Integer mes, @Param("ano") Integer ano,
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

	@Query("SELECT f FROM Fatura f WHERE " +
	       "(:associadoId IS NULL OR f.associado.id = :associadoId) AND " +
	       "(:status IS NULL OR f.status = :status) AND " +
	       "(:mes IS NULL OR f.mesReferencia = :mes) AND " +
	       "(:ano IS NULL OR f.anoReferencia = :ano)")
	Page<Fatura> findByFiltros(@Param("associadoId") Long associadoId,
			@Param("status") String status,
			@Param("mes") Integer mes,
			@Param("ano") Integer ano,
			Pageable pageable);

	@Query("SELECT f FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano")
	Page<Fatura> findByMesReferenciaAndAnoReferencia(@Param("mes") Integer mes, @Param("ano") Integer ano,
			Pageable pageable);

	@Query("SELECT DISTINCT f.associado.id FROM Fatura f WHERE f.mesReferencia = :mes AND f.anoReferencia = :ano")
	List<Long> findAssociadosComFaturaNoPeriodo(@Param("mes") Integer mes, @Param("ano") Integer ano);

	// ========== 🔥 BUSCA COM FILTROS - COM @EntityGraph ==========
	@Query("SELECT DISTINCT f FROM Fatura f " +
		       "LEFT JOIN f.associado a " +
		       "LEFT JOIN LoteProcessamento l ON f.loteProcessamentoId = l.id " +
		       "WHERE (COALESCE(:numeroFatura, '') = '' OR f.numeroFatura LIKE CONCAT('%', :numeroFatura, '%')) " +
		       "AND (COALESCE(:associadoNome, '') = '' OR LOWER(a.nomeRazao) LIKE LOWER(CONCAT('%', :associadoNome, '%'))) " +
		       "AND (COALESCE(:status, '') = '' OR f.status = :status) " +
		       "AND (:mes IS NULL OR f.mesReferencia = :mes) " +
		       "AND (:ano IS NULL OR f.anoReferencia = :ano) " +
		       "AND (:associadoId IS NULL OR a.id = :associadoId) " +
		       "AND (:reguaId IS NULL OR l.regua.id = :reguaId)")
		Page<Fatura> findFaturasComFiltros(
		        @Param("numeroFatura") String numeroFatura,
		        @Param("associadoNome") String associadoNome,
		        @Param("status") String status,
		        @Param("mes") Integer mes,
		        @Param("ano") Integer ano,
		        @Param("associadoId") Long associadoId,
		        @Param("reguaId") Long reguaId,
		        Pageable pageable);
}