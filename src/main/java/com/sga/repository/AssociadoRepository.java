package com.sga.repository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Associado;

@Repository
public interface AssociadoRepository extends JpaRepository<Associado, Long> {

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
	
	

}