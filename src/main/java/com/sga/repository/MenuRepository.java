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

	// ============================================================
	// 🔥 NOVOS MÉTODOS OTIMIZADOS
	//
	// Mesma query do findMenusByPerfilId, mas com LEFT JOIN FETCH m.menuPai
	// para trazer o pai em 1 query e evitar lazy fora de transação.
	// Isso permite que o AuthController monte a árvore em memória
	// sem N+1 e sem LazyInitializationException.
	// ============================================================

	/**
	 * 🔥 Retorna TODOS os menus de um perfil (lista plana) com o menuPai já
	 * carregado — pronto para o AuthController montar a árvore.
	 *
	 * DISTINCT evita duplicatas quando o JOIN FETCH for aplicado.
	 */
	@Query("SELECT DISTINCT m FROM Menu m " + "LEFT JOIN FETCH m.menuPai "
			+ "WHERE m.id IN (SELECT pm.menu.id FROM PerfilMenu pm WHERE pm.perfil.id = :perfilId) "
			+ "AND m.ativo = true " + "ORDER BY m.ordem ASC")
	List<Menu> findMenusByPerfilIdComPai(@Param("perfilId") Long perfilId);

}