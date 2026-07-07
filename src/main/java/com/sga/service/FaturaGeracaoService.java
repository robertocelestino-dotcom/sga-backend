// src/main/java/com/sga/service/FaturaGeracaoService.java
package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.Associado;
import com.sga.model.AssociadoProduto;
import com.sga.model.CancelamentoImportacao;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.NotificacaoAssociado;
import com.sga.model.ParametrosSPC;
import com.sga.model.Produto;
import com.sga.model.ReguaFaturamento;
import com.sga.model.TipoArquivoRegua;
import com.sga.repository.AssociadoProdutoRepository;
import com.sga.repository.CancelamentoImportacaoRepository;
import com.sga.repository.FaturaItemRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.ItemSPCRepository;
import com.sga.repository.NotaDebitoSPCRepository;
import com.sga.repository.NotificacaoAssociadoRepository;
import com.sga.repository.ParametrosSPCRepository;
import com.sga.repository.ProdutoRepository;
import com.sga.rules.FaturamentoMinimoRule;
import com.sga.rules.FranquiaRule;

@Service
public class FaturaGeracaoService {

    private static final Logger log = LoggerFactory.getLogger(FaturaGeracaoService.class);

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private FaturaItemRepository faturaItemRepository;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;

    @Autowired
    private ItemSPCRepository itemSPCRepository;

    @Autowired
    private AssociadoService associadoService;

    @Autowired
    private ReguaFaturamentoService reguaService;

    @Autowired
    private FranquiaRule franquiaRule;

    @Autowired
    private FaturamentoMinimoRule faturamentoMinimoRule;

    @Autowired
    private CancelamentoImportacaoRepository cancelamentoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private NotificacaoAssociadoRepository notificacaoAssociadoRepository;

    @Autowired
    private AssociadoProdutoRepository associadoProdutoRepository;
    
    @Autowired
    private ParametrosSPCRepository parametrosSPCRepository;

    // ========== MAPEAMENTO DOS PRODUTOS DE NOTIFICAÇÃO ==========
    private static final Map<String, ProdutoNotificacaoInfo> PRODUTOS_NOTIFICACAO = new LinkedHashMap<>();
    static {
        PRODUTOS_NOTIFICACAO.put("04.01.03.94343", 
            new ProdutoNotificacaoInfo("cartasTotal", 17L, "NOTIFICAÇÃO SPC CARTA"));
        PRODUTOS_NOTIFICACAO.put("04.01.03.94341", 
            new ProdutoNotificacaoInfo("emailsSemEnriquecimento", 18L, "NOTIFICAÇÃO SPC E-MAIL"));
        PRODUTOS_NOTIFICACAO.put("04.01.03.94432", 
            new ProdutoNotificacaoInfo("emailsComEnriquecimento", 19L, "NOTIFICAÇÃO SPC E-MAIL C/ ENRIQUECIMENTO"));
        PRODUTOS_NOTIFICACAO.put("04.01.03.94342", 
            new ProdutoNotificacaoInfo("smsSemEnriquecimento", 20L, "NOTIFICAÇÃO SPC SMS"));
        PRODUTOS_NOTIFICACAO.put("04.01.03.94431", 
            new ProdutoNotificacaoInfo("smsComEnriquecimento", 21L, "NOTIFICAÇÃO SPC SMS C/ ENRIQUECIMENTO"));
    }

    private static class ProdutoNotificacaoInfo {
        String campoQuantidade;
        Long produtoId;
        String descricao;
        
        ProdutoNotificacaoInfo(String campoQuantidade, Long produtoId, String descricao) {
            this.campoQuantidade = campoQuantidade;
            this.produtoId = produtoId;
            this.descricao = descricao;
        }
    }

    // ========== MÉTODO PRINCIPAL DE GERAÇÃO DE FATURA ==========

    @Transactional
    public Fatura gerarFatura(Associado associado, List<NotaDebitoSPC> notas, ReguaFaturamento regua, Integer mes,
            Integer ano, LocalDate dataEmissao, LocalDate dataVencimento, boolean simular, String usuario) {

        log.info("========== INICIANDO GERAÇÃO DE FATURA ==========");
        log.info("📄 Associado: {} (ID: {}, Código SPC: {})", associado.getNomeRazao(), associado.getId(),
                associado.getCodigoSpc());

        // ========== 1. EXTRAIR PERÍODO DOS PARÂMETROS ==========
        Integer mesReferencia = null;
        Integer anoReferencia = null;
        LocalDate dataInicioPeriodo = null;
        LocalDate dataFimPeriodo = null;

        // 🔥 TENTAR EXTRAIR DOS PARÂMETROS SPC
        Object[] periodo = extrairPeriodoDosParametros(associado);
        
        if (periodo != null) {
            mesReferencia = (Integer) periodo[0];
            anoReferencia = (Integer) periodo[1];
            dataInicioPeriodo = (LocalDate) periodo[2];
            dataFimPeriodo = (LocalDate) periodo[3];
            log.info("📅 Período extraído dos parâmetros: {}/{} ({} à {})", 
                    mesReferencia, anoReferencia, dataInicioPeriodo, dataFimPeriodo);
        } else {
            // FALLBACK: Usar valores fornecidos
            log.warn("⚠️ Usando valores fornecidos como fallback");
            if (dataEmissao != null) {
                mesReferencia = dataEmissao.getMonthValue();
                anoReferencia = dataEmissao.getYear();
            } else if (mes != null && ano != null) {
                mesReferencia = mes;
                anoReferencia = ano;
            } else {
                LocalDate hoje = LocalDate.now();
                mesReferencia = hoje.getMonthValue();
                anoReferencia = hoje.getYear();
            }
            log.info("📅 Período fallback: {}/{}", mesReferencia, anoReferencia);
        }

        // Verificar se há notas
        if (notas == null || notas.isEmpty()) {
            log.warn("⚠️ Nenhuma nota encontrada para o associado");
            return null;
        }

        // Verificar se já existe fatura
        if (!simular) {
            List<Fatura> faturasExistentes = faturaRepository
                    .findByAssociadoIdAndMesReferenciaAndAnoReferencia(associado.getId(), mesReferencia, anoReferencia);
            if (!faturasExistentes.isEmpty()) {
                log.warn("Já existe fatura para o associado {} no período {}/{}", 
                        associado.getId(), mesReferencia, anoReferencia);
                return faturasExistentes.get(0);
            }
        }

        // ========== 2. BUSCAR NOTIFICAÇÕES DO ASSOCIADO ==========
        NotificacaoAssociado notificacao = null;
        if (mesReferencia != null && anoReferencia != null) {
            log.info("🔍 Buscando notificações para associado {} no período {}/{}", 
                    associado.getId(), mesReferencia, anoReferencia);
            
            notificacao = notificacaoAssociadoRepository
                    .findByAssociadoIdAndMesReferenciaAndAnoReferencia(associado.getId(), mesReferencia, anoReferencia)
                    .orElse(null);
            
            if (notificacao != null) {
                log.info("✅ NOTIFICAÇÕES ENCONTRADAS para associado {}!", associado.getId());
                log.info("📊 SMS: {} ({} sem / {} com enriquecimento)", 
                        notificacao.getSmsTotal(),
                        notificacao.getSmsSemEnriquecimento(),
                        notificacao.getSmsComEnriquecimento());
                log.info("📧 E-mail: {} ({} sem / {} com enriquecimento)", 
                        notificacao.getEmailsTotal(),
                        notificacao.getEmailsSemEnriquecimento(),
                        notificacao.getEmailsComEnriquecimento());
                log.info("📬 Cartas: {}", notificacao.getCartasTotal());
            } else {
                log.warn("⚠️ NENHUMA NOTIFICAÇÃO encontrada para associado {} no período {}/{}", 
                        associado.getId(), mesReferencia, anoReferencia);
                log.warn("   🔍 Verifique se as notificações foram sincronizadas para este período");
            }
        }

        // ========== 3. PROCESSAR NOTAS ==========
        Map<String, List<ItemSPC>> itensPorTipo = processarNotasPorTipoArquivo(notas, regua);
        List<FaturaItem> itensCalculados = calcularItensConformeFormula(itensPorTipo, regua);

        // ========== 4. ADICIONAR ITENS DE NOTIFICAÇÕES ==========
        if (notificacao != null) {
            log.info("📊 Adicionando itens de notificações à fatura...");
            Map<String, BigDecimal> produtosAssociado = buscarProdutosNotificacaoAssociado(associado.getId());
            
            if (!produtosAssociado.isEmpty()) {
                List<FaturaItem> itensNotificacao = criarItensNotificacao(notificacao, produtosAssociado, associado);
                if (!itensNotificacao.isEmpty()) {
                    itensCalculados.addAll(itensNotificacao);
                    log.info("✅ Adicionados {} itens de notificações", itensNotificacao.size());
                }
            } else {
                log.warn("⚠️ Associado {} não possui produtos de notificação configurados", associado.getId());
            }
        }

        if (itensCalculados.isEmpty()) {
            log.warn("⚠️ Nenhum item calculado para o associado: {}", associado.getNomeRazao());
            return null;
        }
        
        // ========== 🔥 REMOVER ITEM DUPLICADO ==========
        //if (notificacao != null && !simular) {
        if (notificacao != null) {
            log.info("========================================");
            log.info("🔍 INICIANDO PROCESSO DE REMOÇÃO DE DUPLICADO");
            log.info("========================================");
            
            // 🔥 CHAMAR O MÉTODO DE REMOÇÃO
            removerItemNotificacaoDuplicado(itensCalculados, notificacao);
            
            log.info("========================================");
            log.info("📋 ITENS FINAIS APÓS REMOÇÃO ({} itens):", itensCalculados.size());
            for (FaturaItem item : itensCalculados) {
                log.info("   - {} ({}): Qtd={}", 
                        item.getDescricao(), item.getCodigoProduto(), item.getQuantidade());
            }
            log.info("========================================");
        }

        if (itensCalculados.isEmpty()) {
            log.warn("⚠️ Nenhum item calculado para o associado: {}", associado.getNomeRazao());
            return null;
        }
       
        // ========== 5. CRIAR FATURA ==========
        Fatura fatura = new Fatura();
        fatura.setAssociado(associado);
        fatura.setNumeroFatura(gerarNumeroFatura(associado.getId(), mesReferencia, anoReferencia));

        // Datas
        if (dataEmissao != null) {
            fatura.setDataEmissao(dataEmissao);
        } else {
            fatura.setDataEmissao(calcularDataEmissao(regua));
        }

        if (dataVencimento != null) {
            fatura.setDataVencimento(dataVencimento);
        } else {
            fatura.setDataVencimento(calcularDataVencimento(regua));
        }

        fatura.setStatus(simular ? "SIMULADO" : "PENDENTE");
        fatura.setProcessadoRm(false);

        // Mês/Ano referência
        fatura.setMesReferencia(mesReferencia);
        fatura.setAnoReferencia(anoReferencia);

        fatura.setUsuarioCriacao(usuario);
        fatura.setCriadoEm(LocalDateTime.now());
        fatura.setItens(itensCalculados);

        for (FaturaItem item : itensCalculados) {
            item.setFatura(fatura);
        }

        fatura.recalcularTotal();

        // ========== 6. ADICIONAR OBSERVAÇÃO ==========
        if (notificacao != null) {
            BigDecimal valorNotificacoes = calcularValorTotalNotificacoes(notificacao);
            String observacao = String.format(
                    "📊 RESUMO NOTIFICAÇÕES - %02d/%d\n" +
                    "📱 SMS: %d total (%d sem / %d com enriquecimento)\n" +
                    "📧 E-mails: %d total (%d sem / %d com enriquecimento)\n" +
                    "📬 Cartas: %d\n" +
                    "💰 Valor Total das Notificações: R$ %.2f",
                    mesReferencia, anoReferencia,
                    notificacao.getSmsTotal(),
                    notificacao.getSmsSemEnriquecimento(),
                    notificacao.getSmsComEnriquecimento(),
                    notificacao.getEmailsTotal(),
                    notificacao.getEmailsSemEnriquecimento(),
                    notificacao.getEmailsComEnriquecimento(),
                    notificacao.getCartasTotal(),
                    valorNotificacoes
            );
            fatura.setObservacao(observacao);
            log.info("📝 Observação adicionada à fatura");
        }

        // ========== 7. APLICAR REGRAS ==========
        if (regua != null && Boolean.TRUE.equals(regua.getAplicarFranquia())) {
            fatura = franquiaRule.aplicarRegraFranquia(fatura, associado);
        }

        boolean aplicarMinimo = regua != null && Boolean.TRUE.equals(regua.getAplicarFaturamentoMinimo())
                && regua.isExtemporaneo();
        if (aplicarMinimo) {
            fatura = faturamentoMinimoRule.aplicarRegraComplemento(fatura, associado, true);
        }

        if (regua != null && Boolean.TRUE.equals(regua.getAplicarCancelamentos())) {
            List<CancelamentoImportacao> cancelamentos = buscarCancelamentos(associado, mesReferencia, anoReferencia);
            if (!cancelamentos.isEmpty()) {
                List<FaturaItem> itensAposCancelamento = removerServicosCancelados(fatura.getItens(), cancelamentos);
                fatura.setItens(itensAposCancelamento);
                fatura.recalcularTotal();
            }
        }

        // ========== 8. MARCAR NOTIFICAÇÕES COMO FATURADAS ==========
        if (notificacao != null && !simular) {
            notificacao.setProcessadoFatura(true);
            notificacao.setFaturaId(fatura.getId());
            notificacao.setDataProcessamento(LocalDateTime.now());
            notificacaoAssociadoRepository.save(notificacao);
            log.info("✅ Notificações marcadas como faturadas para associado {}", associado.getId());
        }

        // ========== 9. SALVAR ==========
        if (simular) {
            return fatura;
        } else {
            Fatura faturaSalva = faturaRepository.save(fatura);
            if (fatura.getItens() != null && !fatura.getItens().isEmpty()) {
                for (FaturaItem item : fatura.getItens()) {
                    item.setFatura(faturaSalva);
                    faturaItemRepository.save(item);
                }
            }
            log.info("✅ Fatura {} gerada para associado {} - Valor: R$ {}", 
                    faturaSalva.getId(), associado.getId(), faturaSalva.getValorTotal());
            return faturaSalva;
        }
    }

    // ========== MÉTODOS DE NOTIFICAÇÃO ==========

    /**
     * 🔥 Busca os produtos de notificação configurados para o associado
     * Retorna um mapa com código RM -> valor definido
     */
    private Map<String, BigDecimal> buscarProdutosNotificacaoAssociado(Long associadoId) {
        Map<String, BigDecimal> produtos = new LinkedHashMap<>();
        
        try {
            // IDs dos produtos de notificação (17, 18, 19, 20, 21)
            List<Long> idsProdutos = Arrays.asList(17L, 18L, 19L, 20L, 21L);
            
            List<AssociadoProduto> lista = associadoProdutoRepository
                    .findByAssociadoIdAndProdutoIdIn(associadoId, idsProdutos);
            
            if (lista == null || lista.isEmpty()) {
                log.warn("⚠️ Nenhum produto de notificação configurado para associado {}", associadoId);
                return produtos;
            }
            
            for (AssociadoProduto ap : lista) {
                Produto produto = ap.getProduto();
                if (produto != null && produto.getCodigoRm() != null) {
                    String codigoRm = produto.getCodigoRm();
                    BigDecimal valor = ap.getValorDefinido() != null ? ap.getValorDefinido() : BigDecimal.ZERO;
                    produtos.put(codigoRm, valor);
                    log.debug("📦 Produto encontrado: {} = R$ {}", codigoRm, valor);
                }
            }
            
            log.info("✅ {} produtos de notificação encontrados para associado {}", produtos.size(), associadoId);
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar produtos de notificação para associado {}: {}", associadoId, e.getMessage());
        }
        
        return produtos;
    }

    /**
     * 🔥 Verifica se o associado possui produtos com enriquecimento
     */
    private boolean associadoPossuiEnriquecimento(Map<String, BigDecimal> produtosAssociado) {
        return produtosAssociado.containsKey("04.01.03.94432") ||  // E-MAIL C/ ENR
               produtosAssociado.containsKey("04.01.03.94431");    // SMS C/ ENR
    }

    /**
     * 🔥 Obtém a quantidade de um campo específico da notificação
     */
    private Integer getQuantidadeNotificacao(NotificacaoAssociado notificacao, String campo) {
        if (notificacao == null) return 0;
        
        switch (campo) {
            case "cartasTotal":
                return notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
            case "emailsSemEnriquecimento":
                return notificacao.getEmailsSemEnriquecimento() != null ? notificacao.getEmailsSemEnriquecimento() : 0;
            case "emailsComEnriquecimento":
                return notificacao.getEmailsComEnriquecimento() != null ? notificacao.getEmailsComEnriquecimento() : 0;
            case "smsSemEnriquecimento":
                return notificacao.getSmsSemEnriquecimento() != null ? notificacao.getSmsSemEnriquecimento() : 0;
            case "smsComEnriquecimento":
                return notificacao.getSmsComEnriquecimento() != null ? notificacao.getSmsComEnriquecimento() : 0;
            default:
                return 0;
        }
    }

    /**
     * 🔥 CRIA ITENS DE FATURA A PARTIR DAS NOTIFICAÇÕES
     * APENAS ITENS COM QUANTIDADE > 0
     */
    private List<FaturaItem> criarItensNotificacao(NotificacaoAssociado notificacao, 
            Map<String, BigDecimal> produtosAssociado, Associado associado) {
        
        List<FaturaItem> itens = new ArrayList<>();
        
        if (notificacao == null || produtosAssociado == null || produtosAssociado.isEmpty()) {
            log.warn("⚠️ Notificação ou produtos do associado vazios");
            return itens;
        }
        
        log.info("📊 Criando itens de notificação para associado: {}", associado.getNomeRazao());
        
        boolean possuiEnriquecimento = associadoPossuiEnriquecimento(produtosAssociado);
        log.info("📌 Associado {} - {}", associado.getNomeRazao(), 
                possuiEnriquecimento ? "COM ENRIQUECIMENTO" : "SEM ENRIQUECIMENTO");
        
        int count = 0;
        BigDecimal valorTotalNotificacoes = BigDecimal.ZERO;
        List<String> itensCriados = new ArrayList<>();

        // ========== 1. CARTAS (SEMPRE TOTAL) ==========
        String codigoCarta = "04.01.03.94343";
        if (produtosAssociado.containsKey(codigoCarta)) {
            Integer quantidade = notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
            BigDecimal valorUnitario = produtosAssociado.get(codigoCarta);
            
            if (quantidade > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                FaturaItem item = criarItemFatura(
                        codigoCarta,
                        "NOTIFICAÇÃO SPC CARTA (" + quantidade + " unid.)",
                        quantidade,
                        valorUnitario,
                        "D"
                );
                itens.add(item);
                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                count++;
                itensCriados.add("CARTA: " + quantidade + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                log.info("  ✅ CARTA: {} x R$ {} = R$ {}", quantidade, valorUnitario, item.getValorTotal());
            } else {
                log.debug("  ℹ️ CARTA: quantidade {} ou valor zero - ignorado", quantidade);
            }
        }

        // ========== 2. E-MAILS ==========
        if (possuiEnriquecimento) {
            // COM ENRIQUECIMENTO: SEPARADO
            String codigoEmailSem = "04.01.03.94341";
            if (produtosAssociado.containsKey(codigoEmailSem)) {
                Integer quantidade = notificacao.getEmailsSemEnriquecimento() != null ? 
                        notificacao.getEmailsSemEnriquecimento() : 0;
                BigDecimal valorUnitario = produtosAssociado.get(codigoEmailSem);
                
                if (quantidade > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoEmailSem,
                            "NOTIFICAÇÃO SPC E-MAIL (" + quantidade + " unid.)",
                            quantidade,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("E-MAIL SEM ENR: " + quantidade + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ E-MAIL SEM ENR: {} x R$ {} = R$ {}", quantidade, valorUnitario, item.getValorTotal());
                } else {
                    log.debug("  ℹ️ E-MAIL SEM ENR: quantidade {} ou valor zero - ignorado", quantidade);
                }
            }
            
            String codigoEmailCom = "04.01.03.94432";
            if (produtosAssociado.containsKey(codigoEmailCom)) {
                Integer quantidade = notificacao.getEmailsComEnriquecimento() != null ? 
                        notificacao.getEmailsComEnriquecimento() : 0;
                BigDecimal valorUnitario = produtosAssociado.get(codigoEmailCom);
                
                if (quantidade > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoEmailCom,
                            "NOTIFICAÇÃO SPC E-MAIL C/ ENRIQUECIMENTO (" + quantidade + " unid.)",
                            quantidade,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("E-MAIL COM ENR: " + quantidade + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ E-MAIL COM ENR: {} x R$ {} = R$ {}", quantidade, valorUnitario, item.getValorTotal());
                } else {
                    log.debug("  ℹ️ E-MAIL COM ENR: quantidade {} ou valor zero - ignorado", quantidade);
                }
            }
        } else {
            // SEM ENRIQUECIMENTO: TOTAL
            String codigoEmail = "04.01.03.94341";
            if (produtosAssociado.containsKey(codigoEmail)) {
                Integer totalEmails = (notificacao.getEmailsSemEnriquecimento() != null ? notificacao.getEmailsSemEnriquecimento() : 0) +
                                      (notificacao.getEmailsComEnriquecimento() != null ? notificacao.getEmailsComEnriquecimento() : 0);
                BigDecimal valorUnitario = produtosAssociado.get(codigoEmail);
                
                if (totalEmails > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoEmail,
                            "NOTIFICAÇÃO SPC E-MAIL (" + totalEmails + " unid.)",
                            totalEmails,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("E-MAIL TOTAL: " + totalEmails + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ E-MAIL TOTAL: {} x R$ {} = R$ {}", totalEmails, valorUnitario, item.getValorTotal());
                } else {
                    log.debug("  ℹ️ E-MAIL TOTAL: quantidade {} ou valor zero - ignorado", totalEmails);
                }
            }
        }

        // ========== 3. SMS ==========
        if (possuiEnriquecimento) {
            // COM ENRIQUECIMENTO: SEPARADO
            String codigoSmsSem = "04.01.03.94342";
            if (produtosAssociado.containsKey(codigoSmsSem)) {
                Integer quantidade = notificacao.getSmsSemEnriquecimento() != null ? 
                        notificacao.getSmsSemEnriquecimento() : 0;
                BigDecimal valorUnitario = produtosAssociado.get(codigoSmsSem);
                
                if (quantidade > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoSmsSem,
                            "NOTIFICAÇÃO SPC SMS (" + quantidade + " unid.)",
                            quantidade,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("SMS SEM ENR: " + quantidade + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ SMS SEM ENR: {} x R$ {} = R$ {}", quantidade, valorUnitario, item.getValorTotal());
                } else {
                    log.debug("  ℹ️ SMS SEM ENR: quantidade {} ou valor zero - ignorado", quantidade);
                }
            }
            
            String codigoSmsCom = "04.01.03.94431";
            if (produtosAssociado.containsKey(codigoSmsCom)) {
                Integer quantidade = notificacao.getSmsComEnriquecimento() != null ? 
                        notificacao.getSmsComEnriquecimento() : 0;
                BigDecimal valorUnitario = produtosAssociado.get(codigoSmsCom);
                
                if (quantidade > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoSmsCom,
                            "NOTIFICAÇÃO SPC SMS C/ ENRIQUECIMENTO (" + quantidade + " unid.)",
                            quantidade,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("SMS COM ENR: " + quantidade + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ SMS COM ENR: {} x R$ {} = R$ {}", quantidade, valorUnitario, item.getValorTotal());
                } else {
                    log.debug("  ℹ️ SMS COM ENR: quantidade {} ou valor zero - ignorado", quantidade);
                }
            }
        } else {
            // SEM ENRIQUECIMENTO: TOTAL
            String codigoSms = "04.01.03.94342";
            if (produtosAssociado.containsKey(codigoSms)) {
                Integer totalSms = (notificacao.getSmsSemEnriquecimento() != null ? notificacao.getSmsSemEnriquecimento() : 0) +
                                   (notificacao.getSmsComEnriquecimento() != null ? notificacao.getSmsComEnriquecimento() : 0);
                BigDecimal valorUnitario = produtosAssociado.get(codigoSms);
                
                if (totalSms > 0 && valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoSms,
                            "NOTIFICAÇÃO SPC SMS (" + totalSms + " unid.)",
                            totalSms,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("SMS TOTAL: " + totalSms + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ SMS TOTAL: {} x R$ {} = R$ {}", totalSms, valorUnitario, item.getValorTotal());
                } else {
                    log.debug("  ℹ️ SMS TOTAL: quantidade {} ou valor zero - ignorado", totalSms);
                }
            }
        }

        if (count == 0) {
            log.warn("⚠️ NENHUM item de notificação foi criado para associado {}", associado.getId());
            log.warn("   Verifique se há produtos configurados e quantidades > 0");
        } else {
            log.info("✅ Criados {} itens de notificação - Valor total: R$ {}", count, valorTotalNotificacoes);
            log.info("📋 Itens criados: {}", String.join(" | ", itensCriados));
        }

        return itens;
    }

    /**
     * 🔥 Calcula o valor total das notificações
     */
    private BigDecimal calcularValorTotalNotificacoes(NotificacaoAssociado notificacao) {
        BigDecimal total = BigDecimal.ZERO;
        
        BigDecimal valorSmsSemEnr = notificacao.getValorUnitarioSmsSemEnriquecimento() != null ? 
                notificacao.getValorUnitarioSmsSemEnriquecimento() : BigDecimal.ZERO;
        BigDecimal valorSmsComEnr = notificacao.getValorUnitarioSmsComEnriquecimento() != null ? 
                notificacao.getValorUnitarioSmsComEnriquecimento() : BigDecimal.ZERO;
        BigDecimal valorEmailSemEnr = notificacao.getValorUnitarioEmailSemEnriquecimento() != null ? 
                notificacao.getValorUnitarioEmailSemEnriquecimento() : BigDecimal.ZERO;
        BigDecimal valorEmailComEnr = notificacao.getValorUnitarioEmailComEnriquecimento() != null ? 
                notificacao.getValorUnitarioEmailComEnriquecimento() : BigDecimal.ZERO;
        BigDecimal valorCarta = notificacao.getValorUnitarioCarta() != null ? 
                notificacao.getValorUnitarioCarta() : BigDecimal.ZERO;

        total = total.add(BigDecimal.valueOf(notificacao.getSmsSemEnriquecimento()).multiply(valorSmsSemEnr));
        total = total.add(BigDecimal.valueOf(notificacao.getSmsComEnriquecimento()).multiply(valorSmsComEnr));
        total = total.add(BigDecimal.valueOf(notificacao.getEmailsSemEnriquecimento()).multiply(valorEmailSemEnr));
        total = total.add(BigDecimal.valueOf(notificacao.getEmailsComEnriquecimento()).multiply(valorEmailComEnr));
        total = total.add(BigDecimal.valueOf(notificacao.getCartasTotal()).multiply(valorCarta));

        return total;
    }

    /**
     * Cria um item de fatura
     */
    private FaturaItem criarItemFatura(String codigo, String descricao, Integer quantidade, BigDecimal valorUnitario, String tipoLancamento) {
        FaturaItem item = new FaturaItem();
        item.setCodigoProduto(codigo);
        item.setDescricao(descricao);
        item.setQuantidade(BigDecimal.valueOf(quantidade != null ? quantidade : 0));
        item.setValorUnitario(valorUnitario != null ? valorUnitario : BigDecimal.ZERO);
        item.setValorTotal(item.getQuantidade().multiply(item.getValorUnitario()));
        item.setTipoLancamento(tipoLancamento != null ? tipoLancamento : "D");
        return item;
    }

    // ========== MÉTODOS AUXILIARES EXISTENTES ==========

    private Map<String, List<ItemSPC>> processarNotasPorTipoArquivo(List<NotaDebitoSPC> notas, ReguaFaturamento regua) {
        Map<String, List<ItemSPC>> itensPorTipo = new HashMap<>();
        itensPorTipo.put("PREVIA_ANTERIOR", new ArrayList<>());
        itensPorTipo.put("CONSOLIDACAO", new ArrayList<>());
        itensPorTipo.put("PREVIA_CORRENTE", new ArrayList<>());

        if (notas == null || notas.isEmpty())
            return itensPorTipo;

        List<TipoArquivoRegua> tiposArquivo = regua != null ? regua.getTiposArquivo() : null;

        if (tiposArquivo != null && !tiposArquivo.isEmpty()) {
            tiposArquivo.sort(Comparator.comparing(TipoArquivoRegua::getOrdem));
            for (int i = 0; i < notas.size() && i < tiposArquivo.size(); i++) {
                NotaDebitoSPC nota = notas.get(i);
                String tipo = tiposArquivo.get(i).getTipo();
                if (nota.getItens() != null && !nota.getItens().isEmpty()) {
                    itensPorTipo.getOrDefault(tipo, new ArrayList<>()).addAll(nota.getItens());
                }
            }
        } else {
            List<String> sequencia = regua != null ? regua.getSequenciaArquivosList() : new ArrayList<>();
            for (int i = 0; i < notas.size() && i < sequencia.size(); i++) {
                NotaDebitoSPC nota = notas.get(i);
                String tipo = sequencia.get(i);
                if (nota.getItens() != null && !nota.getItens().isEmpty()) {
                    itensPorTipo.getOrDefault(tipo, new ArrayList<>()).addAll(nota.getItens());
                }
            }
        }
        return itensPorTipo;
    }

    private List<FaturaItem> calcularItensConformeFormula(Map<String, List<ItemSPC>> itensPorTipo,
            ReguaFaturamento regua) {
        List<ItemSPC> itensPreviaAnterior = itensPorTipo.getOrDefault("PREVIA_ANTERIOR", new ArrayList<>());
        List<ItemSPC> itensConsolidacao = itensPorTipo.getOrDefault("CONSOLIDACAO", new ArrayList<>());
        List<ItemSPC> itensPreviaCorrente = itensPorTipo.getOrDefault("PREVIA_CORRENTE", new ArrayList<>());

        Map<String, ItemSPC> consolidacaoMap = agruparItensPorChaveComposta(itensConsolidacao);
        Map<String, ItemSPC> previaAnteriorMap = agruparItensPorChaveComposta(itensPreviaAnterior);
        Map<String, ItemSPC> previaCorrenteMap = agruparItensPorChaveComposta(itensPreviaCorrente);

        List<FaturaItem> resultado = new ArrayList<>();

        for (Map.Entry<String, ItemSPC> entry : consolidacaoMap.entrySet()) {
            String chave = entry.getKey();
            ItemSPC itemConsolidacao = entry.getValue();
            ItemSPC itemPreviaAnterior = previaAnteriorMap.get(chave);
            ItemSPC itemPreviaCorrente = previaCorrenteMap.get(chave);

            BigDecimal qtdeConsolidada = getQuantidadeAsBigDecimal(itemConsolidacao);
            BigDecimal qtdePreviaAnterior = getQuantidadeAsBigDecimal(itemPreviaAnterior);
            BigDecimal qtdePreviaCorrente = getQuantidadeAsBigDecimal(itemPreviaCorrente);

            BigDecimal qtdeCalculada = (qtdeConsolidada.subtract(qtdePreviaAnterior)).add(qtdePreviaCorrente);

            if (qtdeCalculada.compareTo(BigDecimal.ZERO) <= 0)
                continue;

            String codigoProdutoRM = buscarCodigoProdutoRM(itemConsolidacao.getDescricaoServico());

            FaturaItem faturaItem = new FaturaItem();
            faturaItem.setDescricao(itemConsolidacao.getDescricaoServico());
            faturaItem
                    .setCodigoProduto(codigoProdutoRM != null ? codigoProdutoRM : itemConsolidacao.getCodigoProduto());
            faturaItem.setQuantidade(qtdeCalculada);
            faturaItem.setValorUnitario(itemConsolidacao.getValorUnitario());
            faturaItem.setValorTotal(qtdeCalculada.multiply(itemConsolidacao.getValorUnitario()));
            faturaItem.setTipoLancamento("D");
            resultado.add(faturaItem);
        }

        for (Map.Entry<String, ItemSPC> entry : previaCorrenteMap.entrySet()) {
            String chave = entry.getKey();
            if (!consolidacaoMap.containsKey(chave)) {
                ItemSPC item = entry.getValue();
                BigDecimal qtde = getQuantidadeAsBigDecimal(item);
                if (qtde.compareTo(BigDecimal.ZERO) > 0) {
                    String codigoProdutoRM = buscarCodigoProdutoRM(item.getDescricaoServico());

                    FaturaItem faturaItem = new FaturaItem();
                    faturaItem.setDescricao(item.getDescricaoServico());
                    faturaItem.setCodigoProduto(codigoProdutoRM != null ? codigoProdutoRM : item.getCodigoProduto());
                    faturaItem.setQuantidade(qtde);
                    faturaItem.setValorUnitario(item.getValorUnitario());
                    faturaItem.setValorTotal(qtde.multiply(item.getValorUnitario()));
                    faturaItem.setTipoLancamento("D");
                    resultado.add(faturaItem);
                }
            }
        }
        return resultado;
    }

    private Map<String, ItemSPC> agruparItensPorChaveComposta(List<ItemSPC> itens) {
        Map<String, ItemSPC> mapa = new LinkedHashMap<>();
        for (ItemSPC item : itens) {
            String codigo = item.getCodigoProduto() != null ? item.getCodigoProduto() : "SEM_CODIGO";
            String descricao = item.getDescricaoServico() != null ? item.getDescricaoServico() : "SEM_DESCRICAO";
            String chave = codigo + "|" + descricao;

            if (mapa.containsKey(chave)) {
                ItemSPC existente = mapa.get(chave);
                BigDecimal qtdeExistente = getQuantidadeAsBigDecimal(existente);
                BigDecimal qtdeAtual = getQuantidadeAsBigDecimal(item);
                existente.setQuantidadeServicos(qtdeExistente.add(qtdeAtual).intValue());
                BigDecimal valorExistente = existente.getValorTotal() != null ? existente.getValorTotal()
                        : BigDecimal.ZERO;
                BigDecimal valorAtual = item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO;
                existente.setValorTotal(valorExistente.add(valorAtual));
            } else {
                ItemSPC copia = new ItemSPC();
                copia.setCodigoProduto(codigo);
                copia.setDescricaoServico(descricao);
                copia.setQuantidadeServicos(item.getQuantidadeServicos());
                copia.setValorUnitario(item.getValorUnitario());
                copia.setValorTotal(item.getValorTotal());
                mapa.put(chave, copia);
            }
        }
        return mapa;
    }

    private BigDecimal getQuantidadeAsBigDecimal(ItemSPC item) {
        if (item == null)
            return BigDecimal.ZERO;
        if (item.getQuantidadeServicos() == null)
            return BigDecimal.ZERO;
        Object qtde = item.getQuantidadeServicos();
        if (qtde instanceof BigDecimal)
            return (BigDecimal) qtde;
        if (qtde instanceof Integer)
            return BigDecimal.valueOf((Integer) qtde);
        if (qtde instanceof Long)
            return BigDecimal.valueOf((Long) qtde);
        if (qtde instanceof String) {
            try {
                return new BigDecimal((String) qtde);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private List<CancelamentoImportacao> buscarCancelamentos(Associado associado, Integer mes, Integer ano) {
        try {
            String codigoAssociado = associado.getCodigoSpc();
            if (codigoAssociado == null || codigoAssociado.isEmpty())
                return new ArrayList<>();
            return cancelamentoRepository.findByCodigoAssociadoAndPeriodo(codigoAssociado, mes, ano);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<FaturaItem> removerServicosCancelados(List<FaturaItem> itens,
            List<CancelamentoImportacao> cancelamentos) {
        if (cancelamentos == null || cancelamentos.isEmpty())
            return itens;

        Set<String> codigosCancelados = new HashSet<>();
        for (CancelamentoImportacao cancelamento : cancelamentos) {
            if (cancelamento.getCodigoServico() != null)
                codigosCancelados.add(cancelamento.getCodigoServico());
            if (cancelamento.getDescricaoProduto() != null)
                codigosCancelados.add(cancelamento.getDescricaoProduto());
        }

        if (codigosCancelados.isEmpty())
            return itens;

        return itens.stream().filter(item -> !codigosCancelados.contains(item.getCodigoProduto())
                && !codigosCancelados.contains(item.getDescricao())).collect(Collectors.toList());
    }

    private String gerarNumeroFatura(Long associadoId, Integer mes, Integer ano) {
        String anoStr = ano != null ? String.valueOf(ano) : String.valueOf(LocalDate.now().getYear());
        String mesStr = mes != null ? String.format("%02d", mes)
                : String.format("%02d", LocalDate.now().getMonthValue());
        String seq = String.format("%06d", System.currentTimeMillis() % 1000000);
        return "FAT-" + anoStr + mesStr + "-" + associadoId + "-" + seq;
    }

    private LocalDate calcularDataEmissao(ReguaFaturamento regua) {
        LocalDate hoje = LocalDate.now();
        if (regua != null && regua.getDiaEmissao() != null) {
            int diaEmissao = regua.getDiaEmissao();
            try {
                LocalDate dataEmissao = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaEmissao);
                if (dataEmissao.isBefore(hoje))
                    dataEmissao = dataEmissao.plusMonths(1);
                return dataEmissao;
            } catch (Exception e) {
                return hoje;
            }
        }
        return hoje;
    }

    private LocalDate calcularDataVencimento(ReguaFaturamento regua) {
        if (regua != null && regua.getDiaVencimento() != null) {
            LocalDate hoje = LocalDate.now();
            int diaVencimento = regua.getDiaVencimento();
            try {
                LocalDate dataVencimento = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaVencimento);
                if (dataVencimento.isBefore(hoje))
                    dataVencimento = dataVencimento.plusMonths(1);
                return dataVencimento;
            } catch (Exception e) {
                return hoje.plusDays(10);
            }
        }
        return LocalDate.now().plusDays(10);
    }

    private String buscarCodigoProdutoRM(String descricaoServico) {
        if (descricaoServico == null || descricaoServico.trim().isEmpty()) {
            return null;
        }

        try {
            String descricaoSemAcento = removerAcentos(descricaoServico.trim());

            List<Produto> todosProdutos = produtoRepository.findAll();

            List<Produto> produtos = todosProdutos.stream().filter(p -> p.getDescricao() != null)
                    .filter(p -> removerAcentos(p.getDescricao()).equalsIgnoreCase(descricaoSemAcento))
                    .collect(Collectors.toList());

            if (produtos == null || produtos.isEmpty()) {
                produtos = produtoRepository.findByDescricaoContainingIgnoreCase(descricaoServico.trim());
            }

            if (produtos == null || produtos.isEmpty()) {
                log.warn("⚠️ Produto não encontrado para descrição: '{}'", descricaoServico);
                return null;
            }

            Produto produto = produtos.stream()
                    .filter(p -> p.getCodigoRm() != null && !p.getCodigoRm().trim().isEmpty())
                    .max(Comparator.comparing(Produto::getCodigoRm)).orElse(null);

            if (produto != null) {
                String codigoRM = produto.getCodigoRm();
                log.info("✅ Produto encontrado: '{}' -> ID: {}, Código RM: '{}'", descricaoServico, produto.getId(),
                        codigoRM);
                return codigoRM;
            }

            return null;
        } catch (Exception e) {
            log.error("❌ Erro ao buscar produto para descrição '{}': {}", descricaoServico, e.getMessage());
            return null;
        }
    }

    private String removerAcentos(String texto) {
        if (texto == null)
            return null;

        String normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
        return normalizado.replaceAll("[^\\p{ASCII}]", "");
    }
    
    /**
     * 🔥 Extrai o período de referência dos parâmetros SPC
     * Retorna um array com [mes, ano, dataInicio, dataFim]
     */
    private Object[] extrairPeriodoDosParametros(Associado associado) {
        Object[] resultado = new Object[4]; // [mes, ano, dataInicio, dataFim]
        
        try {
            String codigoSocio = associado.getCodigoSpc();
            if (codigoSocio == null || codigoSocio.isEmpty()) {
                log.warn("⚠️ Associado {} sem código SPC", associado.getId());
                return null;
            }
            
            log.info("🔍 Buscando parâmetros para o código SPC: {}", codigoSocio);
            
            Optional<ParametrosSPC> parametroOpt = parametrosSPCRepository
                    .findUltimoByCodigoSocio(codigoSocio);
            
            if (parametroOpt.isPresent()) {
                ParametrosSPC parametro = parametroOpt.get();
                String dataInicioStr = parametro.getDataInicioPeriodoRef();
                String dataFimStr = parametro.getDataFimPeriodoRef();
                
                if (dataInicioStr != null && dataFimStr != null && 
                    !dataInicioStr.isEmpty() && !dataFimStr.isEmpty()) {
                    
                    LocalDate dataInicio = parseDate(dataInicioStr);
                    LocalDate dataFim = parseDate(dataFimStr);
                    
                    if (dataInicio != null && dataFim != null) {
                        log.info("📅 Período encontrado nos parâmetros: {} à {}", dataInicio, dataFim);
                        log.info("   🔍 Código SPC: {}, IDs: {}", codigoSocio, parametro.getId());
                        
                        // 🔥 CORREÇÃO: Usar o mês da data de FIM do período
                        // Período: 26/05/2026 à 25/06/2026 → Competência: 06/2026
                        resultado[0] = dataFim.getMonthValue();  // mes (6 = Junho)
                        resultado[1] = dataFim.getYear();        // ano (2026)
                        resultado[2] = dataInicio;               // dataInicio (26/05/2026)
                        resultado[3] = dataFim;                 // dataFim (25/06/2026)
                        
                        log.info("📅 Competência: {}/{} (período: {} à {})", 
                                resultado[0], resultado[1], dataInicio, dataFim);
                        return resultado;
                    }
                }
            }
            
            log.warn("⚠️ Nenhum parâmetro com período válido para o código SPC: {}", codigoSocio);
            
        } catch (Exception e) {
            log.error("❌ Erro ao extrair período dos parâmetros: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * 🔥 Parse de data no formato DDMMYYYY
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.length() != 8) {
            return null;
        }
        try {
            int dia = Integer.parseInt(dateStr.substring(0, 2));
            int mes = Integer.parseInt(dateStr.substring(2, 4));
            int ano = Integer.parseInt(dateStr.substring(4, 8));
            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 🔥 Remove o item de notificação da nota de débito se a quantidade for igual
     * à soma das notificações digitais
     */
    private void removerItemNotificacaoDuplicado(List<FaturaItem> itens, NotificacaoAssociado notificacao) {
        
        log.info("========================================");
        log.info("🔍 INICIANDO REMOÇÃO DE ITEM DUPLICADO");
        log.info("========================================");
        
        // 1. VALIDAÇÕES INICIAIS
        if (itens == null || itens.isEmpty()) {
            log.warn("⚠️ Lista de itens está vazia ou nula");
            return;
        }
        
        if (notificacao == null) {
            log.warn("⚠️ Notificação é nula");
            return;
        }
        
        // 2. CALCULAR TOTAL DAS NOTIFICAÇÕES DIGITAIS
        int totalSms = notificacao.getSmsTotal() != null ? notificacao.getSmsTotal() : 0;
        int totalEmails = notificacao.getEmailsTotal() != null ? notificacao.getEmailsTotal() : 0;
        int totalCartas = notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
        int totalDigital = totalSms + totalEmails + totalCartas;
        
        log.info("📊 TOTAL NOTIFICAÇÕES DIGITAIS:");
        log.info("   SMS: {}", totalSms);
        log.info("   E-mail: {}", totalEmails);
        log.info("   Cartas: {}", totalCartas);
        log.info("   TOTAL: {}", totalDigital);
        log.info("");
        
        if (totalDigital == 0) {
            log.info("ℹ️ Nenhuma notificação digital para comparar");
            return;
        }
        
        // 3. LISTAR TODOS OS ITENS PARA ANÁLISE
        log.info("📋 LISTA DE ITENS NA FATURA ({} itens):", itens.size());
        for (int i = 0; i < itens.size(); i++) {
            FaturaItem item = itens.get(i);
            log.info("   [{}] Código: {}, Descrição: {}, Quantidade: {}", 
                    i, 
                    item.getCodigoProduto(), 
                    item.getDescricao(), 
                    item.getQuantidade());
        }
        log.info("");
        
        // 4. PROCURAR ITEM PARA REMOVER
        FaturaItem itemParaRemover = null;
        int indexParaRemover = -1;
        
        for (int i = 0; i < itens.size(); i++) {
            FaturaItem item = itens.get(i);
            String descricao = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
            String codigo = item.getCodigoProduto();
            BigDecimal qtd = item.getQuantidade();
            
            log.info("🔍 Verificando item [{}]:", i);
            log.info("   Código: {}", codigo);
            log.info("   Descrição: {}", descricao);
            log.info("   Quantidade: {}", qtd);
            
            // VERIFICAR SE É O ITEM DE NOTIFICAÇÃO DA NOTA
            boolean isNotificacaoNota = false;
            
            // 4a. Verificar pelo código RM específico (mais confiável)
            if (codigo != null && codigo.equals("04.01.03.07326")) {
                isNotificacaoNota = true;
                log.info("   ✅ Item identificado pelo código RM: {}", codigo);
            }
            // 4b. Verificar pela descrição
            else if (descricao.contains("NOTIFICACAO SPC/CN") || 
                     descricao.contains("NOTIFICACAO SPC") ||
                     descricao.contains("REGISTRO / NOTIFICACAO")) {
                isNotificacaoNota = true;
                log.info("   ✅ Item identificado pela descrição: {}", descricao);
            }
            // 4c. Verificar se contém NOTIFICACAO mas NÃO é dos itens digitais
            else if (descricao.contains("NOTIFICACAO") && 
                     !descricao.contains("CARTA") && 
                     !descricao.contains("SMS") && 
                     !descricao.contains("E-MAIL") &&
                     !descricao.contains("ENRIQUECIMENTO")) {
                isNotificacaoNota = true;
                log.info("   ✅ Item identificado por conter NOTIFICACAO: {}", descricao);
            }
            
            if (!isNotificacaoNota) {
                log.info("   ❌ Não é item de notificação da nota");
                continue;
            }
            
            // 5. VERIFICAR SE A QUANTIDADE É IGUAL
            if (qtd != null) {
                int qtdInt = qtd.intValue();
                log.info("   🔍 Comparando quantidade: {} vs {}", qtdInt, totalDigital);
                
                if (qtdInt == totalDigital) {
                    itemParaRemover = item;
                    indexParaRemover = i;
                    log.info("   ✅✅✅ ITEM ENCONTRADO PARA REMOVER! Quantidade igual!");
                    break;
                } else {
                    log.info("   ❌ Quantidade diferente ({} != {}), mantendo item", qtdInt, totalDigital);
                }
            } else {
                log.warn("   ⚠️ Quantidade é nula para este item");
            }
        }
        
        log.info("");
        
        // 6. REMOVER O ITEM SE ENCONTRADO
        if (itemParaRemover != null && indexParaRemover >= 0) {
            log.info("========================================");
            log.info("🗑️ REMOVENDO ITEM DA FATURA:");
            log.info("   Índice: {}", indexParaRemover);
            log.info("   Código: {}", itemParaRemover.getCodigoProduto());
            log.info("   Descrição: {}", itemParaRemover.getDescricao());
            log.info("   Quantidade: {}", itemParaRemover.getQuantidade());
            log.info("   Valor Unitário: {}", itemParaRemover.getValorUnitario());
            log.info("   Valor Total: {}", itemParaRemover.getValorTotal());
            log.info("========================================");
            
            itens.remove(indexParaRemover);
            
            log.info("✅ ITEM REMOVIDO COM SUCESSO!");
            log.info("📋 Itens restantes na fatura ({} itens):", itens.size());
            for (int i = 0; i < itens.size(); i++) {
                FaturaItem item = itens.get(i);
                log.info("   [{}] {} - Qtd: {}", i, item.getDescricao(), item.getQuantidade());
            }
        } else {
            log.warn("========================================");
            log.warn("⚠️ NENHUM ITEM ENCONTRADO PARA REMOVER");
            log.warn("   Total notificações digitais: {}", totalDigital);
            log.warn("   Motivos possíveis:");
            log.warn("   1. Item com quantidade igual não foi encontrado");
            log.warn("   2. O item não está na lista");
            log.warn("   3. O código/descrição não corresponde");
            log.warn("========================================");
        }
    }
    
    /**
     * 🔥 VERSÃO 2: Remove o item de notificação da nota de débito
     * Usa Iterator para remoção (mais seguro)
     */
    private void removerItemNotificacaoDuplicadoV2(List<FaturaItem> itens, NotificacaoAssociado notificacao) {
        
        log.info("🔍 ===== INICIANDO REMOÇÃO DE ITEM DUPLICADO (V2) =====");
        
        if (itens == null || itens.isEmpty()) {
            log.warn("⚠️ Lista de itens vazia ou nula");
            return;
        }
        
        if (notificacao == null) {
            log.warn("⚠️ Notificação é nula");
            return;
        }
        
        // 1. Calcular o total de notificações digitais
        int totalDigital = 0;
        totalDigital += notificacao.getSmsTotal() != null ? notificacao.getSmsTotal() : 0;
        totalDigital += notificacao.getEmailsTotal() != null ? notificacao.getEmailsTotal() : 0;
        totalDigital += notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
        
        log.info("📊 Total notificações digitais: {}", totalDigital);
        
        if (totalDigital == 0) {
            log.info("ℹ️ Nenhuma notificação digital, ignorando");
            return;
        }
        
        // 2. Encontrar e remover o item usando Iterator
        boolean removido = false;
        Iterator<FaturaItem> iterator = itens.iterator();
        
        while (iterator.hasNext()) {
            FaturaItem item = iterator.next();
            String desc = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
            String codigo = item.getCodigoProduto();
            BigDecimal qtd = item.getQuantidade();
            
            // Verificar se é o item de notificação da nota
            boolean isNotificacaoNota = false;
            
            // Pelo código RM específico
            if ("04.01.03.07326".equals(codigo)) {
                isNotificacaoNota = true;
            }
            // Pela descrição
            else if (desc.contains("NOTIFICACAO SPC/CN") || 
                     desc.contains("NOTIFICACAO SPC") ||
                     desc.contains("REGISTRO / NOTIFICACAO") ||
                     (desc.contains("NOTIFICACAO") && 
                      !desc.contains("CARTA") && 
                      !desc.contains("SMS") && 
                      !desc.contains("E-MAIL") &&
                      !desc.contains("ENRIQUECIMENTO"))) {
                isNotificacaoNota = true;
            }
            
            if (isNotificacaoNota && qtd != null && qtd.intValue() == totalDigital) {
                iterator.remove();
                removido = true;
                log.info("🗑️ ITEM REMOVIDO: {} (Código: {}) - Qtd: {}", 
                        item.getDescricao(), codigo, qtd);
                break;
            }
        }
        
        if (!removido) {
            log.info("ℹ️ Nenhum item duplicado encontrado para remover");
        }
    }

    
}