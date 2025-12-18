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

	// conta itens corretamente por importação
	long countByImportacao_Id(Long importacaoId);

	// contagem direta (melhor performance)
	long countByNotaDebito_Importacao_Id(Long importacaoId);

	// soma valor total dos itens vinculados à importação
	@Query("SELECT COALESCE(SUM(i.valorTotal), 0) " + "FROM ItemSPC i WHERE i.importacao.id = :importacaoId")
	BigDecimal sumValorTotalByImportacaoId(@Param("importacaoId") Long importacaoId);

	@Query("SELECT DISTINCT i.descricaoServico " + "FROM ItemSPC i " + "WHERE i.importacao.id = :importacaoId "
			+ "AND i.descricaoServico IS NOT NULL " + "AND TRIM(i.descricaoServico) <> '' "
			+ "ORDER BY i.descricaoServico")
	List<String> findDistinctProdutos(Long importacaoId);

	@Query("SELECT " + "COALESCE(SUM(CASE WHEN i.creditoDebito = 'D' THEN i.valorTotal ELSE 0 END), 0) "
			+ "- COALESCE(SUM(CASE WHEN i.creditoDebito = 'C' THEN i.valorTotal ELSE 0 END), 0) " + "FROM ItemSPC i "
			+ "WHERE i.notaDebito.importacao.id = :importacaoId")
	BigDecimal calcularValorCobrado(Long importacaoId);

	// soma de débito/credito por nota (opcional)
	@Query("select coalesce(sum(case when i.creditoDebito = 'D' then i.valorTotal else 0 end),0) from ItemSPC i where i.notaDebito.id = :notaId")
	BigDecimal sumDebitosByNotaId(Long notaId);

	@Query("select coalesce(sum(case when i.creditoDebito = 'C' then i.valorTotal else 0 end),0) from ItemSPC i where i.notaDebito.id = :notaId")
	BigDecimal sumCreditosByNotaId(Long notaId);

}