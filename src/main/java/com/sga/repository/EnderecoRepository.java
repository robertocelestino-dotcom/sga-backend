package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sga.model.Endereco;

@Repository
public interface EnderecoRepository extends JpaRepository<Endereco, Long> {

	List<Endereco> findByAssociadoId(Long associadoId);

	void deleteByAssociadoId(Long associadoId);

	void deleteByIdAndAssociadoId(Long id, Long associadoId);

}
