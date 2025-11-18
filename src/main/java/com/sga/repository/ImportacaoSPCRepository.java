package com.sga.repository;

import com.sga.model.ImportacaoSPC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportacaoSPCRepository extends JpaRepository<ImportacaoSPC, Long> {
	
	
}