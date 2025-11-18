package com.sga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.HeaderSPC;

@Repository
public interface HeaderSPCRepository extends JpaRepository<HeaderSPC, Long> {
}