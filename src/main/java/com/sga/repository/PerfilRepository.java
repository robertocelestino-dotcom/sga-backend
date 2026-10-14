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

    // ============================================================
    // MÉTODOS EXISTENTES (MANTIDOS)
    // ============================================================

    Optional<Perfil> findByNome(String nome);

    List<Perfil> findByAtivoTrue();

    boolean existsByNome(String nome);

    @Query("SELECT p FROM Perfil p WHERE p.id IN (SELECT pm.perfil.id FROM PerfilMenu pm WHERE pm.menu.id = :menuId)")
    List<Perfil> findPerfisByMenuId(@Param("menuId") Long menuId);

    @Query("SELECT p FROM Perfil p WHERE p.id IN (SELECT pp.perfil.id FROM PerfilPermissao pp WHERE pp.permissao.id = :permissaoId)")
    List<Perfil> findPerfisByPermissaoId(@Param("permissaoId") Long permissaoId);

    // ============================================================
    // 🔥 QUERIES OTIMIZADAS
    // ============================================================

    /**
     * 🔥 Carrega APENAS permissões do perfil em 1 query.
     *
     * Por que separado de menus?
     *   - JOIN FETCH em DUAS coleções (menus + permissões) gera produto
     *     cartesiano (28 menus × 52 permissões = ~1456 linhas).
     *   - O Hibernate deduplica, MAS em versões antigas pode perder itens
     *     em coleções Set<>, especialmente quando combinado com LAZY.
     *   - Fazer 2 queries é mais eficiente e 100% confiável.
     *
     * Os menus são carregados via MenuRepository.findMenusByPerfilIdComPai().
     */
    @Query("SELECT DISTINCT p FROM Perfil p " +
           "LEFT JOIN FETCH p.permissoes " +
           "WHERE p.id = :id")
    Optional<Perfil> findByIdComPermissoes(@Param("id") Long id);

    /**
     * 🔥 Carrega APENAS menus do perfil em 1 query, com menuPai carregado.
     *
     * Como o Perfil.menus é LAZY, esta query é a forma segura de trazer
     * os menus já prontos para o AuthController montar a árvore.
     *
     * ⚠️ Os SUBMENUS (Menu.subMenus) NÃO são carregados aqui — eles são
     *    construídos em memória pelo AuthController.construirArvoreMenus().
     */
    @Query("SELECT DISTINCT p FROM Perfil p " +
           "LEFT JOIN FETCH p.menus m " +
           "LEFT JOIN FETCH m.menuPai " +
           "WHERE p.id = :id")
    Optional<Perfil> findByIdComMenus(@Param("id") Long id);

    /**
     * 🔥 Carrega menus + permissões em 1 query.
     *
     * ⚠️ CUIDADO: gera produto cartesiano. Use apenas se realmente
     *    precisar de ambos ao mesmo tempo e o volume for pequeno.
     *
     * Preferível usar findByIdComPermissoes + MenuRepository.findMenusByPerfilIdComPai.
     */
    @Query("SELECT DISTINCT p FROM Perfil p " +
           "LEFT JOIN FETCH p.menus m " +
           "LEFT JOIN FETCH m.menuPai " +
           "LEFT JOIN FETCH p.permissoes " +
           "WHERE p.id = :id")
    Optional<Perfil> findByIdComMenusEPermissoes(@Param("id") Long id);
}