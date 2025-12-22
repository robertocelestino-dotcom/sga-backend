// src/main/java/com/sga/repository/VendedorRepository.java
package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.Vendedor;

@Repository
public interface VendedorRepository extends JpaRepository<Vendedor, Long> {
	
	Optional<Vendedor> findById(Integer codigo);

	List<Vendedor> findByStatus(String status);

	List<Vendedor> findAllByOrderByNomeRazaoAsc();
	
	// MÉTODOS DE PAGINAÇÃO ADICIONADOS
    Page<Vendedor> findByNomeRazaoContainingIgnoreCase(String nomeRazao, Pageable pageable);
    
    Page<Vendedor> findByNomeRazaoContainingIgnoreCaseAndStatus(String nomeRazao, String status, Pageable pageable);
    
    Page<Vendedor> findByStatus(String status, Pageable pageable);
	
}