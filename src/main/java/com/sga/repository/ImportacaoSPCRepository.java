package com.sga.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.ImportacaoSPC;

@Repository
public interface ImportacaoSPCRepository extends JpaRepository<ImportacaoSPC, Long> {
	
	List<ImportacaoSPC> findAllByOrderByDataImportacaoDesc();
	
	@Query("SELECT COUNT(i) FROM ImportacaoSPC i WHERE i.dataImportacao >= :dataLimite")
	long countByDataImportacaoAfter(@Param("dataLimite") LocalDateTime dataLimite);
	
}