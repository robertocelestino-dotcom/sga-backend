package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.Telefone;

@Repository
public interface TelefoneRepository extends JpaRepository<Telefone, Long> {
	
	List<Telefone> findByAssociadoId(Long associadoId);

	void deleteByAssociadoId(Long associadoId);

	void deleteByIdAndAssociadoId(Long id, Long associadoId);
}