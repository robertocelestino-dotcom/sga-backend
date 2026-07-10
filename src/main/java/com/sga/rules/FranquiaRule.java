package com.sga.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private static final java.util.Map<String, String> MAPEAMENTO_ESPECIFICO = new java.util.HashMap<>();
    static {
        // Mapeamentos explícitos para evitar falsos positivos
        MAPEAMENTO_ESPECIFICO.put("SPC MIX", "SPC MIX (SPC + CHEQUE) . INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX PLUS", "SPC MIX PLUS INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC MAX", "SPC MAX INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC MAXI", "SPC MAXI INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC PLUS", "SPC PLUS INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC RELATORIO", "SPC RELATORIO PJ INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC RELATORIO COMPLETO", "SPC RELATORIO COMPLETO INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX POSITIVO FOR", "SPC MIX POSITIVO FOR INTERNET");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX POSITIVO", "SPC MIX POSITIVO INTERNET");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MIX MAIS", "SPC MIX MAIS INTERNET");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MAXI", "SPC MAXI INTERNET");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MAXI 1/1", "SPC MAXI INTERNET");
        MAPEAMENTO_ESPECIFICO.put("CHEQUE", "CHEQUE INTERNET");
    }
    
    /**
     * Aplica a regra de franquia na fatura
     * Processa TODAS as franquias encontradas
     */
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
        
        // 2. PROCESSAR CADA FRANQUIA
        List<FaturaItem> itensParaRemover = new ArrayList<>();
        
        for (FaturaItem franquiaItem : itensFranquia) {
            Optional<FaturaItem> servicoRemover = processarFranquia(fatura, franquiaItem);
            servicoRemover.ifPresent(itensParaRemover::add);
        }
        
        // 3. REMOVER OS ITENS DE SERVIÇO (se houver)
        for (FaturaItem item : itensParaRemover) {
            if (fatura.getItens().contains(item)) {
                fatura.getItens().remove(item);
                log.info("🗑️ Serviço removido: {} (Qtd: {})", item.getDescricao(), item.getQuantidade());
            }
        }
        
        // 4. REMOVER TODAS AS FRANQUIAS
        for (FaturaItem franquiaItem : itensFranquia) {
            if (fatura.getItens().contains(franquiaItem)) {
                fatura.getItens().remove(franquiaItem);
                log.info("🗑️ Franquia removida: {} (Qtd: {})", franquiaItem.getDescricao(), franquiaItem.getQuantidade());
            }
        }
        
        // Recalcular o valor total da fatura
        fatura.recalcularValor();
        log.info("💰 Novo valor total da fatura após franquias: R$ {}", fatura.getValorTotal());
        log.info("========================================");
        
        return fatura;
    }
    
    /**
     * Verifica se a descrição é uma franquia
     */
    private boolean isFranquia(String descricao) {
        if (descricao == null) return false;
        String desc = descricao.toUpperCase();
        return desc.contains(PREFIXO_FRANQUIA) || 
               desc.contains("FRANQUIA CONSULTA") ||
               desc.contains("FRANQUIA");
    }
    
    /**
     * Processa uma única franquia
     * @return Optional contendo o serviço a ser removido, ou vazio se não houver
     */
    private Optional<FaturaItem> processarFranquia(Fatura fatura, FaturaItem franquiaItem) {
        log.info("========================================");
        log.info("📋 Processando franquia: {}", franquiaItem.getDescricao());
        
        // 1. Extrair a quantidade da franquia (LIMITE)
        BigDecimal quantidadeFranquiaBD = franquiaItem.getQuantidade() != null ? 
            franquiaItem.getQuantidade() : BigDecimal.ZERO;
        Integer limiteFranquia = quantidadeFranquiaBD.intValue();
        log.info("   📊 Limite da franquia: {}", limiteFranquia);
        
        if (limiteFranquia == 0) {
            log.warn("⚠️ Limite da franquia é zero, apenas a franquia será removida");
            return Optional.empty();
        }
        
        // 2. Extrair o nome do serviço vinculado
        String nomeServico = extrairNomeServicoDaFranquia(franquiaItem.getDescricao());
        log.info("🔍 Nome do serviço extraído: '{}'", nomeServico);
        
        if (nomeServico == null || nomeServico.isEmpty()) {
            log.warn("⚠️ Não foi possível extrair o nome do serviço da franquia");
            return Optional.empty();
        }
        
        // 3. Buscar o serviço relacionado na fatura (usando mapeamento específico)
        Optional<FaturaItem> servicoItemOpt = buscarServicoPorFranquia(fatura, nomeServico, franquiaItem);
        
        if (servicoItemOpt.isEmpty()) {
            log.warn("❌ Serviço relacionado NÃO encontrado para: {}", nomeServico);
            log.info("   ℹ️ Apenas a franquia será removida");
            return Optional.empty();
        }
        
        FaturaItem servicoItem = servicoItemOpt.get();
        
        // 4. Obter quantidade do serviço
        BigDecimal quantidadeServicoBD = servicoItem.getQuantidade() != null ? 
            servicoItem.getQuantidade() : BigDecimal.ZERO;
        Integer quantidadeServico = quantidadeServicoBD.intValue();
        
        log.info("📋 Serviço relacionado encontrado: {}", servicoItem.getDescricao());
        log.info("   Quantidade do serviço: {}", quantidadeServico);
        log.info("📊 Comparação: Serviço={} vs Franquia={}", quantidadeServico, limiteFranquia);
        
        // 5. Aplicar regra
        if (quantidadeServico > limiteFranquia) {
            // Excedeu: manter apenas o excedente do serviço
            Integer excedente = quantidadeServico - limiteFranquia;
            servicoItem.setQuantidade(BigDecimal.valueOf(excedente));
            servicoItem.setValorTotal(
                servicoItem.getQuantidade().multiply(servicoItem.getValorUnitario())
            );
            log.info("✅ Franquia EXCEDIDA!");
            log.info("   Limite: {}, Consumo: {}, Excedente: {}", 
                limiteFranquia, quantidadeServico, excedente);
            log.info("   Serviço atualizado: Quantidade={}, Valor=R$ {}", 
                excedente, servicoItem.getValorTotal());
            return Optional.empty(); // Não remove o serviço
        } else {
            // Não excedeu: remover o serviço
            log.info("✅ Franquia NÃO excedida. Serviço será removido");
            return Optional.of(servicoItem);
        }
    }
    
    /**
     * Busca o serviço relacionado usando mapeamento específico
     */
    private Optional<FaturaItem> buscarServicoPorFranquia(Fatura fatura, String nomeServico, FaturaItem franquiaItem) {
        String nomeBusca = nomeServico.toUpperCase().trim();
        
        log.info("   🔍 Buscando serviço específico para: '{}'", nomeBusca);
        
        // 1. Tentar mapeamento específico (mais confiável)
        String servicoEsperado = MAPEAMENTO_ESPECIFICO.get(nomeBusca);
        if (servicoEsperado != null) {
            log.info("   📌 Mapeamento encontrado: '{}' -> '{}'", nomeBusca, servicoEsperado);
            
            // Buscar pelo nome mapeado (exato ou contém)
            Optional<FaturaItem> itemOpt = fatura.getItens().stream()
                .filter(item -> item != franquiaItem)
                .filter(item -> item.getDescricao() != null)
                .filter(item -> item.getDescricao().toUpperCase().contains(servicoEsperado.toUpperCase()))
                .findFirst();
            
            if (itemOpt.isPresent()) {
                log.info("   ✅ Serviço encontrado pelo mapeamento: {}", itemOpt.get().getDescricao());
                return itemOpt;
            }
        }
        
        // 2. Busca por contém (menos agressiva)
        // 🔥 IMPORTANTE: Buscar APENAS o nome exato do serviço, sem palavras adicionais
        log.info("   🔍 Buscando por nome exato: '{}'", nomeBusca);
        
        Optional<FaturaItem> itemOpt = fatura.getItens().stream()
            .filter(item -> item != franquiaItem)
            .filter(item -> item.getDescricao() != null)
            .filter(item -> {
                String desc = item.getDescricao().toUpperCase();
                // Verifica se a descrição contém o nome do serviço
                // E NÃO contém palavras que indicam outro serviço
                boolean contem = desc.contains(nomeBusca);
                
                // Se contém, verificar se não é um falso positivo
                if (contem) {
                    // Lista de palavras que indicam falso positivo
                    String[] falsosPositivos = {"MAIS", "POSITIVO", "PLUS", "MAX", "MAXI"};
                    for (String fp : falsosPositivos) {
                        if (desc.contains(fp) && !nomeBusca.contains(fp)) {
                            // Se a descrição contém uma palavra que não está no nomeBusca, pode ser falso positivo
                            // Mas só rejeita se a diferença for significativa
                            log.info("   ⚠️ Possível falso positivo: '{}' contém '{}' mas também '{}'", 
                                item.getDescricao(), nomeBusca, fp);
                            // Ainda assim, considerar se o nomeBusca for muito específico
                            if (nomeBusca.length() > 10) {
                                return true; // Nome específico, mantém
                            }
                            return false; // Nome curto, pode ser falso positivo
                        }
                    }
                    return true;
                }
                return false;
            })
            .findFirst();
        
        if (itemOpt.isPresent()) {
            log.info("   ✅ Serviço encontrado: {}", itemOpt.get().getDescricao());
            return itemOpt;
        }
        
        // 3. Fallback: buscar por palavras-chave específicas (apenas as primeiras 2 palavras)
        String[] palavras = nomeBusca.split(" ");
        if (palavras.length >= 2) {
            String buscaReduzida = palavras[0] + " " + palavras[1];
            log.info("   🔍 Buscando por palavras-chave reduzidas: '{}'", buscaReduzida);
            
            itemOpt = fatura.getItens().stream()
                .filter(item -> item != franquiaItem)
                .filter(item -> item.getDescricao() != null)
                .filter(item -> {
                    String desc = item.getDescricao().toUpperCase();
                    return desc.contains(buscaReduzida);
                })
                .findFirst();
            
            if (itemOpt.isPresent()) {
                log.info("   ✅ Serviço encontrado por palavras-chave: {}", itemOpt.get().getDescricao());
                return itemOpt;
            }
        }
        
        log.info("   ❌ Nenhum serviço encontrado");
        return Optional.empty();
    }
    
    /**
     * Extrai o nome do serviço a partir da descrição da franquia
     */
    private String extrairNomeServicoDaFranquia(String descricaoFranquia) {
        if (descricaoFranquia == null) return "";
        
        String desc = descricaoFranquia.toUpperCase();
        
        // Remover "FRANQUIA DE CONSULTA" ou variações
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
        
        log.debug("   Extraído: '{}' -> '{}'", descricaoFranquia, desc);
        
        return desc;
    }
}