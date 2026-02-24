package com.sga.repository;

import com.sga.model.TipoEnvio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoEnvioRepository extends JpaRepository<TipoEnvio, Long> {

    Optional<TipoEnvio> findByCodigo(Integer codigo);

    List<TipoEnvio> findByAtivoTrueOrderByCodigo();

    @Query("SELECT t FROM TipoEnvio t WHERE " +
           "(:descricao IS NULL OR LOWER(t.descricao) LIKE LOWER(CONCAT('%', :descricao, '%'))) AND " +
           "(:ativo IS NULL OR t.ativo = :ativo)")
    Page<TipoEnvio> buscarComFiltros(@Param("descricao") String descricao,
                                     @Param("ativo") Boolean ativo,
                                     Pageable pageable);

    boolean existsByCodigo(Integer codigo);
}