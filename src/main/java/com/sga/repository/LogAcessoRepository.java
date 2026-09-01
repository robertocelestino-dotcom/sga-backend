package com.sga.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.LogAcesso;

@Repository
public interface LogAcessoRepository extends JpaRepository<LogAcesso, Long> {
	Page<LogAcesso> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId, Pageable pageable);

	@Query("SELECT l FROM LogAcesso l WHERE l.dataHora BETWEEN :inicio AND :fim ORDER BY l.dataHora DESC")
	Page<LogAcesso> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim,
			Pageable pageable);

	@Query("SELECT l FROM LogAcesso l WHERE " + "(:usuarioId IS NULL OR l.usuario.id = :usuarioId) AND "
			+ "(:inicio IS NULL OR l.dataHora >= :inicio) AND " + "(:fim IS NULL OR l.dataHora <= :fim) "
			+ "ORDER BY l.dataHora DESC")
	Page<LogAcesso> findByFiltros(@Param("usuarioId") Long usuarioId, @Param("inicio") LocalDateTime inicio,
			@Param("fim") LocalDateTime fim, Pageable pageable);

	@Query("SELECT COUNT(l) FROM LogAcesso l WHERE l.usuario.id = :usuarioId AND l.dataHora >= :data")
	Long countAcessosDesde(@Param("usuarioId") Long usuarioId, @Param("data") LocalDateTime data);

	@Query("SELECT COUNT(l) FROM LogAcesso l WHERE l.acao = 'LOGIN_FAILED' AND l.dataHora >= :data")
	Long countLoginFailed(@Param("data") LocalDateTime data);
}