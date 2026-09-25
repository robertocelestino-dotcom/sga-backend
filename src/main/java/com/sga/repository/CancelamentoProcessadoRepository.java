package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.CancelamentoProcessado;

@Repository
public interface CancelamentoProcessadoRepository extends JpaRepository<CancelamentoProcessado, Long> {
	
    List<CancelamentoProcessado> findByFaturaId(Long faturaId);
    
    List<CancelamentoProcessado> findByCancelamentoImportacaoId(Long cancelamentoImportacaoId);
    
    long countByFaturaId(Long faturaId);
}
