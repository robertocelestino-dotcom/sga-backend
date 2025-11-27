package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sga.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	@Query("SELECT COUNT(p) FROM Produto p WHERE p.status = 'A'")
	Long countProdutosAtivos();

	@Query("SELECT p.codigo FROM Produto p WHERE p.status = 'A'")
	List<String> findAllCodigosAtivos();


}