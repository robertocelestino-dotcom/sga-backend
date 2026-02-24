package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.AssociadoDefFaturamento;

@Repository
public interface AssociadoDefFaturamentoRepository extends JpaRepository<AssociadoDefFaturamento, Long> {

	List<AssociadoDefFaturamento> findByAssociadoId(Long associadoId);

	Page<AssociadoDefFaturamento> findByAssociadoId(Long associadoId, Pageable pageable);

	Optional<AssociadoDefFaturamento> findByAssociadoIdAndPlanoId(Long associadoId, Long planoId);

	@Query("SELECT adf FROM AssociadoDefFaturamento adf "
			+ "WHERE adf.associado.id = :associadoId AND adf.diaEmissao = :diaEmissao")
	List<AssociadoDefFaturamento> findByAssociadoIdAndDiaEmissao(@Param("associadoId") Long associadoId,
			@Param("diaEmissao") Integer diaEmissao);

	@Query("SELECT COUNT(adf) > 0 FROM AssociadoDefFaturamento adf "
			+ "WHERE adf.associado.id = :associadoId AND adf.diaEmissao = :diaEmissao")
	boolean existsByAssociadoIdAndDiaEmissao(@Param("associadoId") Long associadoId,
			@Param("diaEmissao") Integer diaEmissao);

	@Modifying
	@Query("DELETE FROM AssociadoDefFaturamento adf WHERE adf.associado.id = :associadoId")
	void deleteByAssociadoId(@Param("associadoId") Long associadoId);

	@Query("SELECT DISTINCT adf.diaEmissao FROM AssociadoDefFaturamento adf "
			+ "WHERE adf.associado.id = :associadoId ORDER BY adf.diaEmissao")
	List<Integer> findDiasEmissaoByAssociadoId(@Param("associadoId") Long associadoId);
}