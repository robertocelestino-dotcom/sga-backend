package com.sga.repository;

import com.sga.model.ConfiguracaoRmParametro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ConfiguracaoRmParametroRepository extends JpaRepository<ConfiguracaoRmParametro, Long> {
    
    List<ConfiguracaoRmParametro> findByConfiguracaoRmIdOrderByOrdemAsc(Long configuracaoId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ConfiguracaoRmParametro p WHERE p.configuracaoRm.id = :configuracaoId")
    void deleteByConfiguracaoRmId(@Param("configuracaoId") Long configuracaoId);
}