package com.sga.repository;

import com.sga.model.PlanoProdutoFranquia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanoProdutoFranquiaRepository extends JpaRepository<PlanoProdutoFranquia, Long> {
    
    // ✅ CORRIGIDO: findByPlanoId (plano.id)
    List<PlanoProdutoFranquia> findByPlanoIdAndAtivoTrue(Long planoId);
    
    // ✅ CORRIGIDO: findByPlanoIdAndProdutoId
    Optional<PlanoProdutoFranquia> findByPlanoIdAndProdutoIdAndAtivoTrue(Long planoId, Long produtoId);
    
    // ✅ CORRIGIDO: Query com caminho correto (produto.id)
    @Query("SELECT ppf FROM PlanoProdutoFranquia ppf " +
           "WHERE ppf.produto.id = :produtoId AND ppf.ativo = true")
    List<PlanoProdutoFranquia> findByProdutoId(@Param("produtoId") Long produtoId);
    
    // ✅ CORRIGIDO: Query com caminho correto (franquia.id)
    @Query("SELECT ppf FROM PlanoProdutoFranquia ppf " +
           "WHERE ppf.franquia.id = :franquiaId AND ppf.ativo = true")
    List<PlanoProdutoFranquia> findByFranquiaId(@Param("franquiaId") Long franquiaId);
    
    // ✅ CORRIGIDO: existsByPlanoIdAndProdutoId
    boolean existsByPlanoIdAndProdutoIdAndAtivoTrue(Long planoId, Long produtoId);
    
    // ✅ CORRIGIDO: Query complexa com caminhos corretos
    @Query("SELECT ppf FROM PlanoProdutoFranquia ppf " +
           "WHERE ppf.plano.id IN (SELECT ap.plano.id FROM AssociadoPlano ap " +
           "WHERE ap.associado.id = :associadoId AND ap.status = 'ATIVO') " +
           "AND ppf.ativo = true")
    List<PlanoProdutoFranquia> findFranquiasPorAssociado(@Param("associadoId") Long associadoId);
    
    @Query("SELECT ppf FROM PlanoProdutoFranquia ppf " +
    	       "JOIN FETCH ppf.plano p " +
    	       "JOIN FETCH ppf.produto pr " +
    	       "JOIN FETCH ppf.franquia f " +
    	       "WHERE ppf.produto.id = :produtoId AND ppf.ativo = true")
    	List<PlanoProdutoFranquia> findByProdutoIdWithFetch(@Param("produtoId") Long produtoId);
    
    // Versão corrigida - usa LEFT JOIN e trata planos nulos
    @Query("SELECT ppf FROM PlanoProdutoFranquia ppf " +
           "LEFT JOIN FETCH ppf.plano p " +
           "LEFT JOIN FETCH ppf.franquia f " +
           "WHERE ppf.produto.id = :produtoId " +
           "AND (p.id IS NOT NULL OR ppf.plano IS NULL)")
    List<PlanoProdutoFranquia> findByProdutoIdWithJoins(@Param("produtoId") Long produtoId);    
    
    
    
    
}