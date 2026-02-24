package com.sga.repository;

import com.sga.model.AssociadoProduto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssociadoProdutoRepository extends JpaRepository<AssociadoProduto, Long> {

    List<AssociadoProduto> findByAssociadoId(Long associadoId);

    Page<AssociadoProduto> findByAssociadoId(Long associadoId, Pageable pageable);

    Optional<AssociadoProduto> findByAssociadoIdAndProdutoId(Long associadoId, Long produtoId);

    @Query("SELECT ap FROM AssociadoProduto ap " +
           "JOIN FETCH ap.associado a " +
           "JOIN FETCH ap.produto p " +
           "LEFT JOIN FETCH ap.tipoEnvio te " +
           "WHERE ap.associado.id = :associadoId AND ap.statusNoProcesso = 'A'")
    List<AssociadoProduto> findAtivosPorAssociado(@Param("associadoId") Long associadoId);

    @Query("SELECT COUNT(ap) > 0 FROM AssociadoProduto ap " +
           "WHERE ap.associado.id = :associadoId AND ap.produto.id = :produtoId")
    boolean existsByAssociadoAndProduto(@Param("associadoId") Long associadoId,
                                        @Param("produtoId") Long produtoId);

    @Modifying
    @Query("DELETE FROM AssociadoProduto ap WHERE ap.associado.id = :associadoId")
    void deleteByAssociadoId(@Param("associadoId") Long associadoId);

    @Modifying
    @Query("UPDATE AssociadoProduto ap SET ap.statusNoProcesso = 'I' WHERE ap.associado.id = :associadoId")
    void inativarPorAssociado(@Param("associadoId") Long associadoId);
}