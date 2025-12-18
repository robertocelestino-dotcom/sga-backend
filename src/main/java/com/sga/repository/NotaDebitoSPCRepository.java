package com.sga.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.model.NotaDebitoSPC;

@Repository
public interface NotaDebitoSPCRepository extends JpaRepository<NotaDebitoSPC, Long> {

	int countByImportacao_Id(Long importacaoId);

	List<NotaDebitoSPC> findByImportacao_Id(Long importacaoId);

	// página de notas por importação
	Page<NotaDebitoSPC> findByImportacao_Id(Long importacaoId, Pageable pageable);

	// filtro simples (busca em numeroNota, codigoSocio, nomeAssociado)
	@Query("select n from NotaDebitoSPC n where n.importacao.id = :importacaoId and "
			+ "(lower(n.numeroNotaDebito) like lower(concat('%', :filtro, '%')) or "
			+ "lower(n.codigoSocio) like lower(concat('%', :filtro, '%')) or "
			+ "lower(n.nomeAssociado) like lower(concat('%', :filtro, '%')) )")
	Page<NotaDebitoSPC> findByImportacaoIdAndFiltro(Long importacaoId, String filtro, Pageable pageable);
	
	@Query(
	        value =
	            " SELECT " +
	            "   nd.id, " +
	            "   nd.numero_nota, " +
	            "   nd.codigo_socio, " +
	            "   nd.nome_associado, " +

	            "   COALESCE(SUM(CASE WHEN ni.credito_debito = 'D' THEN ni.valor_total ELSE 0 END), 0) AS total_debitos, " +
	            "   COALESCE(SUM(CASE WHEN ni.credito_debito = 'C' THEN ni.valor_total ELSE 0 END), 0) AS total_creditos, " +

	            "   COALESCE(SUM( " +
	            "       CASE " +
	            "           WHEN ni.credito_debito = 'D' THEN ni.valor_total " +
	            "           WHEN ni.credito_debito = 'C' THEN -ni.valor_total " +
	            "           ELSE 0 " +
	            "       END " +
	            "   ), 0) AS valor_faturado " +

	            " FROM tb_nota_debito_spc nd " +
	            " JOIN tb_nota_itens_spc ni ON ni.nota_debito_id = nd.id " +
	            " WHERE nd.importacao_id = :importacaoId " +
	            " GROUP BY nd.id, nd.numero_nota, nd.codigo_socio, nd.nome_associado ",
	        countQuery =
	            " SELECT COUNT(*) FROM tb_nota_debito_spc WHERE importacao_id = :importacaoId ",
	        nativeQuery = true
	    )
		Page<Object[]> listarNotasResumo(
	        @Param("importacaoId") Long importacaoId,
	        Pageable pageable
	    );
	    
	    /*
	    @Query(
	            value =
	              "SELECT " +
	              " nd.id AS id, " +
	              " nd.numero_nota AS numeroNota, " +
	              " nd.codigo_socio AS codigoSocio, " +
	              " nd.nome_associado AS nomeAssociado, " +
	              " COALESCE(SUM(CASE WHEN ni.credito_debito = 'D' THEN ni.valor_total ELSE 0 END),0) AS totalDebitos, " +
	              " COALESCE(SUM(CASE WHEN ni.credito_debito = 'C' THEN ni.valor_total ELSE 0 END),0) AS totalCreditos, " +
	              " COALESCE(SUM(CASE WHEN ni.credito_debito = 'D' THEN ni.valor_total ELSE 0 END),0) - " +
	              " COALESCE(SUM(CASE WHEN ni.credito_debito = 'C' THEN ni.valor_total ELSE 0 END),0) AS valorFaturado " +
	              "FROM tb_nota_debito_spc nd " +
	              "JOIN tb_nota_itens_spc ni ON ni.nota_debito_id = nd.id " +
	              "WHERE nd.importacao_id = :importacaoId " +
	              "AND ( " +
	              "  :filtro IS NULL OR :filtro = '' OR " +
	              "  nd.codigo_socio ILIKE '%' || :filtro || '%' OR " +
	              "  nd.nome_associado ILIKE '%' || :filtro || '%' OR " +
	              "  nd.cnpj_cic ILIKE '%' || :filtro || '%' " +
	              ") " +
	              "GROUP BY nd.id, nd.numero_nota, nd.codigo_socio, nd.nome_associado",
	            countQuery =
	              "SELECT COUNT(DISTINCT nd.id) " +
	              "FROM tb_nota_debito_spc nd " +
	              "WHERE nd.importacao_id = :importacaoId " +
	              "AND ( " +
	              "  :filtro IS NULL OR :filtro = '' OR " +
	              "  nd.codigo_socio ILIKE '%' || :filtro || '%' OR " +
	              "  nd.nome_associado ILIKE '%' || :filtro || '%' OR " +
	              "  nd.cnpj_cic ILIKE '%' || :filtro || '%' " +
	              ") ",
	            nativeQuery = true
	        )
	    	Page<NotaDebitoResumoDTO> listarResumoNotas(
	            @Param("importacaoId") Long importacaoId,
	            @Param("filtro") String filtro,
	            Pageable pageable
	        );
	        */
	    @Query(value = "SELECT " +
	            "  id as id, " +
	            "  numero_nota_debito as numeroNota, " +
	            "  codigo_socio as codigoSocio, " +
	            "  nome_associado as nomeAssociado, " +
	            "  0 as totalDebitos, " +
	            "  0 as totalCreditos, " +
	            "  0 as valorFaturado " +
	            "FROM tb_nota_debito_spc " +
	            "WHERE importacao_id = :importacaoId " +
	            "  AND (:filtro IS NULL OR :filtro = '' OR " +
	            "       codigo_socio LIKE CONCAT('%', :filtro, '%') OR " +
	            "       nome_associado LIKE CONCAT('%', :filtro, '%') OR " +
	            "       numero_nota_debito LIKE CONCAT('%', :filtro, '%')) " +
	            "LIMIT 10",
	            nativeQuery = true)
	     Page<NotaDebitoResumoDTO> listarResumoNotas(
	             @Param("importacaoId") Long importacaoId,
	             @Param("filtro") String filtro,
	             Pageable pageable
	     );
	    
	        
	        /*
	        @Query(
	        	      value =
	        	        "SELECT new com.sga.dto.NotaDebitoResumoDTO(" +
	        	        " nd.id, nd.numeroNota, nd.codigoSocio, nd.nomeAssociado, " +
	        	        " SUM(CASE WHEN ni.creditoDebito = 'D' THEN ni.valorTotal ELSE 0 END), " +
	        	        " SUM(CASE WHEN ni.creditoDebito = 'C' THEN ni.valorTotal ELSE 0 END), " +
	        	        " SUM(CASE WHEN ni.creditoDebito = 'D' THEN ni.valorTotal ELSE 0 END) - " +
	        	        " SUM(CASE WHEN ni.creditoDebito = 'C' THEN ni.valorTotal ELSE 0 END) " +
	        	        ") " +
	        	        "FROM NotaDebitoSPC nd " +
	        	        "JOIN nd.itens ni " +
	        	        "WHERE nd.importacao.id = :importacaoId " +
	        	        "AND ( :filtro IS NULL OR " +
	        	        "      nd.codigoSocio LIKE %:filtro% OR " +
	        	        "      nd.nomeAssociado LIKE %:filtro% ) " +
	        	        "GROUP BY nd.id, nd.numeroNota, nd.codigoSocio, nd.nomeAssociado",
	        	      countQuery =
	        	        "SELECT COUNT(DISTINCT nd.id) " +
	        	        "FROM NotaDebitoSPC nd " +
	        	        "WHERE nd.importacao.id = :importacaoId"
	        	    )
	        	    Page<NotaDebitoResumoDTO> listarResumoNotas(
	        	            @Param("importacaoId") Long importacaoId,
	        	            @Param("filtro") String filtro,
	        	            Pageable pageable
	        	    );	  */      

}