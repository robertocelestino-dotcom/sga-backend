package com.sga.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sga.model.ImportacaoLote;

@Repository
public interface ImportacaoLoteRepository extends JpaRepository<ImportacaoLote, Long> {

    /**
     * Busca o ID do último lote concluído com sucesso.
     * Usado pelo filtro "Origem" da tela de Associados.
     */
    @Query("SELECT MAX(i.id) FROM ImportacaoLote i WHERE i.status = 'CONCLUIDO'")
    Long findUltimoLoteId();
}