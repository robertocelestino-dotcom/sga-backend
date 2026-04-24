package com.sga.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.CancelamentoImportacao;

@Repository
public interface CancelamentoImportacaoRepository extends JpaRepository<CancelamentoImportacao, Long> {

	// ========== BUSCAS POR PERÍODO ==========
	
	@Query("SELECT c FROM CancelamentoImportacao c WHERE c.mesReferencia = :mes AND c.anoReferencia = :ano")
	List<CancelamentoImportacao> findByMesReferenciaAndAnoReferencia(@Param("mes") Integer mes,
			@Param("ano") Integer ano);

	// ========== BUSCAS POR IMPORTAÇÃO ==========
	
	List<CancelamentoImportacao> findByImportacaoId(Long importacaoId);

	// ========== BUSCAS POR CÓDIGO DO ASSOCIADO ==========
	
	List<CancelamentoImportacao> findByCodigoAssociado(String codigoAssociado);

	// 🔥 CORRIGIDO - Usando codigoAssociado (String) em vez de associado.id
	@Query("SELECT c FROM CancelamentoImportacao c WHERE c.codigoAssociado = :codigoAssociado " +
		   "AND c.mesReferencia = :mes AND c.anoReferencia = :ano")
	List<CancelamentoImportacao> findByCodigoAssociadoAndPeriodo(@Param("codigoAssociado") String codigoAssociado,
																  @Param("mes") Integer mes,
																  @Param("ano") Integer ano);

	// ========== BUSCAS POR CÓDIGO DO SERVIÇO ==========
	
	List<CancelamentoImportacao> findByCodigoServico(String codigoServico);

	// ========== BUSCAS POR CÓDIGO DO ASSOCIADO COM PROCESSADO ==========
	
	@Query("SELECT c FROM CancelamentoImportacao c WHERE c.codigoAssociado = :codigoAssociado "
			+ "AND c.mesReferencia = :mes AND c.anoReferencia = :ano AND c.processado = false")
	List<CancelamentoImportacao> findPendentesByCodigoAssociadoAndPeriodo(
			@Param("codigoAssociado") String codigoAssociado, 
			@Param("mes") Integer mes, 
			@Param("ano") Integer ano);

	// ========== BUSCAS POR STATUS ==========
	
	List<CancelamentoImportacao> findByProcessadoFalse();
	
	List<CancelamentoImportacao> findByStatus(String status);
	
	// ========== CONTAGENS ==========
	
	long countByImportacaoId(Long importacaoId);
	
	long countByCodigoAssociado(String codigoAssociado);
	
	long countByProcessadoFalse();
}