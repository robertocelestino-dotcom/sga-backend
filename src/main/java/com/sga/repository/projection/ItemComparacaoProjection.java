package com.sga.repository.projection;

import java.math.BigDecimal;

/**
 * Projeção tipada para as queries {@code findItensNotaByFaturaId} e
 * {@code findItensFaturaByFaturaId}.
 *
 * <p>
 * Usada para listar os itens da nota de débito e da fatura na tela de
 * conferência, comparando-os item a item.
 * </p>
 *
 * @author SGA
 */
public interface ItemComparacaoProjection {

	String getCodigo();

	String getDescricao();

	BigDecimal getQuantidade();

	BigDecimal getValorUnitario();

	BigDecimal getValorTotal();
}