package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.ImportacaoSPC;

@Repository
public interface ImportacaoSPCRepository extends JpaRepository<ImportacaoSPC, Long> {
	
	List<ImportacaoSPC> findAllByOrderByDataImportacaoDesc();
	
}