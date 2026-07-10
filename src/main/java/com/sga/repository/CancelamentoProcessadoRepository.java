package com.sga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.CancelamentoProcessado;

@Repository
public interface CancelamentoProcessadoRepository extends JpaRepository<CancelamentoProcessado, Long> {
}