package com.sga.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projeção tipada para a query {@code findConferenciaFaturamento}.
 *
 * <p>
 * Contém todas as colunas da listagem de conferência, incluindo contadores
 * agregados de itens e franquias.
 * </p>
 *
 * @author SGA
 */
public interface ConferenciaFaturamentoProjection {

	// ===== Dados da Fatura =====
	Long getFaturaId();

	String getNumeroFatura();

	LocalDate getDataEmissao();

	String getStatusFatura();

	BigDecimal getValorFatura();

	// ===== Dados da Nota de Débito =====
	Long getNotaId();

	String getNumeroNotaDebito();

	LocalDate getDataVencimento();

	BigDecimal getValorNota();

	String getTipoArquivo();

	// ===== Dados do Associado =====
	Long getAssociadoId();

	String getCodigoSpc();

	String getCodigoRm();

	String getNomeRazao();

	// ===== Contadores =====
	Integer getQtdItensNota();

	Integer getQtdItensFatura();

	Integer getQtdFranquiasNota();

	Integer getQtdFranquiasFatura();
}