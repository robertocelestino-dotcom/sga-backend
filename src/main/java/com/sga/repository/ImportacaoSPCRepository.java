package com.sga.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.ImportacaoSPC;

@Repository
public interface ImportacaoSPCRepository extends JpaRepository<ImportacaoSPC, Long> {
	
	/**
	 * ⚠️ ATENÇÃO: Este método retorna ImportacaoSPC SEM carregar as relações LAZY.
	 * Só use se a sessão estiver aberta (dentro de @Transactional).
	 * 
	 * Se precisar acessar .getNotasDebito() fora de transação, use o método com @EntityGraph abaixo.
	 */
	List<ImportacaoSPC> findAllByOrderByDataImportacaoDesc();
	
	/**
	 * 🔥 OTIMIZADO: Busca todas as importações COM as notas já carregadas (JOIN FETCH).
	 * Evita LazyInitializationException quando open-in-view=false.
	 * 
	 * Carrega apenas "notasDebito" — para não gerar produto cartesiano,
	 * não carregamos "notasDebito.itens" nem outras coleções no mesmo JOIN.
	 */
	@EntityGraph(attributePaths = {"notasDebito"})
	@Query("SELECT DISTINCT i FROM ImportacaoSPC i ORDER BY i.dataImportacao DESC")
	List<ImportacaoSPC> findAllWithNotasOrderByDataImportacaoDesc();
	
	/**
	 * 🔥 OTIMIZADO: Busca uma importação por ID COM todas as relações carregadas.
	 * Usado nos endpoints que precisam acessar notas, itens, headers, parametros e traillers.
	 * 
	 * CUIDADO: Usa múltiplos @EntityGraph — pode gerar produto cartesiano se houverem muitas notas.
	 * Para o cenário atual (centenas de notas) está OK.
	 */
	@EntityGraph(attributePaths = {
		"notasDebito",
		"headers",
		"parametros",
		"traillers"
	})
	@Query("SELECT DISTINCT i FROM ImportacaoSPC i WHERE i.id = :id")
	Optional<ImportacaoSPC> findByIdWithRelations(@Param("id") Long id);
	
	/**
	 * 🔥 OTIMIZADO: Busca uma importação por ID COM notas e itens carregados.
	 * Usado quando o service precisa calcular totais por item.
	 */
	@EntityGraph(attributePaths = {"notasDebito", "notasDebito.itens"})
	@Query("SELECT DISTINCT i FROM ImportacaoSPC i WHERE i.id = :id")
	Optional<ImportacaoSPC> findByIdWithNotasAndItens(@Param("id") Long id);
	
	@Query("SELECT COUNT(i) FROM ImportacaoSPC i WHERE i.dataImportacao >= :dataLimite")
	long countByDataImportacaoAfter(@Param("dataLimite") LocalDateTime dataLimite);
	
}