package com.sga.repository;

import com.sga.model.SessaoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessaoUsuarioRepository extends JpaRepository<SessaoUsuario, Long> {
    
    Optional<SessaoUsuario> findByTokenAndAtivoTrue(String token);
    
    List<SessaoUsuario> findByUsuarioIdAndAtivoTrue(Long usuarioId);
    
    @Modifying
    @Transactional
    @Query("UPDATE SessaoUsuario s SET s.ativo = false WHERE s.id = :id")
    void desativarSessao(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE SessaoUsuario s SET s.ativo = false WHERE s.usuario.id = :usuarioId")
    void desativarSessoesPorUsuario(@Param("usuarioId") Long usuarioId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SessaoUsuario s WHERE s.dataExpiracao < :data")
    void removerSessoesExpiradas(@Param("data") LocalDateTime data);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM SessaoUsuario s WHERE s.usuario.id = :usuarioId")
    void deletarSessoesPorUsuario(@Param("usuarioId") Long usuarioId);
}