package com.sga.rules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
public class FranquiaRuleOLD2 {

    private static final Logger log = LoggerFactory.getLogger(FranquiaRuleOLD2.class);

    private static final String PREFIXO_FRANQUIA = "FRANQUIA DE CONSULTA";
    private static final String PREFIXO_PLANO = "PLANO";
    private static final String PREFIXO_ASSINATURA = "ASSINATURA";
    private static final String PREFIXO_MENSALIDADE = "MENSALIDADE";

    // ================================================================
    // MAPEAMENTO ESPECÍFICO DE FRANQUIA PARA SERVIÇO
    // ================================================================

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

    // ================================================================
    // 🔥 MAPEAMENTO DE SINÔNIMOS
    // ================================================================

    private static final Map<String, List<String>> MAPEAMENTO_SINONIMOS = new HashMap<>();
    static {
        // SPC RELATORIO COMPLETO ↔ SPC RELATORIO (são equivalentes)
        MAPEAMENTO_SINONIMOS.put("SPC RELATORIO COMPLETO", Arrays.asList(
            "SPC RELATORIO",
            "SPC RELATORIO COMPLETO"
        ));

        MAPEAMENTO_SINONIMOS.put("SPC RELATORIO", Arrays.asList(
            "SPC RELATORIO",
            "SPC RELATORIO COMPLETO"
        ));

        // SPC MIX
        MAPEAMENTO_SINONIMOS.put("SPC MIX", Arrays.asList(
            "SPC MIX",
            "SPC MIX (SPC + CHEQUE)"
        ));

        MAPEAMENTO_SINONIMOS.put("SPC MIX (SPC + CHEQUE)", Arrays.asList(
            "SPC MIX",
            "SPC MIX (SPC + CHEQUE)"
        ));

        // SPC MAX / SPC MAXI
        MAPEAMENTO_SINONIMOS.put("SPC MAX", Arrays.asList(
            "SPC MAX",
            "SPC MAXI"
        ));

        MAPEAMENTO_SINONIMOS.put("SPC MAXI", Arrays.asList(
            "SPC MAX",
            "SPC MAXI"
        ));

        // NOVO SPC MAXI
        MAPEAMENTO_SINONIMOS.put("NOVO SPC MAXI", Arrays.asList(
            "NOVO SPC MAXI",
            "NOVO SPC MAXI 1/1"
        ));

        MAPEAMENTO_SINONIMOS.put("NOVO SPC MAXI 1/1", Arrays.asList(
            "NOVO SPC MAXI",
            "NOVO SPC MAXI 1/1"
        ));
    }

    // ================================================================
    // 🔥 MAPEAMENTO DE CORREÇÕES POR ASSOCIADO + PRODUTO
    // ================================================================

    /**
     * Representa uma correção de produto
     */
    public static class CorrecaoProduto {
        private final String codigoRMCorreto;
        private final String descricaoCorreta;
        private final String nomeAmigavel;

        public CorrecaoProduto(String codigoRMCorreto, String descricaoCorreta, String nomeAmigavel) {
            this.codigoRMCorreto = codigoRMCorreto;
            this.descricaoCorreta = descricaoCorreta;
            this.nomeAmigavel = nomeAmigavel;
        }

        public String getCodigoRMCorreto() { return codigoRMCorreto; }
        public String getDescricaoCorreta() { return descricaoCorreta; }
        public String getNomeAmigavel() { return nomeAmigavel; }
    }

    /**
     * Mapeamento de correções específicas por associado
     * Chave: codigoSPC + "|" + descricaoErrada
     * Valor: CorrecaoProduto com dados corretos
     */
    private static final Map<String, CorrecaoProduto> MAPEAMENTO_CORRECOES = new HashMap<>();

    static {
        // 🔥 Caso 1: PRIMOS MOTOPECAS DISTRIBUIDORA LTDA (SPC: 22885)
        // FRANQUIA DE CONSULTA NOVO SPC MAXI 1/1 → PLANO 50 NOVO SPC MAXI 1/1
        MAPEAMENTO_CORRECOES.put("22885|FRANQUIA DE CONSULTA NOVO SPC MAXI 1/1",
            new CorrecaoProduto(
                "04.01.03.94404",                 // Código RM correto
                "PLANO 50 NOVO SPC MAXI 1/1",     // Descrição correta
                "Plano 50 NOVO SPC MAXI"          // Nome amigável
            ));

        // 🔥 Caso 2: VOX COMERCIAL ATACADISTA DE PECAS LTDA (SPC: 6090)
        // FRANQUIA DE CONSULTA SPC RELATORIO 1/1 → PLANO 20 SPC RELATORIO 1/1
        MAPEAMENTO_CORRECOES.put("6090|FRANQUIA DE CONSULTA SPC RELATORIO 1/1",
            new CorrecaoProduto(
                "04.01.03.69847",                 // Código RM correto
                "PLANO 20 SPC RELATORIO 1/1",     // Descrição correta
                "Plano 20 SPC RELATORIO"          // Nome amigável
            ));

        // 🔥 Caso 3: TERMOKLIMA NORTE COMERCIO DE PECAS LTDA (SPC: 22662)
        // FRANQUIA DE CONSULTA SPC RELATORIO COMPLETO 1/1 → PLANO 20 SPC RELATORIO COMPLETO 1/1
        MAPEAMENTO_CORRECOES.put("22662|FRANQUIA DE CONSULTA SPC RELATORIO COMPLETO 1/1",
            new CorrecaoProduto(
                "04.01.03.94397",                 // Código RM correto
                "PLANO 20 SPC RELATORIO COMPLETO 1/1", // Descrição correta
                "Plano 20 SPC RELATORIO COMPLETO" // Nome amigável
            ));

        // 🔥 Adicionar mais casos conforme necessário
        // MAPEAMENTO_CORRECOES.put("CODIGO_SPC|DESCRICAO_ERRADA",
        //     new CorrecaoProduto("CODIGO_RM_CORRETO", "DESCRICAO_CORRETA", "NOME_AMIGAVEL"));
    }

    // ================================================================
    // MÉTODOS AUXILIARES DE CORREÇÃO
    // ================================================================

    /**
     * 🔥 Obtém a correção para um item específico de um associado
     * Retorna null se não houver correção
     */
    private CorrecaoProduto obterCorrecaoProduto(Associado associado, FaturaItem item) {
        if (associado == null || item == null || item.getDescricao() == null) {
            return null;
        }

        String codigoSPC = associado.getCodigoSpc();
        if (codigoSPC == null || codigoSPC.isEmpty()) {
            return null;
        }

        String chave = codigoSPC + "|" + item.getDescricao();
        return MAPEAMENTO_CORRECOES.get(chave);
    }

    /**
     * 🔥 Verifica se o item é um plano disfarçado (deve ser preservado)
     */
    private boolean isPlanoDisfarcado(FaturaItem item) {
        if (item == null || item.getDescricao() == null) return false;
        String desc = item.getDescricao().toUpperCase();
        BigDecimal valorUnitario = item.getValorUnitario();

        // Critério 1: Franquia com "1/1" + valor alto (> R$ 100)
        if (desc.contains("FRANQUIA") && desc.contains("1/1")) {
            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.valueOf(100)) > 0) {
                log.info("   🔍 Item identificado como plano disfarçado: {} (Valor: R$ {})",
                        item.getDescricao(), valorUnitario);
                return true;
            }
        }

        // Critério 2: Franquia com "NOVO" + "1/1"
        if (desc.contains("FRANQUIA") && desc.contains("NOVO") && desc.contains("1/1")) {
            log.info("   🔍 Item identificado como plano disfarçado (NOVO + 1/1): {}",
                    item.getDescricao());
            return true;
        }

        // Critério 3: Franquia com "MAX/MAXI" + "1/1" + valor > R$ 50
        if (desc.contains("FRANQUIA") && (desc.contains("MAX") || desc.contains("MAXI")) && desc.contains("1/1")) {
            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.valueOf(50)) > 0) {
                log.info("   🔍 Item identificado como plano disfarçado (MAX/MAXI + 1/1): {}",
                        item.getDescricao());
                return true;
            }
        }

        return false;
    }

    // ================================================================
    // MÉTODOS PRINCIPAIS
    // ================================================================

    public Fatura aplicarRegraFranquia(Fatura fatura, Associado associado) {
        if (fatura == null || associado == null) {
            log.warn("Fatura ou associado nulo, ignorando regra de franquia");
            return fatura;
        }

        log.info("========================================");
        log.info("📊 APLICANDO REGRA DE FRANQUIA");
        log.info("========================================");
        log.info("📄 Associado: {} (SPC: {})",
                associado.getNomeRazao(), associado.getCodigoSpc());
        log.info("📄 Total de itens na fatura: {}", fatura.getItens().size());

        // ================================================================
        // 🔥 PASSO 1: CORRIGIR ITENS QUE SÃO PLANOS DISFARÇADOS
        // ================================================================

        List<FaturaItem> itensParaCorrigir = new ArrayList<>();

        for (FaturaItem item : fatura.getItens()) {
            CorrecaoProduto correcao = obterCorrecaoProduto(associado, item);
            if (correcao != null) {
                itensParaCorrigir.add(item);
                log.warn("⚠️ CORREÇÃO DE PRODUTO DETECTADA!");
                log.warn("   📌 Associado: {} (SPC: {})", associado.getNomeRazao(), associado.getCodigoSpc());
                log.warn("   🔴 Descrição errada: {}", item.getDescricao());
                log.warn("   ✅ Descrição correta: {}", correcao.getDescricaoCorreta());
                log.warn("   🔑 Código RM correto: {}", correcao.getCodigoRMCorreto());
                log.warn("   💰 Valor preservado: R$ {}", item.getValorUnitario());
                log.warn("   📦 Quantidade preservada: {}", item.getQuantidade());
            }
        }

        // Aplicar correções
        for (FaturaItem item : itensParaCorrigir) {
            CorrecaoProduto correcao = obterCorrecaoProduto(associado, item);
            if (correcao != null) {
                log.info("   ✅ Aplicando correção...");
                item.setCodigoProduto(correcao.getCodigoRMCorreto());
                item.setDescricao(correcao.getDescricaoCorreta());
                log.info("   ✅ Produto corrigido: {}", item.getDescricao());
            }
        }

        // ================================================================
        // PASSO 2: IDENTIFICAR TODOS OS ITENS DE FRANQUIA (após correções)
        // ================================================================

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
            log.info("   - {} (Qtd: {}, Valor: R$ {})",
                    item.getDescricao(),
                    item.getQuantidade(),
                    item.getValorUnitario());
        }
        log.info("");

        // ================================================================
        // PASSO 3: PROCESSAR CADA FRANQUIA
        // ================================================================

        List<FaturaItem> todosServicosRemover = new ArrayList<>();
        List<FaturaItem> servicosComExcedente = new ArrayList<>();

        for (FaturaItem franquiaItem : itensFranquia) {
            // 🔥 Verificar se é um plano disfarçado (preservar)
            if (isPlanoDisfarcado(franquiaItem)) {
                log.warn("⚠️ Item preservado (plano disfarçado): {}", franquiaItem.getDescricao());
                continue;
            }

            processarFranquia(fatura, franquiaItem, todosServicosRemover, servicosComExcedente);
        }

        // ================================================================
        // PASSO 4: REMOVER SERVIÇOS
        // ================================================================

        for (FaturaItem item : todosServicosRemover) {
            if (fatura.getItens().contains(item)) {
                fatura.getItens().remove(item);
                log.info("🗑️ Serviço removido: {} (Qtd: {})",
                        item.getDescricao(), item.getQuantidade());
            }
        }

        // ================================================================
        // PASSO 5: ADICIONAR SERVIÇOS COM EXCEDENTE
        // ================================================================

        for (FaturaItem item : servicosComExcedente) {
            if (!fatura.getItens().contains(item)) {
                fatura.getItens().add(item);
                log.info("✅ Serviço com excedente adicionado: {} (Qtd: {})",
                        item.getDescricao(), item.getQuantidade());
            }
        }

        // ================================================================
        // PASSO 6: REMOVER FRANQUIAS
        // ================================================================

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

    // ================================================================
    // MÉTODOS AUXILIARES
    // ================================================================

    private boolean isFranquia(String descricao) {
        if (descricao == null) return false;
        String desc = descricao.toUpperCase();
        return desc.contains(PREFIXO_FRANQUIA) ||
               desc.contains("FRANQUIA CONSULTA") ||
               desc.contains("FRANQUIA");
    }

    private boolean isPlanoItem(FaturaItem item) {
        if (item == null || item.getDescricao() == null) return false;
        String desc = item.getDescricao().toUpperCase();

        // ✅ 1. Verificação padrão
        if (desc.contains(PREFIXO_PLANO) ||
            desc.contains(PREFIXO_ASSINATURA) ||
            desc.contains(PREFIXO_MENSALIDADE) ||
            desc.matches(".*PLANO\\s+\\d+.*") ||
            desc.matches(".*PLANO\\s+[A-Z].*")) {
            return true;
        }

        // 🔥 2. Plano disfarçado de franquia (validação extra)
        if (desc.contains("FRANQUIA") && desc.contains("1/1")) {
            BigDecimal valorUnitario = item.getValorUnitario();
            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.valueOf(100)) > 0) {
                log.info("   🔍 Item identificado como plano disfarçado: {} (Valor: R$ {})",
                        item.getDescricao(), valorUnitario);
                return true;
            }
        }

        return false;
    }

    private List<String> obterSinonimos(String nomeBase) {
        for (Map.Entry<String, List<String>> entry : MAPEAMENTO_SINONIMOS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(nomeBase)) {
                log.debug("   Sinônimos encontrados para '{}': {}", nomeBase, entry.getValue());
                return entry.getValue();
            }
        }
        return Arrays.asList(nomeBase);
    }

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

        // 3. BUSCAR TODOS OS SERVIÇOS RELACIONADOS (USANDO SINÔNIMOS)
        List<FaturaItem> servicosEncontrados = new ArrayList<>();
        List<FaturaItem> planosIgnorados = new ArrayList<>();

        List<String> sinonimos = obterSinonimos(nomeBase);
        log.info("🔍 Buscando por sinônimos: {}", sinonimos);

        for (FaturaItem item : fatura.getItens()) {
            if (item == franquiaItem || item.getDescricao() == null) continue;

            // Ignorar itens que são planos (incluindo disfarçados)
            if (isPlanoItem(item)) {
                planosIgnorados.add(item);
                log.info("   ⏭️ Ignorando item do plano: {} (Qtd: {})",
                        item.getDescricao(), item.getQuantidade());
                continue;
            }

            String descItem = item.getDescricao().toUpperCase();

            // Verificar se a descrição do item contém algum dos sinônimos
            boolean encontrado = false;
            for (String sinonimo : sinonimos) {
                if (descItem.contains(sinonimo.toUpperCase())) {
                    encontrado = true;
                    break;
                }
            }

            if (encontrado) {
                servicosEncontrados.add(item);
                log.info("   ✅ Serviço encontrado: {} (Qtd: {})",
                        item.getDescricao(), item.getQuantidade());
            }
        }

        if (!planosIgnorados.isEmpty()) {
            log.info("📌 Total de planos ignorados: {}", planosIgnorados.size());
        }

        if (servicosEncontrados.isEmpty()) {
            log.warn("❌ Nenhum serviço relacionado encontrado para: {} (sinônimos: {})",
                    nomeBase, sinonimos);
            return;
        }

        // 4. SOMAR QUANTIDADES DE TODOS OS SERVIÇOS
        int totalQuantidade = 0;
        for (FaturaItem item : servicosEncontrados) {
            totalQuantidade += item.getQuantidade().intValue();
        }

        log.info("📊 TOTAL DE SERVIÇOS ENCONTRADOS: {} serviços, soma={}",
                servicosEncontrados.size(), totalQuantidade);
        log.info("📊 Comparação: Soma={} vs Franquia={}", totalQuantidade, limiteFranquia);

        // 5. APLICAR REGRA BASEADA NA SOMA TOTAL
        if (totalQuantidade > limiteFranquia) {
            int excedente = totalQuantidade - limiteFranquia;
            log.info("✅ Franquia EXCEDIDA! Excedente: {}", excedente);

            todosServicosRemover.addAll(servicosEncontrados);

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
            log.info("✅ Franquia NÃO excedida. Removendo todos os {} serviços",
                    servicosEncontrados.size());
            todosServicosRemover.addAll(servicosEncontrados);
        }

        log.info("========================================");
    }

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