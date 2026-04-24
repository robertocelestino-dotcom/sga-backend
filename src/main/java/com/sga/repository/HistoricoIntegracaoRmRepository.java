package com.sga.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.HistoricoIntegracaoRm;

@Repository
public interface HistoricoIntegracaoRmRepository extends JpaRepository<HistoricoIntegracaoRm, Long> {
    
    List<HistoricoIntegracaoRm> findByMesReferenciaAndAnoReferencia(Integer mes, Integer ano);
    
    Page<HistoricoIntegracaoRm> findByStatus(String status, Pageable pageable);
    
    List<HistoricoIntegracaoRm> findByDataIntegracaoBetween(LocalDateTime inicio, LocalDateTime fim);
}