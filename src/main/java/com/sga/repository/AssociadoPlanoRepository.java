package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.AssociadoPlano;

@Repository
public interface AssociadoPlanoRepository extends JpaRepository<AssociadoPlano, Long> {

	List<AssociadoPlano> findByAssociadoIdAndStatus(Long associadoId, String status);

	Optional<AssociadoPlano> findByAssociadoIdAndPlanoIdAndStatus(Long associadoId, Long planoId, String status);

	@Query("SELECT ap FROM AssociadoPlano ap " + "WHERE ap.associado.id = :associadoId " + "AND ap.status = 'ATIVO' "
			+ "AND (ap.dataCancelamento IS NULL OR ap.dataCancelamento > CURRENT_DATE)")
	List<AssociadoPlano> findPlanosAtivosPorAssociado(@Param("associadoId") Long associadoId);

	@Query("SELECT COUNT(ap) > 0 FROM AssociadoPlano ap " + "WHERE ap.associado.id = :associadoId "
			+ "AND ap.plano.id = :planoId " + "AND ap.status = 'ATIVO'")
	boolean associadoPossuiPlanoAtivo(@Param("associadoId") Long associadoId, @Param("planoId") Long planoId);
	
}