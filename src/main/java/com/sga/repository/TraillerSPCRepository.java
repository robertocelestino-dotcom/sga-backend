package com.sga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.TraillerSPC;

@Repository
public interface TraillerSPCRepository extends JpaRepository<TraillerSPC, Long> {
}