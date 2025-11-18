package com.sga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.ParametrosSPC;

@Repository
public interface ParametrosSPCRepository extends JpaRepository<ParametrosSPC, Long> {
}