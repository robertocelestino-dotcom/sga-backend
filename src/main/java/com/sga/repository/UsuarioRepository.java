package com.sga.repository;

import com.sga.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // ============================================================
    // MÉTODOS EXISTENTES (MANTIDOS)
    // ============================================================

    Optional<Usuario> findByUsername(String username);

    Boolean existsByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<Usuario> findByAtivoTrue();

    List<Usuario> findByPerfilId(Long perfilId);

    /**
     * 🔥 Carrega o usuário + perfil + permissões em uma única query.
     *
     * ESSENCIAL para o login e para o JwtAuthenticationFilter, pois:
     *   1) O getAuthorities() do Usuario acessa perfil.getPermissoes()
     *      e, se o perfil fosse LAZY fora de transação, quebraria.
     *   2) Garante que permissões venham em 1 SELECT em vez de N+1.
     *
     * O foto_base64 NÃO é afetado — ele continua sendo carregado
     * por ser coluna simples (não é relação). Se quiser evitar,
     * crie uma projeção Resumo. Por ora, mantemos para compatibilidade.
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.perfil p " +
           "LEFT JOIN FETCH p.permissoes " +
           "WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithPerfil(@Param("username") String username);

    @Query("SELECT DISTINCT u FROM Usuario u " +
           "LEFT JOIN FETCH u.perfil p " +
           "LEFT JOIN FETCH p.permissoes " +
           "WHERE u.id = :id")
    Optional<Usuario> findByIdWithPerfil(@Param("id") Long id);

    List<Usuario> findByRole(String role);

    // ============================================================
    // 🔥 NOVO — Query de lookup por username SEM a foto (opcional)
    //
    // Use quando você precisar apenas validar existência/bloqueio
    // e NÃO for usar a foto. Evita trafegar 1-2MB do banco.
    // ============================================================

    @Query("SELECT u.id FROM Usuario u WHERE u.username = :username")
    Optional<Long> findIdByUsername(@Param("username") String username);
}