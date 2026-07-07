package com.sga.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.ParametrosSPC;

@Repository
public interface ParametrosSPCRepository extends JpaRepository<ParametrosSPC, Long> {

	// Buscar parâmetros por importação
	List<ParametrosSPC> findByImportacaoId(Long importacaoId);

	// Buscar parâmetros por tipo de registro
	List<ParametrosSPC> findByTipoRegistro(String tipoRegistro);

	// Buscar parâmetros com período de referência válido
	@Query("SELECT p FROM ParametrosSPC p WHERE p.dataInicioPeriodoRef IS NOT NULL AND p.dataFimPeriodoRef IS NOT NULL")
	List<ParametrosSPC> findComPeriodoValido();

	// 🔥 CORRIGIDO: Buscar parâmetros por código do associado via importacao
	// Usando uma subquery ou join com a tabela de importacao
	// 🔥 CORRIGIDO: Buscar parâmetros pelo código do sócio (com padding para PostgreSQL)
	@Query(value = "SELECT p.* FROM tb_nota_parametros_spc p " +
		       "JOIN tb_importacao_spc i ON i.id = p.importacao_id " +
		       "JOIN tb_nota_debito_spc n ON n.importacao_id = i.id " +
		       "WHERE LPAD(n.codigo_socio, 8, '0') = LPAD(:codigoSocio, 8, '0') " +
		       "AND p.data_inicio_periodo_ref IS NOT NULL " +
		       "AND p.data_fim_periodo_ref IS NOT NULL " +
		       "ORDER BY p.id DESC", 
		       nativeQuery = true)
	List<ParametrosSPC> findByCodigoSocio(@Param("codigoSocio") String codigoSocio);

	// 🔥 CORRIGIDO: Buscar o parâmetro mais recente por código do associado
	// 🔥 Buscar o parâmetro mais recente pelo código do sócio (com padding)
	@Query(value = "SELECT p.* FROM tb_nota_parametros_spc p " +
		       "JOIN tb_importacao_spc i ON i.id = p.importacao_id " +
		       "JOIN tb_nota_debito_spc n ON n.importacao_id = i.id " +
		       "WHERE LPAD(n.codigo_socio, 8, '0') = LPAD(:codigoSocio, 8, '0') " +
		       "AND p.data_inicio_periodo_ref IS NOT NULL " +
		       "AND p.data_fim_periodo_ref IS NOT NULL " +
		       "ORDER BY p.id DESC LIMIT 1", 
		       nativeQuery = true)
	Optional<ParametrosSPC> findUltimoByCodigoSocio(@Param("codigoSocio") String codigoSocio);
	
	
	// 🔥 Buscar período de referência (data_inicio e data_fim) pelo código do sócio
	@Query(value = "SELECT p.data_inicio_periodo_ref, p.data_fim_periodo_ref FROM tb_nota_parametros_spc p " +
		       "JOIN tb_importacao_spc i ON i.id = p.importacao_id " +
		       "JOIN tb_nota_debito_spc n ON n.importacao_id = i.id " +
		       "WHERE LPAD(n.codigo_socio, 8, '0') = LPAD(:codigoSocio, 8, '0') " +
		       "AND p.data_inicio_periodo_ref IS NOT NULL " +
		       "AND p.data_fim_periodo_ref IS NOT NULL " +
		       "ORDER BY p.id DESC LIMIT 1", 
		       nativeQuery = true)
	Optional<Object[]> findPeriodoRefByCodigoSocio(@Param("codigoSocio") String codigoSocio);
	
}