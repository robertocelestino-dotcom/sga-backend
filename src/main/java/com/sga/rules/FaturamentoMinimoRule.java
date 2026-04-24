// src/main/java/com/sga/rules/FaturamentoMinimoRule.java

package com.sga.rules;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;

@Component
public class FaturamentoMinimoRule {
    
    private static final Logger log = LoggerFactory.getLogger(FaturamentoMinimoRule.class);
    
    private static final List<String> EXCECOES = Arrays.asList(
        "SPC AVISA", "HSM", "CONTRIBUICAO ASSOCIATIVA");
    
    /**
     * Aplica a regra de complemento de faturamento mínimo
     * @param extemporaneo Se é faturamento extemporâneo (só aplica se true)
     */
    public Fatura aplicarRegraComplemento(Fatura fatura, Associado associado, Boolean extemporaneo) {
        if (fatura == null || associado == null) return fatura;
        
        // Só aplica para faturamentos extemporâneos
        if (extemporaneo == null || !extemporaneo) {
            return fatura;
        }
        
        // 🔥 CORREÇÃO: Verificar se associado tem categoria de faturamento mínimo
        // Usar o campo faturamentoMinimo do associado ou da categoria
        Boolean temFaturamentoMinimo = false;
        
        if (associado.getCategoria() != null) {
            temFaturamentoMinimo = associado.getCategoria().getFaturamentoMinimo() != null && 
                                   associado.getCategoria().getFaturamentoMinimo();
        }
        
        // Alternativa: verificar se o associado tem faturamentoMinimo configurado
        if (associado.getFaturamentoMinimo() != null && 
            associado.getFaturamentoMinimo().compareTo(BigDecimal.ZERO) > 0) {
            temFaturamentoMinimo = true;
        }
        
        if (!temFaturamentoMinimo) {
            return fatura;
        }
        
        // Identificar item COMPLEMENTO FATURAMENTO MINIMO
        Optional<FaturaItem> complementoItem = fatura.getItens().stream()
            .filter(item -> item.getDescricao() != null && 
                   item.getDescricao().toUpperCase().contains("COMPLEMENTO FATURAMENTO MINIMO"))
            .findFirst();
        
        if (complementoItem.isEmpty()) return fatura;
        
        // Somar todos os itens exceto exceções e o próprio complemento
        BigDecimal somaOutrosItens = fatura.getItens().stream()
            .filter(item -> !isExcecao(item.getDescricao()))
            .filter(item -> item != complementoItem.get())
            .map(item -> item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Buscar valor do plano (faturamento mínimo)
        BigDecimal valorPlano = associado.getFaturamentoMinimo() != null ? 
            associado.getFaturamentoMinimo() : BigDecimal.ZERO;
        
        // Se não tem valor no associado, buscar da categoria
        if (valorPlano.compareTo(BigDecimal.ZERO) == 0 && associado.getCategoria() != null) {
            valorPlano = associado.getCategoria().getValorMinimo() != null ? 
                associado.getCategoria().getValorMinimo() : BigDecimal.ZERO;
        }
        
        BigDecimal diferenca = valorPlano.subtract(somaOutrosItens);
        
        if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
            // Ajustar valor do complemento
            complementoItem.get().setValorUnitario(diferenca);
            complementoItem.get().setValorTotal(diferenca);
            log.info("Complemento faturamento mínimo aplicado. Valor: {}", diferenca);
        } else {
            // Excedeu, remover complemento
            fatura.getItens().remove(complementoItem.get());
            log.info("Complemento faturamento mínimo removido - valor excedido");
        }
        
        fatura.recalcularValor();
        return fatura;
    }
    
    /**
     * Verifica se a descrição é uma exceção
     */
    private boolean isExcecao(String descricao) {
        if (descricao == null) return false;
        String descUpper = descricao.toUpperCase();
        return EXCECOES.stream().anyMatch(descUpper::contains);
    }
}