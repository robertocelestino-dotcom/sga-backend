package com.sga.rules;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.Produto;
import com.sga.repository.ProdutoRepository;

@Component
public class FaturamentoMinimoRule {
    
    private static final Logger log = LoggerFactory.getLogger(FaturamentoMinimoRule.class);
    
    private static final List<String> EXCECOES = Arrays.asList(
        "SPC AVISA", "HSM", "CONTRIBUICAO ASSOCIATIVA");
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    /**
     * Aplica a regra de complemento de faturamento mínimo
     * @param extemporaneo Se é faturamento extemporâneo (só aplica se true)
     */
    public Fatura aplicarRegraComplemento(Fatura fatura, Associado associado, Boolean extemporaneo) {
        
        log.info("========================================");
        log.info("💰 APLICANDO REGRA DE FATURAMENTO MÍNIMO");
        log.info("========================================");
        
        if (fatura == null || associado == null) {
            log.warn("⚠️ Fatura ou associado nulo");
            return fatura;
        }
        
        log.info("📄 Associado: {}", associado.getNomeRazao());
        log.info("📄 ID Associado: {}", associado.getId());
        log.info("📄 Valor atual da fatura: R$ {}", fatura.getValorTotal());
        log.info("📄 Quantidade de itens: {}", fatura.getItens() != null ? fatura.getItens().size() : 0);
        
        // 🔥 COMENTADO TEMPORARIAMENTE - PERMITE APLICAR PARA TODAS AS RÉGUAS
        /*if (extemporaneo == null || !extemporaneo) {
            log.info("⏭️ Régua não é extemporânea, pulando regra");
            return fatura;
        }*/
        
        // ========== 1. VERIFICAR SE O ASSOCIADO TEM FATURAMENTO MÍNIMO ==========
        BigDecimal valorPlano = BigDecimal.ZERO;
        Boolean temFaturamentoMinimo = false;
        String origemValor = "NÃO CONFIGURADO";
        
        // 1.1 Verificar faturamento mínimo diretamente no associado
        if (associado.getFaturamentoMinimo() != null && 
            associado.getFaturamentoMinimo().compareTo(BigDecimal.ZERO) > 0) {
            valorPlano = associado.getFaturamentoMinimo();
            temFaturamentoMinimo = true;
            origemValor = "ASSOCIADO";
            log.info("💰 Faturamento mínimo do associado: R$ {}", valorPlano);
        }
        
        // 1.2 Verificar faturamento mínimo da categoria
        if (!temFaturamentoMinimo && associado.getCategoria() != null) {
            log.info("🔍 Verificando categoria: {}", associado.getCategoria().getDescricao());
            log.info("   FaturamentoMinimo: {}", associado.getCategoria().getFaturamentoMinimo());
            log.info("   ValorMinimo: {}", associado.getCategoria().getValorMinimo());
            
            if (associado.getCategoria().getFaturamentoMinimo() != null && 
                associado.getCategoria().getFaturamentoMinimo()) {
                valorPlano = associado.getCategoria().getValorMinimo() != null ? 
                    associado.getCategoria().getValorMinimo() : BigDecimal.ZERO;
                temFaturamentoMinimo = valorPlano.compareTo(BigDecimal.ZERO) > 0;
                origemValor = "CATEGORIA";
                
                if (temFaturamentoMinimo) {
                    log.info("💰 Faturamento mínimo da categoria: R$ {}", valorPlano);
                } else {
                    log.warn("⚠️ Categoria permite complemento mas valor mínimo é zero");
                }
            }
        }
        
        if (!temFaturamentoMinimo) {
            log.info("⏭️ Associado SEM faturamento mínimo configurado");
            return fatura;
        }
        
        log.info("📊 Origem do valor mínimo: {}", origemValor);
        
        // ========== 2. VERIFICAR SE HÁ ITENS NA FATURA ==========
        if (fatura.getItens() == null || fatura.getItens().isEmpty()) {
            log.info("⏭️ Fatura sem itens, pulando regra");
            return fatura;
        }
        
        // ========== 3. BUSCAR ITEM DE COMPLEMENTO EXISTENTE ==========
        Optional<FaturaItem> complementoItemOpt = fatura.getItens().stream()
            .filter(item -> item.getDescricao() != null && 
                   item.getDescricao().toUpperCase().contains("COMPLEMENTO FATURAMENTO MINIMO"))
            .findFirst();
        
        // ========== 4. CALCULAR SOMA DOS ITENS (EXCLUINDO O COMPLEMENTO) ==========
        log.info("📋 ITENS DA FATURA:");
        for (FaturaItem item : fatura.getItens()) {
            boolean isComplemento = item.getDescricao() != null && 
                item.getDescricao().toUpperCase().contains("COMPLEMENTO FATURAMENTO MINIMO");
            log.info("   - {}: R$ {} (Complemento: {})", 
                     item.getDescricao(), 
                     item.getValorTotal(),
                     isComplemento);
        }
        
        // 🔥 CORREÇÃO: Excluir o próprio complemento do cálculo
        BigDecimal somaItens = fatura.getItens().stream()
            .filter(item -> item.getDescricao() != null)
            .filter(item -> !isExcecao(item.getDescricao()))
            .filter(item -> !item.getDescricao().toUpperCase().contains("COMPLEMENTO FATURAMENTO MINIMO")) // ← EXCLUI COMPLEMENTO
            .map(item -> item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("📊 Soma dos itens (exceto exceções e complemento): R$ {}", somaItens);
        log.info("📊 Valor mínimo do plano: R$ {}", valorPlano);
        
        // ========== 5. CALCULAR DIFERENÇA ==========
        BigDecimal diferenca = valorPlano.subtract(somaItens);
        log.info("📊 Diferença necessária: R$ {}", diferenca);
        
        // ========== 6. DECISÃO ==========
        if (diferenca.compareTo(BigDecimal.ZERO) > 0) {
            // PRECISA DE COMPLEMENTO
            if (complementoItemOpt.isPresent()) {
                // ATUALIZAR ITEM EXISTENTE
                FaturaItem item = complementoItemOpt.get();
                item.setValorUnitario(diferenca);
                item.setValorTotal(diferenca);
                log.info("✅ Complemento ATUALIZADO: R$ {}", diferenca);
            } else {
                // 🔥 CRIAR NOVO ITEM DE COMPLEMENTO
                log.info("🆕 CRIANDO novo item de COMPLEMENTO FATURAMENTO MINIMO");
                
                // Buscar o produto de complemento
                Produto produtoComplemento = buscarProdutoComplemento();
                
                FaturaItem novoItem = new FaturaItem();
                novoItem.setFatura(fatura);
                
                if (produtoComplemento != null) {
                    novoItem.setCodigoProduto(produtoComplemento.getCodigoRm());
                    novoItem.setDescricao(produtoComplemento.getDescricao());
                    novoItem.setProduto(produtoComplemento);
                    log.info("   Produto encontrado: ID={}, Código={}, Descrição={}", 
                             produtoComplemento.getId(),
                             produtoComplemento.getCodigoRm(), 
                             produtoComplemento.getDescricao());
                } else {
                    // FALLBACK: usar dados fixos
                    novoItem.setCodigoProduto("04.01.03.57665");
                    novoItem.setDescricao("COMPLEMENTO FATURAMENTO MINIMO");
                    log.warn("⚠️ Produto de complemento NÃO encontrado, usando fallback");
                }
                
                novoItem.setQuantidade(BigDecimal.ONE);
                novoItem.setValorUnitario(diferenca);
                novoItem.setValorTotal(diferenca);
                novoItem.setTipoLancamento("D");
                
                fatura.getItens().add(novoItem);
                log.info("✅ Novo complemento CRIADO: R$ {}", diferenca);
            }
        } else {
            // NÃO PRECISA DE COMPLEMENTO
            if (complementoItemOpt.isPresent()) {
                fatura.getItens().remove(complementoItemOpt.get());
                log.info("🗑️ Complemento REMOVIDO - valor já atinge o mínimo");
            } else {
                log.info("ℹ️ Nenhum complemento necessário");
            }
        }
        
        // ========== 7. RECALCULAR VALOR TOTAL ==========
        fatura.recalcularTotal();
        log.info("📊 Valor FINAL da fatura: R$ {}", fatura.getValorTotal());
        log.info("========================================");
        
        return fatura;
    }
    
    /**
     * Verifica se a descrição é uma exceção
     * Itens de exceção não são considerados para o cálculo do complemento
     */
    private boolean isExcecao(String descricao) {
        if (descricao == null) return false;
        String descUpper = descricao.toUpperCase();
        return EXCECOES.stream().anyMatch(descUpper::contains);
    }
    
    /**
     * 🔥 Busca o produto de complemento de faturamento mínimo
     * 
     * @return Produto de complemento ou null se não encontrado
     */
    private Produto buscarProdutoComplemento() {
        log.info("🔍 Buscando produto de complemento de faturamento mínimo");
        
        // Tentar buscar pelo código RM específico
        try {
            Optional<Produto> produtoOpt = produtoRepository.findByCodigoRm("04.01.03.57665");
            
            if (produtoOpt.isPresent()) {
                Produto produto = produtoOpt.get();
                log.info("✅ Produto encontrado pelo código RM: {} - {}", 
                         produto.getCodigoRm(), produto.getDescricao());
                return produto;
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao buscar produto pelo código RM: {}", e.getMessage());
        }
        
        // Fallback: buscar por descrição
        log.info("🔍 Produto não encontrado pelo código RM, buscando por descrição...");
        
        try {
            List<Produto> produtos = produtoRepository.findByDescricaoContainingIgnoreCase("COMPLEMENTO");
            
            if (produtos != null && !produtos.isEmpty()) {
                log.info("   Encontrados {} produtos com 'COMPLEMENTO' na descrição", produtos.size());
                
                Produto produto = produtos.stream()
                    .filter(p -> p.getDescricao() != null && 
                           p.getDescricao().toUpperCase().contains("FATURAMENTO MINIMO"))
                    .findFirst()
                    .orElse(null);
                
                if (produto != null) {
                    log.info("✅ Produto encontrado pela descrição: {} - {}", 
                             produto.getCodigoRm(), produto.getDescricao());
                    return produto;
                }
                
                produto = produtos.get(0);
                log.info("✅ Produto encontrado pela descrição (fallback): {} - {}", 
                         produto.getCodigoRm(), produto.getDescricao());
                return produto;
            }
        } catch (Exception e) {
            log.warn("⚠️ Erro ao buscar produto por descrição: {}", e.getMessage());
        }
        
        log.warn("⚠️ Nenhum produto de complemento encontrado");
        return null;
    }
}