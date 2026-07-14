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
import com.sga.model.CancelamentoProcessado;
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

    @Autowired
    private CancelamentoService cancelamentoService;

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

        Object[] periodo = extrairPeriodoDosParametros(associado);
        
        if (periodo != null) {
            mesReferencia = (Integer) periodo[0];
            anoReferencia = (Integer) periodo[1];
            dataInicioPeriodo = (LocalDate) periodo[2];
            dataFimPeriodo = (LocalDate) periodo[3];
            log.info("📅 Período extraído dos parâmetros: {}/{} ({} à {})", 
                    mesReferencia, anoReferencia, dataInicioPeriodo, dataFimPeriodo);
        } else {
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

        if (notas == null || notas.isEmpty()) {
            log.warn("⚠️ Nenhuma nota encontrada para o associado");
            return null;
        }

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
            }
        }

        // ========== 3. PROCESSAR NOTAS ==========
        Map<String, List<ItemSPC>> itensPorTipo = processarNotasPorTipoArquivo(notas, regua);
        List<FaturaItem> itensCalculados = calcularItensConformeFormula(itensPorTipo, regua);

        // ========== 4. ADICIONAR ITENS DE NOTIFICAÇÕES ==========
        if (notificacao != null) {
            log.info("📊 Adicionando itens de notificações à fatura...");
            Map<String, BigDecimal> produtosAssociado = buscarProdutosNotificacaoAssociado(associado.getId());
            
            log.info("📦 Produtos encontrados: {}", produtosAssociado.size());
            for (Map.Entry<String, BigDecimal> entry : produtosAssociado.entrySet()) {
                log.info("   - {}: R$ {}", entry.getKey(), entry.getValue());
            }
            
            List<FaturaItem> itensNotificacao = criarItensNotificacao(notificacao, produtosAssociado, associado);
            if (!itensNotificacao.isEmpty()) {
                itensCalculados.addAll(itensNotificacao);
                log.info("✅ Adicionados {} itens de notificações", itensNotificacao.size());
            } else {
                log.warn("⚠️ Nenhum item de notificação foi criado");
            }
        } else {
            log.info("ℹ️ Sem notificações para este associado no período");
        }

        if (itensCalculados.isEmpty()) {
            log.warn("⚠️ Nenhum item calculado para o associado: {}", associado.getNomeRazao());
            return null;
        }
        
        // ========== REMOVER ITEM DUPLICADO ==========
        if (notificacao != null) {
            log.info("========================================");
            log.info("🔍 INICIANDO PROCESSO DE REMOÇÃO DE DUPLICADO");
            log.info("========================================");
            
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
        
        // 7.1 Franquia
        if (regua != null && Boolean.TRUE.equals(regua.getAplicarFranquia())) {
            log.info("📊 Aplicando regra de franquia para associado: {}", associado.getNomeRazao());
            fatura = franquiaRule.aplicarRegraFranquia(fatura, associado);
        }
        
        // 7.2 Faturamento mínimo
        if (regua != null && Boolean.TRUE.equals(regua.getAplicarFaturamentoMinimo())) {
            log.info("💰 Aplicando regra de faturamento mínimo...");
            log.info("   Valor atual: R$ {}", fatura.getValorTotal());
            fatura = faturamentoMinimoRule.aplicarRegraComplemento(fatura, associado, true);
            log.info("   Valor após faturamento mínimo: R$ {}", fatura.getValorTotal());
        } else {
            log.info("⏭️ Pular regra de faturamento mínimo");
        }
        
        // 7.3 Cancelamentos
        if (regua != null && Boolean.TRUE.equals(regua.getAplicarCancelamentos())) {
            log.info("🗑️ Verificando cancelamentos para associado: {}", associado.getNomeRazao());
            
            List<CancelamentoImportacao> cancelamentos = cancelamentoService.buscarCancelamentosPendentes(
                    associado.getCodigoSpc(), 
                    mesReferencia, 
                    anoReferencia);
            
            if (!cancelamentos.isEmpty()) {
                log.info("📋 Encontrados {} cancelamentos pendentes", cancelamentos.size());
                for (CancelamentoImportacao c : cancelamentos) {
                    log.info("   - ID: {}, Produto: {}", c.getId(), c.getProdutoPersonalizado());
                }
                
                List<CancelamentoProcessado> processados = cancelamentoService.aplicarCancelamentos(
                        fatura,
                        associado.getCodigoSpc(),
                        mesReferencia,
                        anoReferencia,
                        usuario,
                        simular);
                
                log.info("✅ {} cancelamentos aplicados {}", processados.size(), 
                        simular ? "(SIMULAÇÃO - NÃO PERSISTIDO)" : "(PERSISTIDO)");
            } else {
                log.info("ℹ️ Nenhum cancelamento pendente para o período");
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
            log.info("🔍 SIMULAÇÃO: Fatura NÃO salva no banco");
            return fatura;
        } else {
            // 9.1 SALVAR A FATURA
            Fatura faturaSalva = faturaRepository.save(fatura);
            log.info("✅ Fatura salva com ID: {}", faturaSalva.getId());
            
            // 9.2 SALVAR OS ITENS DA FATURA
            if (faturaSalva.getItens() != null && !faturaSalva.getItens().isEmpty()) {
                for (FaturaItem item : faturaSalva.getItens()) {
                    item.setFatura(faturaSalva);
                    faturaItemRepository.save(item);
                }
                log.info("✅ {} itens da fatura salvos", faturaSalva.getItens().size());
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
     * 🔥 MANTIDO: Busca por IDs fixos (17, 18, 19, 20, 21)
     */
    private Map<String, BigDecimal> buscarProdutosNotificacaoAssociado(Long associadoId) {
        Map<String, BigDecimal> produtos = new LinkedHashMap<>();
        
        try {
            // 🔥 IDs DOS PRODUTOS DE NOTIFICAÇÃO (FIXOS)
            List<Long> idsProdutos = Arrays.asList(17L, 18L, 19L, 20L, 21L);
            
            List<AssociadoProduto> lista = associadoProdutoRepository
                    .findByAssociadoIdAndProdutoIdIn(associadoId, idsProdutos);
            
            if (lista == null || lista.isEmpty()) {
                log.warn("⚠️ Nenhum produto de notificação configurado para associado {}", associadoId);
                return produtos;
            }
            
            log.info("✅ {} produtos de notificação encontrados para associado {}", lista.size(), associadoId);
            
            for (AssociadoProduto ap : lista) {
                Produto produto = ap.getProduto();
                if (produto != null && produto.getCodigoRm() != null) {
                    String codigoRm = produto.getCodigoRm();
                    BigDecimal valor = ap.getValorDefinido() != null ? ap.getValorDefinido() : BigDecimal.ZERO;
                    produtos.put(codigoRm, valor);
                    log.debug("📦 Produto encontrado: {} = R$ {}", codigoRm, valor);
                }
            }
            
            log.info("✅ {} produtos de notificação carregados para associado {}", produtos.size(), associadoId);
            
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
     * 🔥 CRIA ITENS DE FATURA A PARTIR DAS NOTIFICAÇÕES
     * APENAS ITENS COM QUANTIDADE > 0
     * 🔥 CORRIGIDO: Cria TODOS os itens com quantidade > 0, independentemente de ter produto configurado
     */
    private List<FaturaItem> criarItensNotificacao(NotificacaoAssociado notificacao, 
            Map<String, BigDecimal> produtosAssociado, Associado associado) {
        
        List<FaturaItem> itens = new ArrayList<>();
        
        if (notificacao == null || produtosAssociado == null || produtosAssociado.isEmpty()) {
            log.warn("⚠️ Notificação ou produtos do associado vazios");
            return itens;
        }
        
        log.info("📊 Criando itens de notificação para associado: {}", associado.getNomeRazao());
        
        // 🔥 LOG DETALHADO DOS PRODUTOS DO ASSOCIADO
        log.info("📦 Produtos de notificação do associado:");
        for (Map.Entry<String, BigDecimal> entry : produtosAssociado.entrySet()) {
            log.info("   - {}: R$ {}", entry.getKey(), entry.getValue());
        }
        
        // 🔥 LOG DETALHADO DAS QUANTIDADES
        int smsSemEnr = notificacao.getSmsSemEnriquecimento() != null ? notificacao.getSmsSemEnriquecimento() : 0;
        int smsComEnr = notificacao.getSmsComEnriquecimento() != null ? notificacao.getSmsComEnriquecimento() : 0;
        int emailSemEnr = notificacao.getEmailsSemEnriquecimento() != null ? notificacao.getEmailsSemEnriquecimento() : 0;
        int emailComEnr = notificacao.getEmailsComEnriquecimento() != null ? notificacao.getEmailsComEnriquecimento() : 0;
        int cartas = notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
        
        log.info("📊 Quantidades de notificação:");
        log.info("   SMS SEM ENR: {}", smsSemEnr);
        log.info("   SMS COM ENR: {}", smsComEnr);
        log.info("   E-mail SEM ENR: {}", emailSemEnr);
        log.info("   E-mail COM ENR: {}", emailComEnr);
        log.info("   Cartas: {}", cartas);
        
        // 🔥 VERIFICAR SE O ASSOCIADO TEM PRODUTOS COM ENRIQUECIMENTO
        boolean possuiEnriquecimento = associadoPossuiEnriquecimento(produtosAssociado);
        
        // 🔥 VERIFICAR SE EXISTEM NOTIFICAÇÕES COM ENRIQUECIMENTO
        boolean temNotificacoesComEnriquecimento = (smsComEnr > 0 || emailComEnr > 0);
        
        log.info("📌 Associado {} - {}", associado.getNomeRazao(), 
                possuiEnriquecimento ? "COM ENRIQUECIMENTO" : "SEM ENRIQUECIMENTO");
        
        if (temNotificacoesComEnriquecimento && !possuiEnriquecimento) {
            log.warn("⚠️ Associado possui notificações COM enriquecimento, mas não tem produtos configurados!");
            log.warn("   SMS COM ENR: {}, E-mail COM ENR: {}", smsComEnr, emailComEnr);
            log.warn("   🔥 Somando notificações com enriquecimento às sem enriquecimento");
        }
        
        int count = 0;
        BigDecimal valorTotalNotificacoes = BigDecimal.ZERO;
        List<String> itensCriados = new ArrayList<>();

        // ========== 1. CARTAS ==========
        String codigoCarta = "04.01.03.94343";
        if (cartas > 0 && produtosAssociado.containsKey(codigoCarta)) {
            BigDecimal valorUnitario = produtosAssociado.get(codigoCarta);
            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                FaturaItem item = criarItemFatura(
                        codigoCarta,
                        "NOTIFICAÇÃO SPC CARTA (" + cartas + " unid.)",
                        cartas,
                        valorUnitario,
                        "D"
                );
                itens.add(item);
                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                count++;
                itensCriados.add("CARTA: " + cartas + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                log.info("  ✅ CARTA: {} x R$ {} = R$ {}", cartas, valorUnitario, item.getValorTotal());
            }
        }

        // ========== 2. E-MAILS ==========
        if (possuiEnriquecimento) {
            // 🔥 COM ENRIQUECIMENTO: SEPARADO
            String codigoEmailSem = "04.01.03.94341";
            if (emailSemEnr > 0 && produtosAssociado.containsKey(codigoEmailSem)) {
                BigDecimal valorUnitario = produtosAssociado.get(codigoEmailSem);
                if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoEmailSem,
                            "NOTIFICAÇÃO SPC E-MAIL (" + emailSemEnr + " unid.)",
                            emailSemEnr,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("E-MAIL SEM ENR: " + emailSemEnr + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ E-MAIL SEM ENR: {} x R$ {} = R$ {}", emailSemEnr, valorUnitario, item.getValorTotal());
                }
            }
            
            String codigoEmailCom = "04.01.03.94432";
            if (emailComEnr > 0 && produtosAssociado.containsKey(codigoEmailCom)) {
                BigDecimal valorUnitario = produtosAssociado.get(codigoEmailCom);
                if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoEmailCom,
                            "NOTIFICAÇÃO SPC E-MAIL C/ ENRIQUECIMENTO (" + emailComEnr + " unid.)",
                            emailComEnr,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("E-MAIL COM ENR: " + emailComEnr + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ E-MAIL COM ENR: {} x R$ {} = R$ {}", emailComEnr, valorUnitario, item.getValorTotal());
                }
            }
        } else {
            // 🔥 SEM ENRIQUECIMENTO: SOMA TOTAL (INCLUINDO OS COM ENRIQUECIMENTO)
            int totalEmails = emailSemEnr + emailComEnr;
            String codigoEmail = "04.01.03.94341";
            if (totalEmails > 0 && produtosAssociado.containsKey(codigoEmail)) {
                BigDecimal valorUnitario = produtosAssociado.get(codigoEmail);
                if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
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
                }
            }
        }

        // ========== 3. SMS ==========
        if (possuiEnriquecimento) {
            // 🔥 COM ENRIQUECIMENTO: SEPARADO
            String codigoSmsSem = "04.01.03.94342";
            if (smsSemEnr > 0 && produtosAssociado.containsKey(codigoSmsSem)) {
                BigDecimal valorUnitario = produtosAssociado.get(codigoSmsSem);
                if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoSmsSem,
                            "NOTIFICAÇÃO SPC SMS (" + smsSemEnr + " unid.)",
                            smsSemEnr,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("SMS SEM ENR: " + smsSemEnr + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ SMS SEM ENR: {} x R$ {} = R$ {}", smsSemEnr, valorUnitario, item.getValorTotal());
                }
            }
            
            String codigoSmsCom = "04.01.03.94431";
            if (smsComEnr > 0 && produtosAssociado.containsKey(codigoSmsCom)) {
                BigDecimal valorUnitario = produtosAssociado.get(codigoSmsCom);
                if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem item = criarItemFatura(
                            codigoSmsCom,
                            "NOTIFICAÇÃO SPC SMS C/ ENRIQUECIMENTO (" + smsComEnr + " unid.)",
                            smsComEnr,
                            valorUnitario,
                            "D"
                    );
                    itens.add(item);
                    valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
                    count++;
                    itensCriados.add("SMS COM ENR: " + smsComEnr + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
                    log.info("  ✅ SMS COM ENR: {} x R$ {} = R$ {}", smsComEnr, valorUnitario, item.getValorTotal());
                }
            }
        } else {
            // 🔥 SEM ENRIQUECIMENTO: SOMA TOTAL (INCLUINDO OS COM ENRIQUECIMENTO)
            int totalSms = smsSemEnr + smsComEnr;
            String codigoSms = "04.01.03.94342";
            if (totalSms > 0 && produtosAssociado.containsKey(codigoSms)) {
                BigDecimal valorUnitario = produtosAssociado.get(codigoSms);
                if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
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
                }
            }
        }

        // 🔥 VERIFICAR SE A SOMA DOS ITENS CORRESPONDE AO TOTAL DIGITAL
        int totalItensCriados = 0;
        for (FaturaItem item : itens) {
            totalItensCriados += item.getQuantidade().intValue();
        }
        
        int totalDigitalEsperado = (notificacao.getSmsTotal() != null ? notificacao.getSmsTotal() : 0) +
                                   (notificacao.getEmailsTotal() != null ? notificacao.getEmailsTotal() : 0) +
                                   (notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0);
        
        log.info("📊 VERIFICAÇÃO: Total itens criados: {}, Total digital esperado: {}", 
                totalItensCriados, totalDigitalEsperado);
        
        if (totalItensCriados != totalDigitalEsperado) {
            log.warn("⚠️ INCONSISTÊNCIA: Total itens criados ({}) difere do total digital esperado ({})", 
                    totalItensCriados, totalDigitalEsperado);
            log.warn("   Verifique se todos os produtos de notificação estão configurados para o associado");
            if (temNotificacoesComEnriquecimento && !possuiEnriquecimento) {
                log.warn("   🔥 Corrigido: Notificações COM enriquecimento foram somadas às SEM enriquecimento");
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

    // ========== MÉTODOS AUXILIARES ==========

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
    
    private Object[] extrairPeriodoDosParametros(Associado associado) {
        Object[] resultado = new Object[4];
        
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
                        
                        resultado[0] = dataFim.getMonthValue();
                        resultado[1] = dataFim.getYear();
                        resultado[2] = dataInicio;
                        resultado[3] = dataFim;
                        
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
     * 🔥 Remove itens duplicados de notificação
     * Remove QUALQUER item que contenha "NOTIFICACAO" na descrição (exceto os detalhados)
     * quando houver mais de um com a mesma quantidade OU quando a quantidade for igual ao total digital
     */
    private void removerItemNotificacaoDuplicado(List<FaturaItem> itens, NotificacaoAssociado notificacao) {
        
        log.info("========================================");
        log.info("🔍 INICIANDO REMOÇÃO DE ITEM DUPLICADO");
        log.info("========================================");
        
        if (itens == null || itens.isEmpty()) {
            log.warn("⚠️ Lista de itens está vazia ou nula");
            return;
        }
        
        if (notificacao == null) {
            log.warn("⚠️ Notificação é nula");
            return;
        }
        
        int totalSms = notificacao.getSmsTotal() != null ? notificacao.getSmsTotal() : 0;
        int totalEmails = notificacao.getEmailsTotal() != null ? notificacao.getEmailsTotal() : 0;
        int totalCartas = notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
        int totalDigital = totalSms + totalEmails + totalCartas;
        
        log.info("📊 TOTAL NOTIFICAÇÕES DIGITAIS: {}", totalDigital);
        
        if (totalDigital == 0) {
            log.info("ℹ️ Nenhuma notificação digital para comparar");
            return;
        }
        
        log.info("📋 LISTA DE ITENS NA FATURA ({} itens):", itens.size());
        for (int i = 0; i < itens.size(); i++) {
            FaturaItem item = itens.get(i);
            log.info("   [{}] Código: {}, Descrição: {}, Quantidade: {}", 
                    i, item.getCodigoProduto(), item.getDescricao(), item.getQuantidade());
        }
        log.info("");
        
        List<FaturaItem> itensNotificacaoNota = new ArrayList<>();
        
        for (FaturaItem item : itens) {
            String descricao = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
            String codigo = item.getCodigoProduto();
            BigDecimal qtd = item.getQuantidade();
            
            if (qtd == null) continue;
            
            boolean isNotificacaoNota = false;
            
            if (codigo != null && codigo.equals("04.01.03.07326")) {
                isNotificacaoNota = true;
            } else if (descricao.contains("REGISTRO / NOTIFICACAO") || 
                     descricao.contains("REGISTRO/NOTIFICACAO")) {
                isNotificacaoNota = true;
            } else if (descricao.equals("NOTIFICACAO SPC") ||
                     descricao.contains("NOTIFICACAO SPC/CN") ||
                     (descricao.contains("NOTIFICACAO") && 
                      !descricao.contains("CARTA") && 
                      !descricao.contains("SMS") && 
                      !descricao.contains("E-MAIL") &&
                      !descricao.contains("ENRIQUECIMENTO"))) {
                isNotificacaoNota = true;
            }
            
            if (isNotificacaoNota) {
                itensNotificacaoNota.add(item);
                log.info("🔍 Item de notificação da nota encontrado: '{}' - Qtd: {}", 
                        item.getDescricao(), qtd.intValue());
            }
        }
        
        if (itensNotificacaoNota.isEmpty()) {
            log.info("ℹ️ Nenhum item de notificação da nota encontrado");
            log.info("========================================");
            return;
        }
        
        log.info("⚠️ ENCONTRADOS {} ITENS DE NOTIFICAÇÃO DA NOTA", itensNotificacaoNota.size());
        
        List<FaturaItem> itensParaRemover = new ArrayList<>();
        
        // Verificar duplicatas (mesma quantidade)
        Map<Integer, List<FaturaItem>> itensPorQuantidade = new LinkedHashMap<>();
        for (FaturaItem item : itensNotificacaoNota) {
            int qtd = item.getQuantidade().intValue();
            itensPorQuantidade.computeIfAbsent(qtd, k -> new ArrayList<>()).add(item);
        }
        
        for (Map.Entry<Integer, List<FaturaItem>> entry : itensPorQuantidade.entrySet()) {
            int quantidade = entry.getKey();
            List<FaturaItem> items = entry.getValue();
            
            if (items.size() >= 2) {
                itensParaRemover.addAll(items);
                log.info("📊 Quantidade {} tem {} itens duplicados - todos serão removidos", quantidade, items.size());
            }
        }
        
        // Verificar NOTIFICACAO SPC com quantidade igual ao total digital
        for (FaturaItem item : itensNotificacaoNota) {
            String desc = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
            int qtdItem = item.getQuantidade().intValue();
            
            boolean isNotificacaoSPC = desc.equals("NOTIFICACAO SPC") || 
                                       desc.contains("NOTIFICACAO SPC/CN") ||
                                       (desc.contains("NOTIFICACAO") && 
                                        !desc.contains("CARTA") && 
                                        !desc.contains("SMS") && 
                                        !desc.contains("E-MAIL") &&
                                        !desc.contains("ENRIQUECIMENTO") &&
                                        !desc.contains("REGISTRO"));
            
            if (isNotificacaoSPC && qtdItem == totalDigital) {
                if (!itensParaRemover.contains(item)) {
                    itensParaRemover.add(item);
                    log.info("📊 NOTIFICACAO SPC com quantidade {} igual ao total digital {} - será removido", 
                            qtdItem, totalDigital);
                }
            }
        }
        
        // Verificar REGISTRO / NOTIFICACAO com quantidade igual ao total digital
        for (FaturaItem item : itensNotificacaoNota) {
            String desc = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
            int qtdItem = item.getQuantidade().intValue();
            
            boolean isRegistro = desc.contains("REGISTRO / NOTIFICACAO") || 
                                desc.contains("REGISTRO/NOTIFICACAO");
            
            if (isRegistro && qtdItem == totalDigital) {
                if (!itensParaRemover.contains(item)) {
                    itensParaRemover.add(item);
                    log.info("📊 REGISTRO / NOTIFICACAO com quantidade {} igual ao total digital {} - será removido", 
                            qtdItem, totalDigital);
                }
            }
        }
        
        if (itensParaRemover.isEmpty()) {
            log.info("ℹ️ Nenhum item encontrado para remover");
            log.info("========================================");
            return;
        }
        
        log.info("");
        log.info("🗑️ REMOVENDO {} ITENS:", itensParaRemover.size());
        
        for (FaturaItem item : itensParaRemover) {
            if (itens.contains(item)) {
                itens.remove(item);
                log.info("   ✅ REMOVIDO: '{}' - Qtd: {}", 
                        item.getDescricao(), item.getQuantidade());
            } else {
                log.warn("   ⚠️ Item já foi removido: '{}'", item.getDescricao());
            }
        }
        
        log.info("");
        log.info("📋 Itens restantes na fatura ({} itens):", itens.size());
        for (int i = 0; i < itens.size(); i++) {
            FaturaItem item = itens.get(i);
            log.info("   [{}] {} - Qtd: {}", i, item.getDescricao(), item.getQuantidade());
        }
        
        log.info("========================================");
        log.info("✅ REMOÇÃO DE DUPLICADOS CONCLUÍDA!");
        log.info("========================================");
    }
}