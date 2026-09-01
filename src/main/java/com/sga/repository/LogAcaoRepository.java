package com.sga.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.LogAcao;

@Repository
public interface LogAcaoRepository extends JpaRepository<LogAcao, Long> {
	Page<LogAcao> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId, Pageable pageable);

	Page<LogAcao> findByRecursoOrderByDataHoraDesc(String recurso, Pageable pageable);

	@Query("SELECT l FROM LogAcao l WHERE " + "(:usuarioId IS NULL OR l.usuario.id = :usuarioId) AND "
			+ "(:recurso IS NULL OR l.recurso = :recurso) AND " + "(:inicio IS NULL OR l.dataHora >= :inicio) AND "
			+ "(:fim IS NULL OR l.dataHora <= :fim) " + "ORDER BY l.dataHora DESC")
	Page<LogAcao> findByFiltros(@Param("usuarioId") Long usuarioId, @Param("recurso") String recurso,
			@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim, Pageable pageable);

	@Query("SELECT l FROM LogAcao l WHERE l.usuario.id = :usuarioId AND l.recurso = :recurso ORDER BY l.dataHora DESC")
	Page<LogAcao> findByUsuarioAndRecurso(@Param("usuarioId") Long usuarioId, @Param("recurso") String recurso,
			Pageable pageable);

	@Query("SELECT COUNT(l) FROM LogAcao l WHERE l.usuario.id = :usuarioId AND l.dataHora >= :data AND l.status = 'SUCCESS'")
	Long countAcoesSucessoDesde(@Param("usuarioId") Long usuarioId, @Param("data") LocalDateTime data);
}