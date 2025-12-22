// src/main/java/com/sga/repository/CategoriaRepository.java
package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sga.model.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	Optional<Categoria> findByCodigo(String codigo);

	List<Categoria> findByStatus(String status);

	List<Categoria> findByTipo(String tipo);

	List<Categoria> findAllByOrderByDescricaoAsc();

	// Métodos de paginação (adicione)
	Page<Categoria> findByDescricaoContainingIgnoreCase(String descricao, Pageable pageable);

	Page<Categoria> findByDescricaoContainingIgnoreCaseAndStatus(String descricao, String status, Pageable pageable);

	Page<Categoria> findByStatus(String status, Pageable pageable);

	Page<Categoria> findByTipo(String tipo, Pageable pageable);

	@Query("SELECT c FROM Categoria c WHERE c.status = 'ATIVO' ORDER BY c.descricao")
	List<Categoria> findAllAtivas();

}