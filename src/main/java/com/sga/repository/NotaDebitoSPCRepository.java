package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.NotaDebitoSPC;

@Repository
public interface NotaDebitoSPCRepository extends JpaRepository<NotaDebitoSPC, Long> {
	
	 List<NotaDebitoSPC> findByCodigoSocio(String codigoSocio);
}