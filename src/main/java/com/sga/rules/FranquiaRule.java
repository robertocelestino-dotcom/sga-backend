package com.sga.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;

@Component
public class FranquiaRule {
    
    private static final Logger log = LoggerFactory.getLogger(FranquiaRule.class);
    
    private static final String PREFIXO_FRANQUIA = "FRANQUIA DE CONSULTA";
    
    // Mapeamento específico de franquia para serviço
    private static final Map<String, String> MAPEAMENTO_ESPECIFICO = new HashMap<>();
    static {
        MAPEAMENTO_ESPECIFICO.put("SPC MIX", "SPC MIX (SPC + CHEQUE)");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX PLUS", "SPC MIX PLUS");
        MAPEAMENTO_ESPECIFICO.put("SPC MAX", "SPC MAX");
        MAPEAMENTO_ESPECIFICO.put("SPC MAXI", "SPC MAXI");
        MAPEAMENTO_ESPECIFICO.put("SPC PLUS", "SPC PLUS");
        MAPEAMENTO_ESPECIFICO.put("SPC RELATORIO", "SPC RELATORIO");
        MAPEAMENTO_ESPECIFICO.put("SPC RELATORIO COMPLETO", "SPC RELATORIO COMPLETO");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX POSITIVO FOR", "SPC MIX POSITIVO FOR");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX POSITIVO", "SPC MIX POSITIVO");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MIX MAIS", "NOVO SPC MIX MAIS");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MAXI", "NOVO SPC MAXI");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MAXI 1/1", "NOVO SPC MAXI 1/1");
        MAPEAMENTO_ESPECIFICO.put("CHEQUE", "CHEQUE");
    }
    
    public Fatura aplicarRegraFranquia(Fatura fatura, Associado associado) {
        if (fatura == null || associado == null) {
            log.warn("Fatura ou associado nulo, ignorando regra de franquia");
            return fatura;
        }
        
        log.info("========================================");
        log.info("📊 APLICANDO REGRA DE FRANQUIA");
        log.info("========================================");
        log.info("📄 Associado: {}", associado.getNomeRazao());
        log.info("📄 Total de itens na fatura: {}", fatura.getItens().size());
        
        // 1. IDENTIFICAR TODOS OS ITENS DE FRANQUIA
        List<FaturaItem> itensFranquia = new ArrayList<>();
        
        for (FaturaItem item : fatura.getItens()) {
            if (item.getDescricao() != null && isFranquia(item.getDescricao())) {
                itensFranquia.add(item);
            }
        }
        
        if (itensFranquia.isEmpty()) {
            log.info("⏭️ Nenhum item de franquia encontrado na fatura");
            log.info("========================================");
            return fatura;
        }
        
        log.info("📋 Encontrados {} itens de franquia:", itensFranquia.size());
        for (FaturaItem item : itensFranquia) {
            log.info("   - {} (Qtd: {})", item.getDescricao(), item.getQuantidade());
        }
        log.info("");
        
        // 2. PROCESSAR CADA FRANQUIA (agrupando serviços)
        List<FaturaItem> todosServicosRemover = new ArrayList<>();
        List<FaturaItem> servicosComExcedente = new ArrayList<>();
        
        for (FaturaItem franquiaItem : itensFranquia) {
            processarFranquia(fatura, franquiaItem, todosServicosRemover, servicosComExcedente);
        }
        
        // 3. REMOVER TODOS OS SERVIÇOS MARCADOS
        for (FaturaItem item : todosServicosRemover) {
            if (fatura.getItens().contains(item)) {
                fatura.getItens().remove(item);
                log.info("🗑️ Serviço removido: {} (Qtd: {})", 
                        item.getDescricao(), item.getQuantidade());
            }
        }
        
        // 4. ADICIONAR SERVIÇOS COM EXCEDENTE (se houver)
        for (FaturaItem item : servicosComExcedente) {
            if (!fatura.getItens().contains(item)) {
                fatura.getItens().add(item);
                log.info("✅ Serviço com excedente adicionado: {} (Qtd: {})", 
                        item.getDescricao(), item.getQuantidade());
            }
        }
        
        // 5. REMOVER TODAS AS FRANQUIAS
        for (FaturaItem franquiaItem : itensFranquia) {
            if (fatura.getItens().contains(franquiaItem)) {
                fatura.getItens().remove(franquiaItem);
                log.info("🗑️ Franquia removida: {} (Qtd: {})", 
                        franquiaItem.getDescricao(), franquiaItem.getQuantidade());
            }
        }
        
        // Recalcular o valor total da fatura
        fatura.recalcularValor();
        log.info("💰 Novo valor total da fatura após franquias: R$ {}", fatura.getValorTotal());
        log.info("========================================");
        
        return fatura;
    }
    
    private boolean isFranquia(String descricao) {
        if (descricao == null) return false;
        String desc = descricao.toUpperCase();
        return desc.contains(PREFIXO_FRANQUIA) || 
               desc.contains("FRANQUIA CONSULTA") ||
               desc.contains("FRANQUIA");
    }
    
    /**
     * Processa uma única franquia - AGRUPANDO TODOS OS SERVIÇOS
     */
    private void processarFranquia(Fatura fatura, FaturaItem franquiaItem, 
            List<FaturaItem> todosServicosRemover, List<FaturaItem> servicosComExcedente) {
        
        log.info("========================================");
        log.info("📋 Processando franquia: {}", franquiaItem.getDescricao());
        
        // 1. Extrair a quantidade da franquia (LIMITE)
        BigDecimal quantidadeFranquiaBD = franquiaItem.getQuantidade() != null ? 
            franquiaItem.getQuantidade() : BigDecimal.ZERO;
        Integer limiteFranquia = quantidadeFranquiaBD.intValue();
        log.info("   📊 Limite da franquia: {}", limiteFranquia);
        
        if (limiteFranquia == 0) {
            log.warn("⚠️ Limite da franquia é zero, apenas a franquia será removida");
            return;
        }
        
        // 2. Extrair o nome base do serviço
        String nomeBase = extrairNomeBaseDaFranquia(franquiaItem.getDescricao());
        log.info("🔍 Nome base do serviço: '{}'", nomeBase);
        
        if (nomeBase == null || nomeBase.isEmpty()) {
            log.warn("⚠️ Não foi possível extrair o nome base da franquia");
            return;
        }
        
        // 3. 🔥 BUSCAR TODOS OS SERVIÇOS RELACIONADOS (TODOS OS MEIOS DE ACESSO)
        List<FaturaItem> servicosEncontrados = new ArrayList<>();
        
        for (FaturaItem item : fatura.getItens()) {
            if (item == franquiaItem || item.getDescricao() == null) continue;
            
            String descItem = item.getDescricao().toUpperCase();
            
            // Verificar se o item contém o nome base (ignorando meio de acesso)
            if (descItem.contains(nomeBase.toUpperCase())) {
                servicosEncontrados.add(item);
                log.info("   ✅ Serviço encontrado: {} (Qtd: {})", 
                        item.getDescricao(), item.getQuantidade());
            }
        }
        
        if (servicosEncontrados.isEmpty()) {
            log.warn("❌ Nenhum serviço relacionado encontrado para: {}", nomeBase);
            return;
        }
        
        // 4. 🔥 SOMAR QUANTIDADES DE TODOS OS SERVIÇOS
        int totalQuantidade = 0;
        for (FaturaItem item : servicosEncontrados) {
            totalQuantidade += item.getQuantidade().intValue();
        }
        
        log.info("📊 TOTAL DE SERVIÇOS ENCONTRADOS: {} serviços, soma={}", 
                servicosEncontrados.size(), totalQuantidade);
        log.info("📊 Comparação: Soma={} vs Franquia={}", totalQuantidade, limiteFranquia);
        
        // 5. 🔥 APLICAR REGRA BASEADA NA SOMA TOTAL
        if (totalQuantidade > limiteFranquia) {
            // Excedeu: manter apenas o excedente
            int excedente = totalQuantidade - limiteFranquia;
            log.info("✅ Franquia EXCEDIDA! Excedente: {}", excedente);
            
            // Marcar todos os serviços para remover
            todosServicosRemover.addAll(servicosEncontrados);
            
            // 🔥 Criar um novo item com o excedente (usando o primeiro serviço como base)
            if (excedente > 0 && !servicosEncontrados.isEmpty()) {
                FaturaItem primeiroItem = servicosEncontrados.get(0);
                FaturaItem novoItem = new FaturaItem();
                novoItem.setCodigoProduto(primeiroItem.getCodigoProduto());
                novoItem.setDescricao(primeiroItem.getDescricao());
                novoItem.setQuantidade(BigDecimal.valueOf(excedente));
                novoItem.setValorUnitario(primeiroItem.getValorUnitario());
                novoItem.setValorTotal(
                    novoItem.getQuantidade().multiply(novoItem.getValorUnitario())
                );
                novoItem.setTipoLancamento(primeiroItem.getTipoLancamento());
                novoItem.setFatura(fatura);
                
                servicosComExcedente.add(novoItem);
                log.info("   ✅ Novo item criado com excedente: {} (Qtd: {})", 
                        novoItem.getDescricao(), excedente);
            }
            
        } else {
            // Não excedeu: remover todos os serviços
            log.info("✅ Franquia NÃO excedida. Removendo todos os {} serviços", 
                    servicosEncontrados.size());
            todosServicosRemover.addAll(servicosEncontrados);
        }
        
        log.info("========================================");
    }
    
    /**
     * Extrai o nome base da franquia
     */
    private String extrairNomeBaseDaFranquia(String descricaoFranquia) {
        if (descricaoFranquia == null) return "";
        
        String desc = descricaoFranquia.toUpperCase();
        
        // Remover "FRANQUIA DE CONSULTA"
        desc = desc.replace(PREFIXO_FRANQUIA, "").trim();
        
        // Remover "DE" se estiver no início
        if (desc.startsWith("DE ")) {
            desc = desc.substring(3).trim();
        }
        
        // Remover "NOVO" se estiver no início
        if (desc.startsWith("NOVO ")) {
            desc = desc.substring(5).trim();
        }
        
        // Remover "1/1", "2/2", etc.
        desc = desc.replaceAll("\\d+/\\d+", "").trim();
        
        // Usar mapeamento específico se existir
        String mapeado = MAPEAMENTO_ESPECIFICO.get(desc);
        if (mapeado != null) {
            log.debug("   Mapeamento encontrado: '{}' -> '{}'", desc, mapeado);
            return mapeado;
        }
        
        log.debug("   Extraído: '{}' -> '{}'", descricaoFranquia, desc);
        return desc;
    }
}