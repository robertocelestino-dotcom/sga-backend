package com.sga.repository.projection;

import java.math.BigDecimal;

/**
 * Projeção tipada para a query {@code findResumoConferencia}.
 *
 * <p>
 * Contém os totais agregados do período consultado na tela de conferência.
 * </p>
 *
 * @author SGA
 */
public interface ResumoConferenciaProjection {

	BigDecimal getSomaDiferencas();

	Integer getTotalItensFatura();

	Integer getTotalItensNota();

	Integer getTotalFranquiasFatura();

	Integer getTotalFranquiasNota();

	BigDecimal getSomaValorNotas();

	BigDecimal getSomaValorFaturas();
}