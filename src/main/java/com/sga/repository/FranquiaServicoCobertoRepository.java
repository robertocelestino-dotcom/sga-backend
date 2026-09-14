package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.FranquiaServicoCoberto;

@Repository
public interface FranquiaServicoCobertoRepository extends JpaRepository<FranquiaServicoCoberto, Long> {

	@Query("SELECT fsc.servicoNomeBase " + "FROM FranquiaServicoCoberto fsc "
			+ "WHERE UPPER(fsc.franquiaNomeBase) = UPPER(:franquiaNomeBase) " + "  AND fsc.ativo = true "
			+ "ORDER BY fsc.prioridade DESC, fsc.servicoNomeBase")
	List<String> findServicosByFranquia(@Param("franquiaNomeBase") String franquiaNomeBase);

	@Query("SELECT COUNT(fsc) > 0 " + "FROM FranquiaServicoCoberto fsc "
			+ "WHERE UPPER(fsc.franquiaNomeBase) = UPPER(:franquiaNomeBase) "
			+ "  AND UPPER(fsc.servicoNomeBase) = UPPER(:servicoNomeBase) " + "  AND fsc.ativo = true")
	boolean existsCobertura(@Param("franquiaNomeBase") String franquiaNomeBase,
			@Param("servicoNomeBase") String servicoNomeBase);

	@Query("SELECT DISTINCT fsc.franquiaNomeBase " + "FROM FranquiaServicoCoberto fsc " + "WHERE fsc.ativo = true "
			+ "ORDER BY fsc.franquiaNomeBase")
	List<String> findAllFranquiasAtivas();
}