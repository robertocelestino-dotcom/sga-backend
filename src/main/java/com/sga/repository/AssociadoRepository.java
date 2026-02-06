package com.sga.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Associado;

@Repository
public interface AssociadoRepository extends JpaRepository<Associado, Long>, JpaSpecificationExecutor<Associado> {

	// Métodos de filtro para paginação
	Page<Associado> findAllByCodigoSpcContainingIgnoreCase(String codigoSpc, Pageable pageable);

	Page<Associado> findAllByNomeRazaoContainingIgnoreCase(String nomeRazao, Pageable pageable);

	Page<Associado> findAllByCnpjCpfContaining(String cnpjCpf, Pageable pageable);

	Page<Associado> findAllByStatus(String status, Pageable pageable);

	// Método correto - usando campo cnpjCpf
	@Query("SELECT COUNT(a) FROM Associado a WHERE a.status = 'A'")
	Long countAssociadosAtivos();

	// Método correto - usando campo cnpjCpf
	@Query("SELECT a.cnpjCpf FROM Associado a WHERE a.status = 'A'")
	List<String> findAllCnpjCpfAtivos();

	// Método para valor total - ajuste conforme sua lógica de negócio
	// Se não tiver campo específico, use um valor fixo ou outro campo disponível
	@Query("SELECT COALESCE(SUM(a.faturamentoMinimo), 0) FROM Associado a WHERE a.status = 'A'")
	BigDecimal findValorTotalNotasDebito();

	// Método alternativo simplificado
	@Query("SELECT COALESCE(SUM(a.faturamentoMinimo), 0) FROM Associado a")
	BigDecimal findTotalValorNotas();

	// Método com período - usando dataCadastro
	@Query("SELECT COALESCE(SUM(a.faturamentoMinimo), 0) FROM Associado a WHERE a.dataCadastro BETWEEN :dataInicio AND :dataFim")
	BigDecimal calcularValorTotalPeriodo(@Param("dataInicio") Date dataInicio, @Param("dataFim") Date dataFim);

	// Método adicional útil
	@Query("SELECT COUNT(a) FROM Associado a")
	Long countAllAssociados();

	@Query("SELECT a FROM Associado a LEFT JOIN FETCH a.vendedor v LEFT JOIN FETCH a.vendedorExterno ve WHERE a.id = :id")
	Associado findByIdWithVendedores(@Param("id") Integer id);

	@Query("SELECT a FROM Associado a WHERE "
			+ "(:codigoSpc IS NULL OR LOWER(a.codigoSpc) LIKE LOWER(CONCAT('%', :codigoSpc, '%'))) AND "
			+ "(:nomeRazao IS NULL OR LOWER(a.nomeRazao) LIKE LOWER(CONCAT('%', :nomeRazao, '%'))) AND "
			+ "(:cnpjCpf IS NULL OR a.cnpjCpf LIKE CONCAT('%', :cnpjCpf, '%')) AND "
			+ "(:status IS NULL OR a.status = :status)")
	Page<Associado> findAllByFiltros(@Param("codigoSpc") String codigoSpc, @Param("nomeRazao") String nomeRazao,
			@Param("cnpjCpf") String cnpjCpf, @Param("status") String status, Pageable pageable);

	// Adicione este método para carregar relacionamentos
	/*
	 * @Query("SELECT DISTINCT a FROM Associado a " + "LEFT JOIN FETCH a.vendedor "
	 * + "LEFT JOIN FETCH a.vendedorExterno " + "LEFT JOIN FETCH a.plano " +
	 * "LEFT JOIN FETCH a.categoria " + "LEFT JOIN FETCH a.enderecos " +
	 * "LEFT JOIN FETCH a.emails " + "LEFT JOIN FETCH a.telefones " +
	 * "WHERE a.id = :id") Optional<Associado>
	 * findByIdWithRelacionamentos(@Param("id") Long id);
	 */

	@Query("SELECT a FROM Associado a WHERE "
			+ "(:codigoSpc IS NULL OR a.codigoSpc IS NULL OR LOWER(a.codigoSpc) LIKE LOWER(CONCAT('%', :codigoSpc, '%'))) AND "
			+ "(:nomeRazao IS NULL OR LOWER(a.nomeRazao) LIKE LOWER(CONCAT('%', :nomeRazao, '%'))) AND "
			+ "(:cnpjCpf IS NULL OR a.cnpjCpf LIKE CONCAT('%', :cnpjCpf, '%')) AND "
			+ "(:status IS NULL OR a.status = :status)")

	Page<Associado> findByFiltrosCombinados(@Param("codigoSpc") String codigoSpc, @Param("nomeRazao") String nomeRazao,
			@Param("cnpjCpf") String cnpjCpf, @Param("status") String status, Pageable pageable);

	Optional<Associado> findByCnpjCpf(String cnpjCpf);

	List<Associado> findByStatus(String status);

	Long countByStatus(String status);

	@Query("SELECT a FROM Associado a WHERE a.nomeRazao LIKE %:nome% OR a.nomeFantasia LIKE %:nome%")
	List<Associado> findByNomeContaining(String nome);

	@Query("SELECT a FROM Associado a WHERE a.codigoSpc = :codigoSpc")
	Optional<Associado> findByCodigoSpc(String codigoSpc);
}