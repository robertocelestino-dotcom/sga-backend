package com.sga.repository.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;

/**
 * Classe técnica que define os mapeamentos de {@code @SqlResultSetMapping}
 * para as queries nativas da conferência.
 *
 * <p>Não é uma entidade persistida — existe apenas para registrar os mapeamentos
 * de ResultSet → DTO tipado no contexto JPA.</p>
 *
 * @author SGA
 */
@Entity
@Table(name = "tb_fatura")  // qualquer tabela existente — não é persistida
@SqlResultSetMappings({
    // ============================================================
    // MAPPING 1: ConferenciaFaturamento
    // ============================================================
    @SqlResultSetMapping(
        name = "ConferenciaFaturamentoMapping",
        classes = @ConstructorResult(
            targetClass = ConferenciaFaturamentoResult.class,
            columns = {
                @ColumnResult(name = "fatura_id",           type = Long.class),
                @ColumnResult(name = "numero_fatura",       type = String.class),
                @ColumnResult(name = "data_emissao",        type = LocalDate.class),
                @ColumnResult(name = "status_fatura",       type = String.class),
                @ColumnResult(name = "valor_fatura",        type = BigDecimal.class),
                @ColumnResult(name = "nota_id",             type = Long.class),
                @ColumnResult(name = "numero_nota_debito",  type = String.class),
                @ColumnResult(name = "data_vencimento",     type = LocalDate.class),
                @ColumnResult(name = "valor_nota",          type = BigDecimal.class),
                @ColumnResult(name = "tipo_arquivo",        type = String.class),
                @ColumnResult(name = "associado_id",        type = Long.class),
                @ColumnResult(name = "codigo_spc",          type = String.class),
                @ColumnResult(name = "codigo_rm",           type = String.class),
                @ColumnResult(name = "nome_razao",          type = String.class),
                @ColumnResult(name = "qtd_itens_nota",      type = Integer.class),
                @ColumnResult(name = "qtd_itens_fatura",    type = Integer.class),
                @ColumnResult(name = "qtd_franquias_nota",  type = Integer.class),
                @ColumnResult(name = "qtd_franquias_fatura",type = Integer.class)
            }
        )
    ),
    // ============================================================
    // MAPPING 2: ConferenciaFaturaDetalhe
    // ============================================================
    @SqlResultSetMapping(
        name = "ConferenciaFaturaDetalheMapping",
        classes = @ConstructorResult(
            targetClass = ConferenciaFaturaDetalheResult.class,
            columns = {
                @ColumnResult(name = "fatura_id",           type = Long.class),
                @ColumnResult(name = "numero_fatura",       type = String.class),
                @ColumnResult(name = "data_emissao",        type = LocalDate.class),
                @ColumnResult(name = "status_fatura",       type = String.class),
                @ColumnResult(name = "valor_fatura",        type = BigDecimal.class),
                @ColumnResult(name = "nota_id",             type = Long.class),
                @ColumnResult(name = "numero_nota_debito",  type = String.class),
                @ColumnResult(name = "data_vencimento",     type = LocalDate.class),
                @ColumnResult(name = "valor_nota",          type = BigDecimal.class),
                @ColumnResult(name = "associado_id",        type = Long.class),
                @ColumnResult(name = "codigo_spc",          type = String.class),
                @ColumnResult(name = "codigo_rm",           type = String.class),
                @ColumnResult(name = "nome_razao",          type = String.class)
            }
        )
    ),
    // ============================================================
    // MAPPING 3: ItemComparacao
    // ============================================================
    @SqlResultSetMapping(
        name = "ItemComparacaoMapping",
        classes = @ConstructorResult(
            targetClass = ItemComparacaoResult.class,
            columns = {
                @ColumnResult(name = "codigo",          type = String.class),
                @ColumnResult(name = "descricao",       type = String.class),
                @ColumnResult(name = "quantidade",      type = BigDecimal.class),
                @ColumnResult(name = "valor_unitario",  type = BigDecimal.class),
                @ColumnResult(name = "valor_total",     type = BigDecimal.class)
            }
        )
    ),
    // ============================================================
    // MAPPING 4: ResumoConferencia
    // ============================================================
    @SqlResultSetMapping(
        name = "ResumoConferenciaMapping",
        classes = @ConstructorResult(
            targetClass = ResumoConferenciaResult.class,
            columns = {
                @ColumnResult(name = "soma_diferencas",         type = BigDecimal.class),
                @ColumnResult(name = "total_itens_fatura",      type = Integer.class),
                @ColumnResult(name = "total_itens_nota",        type = Integer.class),
                @ColumnResult(name = "total_franquias_fatura",  type = Integer.class),
                @ColumnResult(name = "total_franquias_nota",    type = Integer.class),
                @ColumnResult(name = "soma_valor_notas",        type = BigDecimal.class),
                @ColumnResult(name = "soma_valor_faturas",      type = BigDecimal.class)
            }
        )
    )
})
public class ConferenciaFaturamentoMapping {

    @Id
    private Long id;

    // Getter/setter obrigatório para a entidade
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}