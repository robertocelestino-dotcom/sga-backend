package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Permissao;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    Optional<Permissao> findByNome(String nome);
    List<Permissao> findByRecurso(String recurso);

    @Query("SELECT p.nome FROM Permissao p WHERE p.id IN (SELECT pp.permissao.id FROM PerfilPermissao pp WHERE pp.perfil.id = :perfilId)")
    List<String> findPermissoesByPerfilId(@Param("perfilId") Long perfilId);
    
    @Query("SELECT p FROM Permissao p WHERE p.id IN (SELECT pp.permissao.id FROM PerfilPermissao pp WHERE pp.perfil.id = :perfilId)")
    List<Permissao> findPermissoesCompletasByPerfilId(@Param("perfilId") Long perfilId);
}