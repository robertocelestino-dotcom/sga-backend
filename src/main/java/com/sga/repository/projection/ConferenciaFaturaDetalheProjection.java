package com.sga.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projeção tipada para a query {@code findFaturaDetalheConferenciaList}.
 *
 * <p>
 * Spring Data JPA mapeia as colunas do SELECT pelos nomes dos getters. Os
 * aliases do SELECT devem casar com os nomes dos getters em snake_case:
 * </p>
 *
 * <ul>
 * <li>{@code AS fatura_id} → {@code getFaturaId()}</li>
 * <li>{@code AS numero_fatura} → {@code getNumeroFatura()}</li>
 * <li>{@code AS data_emissao} → {@code getDataEmissao()}</li>
 * <li>{@code AS status_fatura} → {@code getStatusFatura()}</li>
 * <li>{@code AS valor_fatura} → {@code getValorFatura()}</li>
 * <li>{@code AS nota_id} → {@code getNotaId()}</li>
 * <li>{@code AS numero_nota_debito} → {@code getNumeroNotaDebito()}</li>
 * <li>{@code AS data_vencimento} → {@code getDataVencimento()}</li>
 * <li>{@code AS valor_nota} → {@code getValorNota()}</li>
 * <li>{@code AS associado_id} → {@code getAssociadoId()}</li>
 * <li>{@code AS codigo_spc} → {@code getCodigoSpc()}</li>
 * <li>{@code AS codigo_rm} → {@code getCodigoRm()}</li>
 * <li>{@code AS nome_razao} → {@code getNomeRazao()}</li>
 * </ul>
 *
 * @author SGA
 */
public interface ConferenciaFaturaDetalheProjection {

	Long getFaturaId();

	String getNumeroFatura();

	LocalDate getDataEmissao();

	String getStatusFatura();

	BigDecimal getValorFatura();

	Long getNotaId();

	String getNumeroNotaDebito();

	LocalDate getDataVencimento();

	BigDecimal getValorNota();

	Long getAssociadoId();

	String getCodigoSpc();

	String getCodigoRm();

	String getNomeRazao();
}
