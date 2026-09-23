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
	
	/*@Query("SELECT c FROM CancelamentoImportacao c WHERE c.codigoAssociado = :codigoAssociado "
			+ "AND c.mesReferencia = :mes AND c.anoReferencia = :ano AND c.processado = false")
	List<CancelamentoImportacao> findPendentesByCodigoAssociadoAndPeriodo(
			@Param("codigoAssociado") String codigoAssociado, 
			@Param("mes") Integer mes, 
			@Param("ano") Integer ano);
	*/
	
	/**
	 * 🔥 CORRIGIDO — Busca cancelamentos PENDENTES com matching ROBUSTO de código.
	 * 
	 * Como o código pode estar armazenado de formas diferentes entre as tabelas:
	 *   - tb_nota_debito_spc.codigo_socio → 8 dígitos com LPAD ('00022902')
	 *   - tb_associado.codigospc → SEM padding ('22902')
	 *   - tb_cancelamento_importacao.codigo_associado → SEM padding ('22902')
	 * 
	 * Esta query normaliza AMBOS os lados com LPAD(.., 8, '0') para garantir o match.
	 */
	@Query(value = 
	    "SELECT c.* FROM tb_cancelamento_importacao c " +
	    "WHERE c.mes_referencia = :mes " +
	    "  AND c.ano_referencia = :ano " +
	    "  AND c.processado = false " +
	    "  AND c.status = 'PENDENTE' " +
	    "  AND LPAD(TRIM(c.codigo_associado), 8, '0') = LPAD(TRIM(:codigoAssociado), 8, '0') " +
	    //"  AND c.codigo_associado = :codigoAssociado " +
	    "ORDER BY c.id",
	    nativeQuery = true)
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