package com.sga.repository;

import com.sga.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    
    List<Menu> findByAtivoTrueOrderByOrdemAsc();
    
    List<Menu> findByMenuPaiIsNullAndAtivoTrueOrderByOrdemAsc();
    
    List<Menu> findByMenuPaiIdAndAtivoTrueOrderByOrdemAsc(Long menuPaiId);

    @Query("SELECT m FROM Menu m WHERE m.id IN (SELECT pm.menu.id FROM PerfilMenu pm WHERE pm.perfil.id = :perfilId) AND m.ativo = true ORDER BY m.ordem ASC")
    List<Menu> findMenusByPerfilId(@Param("perfilId") Long perfilId);
    
    @Query("SELECT m FROM Menu m WHERE m.id IN (SELECT pm.menu.id FROM PerfilMenu pm WHERE pm.perfil.id = :perfilId) AND m.menuPai IS NULL ORDER BY m.ordem ASC")
    List<Menu> findMenusRaizByPerfilId(@Param("perfilId") Long perfilId);
}