package com.sga.rules;

import java.math.BigDecimal;
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
    
    /**
     * Aplica a regra de franquia na fatura
     * 
     * Regras:
     * 1. Identificar item da franquia (contém "FRANQUIA DE CONSULTA")
     * 2. Extrair a quantidade da franquia do próprio item - este é o LIMITE
     * 3. Extrair o nome do serviço vinculado (removendo o prefixo "FRANQUIA DE CONSULTA")
     * 4. Buscar o item de serviço correspondente na fatura (EXCLUINDO o próprio item da franquia)
     * 5. SEMPRE remover o item da franquia
     * 6. Se consumo > franquia: atualizar serviço com excedente
     * 7. Se consumo <= franquia: remover o serviço também
     */
    public Fatura aplicarRegraFranquia(Fatura fatura, Associado associado) {
        if (fatura == null || associado == null) {
            log.warn("Fatura ou associado nulo, ignorando regra de franquia");
            return fatura;
        }
        
        log.info("📊 Aplicando regra de franquia para associado: {}", associado.getNomeRazao());
        
        // 1. Identificar item da franquia
        Optional<FaturaItem> franquiaItemOpt = fatura.getItens().stream()
            .filter(item -> item.getDescricao() != null && 
                   item.getDescricao().toUpperCase().contains(PREFIXO_FRANQUIA))
            .findFirst();
        
        if (franquiaItemOpt.isEmpty()) {
            log.info("Nenhum item de franquia encontrado na fatura");
            return fatura;
        }
        
        FaturaItem franquiaItem = franquiaItemOpt.get();
        
        // 2. Extrair a quantidade da franquia (LIMITE)
        BigDecimal quantidadeFranquiaBD = franquiaItem.getQuantidade() != null ? 
            franquiaItem.getQuantidade() : BigDecimal.ZERO;
        Integer limiteFranquia = quantidadeFranquiaBD.intValue();
        
        log.info("📋 Item de franquia encontrado: {}", franquiaItem.getDescricao());
        log.info("   Limite da franquia: {}", limiteFranquia);
        
        // 3. SEMPRE remover o item da franquia
        fatura.getItens().remove(franquiaItem);
        log.info("🗑️ Item de franquia removido (sempre removido)");
        
        if (limiteFranquia == 0) {
            log.warn("Limite da franquia é zero, apenas a franquia foi removida");
            fatura.recalcularValor();
            return fatura;
        }
        
        // 4. Extrair o nome do serviço vinculado (remover o prefixo)
        String nomeServico = extrairNomeServicoDaFranquia(franquiaItem.getDescricao());
        log.info("🔍 Nome do serviço a procurar: '{}'", nomeServico);
        
        if (nomeServico == null || nomeServico.isEmpty()) {
            log.warn("Não foi possível extrair o nome do serviço da franquia");
            fatura.recalcularValor();
            return fatura;
        }
        
        // 5. Buscar o item de serviço relacionado na fatura
        Optional<FaturaItem> servicoItemOpt = fatura.getItens().stream()
            .filter(item -> item.getDescricao() != null && 
                   (item.getDescricao().toUpperCase().contains(nomeServico.toUpperCase()) ||
                    nomeServico.toUpperCase().contains(item.getDescricao().toUpperCase())))
            .findFirst();
        
        if (servicoItemOpt.isEmpty()) {
            log.warn("Serviço relacionado não encontrado para: {}", nomeServico);
            log.info("Itens restantes na fatura:");
            for (FaturaItem item : fatura.getItens()) {
                log.info("  - {}", item.getDescricao());
            }
            fatura.recalcularValor();
            return fatura;
        }
        
        FaturaItem servicoItem = servicoItemOpt.get();
        
        // 6. Obter quantidade do serviço
        BigDecimal quantidadeServicoBD = servicoItem.getQuantidade() != null ? 
            servicoItem.getQuantidade() : BigDecimal.ZERO;
        Integer quantidadeServico = quantidadeServicoBD.intValue();
        
        log.info("📋 Serviço relacionado encontrado: {}", servicoItem.getDescricao());
        log.info("   Quantidade do serviço: {}", quantidadeServico);
        log.info("📊 Comparação: Serviço={} vs Franquia={}", quantidadeServico, limiteFranquia);
        
        // 7. Aplicar regra
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
        } else {
            // Não excedeu: remover o serviço também
            fatura.getItens().remove(servicoItem);
            log.info("✅ Franquia NÃO excedida. Serviço removido: {}", nomeServico);
        }
        
        // Recalcular o valor total da fatura
        fatura.recalcularValor();
        log.info("💰 Novo valor total da fatura após franquia: R$ {}", fatura.getValorTotal());
        
        return fatura;
    }
    
    /**
     * Extrai o nome do serviço a partir da descrição da franquia
     * Remove o prefixo "FRANQUIA DE CONSULTA" e retorna o restante
     * Ex: "FRANQUIA DE CONSULTA SPC MIX POSITIVO FOR" -> "SPC MIX POSITIVO FOR"
     */
    private String extrairNomeServicoDaFranquia(String descricaoFranquia) {
        if (descricaoFranquia == null) return "";
        
        String nome = descricaoFranquia
            .toUpperCase()
            .replace(PREFIXO_FRANQUIA, "")
            .trim();
        
        log.debug("Nome do serviço extraído: '{}' -> '{}'", descricaoFranquia, nome);
        
        return nome;
    }
}