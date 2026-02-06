package com.sga.repository;

import java.time.LocalDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.SistemaLog;

@Repository
public interface SistemaLogRepository extends JpaRepository<SistemaLog, Long> {

	// Buscar logs por tabela
	List<SistemaLog> findByTabelaAfetadaOrderByDataHoraDesc(String tabelaAfetada);

	// Buscar logs por ação
	List<SistemaLog> findByAcaoOrderByDataHoraDesc(String acao);

	// Buscar logs por usuário
	List<SistemaLog> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId);

	// Buscar logs por módulo
	List<SistemaLog> findByModuloOrderByDataHoraDesc(String modulo);

	// Buscar logs por período
	List<SistemaLog> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

	// Buscar logs por tabela e ID do registro
	List<SistemaLog> findByTabelaAfetadaAndIdRegistroOrderByDataHoraDesc(String tabelaAfetada, Long idRegistro);

	// Busca avançada com filtros
	@Query("SELECT l FROM SistemaLog l WHERE " + "(:tabela IS NULL OR l.tabelaAfetada = :tabela) AND "
			+ "(:acao IS NULL OR l.acao = :acao) AND " + "(:modulo IS NULL OR l.modulo = :modulo) AND "
			+ "(:usuarioId IS NULL OR l.usuarioId = :usuarioId) AND "
			+ "(:sucesso IS NULL OR l.sucesso = :sucesso) AND " + "l.dataHora BETWEEN :dataInicio AND :dataFim "
			+ "ORDER BY l.dataHora DESC")
	Page<SistemaLog> buscarComFiltros(@Param("tabela") String tabela, @Param("acao") String acao,
			@Param("modulo") String modulo, @Param("usuarioId") Long usuarioId, @Param("sucesso") Boolean sucesso,
			@Param("dataInicio") LocalDateTime dataInicio, @Param("dataFim") LocalDateTime dataFim, Pageable pageable);

	// Estatísticas de uso
	@Query("SELECT l.modulo, COUNT(l) as total FROM SistemaLog l " + "WHERE l.dataHora BETWEEN :inicio AND :fim "
			+ "GROUP BY l.modulo " + "ORDER BY total DESC")
	List<Object[]> getEstatisticasPorModulo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("SELECT l.acao, COUNT(l) as total FROM SistemaLog l " + "WHERE l.dataHora BETWEEN :inicio AND :fim "
			+ "GROUP BY l.acao " + "ORDER BY total DESC")
	List<Object[]> getEstatisticasPorAcao(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

	@Query("SELECT s FROM SistemaLog s WHERE s.dataHora < :dataLimite ORDER BY s.dataHora ASC")
	List<SistemaLog> findByDataHoraBeforeOrderByDataHoraAsc(@Param("dataLimite") LocalDateTime dataLimite);

	// Ou este método para deleção direta:
	@Modifying
	@Query("DELETE FROM SistemaLog s WHERE s.dataHora < :dataLimite")
	@Transactional
	int deleteByDataHoraBefore(@Param("dataLimite") LocalDateTime dataLimite);

	 // Estatísticas
    @Query("SELECT COUNT(l) FROM SistemaLog l WHERE l.dataHora BETWEEN :inicio AND :fim")
    Long countByDataHoraBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT COUNT(l) FROM SistemaLog l WHERE l.dataHora BETWEEN :inicio AND :fim AND l.sucesso = :sucesso")
    Long countByDataHoraBetweenAndSucesso(
            @Param("inicio") LocalDateTime inicio, 
            @Param("fim") LocalDateTime fim, 
            @Param("sucesso") boolean sucesso);
    
 // Opções de filtro
    @Query("SELECT DISTINCT l.tabelaAfetada FROM SistemaLog l ORDER BY l.tabelaAfetada")
    List<String> findDistinctTabelaAfetada();
    
    @Query("SELECT DISTINCT l.acao FROM SistemaLog l ORDER BY l.acao")
    List<String> findDistinctAcao();
    
    @Query("SELECT DISTINCT l.modulo FROM SistemaLog l ORDER BY l.modulo")
    List<String> findDistinctModulo();
    
    // Top módulos mais ativos
    // Opção 1: Método corrigido SEM parâmetro limit (Recomendado)
 	@Query("SELECT l.modulo, COUNT(l) as quantidade FROM SistemaLog l " +
 		   "WHERE l.dataHora BETWEEN :inicio AND :fim " +
 		   "GROUP BY l.modulo " +
 		   "ORDER BY quantidade DESC")
 	List<Object[]> findTopModulosByDataHoraBetween(@Param("inicio") LocalDateTime inicio, 
 												   @Param("fim") LocalDateTime fim);
   
 // Opção 2: Método com Pageable para limit (Alternativa)
 	@Query("SELECT l.modulo, COUNT(l) as quantidade FROM SistemaLog l " +
 		   "WHERE l.dataHora BETWEEN :inicio AND :fim " +
 		   "GROUP BY l.modulo " +
 		   "ORDER BY quantidade DESC")
 	Page<Object[]> findTopModulosByDataHoraBetweenPageable(@Param("inicio") LocalDateTime inicio, 
 															@Param("fim") LocalDateTime fim,
 															Pageable pageable);
 	
 	// Método para logs recentes
 	@Query("SELECT l FROM SistemaLog l ORDER BY l.dataHora DESC")
 	List<SistemaLog> findTop100ByOrderByDataHoraDesc(Pageable pageable);
 	
 	// Método nativo para logs por status
 	@Query(value = "SELECT * FROM tb_sistema_log WHERE sucesso = :sucesso ORDER BY data_hora DESC LIMIT :limit", 
 		   nativeQuery = true)
 	List<SistemaLog> findBySucessoNative(@Param("sucesso") boolean sucesso, 
 										 @Param("limit") int limit);
 	
 	// Método para buscar logs por IP
 	List<SistemaLog> findByEnderecoIpContainingOrderByDataHoraDesc(String ip);
 	
 	// Método para logs de erro recentes
 	@Query("SELECT l FROM SistemaLog l WHERE l.sucesso = false ORDER BY l.dataHora DESC")
 	List<SistemaLog> findUltimosErros(Pageable pageable);
 	
 	
    	
}