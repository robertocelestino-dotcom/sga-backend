// src/main/java/com/sga/repository/TipoArquivoReguaRepository.java

package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.TipoArquivoRegua;

@Repository
public interface TipoArquivoReguaRepository extends JpaRepository<TipoArquivoRegua, Long> {

	List<TipoArquivoRegua> findByReguaId(Long reguaId);

	List<TipoArquivoRegua> findByReguaIdOrderByOrdemAsc(Long reguaId);

	@Modifying
	@Transactional
	@Query("DELETE FROM TipoArquivoRegua t WHERE t.regua.id = :reguaId")
	void deleteByReguaId(@Param("reguaId") Long reguaId);
	
}