package com.sga.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

	// Buscas básicas
    Optional<Produto> findByCodigo(String codigo);
    Optional<Produto> findByCodigoRm(String codigoRm);
    
    // Listagens com filtros
    List<Produto> findByStatus(String status);
    List<Produto> findByTipoProduto(String tipoProduto);
    List<Produto> findByCategoria(String categoria);
    List<Produto> findByModalidade(String modalidade);
    
    // Produtos ativos
    List<Produto> findByStatusAndTipoProduto(String status, String tipoProduto);
    
    // Produtos com valor acima de X
    List<Produto> findByValorUnitarioGreaterThan(BigDecimal valor);
    
    // Busca por nome ou descrição
    List<Produto> findByNomeContainingIgnoreCaseOrDescricaoContainingIgnoreCase(String nome, String descricao);
    
    // ========== QUERIES PARA FRANQUIAS ==========
    
    // Produtos que têm franquias
    @Query("SELECT p FROM Produto p WHERE p.temFranquia = true")
    List<Produto> findProdutosComFranquia();
    
    // Franquias disponíveis (produtos que podem ser usados como franquia)
    @Query("SELECT p FROM Produto p WHERE p.tipoProduto = 'FRANQUIA' OR p.temFranquia = true")
    List<Produto> findFranquiasDisponiveis();
    
    // Produtos que usam uma franquia específica
    @Query("SELECT p FROM Produto p JOIN p.franquias f WHERE f.id = :franquiaId")
    List<Produto> findProdutosQueUsamFranquia(@Param("franquiaId") Long franquiaId);
    
    // Franquias de um produto específico
    @Query("SELECT f FROM Produto p JOIN p.franquias f WHERE p.id = :produtoId")
    List<Produto> findFranquiasByProdutoId(@Param("produtoId") Long produtoId);
    
    // Conta quantas franquias um produto tem
    @Query("SELECT COUNT(f) FROM Produto p JOIN p.franquias f WHERE p.id = :produtoId")
    Integer countFranquiasByProdutoId(@Param("produtoId") Long produtoId);
    
    // ========== QUERIES PARA PRODUTOS SPC/MIX ==========
    
    // Produtos da categoria SPC
    @Query("SELECT p FROM Produto p WHERE p.categoria = 'SPC' OR LOWER(p.nome) LIKE '%spc%'")
    List<Produto> findProdutosSPC();
    
    // Produtos do tipo MIX
    @Query("SELECT p FROM Produto p WHERE p.isMix() = true OR LOWER(p.nome) LIKE '%mix%'")
    List<Produto> findProdutosMix();
    
    // Produtos SPC com modalidade específica
    @Query("SELECT p FROM Produto p WHERE p.categoria = 'SPC' AND p.modalidade = :modalidade")
    List<Produto> findProdutosSPCByModalidade(@Param("modalidade") String modalidade);
    
    // Produtos relacionados a um produto MIX
    @Query("SELECT pr FROM Produto p JOIN p.produtosRelacionados pr WHERE p.id = :produtoId")
    List<Produto> findProdutosRelacionados(@Param("produtoId") Long produtoId);
    
    // Produtos que são combinações (ex: SPC + CHEQUE)
    @Query("SELECT p FROM Produto p WHERE p.categoria LIKE '%+%' OR p.nome LIKE '%+%'")
    List<Produto> findProdutosCombinados();
    
    // ========== QUERIES COM PAGINAÇÃO ==========
    
    // Filtro completo com paginação
    @Query("SELECT p FROM Produto p WHERE " +
           "(:codigo IS NULL OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :codigo, '%'))) AND " +
           "(:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:tipoProduto IS NULL OR p.tipoProduto = :tipoProduto) AND " +
           "(:categoria IS NULL OR p.categoria = :categoria) AND " +
           "(:modalidade IS NULL OR p.modalidade = :modalidade) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:temFranquia IS NULL OR p.temFranquia = :temFranquia)")
    Page<Produto> filtrarProdutos(
            @Param("codigo") String codigo,
            @Param("nome") String nome,
            @Param("tipoProduto") String tipoProduto,
            @Param("categoria") String categoria,
            @Param("modalidade") String modalidade,
            @Param("status") String status,
            @Param("temFranquia") Boolean temFranquia,
            Pageable pageable
    );
    
    // Busca produtos para faturamento (ativos, com cobrança automática)
    @Query("SELECT p FROM Produto p WHERE p.status = 'ATIVO' AND p.geraCobrancaAutomatica = true")
    List<Produto> findProdutosParaFaturamento();
    
    // Produtos que exigem autorização por nível
    @Query("SELECT p FROM Produto p WHERE p.exigeAutorizacao = true AND p.nivelAutorizacao <= :nivelUsuario")
    List<Produto> findProdutosAutorizaveisPorNivel(@Param("nivelUsuario") Integer nivelUsuario);
    
    // Valor total estimado de produtos ativos
    @Query("SELECT COALESCE(SUM(p.valorUnitario), 0) FROM Produto p WHERE p.status = 'ATIVO'")
    BigDecimal calcularValorTotalAtivos();
    
    @Query("SELECT p.codigo FROM Produto p WHERE p.status = 'ATIVO'")
    List<String> findAllCodigosAtivos();

    // Ou se quiser também os IDs:
    @Query("SELECT p.id, p.codigo FROM Produto p WHERE p.status = 'ATIVO'")
    List<Object[]> findAllAtivosComCodigo();


}