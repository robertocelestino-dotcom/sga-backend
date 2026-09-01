package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Perfil;

@Repository
public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    Optional<Perfil> findByNome(String nome);
    List<Perfil> findByAtivoTrue();
    boolean existsByNome(String nome);
    
    @Query("SELECT p FROM Perfil p WHERE p.id IN (SELECT pm.perfil.id FROM PerfilMenu pm WHERE pm.menu.id = :menuId)")
    List<Perfil> findPerfisByMenuId(@Param("menuId") Long menuId);
    
    @Query("SELECT p FROM Perfil p WHERE p.id IN (SELECT pp.perfil.id FROM PerfilPermissao pp WHERE pp.permissao.id = :permissaoId)")
    List<Perfil> findPerfisByPermissaoId(@Param("permissaoId") Long permissaoId);
}
