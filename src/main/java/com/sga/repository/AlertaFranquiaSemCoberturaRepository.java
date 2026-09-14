package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sga.model.AlertaFranquiaSemCobertura;

@Repository
public interface AlertaFranquiaSemCoberturaRepository extends JpaRepository<AlertaFranquiaSemCobertura, Long> {

	List<AlertaFranquiaSemCobertura> findByStatusOrderByCriadoEmDesc(String status);

	@Query("SELECT a FROM AlertaFranquiaSemCobertura a " + "WHERE a.status = 'PENDENTE' "
			+ "ORDER BY a.valorPotencialAfetado DESC NULLS LAST, a.criadoEm DESC")
	List<AlertaFranquiaSemCobertura> findPendentesOrdenadosPorValor();

	long countByStatus(String status);
}