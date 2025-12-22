// src/main/java/com/sga/repository/PlanosRepository.java
package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sga.model.Planos;

@Repository
public interface PlanosRepository extends JpaRepository<Planos, Long> {

	Optional<Planos> findByCodigo(String codigo);

	List<Planos> findByStatus(String status);

	List<Planos> findAllByOrderByPlanoAsc();

	List<Planos> findByStatusOrderByNomeAsc(String status);

	@Query("SELECT p FROM Plano p WHERE p.status = 'A' ORDER BY p.nome")
	List<Planos> findAllAtivos();

	Page<Planos> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

	Page<Planos> findByNomeContainingIgnoreCaseAndStatus(String nome, String status, Pageable pageable);

	@Query("SELECT COUNT(a) FROM Associado a WHERE a.plano.id = :planoId")
	Long countAssociadosByPlanoId(Long planoId);

}