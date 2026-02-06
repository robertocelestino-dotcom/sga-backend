package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.Email;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
	
	List<Email> findByAssociadoId(Long associadoId);

	void deleteByAssociadoId(Long associadoId);

	void deleteByIdAndAssociadoId(Long id, Long associadoId);
}