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
public class FranquiaRule_OLD3 {

    private static final Logger log = LoggerFactory.getLogger(FranquiaRule_OLD3.class);

    // ================================================================
    // CONSTANTES
    // ================================================================

    private static final String PREFIXO_FRANQUIA = "FRANQUIA DE CONSULTA";
    private static final String PREFIXO_PLANO = "PLANO";
    private static final String PREFIXO_ASSINATURA = "ASSINATURA";
    private static final String PREFIXO_MENSALIDADE = "MENSALIDADE";
    private static final String PREFIXO_CONTRIBUICAO = "CONTRIBUICAO ASSOCIATIVA";

    // ================================================================
    // MAPEAMENTO ESPECÍFICO DE FRANQUIA PARA NOME BASE
    // ================================================================

    private static final Map<String, String> MAPEAMENTO_ESPECIFICO = new HashMap<>();
    static {
        // SPC MIX
        MAPEAMENTO_ESPECIFICO.put("SPC MIX", "SPC MIX");
        MAPEAMENTO_ESPECIFICO.put("SPC MIX (SPC + CHEQUE)", "SPC MIX");

        // SPC MIX POSITIVO FOR (separado!)
        MAPEAMENTO_ESPECIFICO.put("SPC MIX POSITIVO FOR", "SPC MIX POSITIVO FOR");

        // SPC MIX POSITIVO (separado!)
        MAPEAMENTO_ESPECIFICO.put("SPC MIX POSITIVO", "SPC MIX POSITIVO");

        // SPC MIX PLUS
        MAPEAMENTO_ESPECIFICO.put("SPC MIX PLUS", "SPC MIX PLUS");

        // SPC MAX / MAXI
        MAPEAMENTO_ESPECIFICO.put("SPC MAX", "SPC MAX");
        MAPEAMENTO_ESPECIFICO.put("SPC MAXI", "SPC MAX");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MAXI", "NOVO SPC MAXI");

        // SPC PLUS
        MAPEAMENTO_ESPECIFICO.put("SPC PLUS", "SPC PLUS");

        // SPC RELATORIO
        MAPEAMENTO_ESPECIFICO.put("SPC RELATORIO", "SPC RELATORIO");
        MAPEAMENTO_ESPECIFICO.put("SPC RELATORIO COMPLETO", "SPC RELATORIO COMPLETO");

        // SPC MIX MAIS
        MAPEAMENTO_ESPECIFICO.put("SPC MIX MAIS", "SPC MIX MAIS");
        MAPEAMENTO_ESPECIFICO.put("NOVO SPC MIX MAIS", "SPC MIX MAIS");

        // CHEQUE
        MAPEAMENTO_ESPECIFICO.put("CHEQUE", "CHEQUE");
    }

    // ================================================================
    // MAPEAMENTO DE SINÔNIMOS (CORRESPONDÊNCIA EXATA)
    // ================================================================

    private static final Map<String, List<String>> MAPEAMENTO_SINONIMOS = new HashMap<>();
    static {
        // ===== SPC MIX (apenas SPC MIX) =====
        MAPEAMENTO_SINONIMOS.put("SPC MIX", Arrays.asList(
            "SPC MIX",
            "SPC MIX (SPC + CHEQUE)"
        ));

        // ===== SPC MIX POSITIVO FOR (separado!) =====
        MAPEAMENTO_SINONIMOS.put("SPC MIX POSITIVO FOR", Arrays.asList(
            "SPC MIX POSITIVO FOR"
        ));

        // ===== SPC MIX POSITIVO (separado!) =====
        MAPEAMENTO_SINONIMOS.put("SPC MIX POSITIVO", Arrays.asList(
            "SPC MIX POSITIVO"
        ));

        // ===== SPC MIX PLUS =====
        MAPEAMENTO_SINONIMOS.put("SPC MIX PLUS", Arrays.asList(
            "SPC MIX PLUS"
        ));

        // ===== SPC MAX =====
        MAPEAMENTO_SINONIMOS.put("SPC MAX", Arrays.asList(
            "SPC MAX",
            "SPC MAXI"
        ));

        // ===== NOVO SPC MAXI (separado!) =====
        MAPEAMENTO_SINONIMOS.put("NOVO SPC MAXI", Arrays.asList(
            "NOVO SPC MAXI"
        ));

        // ===== SPC PLUS =====
        MAPEAMENTO_SINONIMOS.put("SPC PLUS", Arrays.asList(
            "SPC PLUS"
        ));

        // ===== SPC RELATORIO =====
        MAPEAMENTO_SINONIMOS.put("SPC RELATORIO", Arrays.asList(
            "SPC RELATORIO",
            "SPC RELATORIO COMPLETO"
        ));

        // ===== SPC RELATORIO COMPLETO =====
        MAPEAMENTO_SINONIMOS.put("SPC RELATORIO COMPLETO", Arrays.asList(
            "SPC RELATORIO COMPLETO",
        	"SPC RELATORIO PJ",
        	"SPC RELATORIO PF",
        	"SPC RELATORIO",   
        	"SPC RELATORIO PJ INTERNET",
        	"SPC RELATORIO PF INTERNET"
        ));

        // ===== SPC MIX MAIS =====
        MAPEAMENTO_SINONIMOS.put("SPC MIX MAIS", Arrays.asList(
            "SPC MIX MAIS",
            "NOVO SPC MIX MAIS"
        ));

        // ===== CHEQUE =====
        MAPEAMENTO_SINONIMOS.put("CHEQUE", Arrays.asList(
            "CHEQUE",
            "SPCHEQUE ANALITICA",
            "SPCHEQUE SINTETICA"
        ));
    }

    // ================================================================
    // MAPEAMENTO DE CORREÇÕES POR ASSOCIADO + PRODUTO
    // ================================================================

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

    private static final Map<String, CorrecaoProduto> MAPEAMENTO_CORRECOES = new HashMap<>();
    static {
        // PRIMOS MOTOPECAS (SPC: 22885) - plano disfarçado
        MAPEAMENTO_CORRECOES.put("22885|FRANQUIA DE CONSULTA NOVO SPC MAXI 1/1",
            new CorrecaoProduto(
                "04.01.03.94404",
                "PLANO 50 NOVO SPC MAXI 1/1",
                "Plano 50 NOVO SPC MAXI"
            ));

        // VOX COMERCIAL (SPC: 6090)
        MAPEAMENTO_CORRECOES.put("6090|FRANQUIA DE CONSULTA SPC RELATORIO 1/1",
            new CorrecaoProduto(
                "04.01.03.69847",
                "PLANO 20 SPC RELATORIO 1/1",
                "Plano 20 SPC RELATORIO"
            ));

        // NORTE SERVICOS (SPC: 22662)
        MAPEAMENTO_CORRECOES.put("22662|FRANQUIA DE CONSULTA SPC RELATORIO COMPLETO 1/1",
            new CorrecaoProduto(
                "04.01.03.94397",
                "PLANO 20 SPC RELATORIO COMPLETO 1/1",
                "Plano 20 SPC RELATORIO COMPLETO"
            ));
        
        // 863 - COMERCIAL UNIMAQ LTDA (SPC: 863)
        MAPEAMENTO_CORRECOES.put("863|FRANQUIA DE CONSULTA NOVO SPC MAXI 1/1",
            new CorrecaoProduto(
                "04.01.03.94404",
                "PLANO 50 NOVO SPC MAXI 1/1",
                "PLANO 50 NOVO SPC MAXI"
            ));        
    }

    // ================================================================
    // MÉTODOS AUXILIARES - IDENTIFICAÇÃO
    // ================================================================

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
     * Verifica se o item é um plano (ou plano disfarçado)
     */
    private boolean isPlanoItem(FaturaItem item) {
        if (item == null || item.getDescricao() == null) return false;
        String desc = item.getDescricao().toUpperCase();

        // 1. Verificação padrão
        if (desc.contains(PREFIXO_PLANO) ||
            desc.contains(PREFIXO_ASSINATURA) ||
            desc.contains(PREFIXO_MENSALIDADE) ||
            desc.contains(PREFIXO_CONTRIBUICAO) ||
            desc.matches(".*PLANO\\s+\\d+.*") ||
            desc.matches(".*PLANO\\s+[A-Z].*")) {
            return true;
        }

        // 2. Plano disfarçado de franquia (valor alto + 1/1)
        if (desc.contains("FRANQUIA") && desc.contains("1/1")) {
            BigDecimal valorUnitario = item.getValorUnitario();
            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.valueOf(100)) > 0) {
                log.debug("   🔍 Item identificado como plano disfarçado: {} (Valor: R$ {})",
                        item.getDescricao(), valorUnitario);
                return true;
            }
        }

        return false;
    }

    /**
     * Obtém a correção para um item específico de um associado
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

    // ================================================================
    // MÉTODOS AUXILIARES - EXTRAÇÃO DE MEIOS DE ACESSO
    // ================================================================

    /**
     * Extrai o meio de acesso da descrição do serviço
     */
    private String extrairMeioAcesso(String descricao) {
        if (descricao == null) return "DESCONHECIDO";
        String desc = descricao.toUpperCase();

        if (desc.contains(". HOST A HOST") || desc.contains(".HOST A HOST") ||
            desc.contains(".HOST-A-HOST") || desc.contains(" HOST A HOST") ||
            desc.contains(" HOST-A-HOST")) {
            return "HOST A HOST";
        }

        if (desc.contains(". HOST") || desc.contains(".HOST") || desc.contains(" HOST")) {
            return "HOST";
        }

        if (desc.contains(". WEBSERVICE") || desc.contains(".WEBSERVICE") ||
            desc.contains(" WEBSERVICE")) {
            return "WEBSERVICE";
        }

        if (desc.contains(". INTERNET") || desc.contains(".INTERNET") ||
            desc.contains(" INTERNET")) {
            return "INTERNET";
        }

        return "DESCONHECIDO";
    }

    /**
     * Extrai o nome base do serviço, removendo APENAS o meio de acesso.
     * NÃO remove sufixos como POSITIVO FOR - eles diferenciam os serviços.
     */
    private String extrairNomeBase(String descricaoServico) {
        if (descricaoServico == null) return "";

        String base = descricaoServico;

        // ===== 1. REMOVER MEIOS DE ACESSO COM PONTO =====
        base = base.replaceAll("(?i)\\.\\s*HOST\\s+A\\s+HOST", "").trim();
        base = base.replaceAll("(?i)\\.\\s*HOST-A-HOST", "").trim();
        base = base.replaceAll("(?i)\\.\\s*HOST", "").trim();
        base = base.replaceAll("(?i)\\.\\s*WEBSERVICE", "").trim();
        base = base.replaceAll("(?i)\\.WEBSERVICE", "").trim();
        base = base.replaceAll("(?i)\\.\\s*INTERNET", "").trim();
        base = base.replaceAll("(?i)\\.INTERNET", "").trim();

        // ===== 2. REMOVER MEIOS DE ACESSO SEM PONTO =====
        base = base.replaceAll("(?i)\\s+HOST\\s+A\\s+HOST$", "").trim();
        base = base.replaceAll("(?i)\\s+HOST-A-HOST$", "").trim();
        base = base.replaceAll("(?i)\\s+HOST$", "").trim();
        base = base.replaceAll("(?i)\\s+WEBSERVICE$", "").trim();
        base = base.replaceAll("(?i)\\s+INTERNET$", "").trim();

        // ===== 3. REMOVER MEIOS DE ACESSO NO MEIO =====
        base = base.replaceAll("(?i)\\s+INTERNET\\s+", " ").trim();
        base = base.replaceAll("(?i)\\s+WEBSERVICE\\s+", " ").trim();
        base = base.replaceAll("(?i)\\s+HOST\\s+A\\s+HOST\\s+", " ").trim();
        base = base.replaceAll("(?i)\\s+HOST-A-HOST\\s+", " ").trim();

        // ===== 4. REMOVER "PLANO - PACOTE ..." =====
        // Remove sufixos como "PLANO - PACOTE 20 MIX - 53"
        base = base.replaceAll("(?i)\\s*[-–]\\s*PLANO\\s+[-–]\\s+PACOTE\\s+.*$", "").trim();

        // ===== 5. LIMPAR ESPAÇOS DUPLOS =====
        base = base.replaceAll("\\s+", " ").trim();

        // ===== 6. USAR MAPEAMENTO ESPECÍFICO =====
        String mapeado = MAPEAMENTO_ESPECIFICO.get(base.toUpperCase());
        if (mapeado != null) {
            return mapeado;
        }

        return base;
    }

    /**
     * Extrai o nome base da franquia
     */
    private String extrairNomeBaseDaFranquia(String descricaoFranquia) {
        if (descricaoFranquia == null) return "";

        String desc = descricaoFranquia;

        // Remover "FRANQUIA DE CONSULTA"
        desc = desc.replace(PREFIXO_FRANQUIA, "").trim();

        // Remover "DE" se estiver no início
        if (desc.toUpperCase().startsWith("DE ")) {
            desc = desc.substring(3).trim();
        }

        // Remover "NOVO" se estiver no início
        if (desc.toUpperCase().startsWith("NOVO ")) {
            desc = desc.substring(4).trim();
        }

        // Remover "1/1", "2/2", etc.
        desc = desc.replaceAll("\\d+/\\d+", "").trim();

        // Usar mapeamento específico se existir
        String mapeado = MAPEAMENTO_ESPECIFICO.get(desc.toUpperCase());
        if (mapeado != null) {
            return mapeado;
        }

        return desc;
    }

    /**
     * Obtém a lista de sinônimos para um nome base
     */
    private List<String> obterSinonimos(String nomeBase) {
        for (Map.Entry<String, List<String>> entry : MAPEAMENTO_SINONIMOS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(nomeBase)) {
                return entry.getValue();
            }
        }
        return Arrays.asList(nomeBase);
    }

    // ================================================================
    // 🔥 MÉTODO PRINCIPAL - APLICAR REGRA DE FRANQUIA
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
        // PASSO 1: CORRIGIR ITENS QUE SÃO PLANOS DISFARÇADOS
        // ================================================================

        for (FaturaItem item : fatura.getItens()) {
            CorrecaoProduto correcao = obterCorrecaoProduto(associado, item);
            if (correcao != null) {
                log.warn("⚠️ CORREÇÃO DE PRODUTO DETECTADA!");
                log.warn("   🔴 Descrição errada: {}", item.getDescricao());
                log.warn("   ✅ Descrição correta: {}", correcao.getDescricaoCorreta());
                log.warn("   🔑 Código RM correto: {}", correcao.getCodigoRMCorreto());
                item.setCodigoProduto(correcao.getCodigoRMCorreto());
                item.setDescricao(correcao.getDescricaoCorreta());
            }
        }

        // ================================================================
        // PASSO 2: IDENTIFICAR FRANQUIAS
        // ================================================================

        List<FaturaItem> itensFranquia = new ArrayList<>();

        for (FaturaItem item : fatura.getItens()) {
            if (isFranquia(item.getDescricao())) {
                itensFranquia.add(item);
            }
        }

        if (itensFranquia.isEmpty()) {
            log.info("⏭️ Nenhum item de franquia encontrado");
            log.info("========================================");
            return fatura;
        }

        log.info("📋 Encontrados {} itens de franquia:", itensFranquia.size());
        for (FaturaItem item : itensFranquia) {
            log.info("   - {} (Qtd: {}, Valor: R$ {})",
                    item.getDescricao(), item.getQuantidade(), item.getValorUnitario());
        }
        log.info("");

        // ================================================================
        // PASSO 3: PROCESSAR CADA FRANQUIA
        // ================================================================

        List<FaturaItem> itensRemover = new ArrayList<>();
        List<FaturaItem> excedentes = new ArrayList<>();

        for (FaturaItem franquiaItem : itensFranquia) {
            // Verificar se é um plano disfarçado (preservar)
            if (isPlanoItem(franquiaItem)) {
                log.warn("⚠️ Item preservado (plano disfarçado): {}", franquiaItem.getDescricao());
                continue;
            }

            processarFranquia(fatura, franquiaItem, itensRemover, excedentes);
        }

        // ================================================================
        // PASSO 4: REMOVER ITENS E ADICIONAR EXCEDENTES
        // ================================================================

        for (FaturaItem item : itensRemover) {
            if (fatura.getItens().contains(item)) {
                fatura.getItens().remove(item);
                log.info("🗑️ Item removido: {} (Qtd: {})",
                        item.getDescricao(), item.getQuantidade());
            }
        }

        for (FaturaItem item : excedentes) {
            if (!fatura.getItens().contains(item)) {
                fatura.getItens().add(item);
                log.info("✅ Excedente adicionado: {} (Qtd: {})",
                        item.getDescricao(), item.getQuantidade());
            }
        }

        // ================================================================
        // PASSO 5: RECALCULAR VALOR TOTAL
        // ================================================================

        fatura.recalcularValor();
        log.info("💰 Valor total da fatura após franquias: R$ {}", fatura.getValorTotal());
        log.info("========================================");

        return fatura;
    }

    // ================================================================
    // 🔥 MÉTODO CENTRAL - PROCESSAR FRANQUIA
    // ================================================================

    private void processarFranquia(Fatura fatura, FaturaItem franquiaItem,
            List<FaturaItem> itensRemover, List<FaturaItem> excedentes) {

        log.info("========================================");
        log.info("📋 Processando franquia: {}", franquiaItem.getDescricao());

        // ================================================================
        // 1. EXTRAIR LIMITE DA FRANQUIA
        // ================================================================

        BigDecimal quantidadeFranquiaBD = franquiaItem.getQuantidade() != null ?
            franquiaItem.getQuantidade() : BigDecimal.ZERO;
        Integer limiteFranquia = quantidadeFranquiaBD.intValue();
        log.info("   📊 Limite da franquia: {}", limiteFranquia);

        if (limiteFranquia == 0) {
            log.warn("⚠️ Limite da franquia é zero, apenas a franquia será removida");
            itensRemover.add(franquiaItem);
            return;
        }

        // ================================================================
        // 2. EXTRAIR NOME BASE DA FRANQUIA
        // ================================================================

        String nomeBase = extrairNomeBaseDaFranquia(franquiaItem.getDescricao());
        log.info("🔍 Nome base da franquia: '{}'", nomeBase);

        if (nomeBase == null || nomeBase.isEmpty()) {
            log.warn("⚠️ Não foi possível extrair o nome base da franquia");
            itensRemover.add(franquiaItem);
            return;
        }

        // ================================================================
        // 3. BUSCAR SERVIÇOS RELACIONADOS (CORRESPONDÊNCIA EXATA)
        // ================================================================

        List<FaturaItem> servicosEncontrados = new ArrayList<>();
        List<String> sinonimos = obterSinonimos(nomeBase);
        log.info("🔍 Buscando por sinônimos (correspondência exata): {}", sinonimos);

        for (FaturaItem item : fatura.getItens()) {
            // Ignorar o próprio item da franquia
            if (item == franquiaItem) continue;

            // 🔥 IGNORAR PLANOS
            if (isPlanoItem(item)) {
                log.debug("   ⏭️ Ignorando plano: {}", item.getDescricao());
                continue;
            }

            // 🔥 IGNORAR OUTRAS FRANQUIAS
            if (isFranquia(item.getDescricao())) {
                log.info("   ⏭️ Ignorando outra franquia: {}", item.getDescricao());
                continue;
            }

            // 🔥 EXTRAIR NOME BASE DO ITEM (SEM MEIO DE ACESSO)
            String nomeBaseItem = extrairNomeBase(item.getDescricao());

            // 🔥 CORRESPONDÊNCIA EXATA (não parcial!)
            boolean corresponde = false;
            for (String sinonimo : sinonimos) {
                if (nomeBaseItem.equalsIgnoreCase(sinonimo)) {
                    corresponde = true;
                    break;
                }
            }

            if (corresponde) {
                servicosEncontrados.add(item);
                String meio = extrairMeioAcesso(item.getDescricao());
                log.info("   ✅ Serviço encontrado: {} (Base: '{}', Meio: {}, Qtd: {})",
                        item.getDescricao(), nomeBaseItem, meio, item.getQuantidade());
            }
        }

        if (servicosEncontrados.isEmpty()) {
            log.warn("❌ Nenhum serviço relacionado encontrado. Apenas a franquia será removida.");
            itensRemover.add(franquiaItem);
            return;
        }

        // ================================================================
        // 4. SOMAR QUANTIDADES DE TODOS OS SERVIÇOS
        // ================================================================

        int totalQuantidade = 0;
        for (FaturaItem item : servicosEncontrados) {
            totalQuantidade += item.getQuantidade().intValue();
        }

        log.info("📊 TOTAL DE SERVIÇOS ENCONTRADOS: {} serviços, soma={}",
                servicosEncontrados.size(), totalQuantidade);
        log.info("📊 Comparação: Soma={} vs Limite={}", totalQuantidade, limiteFranquia);

        // ================================================================
        // 5. APLICAR REGRA
        // ================================================================

        // Remover todos os serviços
        itensRemover.addAll(servicosEncontrados);

        // Calcular excedente
        int excedente = totalQuantidade - limiteFranquia;

        if (excedente > 0) {
            log.info("✅ Franquia EXCEDIDA! Excedente: {}", excedente);

            // 🔥 CRIAR ITEM COM EXCEDENTE
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

            excedentes.add(novoItem);
            log.info("   ✅ Excedente criado: {} (Qtd: {})",
                    novoItem.getDescricao(), excedente);

        } else {
            log.info("✅ Franquia NÃO excedida. Todos os serviços cobertos.");
        }

        // Remover a franquia
        itensRemover.add(franquiaItem);
        log.info("🗑️ Franquia removida: {} (Qtd: {})",
                franquiaItem.getDescricao(), franquiaItem.getQuantidade());

        log.info("========================================");
    }
}