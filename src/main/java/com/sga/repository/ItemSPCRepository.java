package com.sga.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.ItemSPC;

@Repository
public interface ItemSPCRepository extends JpaRepository<ItemSPC, Long> {

	// Mantém o método já existente
	List<ItemSPC> findByImportacaoId(Long importacaoId);

	List<ItemSPC> findByNotaDebito_Importacao_Id(Long importacaoId);

	// Novo: conta itens corretamente por importação
	long countByImportacao_Id(Long importacaoId);

	// Novo: contagem direta (melhor performance)
	long countByNotaDebito_Importacao_Id(Long importacaoId);

	// Novo: soma valor total dos itens vinculados à importação
	@Query("SELECT COALESCE(SUM(i.valorTotal), 0) " + "FROM ItemSPC i WHERE i.importacao.id = :importacaoId")
	BigDecimal sumValorTotalByImportacaoId(@Param("importacaoId") Long importacaoId);

	@Query("SELECT DISTINCT i.descricaoServico " + "FROM ItemSPC i " + "WHERE i.importacao.id = :importacaoId "
			+ "AND i.descricaoServico IS NOT NULL " + "AND TRIM(i.descricaoServico) <> '' "
			+ "ORDER BY i.descricaoServico")
	List<String> findDistinctProdutos(Long importacaoId);

}