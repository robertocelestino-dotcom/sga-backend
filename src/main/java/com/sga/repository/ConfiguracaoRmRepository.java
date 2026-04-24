package com.sga.repository;

import com.sga.model.ConfiguracaoRm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfiguracaoRmRepository extends JpaRepository<ConfiguracaoRm, Long> {

    Optional<ConfiguracaoRm> findByTipoMovimentoAndAtivoTrue(String tipoMovimento);

    List<ConfiguracaoRm> findByAtivoTrue();

    @Query("SELECT c FROM ConfiguracaoRm c WHERE c.codigoTmv = :codigoTmv")
    Optional<ConfiguracaoRm> findByCodigoTmv(@Param("codigoTmv") String codigoTmv);
}