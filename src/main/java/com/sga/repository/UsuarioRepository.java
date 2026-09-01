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
    
    // ============================================================
    // NOVOS MÉTODOS
    // ============================================================
    
    Boolean existsByEmail(String email);
    
    List<Usuario> findByAtivoTrue();
    
    List<Usuario> findByPerfilId(Long perfilId);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.perfil WHERE u.username = :username")
    Optional<Usuario> findByUsernameWithPerfil(@Param("username") String username);
    
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.perfil WHERE u.id = :id")
    Optional<Usuario> findByIdWithPerfil(@Param("id") Long id);
    
    List<Usuario> findByRole(String role);
}