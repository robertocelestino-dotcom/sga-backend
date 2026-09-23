package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	private AssociadoService associadoService;

	@Autowired
	private ReguaFaturamentoService reguaService;
	
	@Autowired
	private CacheService cacheService;   // 🔥 NOVO

	// @Autowired
	// private FranquiaRuleOLD2 franquiaRule;

	// @Autowired
	// private FranquiaRule_OLD3 franquiaRule;

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

	@Autowired
	private FaturamentoExtemporaneoService faturamentoExtemporaneoService;

	@Autowired
	private LogFaturaService logFaturaService;

	// ========== BUFFER DE LOGS PARA FATURAS SEM ID ==========
	private final List<LogEntry> logBuffer = new ArrayList<>();

	private static class LogEntry {
		String nivel;
		String mensagem;
		String passo;
		LocalDateTime dataHora;

		LogEntry(String nivel, String mensagem, String passo) {
			this.nivel = nivel;
			this.mensagem = mensagem;
			this.passo = passo;
			this.dataHora = LocalDateTime.now();
		}
	}

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

	// ========== MÉTODOS DE LOG ==========

	/**
	 * Adiciona um log de processamento (usa buffer se fatura sem ID)
	 */
	private void adicionarLogFatura(Fatura fatura, String nivel, String mensagem, String passo) {
		try {
			if (fatura == null) {
				log.debug("⏳ Fatura nula - Log não persistido: {}", mensagem);
				return;
			}

			// Se a fatura já tem ID, persiste direto
			if (fatura.getId() != null) {
				logFaturaService.adicionarLog(fatura, nivel, mensagem, passo);
			} else {
				// Se não tem ID, adiciona ao buffer
				logBuffer.add(new LogEntry(nivel, mensagem, passo));
				log.debug("📝 [BUFFER] {} - {}: {}", passo, nivel, mensagem);
			}
		} catch (Exception e) {
			log.error("❌ Erro ao adicionar log da fatura: {}", e.getMessage());
		}
	}

	/**
	 * Persiste todos os logs do buffer na fatura
	 */
	private void persistirLogs(Fatura fatura) {
		if (fatura == null || fatura.getId() == null) {
			log.warn("⚠️ Fatura sem ID, logs não persistidos");
			return;
		}

		int count = 0;
		for (LogEntry entry : logBuffer) {
			logFaturaService.adicionarLog(fatura, entry.nivel, entry.mensagem, entry.passo);
			count++;
		}
		if (count > 0) {
			log.info("✅ {} logs persistidos para a fatura {}", count, fatura.getId());
		}
		logBuffer.clear();
	}

	// ========== MÉTODO PRINCIPAL DE GERAÇÃO DE FATURA ==========

	@Transactional
	public Fatura gerarFatura(Associado associado, List<NotaDebitoSPC> notas, ReguaFaturamento regua, Integer mes,
	        Integer ano, LocalDate dataEmissao, LocalDate dataVencimento, boolean simular, String usuario) {

	    log.info("========================================");
	    log.info("🔍 INICIANDO GERAÇÃO DE FATURA");
	    log.info("========================================");
	    log.info("📄 Associado: {} (ID: {}, Código SPC: {})", associado.getNomeRazao(), associado.getId(),
	            associado.getCodigoSpc());
	    log.info("📋 Régua: {} (ID: {})", regua != null ? regua.getDescricao() : "N/A",
	            regua != null ? regua.getId() : "N/A");
	    log.info("📋 Notas recebidas: {}", notas != null ? notas.size() : 0);
	    log.info("🔍 Modo: {}", simular ? "SIMULAÇÃO" : "PROCESSAMENTO REAL");
	    log.info("========================================");

	    // Limpar buffer de logs a cada nova fatura
	    logBuffer.clear();

	    Fatura faturaTemp = new Fatura();
	    faturaTemp.setAssociado(associado);
	    adicionarLogFatura(faturaTemp, "INFO",
	            "Iniciando geração de fatura para associado: " + associado.getNomeRazao(), "INICIO");

	    // ========== 1. EXTRAIR PERÍODO DOS PARÂMETROS ==========
	    log.info("📅 PASSO 1: EXTRAINDO PERÍODO DOS PARÂMETROS");
	    log.info("----------------------------------------");

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

	        log.info("📅 Período extraído: {}/{}", mesReferencia, anoReferencia);
	        log.info("📅 Data Início: {}, Data Fim: {}", dataInicioPeriodo, dataFimPeriodo);

	        adicionarLogFatura(faturaTemp, "INFO", "Período extraído: " + mesReferencia + "/" + anoReferencia
	                + " (Início: " + dataInicioPeriodo + ", Fim: " + dataFimPeriodo + ")", "EXTRAIR_PERIODO");
	    } else {
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
	        log.warn("⚠️ Usando período fallback: {}/{}", mesReferencia, anoReferencia);
	        adicionarLogFatura(faturaTemp, "WARN", "Período fallback utilizado: " + mesReferencia + "/" + anoReferencia,
	                "EXTRAIR_PERIODO");
	    }

	    if (notas == null || notas.isEmpty()) {
	        log.warn("⚠️ Nenhuma nota encontrada para o associado");
	        adicionarLogFatura(faturaTemp, "ERROR",
	                "Nenhuma nota encontrada para o associado " + associado.getNomeRazao(), "VALIDAR_NOTAS");
	        return null;
	    }

	    log.info("📋 Total de notas: {}", notas.size());
	    adicionarLogFatura(faturaTemp, "INFO", "Total de notas encontradas: " + notas.size(), "VALIDAR_NOTAS");

	    if (!simular) {
	        List<Fatura> faturasExistentes = faturaRepository
	                .findByAssociadoIdAndMesReferenciaAndAnoReferencia(associado.getId(), mesReferencia, anoReferencia);
	        if (!faturasExistentes.isEmpty()) {
	            log.warn("⚠️ Já existe fatura para o período {}/{}", mesReferencia, anoReferencia);
	            adicionarLogFatura(faturaTemp, "WARN",
	                    "Fatura já existe para o período " + mesReferencia + "/" + anoReferencia,
	                    "VALIDAR_DUPLICIDADE");
	            return faturasExistentes.get(0);
	        }
	    }

	    // ========== 2. BUSCAR NOTIFICAÇÕES DO ASSOCIADO ==========
	    log.info("📱 PASSO 2: BUSCANDO NOTIFICAÇÕES DO ASSOCIADO");
	    log.info("----------------------------------------");

	    NotificacaoAssociado notificacao = null;
	    if (mesReferencia != null && anoReferencia != null) {
	        log.info("🔍 Buscando notificações para associado {} no período {}/{}", associado.getId(), mesReferencia,
	                anoReferencia);

	        notificacao = notificacaoAssociadoRepository
	                .findByAssociadoIdAndMesReferenciaAndAnoReferencia(associado.getId(), mesReferencia, anoReferencia)
	                .orElse(null);

	        if (notificacao != null) {
	            log.info("✅ NOTIFICAÇÕES ENCONTRADAS!");
	            log.info("📊 SMS: {} total ({} sem / {} com enriquecimento)", notificacao.getSmsTotal(),
	                    notificacao.getSmsSemEnriquecimento(), notificacao.getSmsComEnriquecimento());
	            log.info("📧 E-mail: {} total ({} sem / {} com enriquecimento)", notificacao.getEmailsTotal(),
	                    notificacao.getEmailsSemEnriquecimento(), notificacao.getEmailsComEnriquecimento());
	            log.info("📬 Cartas: {}", notificacao.getCartasTotal());

	            adicionarLogFatura(faturaTemp, "INFO",
	                    "Notificações encontradas: SMS=" + notificacao.getSmsTotal() + " (Sem ENR: "
	                            + notificacao.getSmsSemEnriquecimento() + ", Com ENR: "
	                            + notificacao.getSmsComEnriquecimento() + ")" + ", E-mail="
	                            + notificacao.getEmailsTotal() + ", Cartas=" + notificacao.getCartasTotal(),
	                    "BUSCAR_NOTIFICACOES");
	        } else {
	            log.warn("⚠️ NENHUMA NOTIFICAÇÃO encontrada para o período");
	            adicionarLogFatura(faturaTemp, "WARN",
	                    "Nenhuma notificação encontrada para o período " + mesReferencia + "/" + anoReferencia,
	                    "BUSCAR_NOTIFICACOES");
	        }
	    }

	    // ========== 3. PROCESSAR NOTAS ==========
	    log.info("📊 PASSO 3: PROCESSANDO NOTAS");
	    log.info("----------------------------------------");

	    List<FaturaItem> itensCalculados = new ArrayList<>();

	    boolean isExtemporaneo = isReguaExtemporanea(regua);

	    if (isExtemporaneo) {
	        log.info("📊 USANDO FATURAMENTO EXTEMPORÂNEO - Régua ID: {}", regua.getId());
	        adicionarLogFatura(faturaTemp, "INFO", "Processando com FATURAMENTO EXTEMPORÂNEO - Régua: " + regua.getId(),
	                "TIPO_FATURAMENTO");

	        itensCalculados = faturamentoExtemporaneoService.processarExtemporaneo(notas);

	        log.info("📊 Itens calculados (extemporâneo): {}", itensCalculados.size());
	        adicionarLogFatura(faturaTemp, "INFO", "Itens calculados (extemporâneo): " + itensCalculados.size(),
	                "PROCESSAR_NOTAS");

	        // 🔥 NOTA: As notificações são adicionadas APENAS no PASSO 3.3 abaixo.
	        // A adição duplicada que existia aqui foi REMOVIDA.

	    } else {
	        // ========== PROCESSAMENTO NORMAL (EXISTENTE) ==========
	        log.info("📊 USANDO FATURAMENTO NORMAL - Régua ID: {}", regua != null ? regua.getId() : "N/A");
	        adicionarLogFatura(faturaTemp, "INFO",
	                "Processando com FATURAMENTO NORMAL - Régua: " + (regua != null ? regua.getId() : "N/A"),
	                "TIPO_FATURAMENTO");

	        log.info("📊 PASSO 3.1: CLASSIFICANDO NOTAS POR TIPO DE ARQUIVO");
	        log.info("----------------------------------------");

	        Map<String, List<ItemSPC>> itensPorTipo = processarNotasPorTipoArquivo(notas, regua, faturaTemp);

	        int totalPreviaAnterior = itensPorTipo.getOrDefault("PREVIA_ANTERIOR", new ArrayList<>()).size();
	        int totalConsolidacao = itensPorTipo.getOrDefault("CONSOLIDACAO", new ArrayList<>()).size();
	        int totalPreviaCorrente = itensPorTipo.getOrDefault("PREVIA_CORRENTE", new ArrayList<>()).size();

	        log.info("📋 Classificação concluída:");
	        log.info("   - Prévia Anterior: {} itens", totalPreviaAnterior);
	        log.info("   - Consolidação: {} itens", totalConsolidacao);
	        log.info("   - Prévia Corrente: {} itens", totalPreviaCorrente);

	        log.info("📊 PASSO 3.2: CALCULANDO ITENS CONFORME FÓRMULA");
	        log.info("----------------------------------------");
	        log.info("📋 Fórmula: (Consolidado - Prévia Anterior) + Prévia Corrente");

	        itensCalculados = calcularItensConformeFormula(itensPorTipo, regua, faturaTemp);

	        log.info("📊 Itens calculados: {}", itensCalculados.size());
	    }

	    // ========== 3.3 Adicionar notificações (ÚNICO PONTO DE ADIÇÃO) ==========
	    log.info("📱 PASSO 3.3: ADICIONANDO NOTIFICAÇÕES");
	    log.info("----------------------------------------");

	    if (notificacao != null) {
	        log.info("📊 Adicionando itens de notificações à fatura...");
	        Map<String, BigDecimal> produtosAssociado = buscarProdutosNotificacaoAssociado(associado.getId());

	        log.info("📦 Produtos de notificação encontrados: {}", produtosAssociado.size());
	        for (Map.Entry<String, BigDecimal> entry : produtosAssociado.entrySet()) {
	            log.info("   - {}: R$ {}", entry.getKey(), entry.getValue());
	        }

	        List<FaturaItem> itensNotificacao = criarItensNotificacao(notificacao, produtosAssociado, associado,
	                faturaTemp);
	        if (!itensNotificacao.isEmpty()) {
	            itensCalculados.addAll(itensNotificacao);
	            log.info("✅ Adicionados {} itens de notificações", itensNotificacao.size());
	            adicionarLogFatura(faturaTemp, "INFO",
	                    "Adicionados " + itensNotificacao.size() + " itens de notificações", "ADICIONAR_NOTIFICACOES");
	        } else {
	            log.warn("⚠️ Nenhum item de notificação foi criado");
	            adicionarLogFatura(faturaTemp, "WARN", "Nenhum item de notificação foi criado",
	                    "ADICIONAR_NOTIFICACOES");
	        }
	    } else {
	        log.info("ℹ️ Sem notificações para este associado no período");
	        adicionarLogFatura(faturaTemp, "INFO", "Sem notificações para o período", "ADICIONAR_NOTIFICACOES");
	    }

	    // ========== 3.4 Remover itens duplicados ==========
	    log.info("🗑️ PASSO 3.4: REMOVENDO ITENS DUPLICADOS");
	    log.info("----------------------------------------");

	    if (notificacao != null && !itensCalculados.isEmpty()) {
	        int antes = itensCalculados.size();
	        removerItemNotificacaoDuplicado(itensCalculados, notificacao, faturaTemp);
	        int depois = itensCalculados.size();
	        log.info("📊 Itens removidos: {} (antes: {}, depois: {})", antes - depois, antes, depois);
	        adicionarLogFatura(faturaTemp, "INFO", "Remoção de duplicados: " + (antes - depois) + " itens removidos",
	                "REMOVER_DUPLICADOS");
	    }

	    if (itensCalculados.isEmpty()) {
	        log.warn("⚠️ Nenhum item calculado para o associado: {}", associado.getNomeRazao());
	        adicionarLogFatura(faturaTemp, "WARN", "Nenhum item calculado para o associado", "VALIDAR_ITENS");
	        return null;
	    }

	    log.info("📊 Total de itens após processamento: {}", itensCalculados.size());
	    adicionarLogFatura(faturaTemp, "INFO", "Total de itens após processamento: " + itensCalculados.size(),
	            "VALIDAR_ITENS");

	    // ========== 4. CRIAR FATURA ==========
	    log.info("📄 PASSO 4: CRIANDO FATURA");
	    log.info("----------------------------------------");

	    Fatura fatura = new Fatura();
	    fatura.setAssociado(associado);
	    fatura.setNumeroFatura(gerarNumeroFatura(associado.getId(), mesReferencia, anoReferencia));

	    log.info("📋 Número da fatura: {}", fatura.getNumeroFatura());
	    adicionarLogFatura(fatura, "INFO", "Criando fatura: " + fatura.getNumeroFatura(), "CRIAR_FATURA");

	    if (notas != null && !notas.isEmpty()) {
	        NotaDebitoSPC notaPrincipal = notas.get(0);
	        fatura.setNotaDebitoId(notaPrincipal.getId());
	        log.info("🔗 Vinculada à nota de débito ID: {}", notaPrincipal.getId());
	        adicionarLogFatura(fatura, "INFO", "Vinculada à nota de débito ID: " + notaPrincipal.getId(),
	                "VINCULAR_NOTA");
	    } else {
	        log.warn("⚠️ Nenhuma nota disponível para associar à fatura");
	        adicionarLogFatura(fatura, "WARN", "Nenhuma nota disponível para associar à fatura", "VINCULAR_NOTA");
	    }

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

	    log.info("📅 Data Emissão: {}, Data Vencimento: {}", fatura.getDataEmissao(), fatura.getDataVencimento());
	    adicionarLogFatura(fatura, "INFO",
	            "Data Emissão: " + fatura.getDataEmissao() + ", Data Vencimento: " + fatura.getDataVencimento(),
	            "DEFINIR_DATAS");

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
	    log.info("💰 Valor total da fatura: R$ {}", fatura.getValorTotal());
	    adicionarLogFatura(fatura, "INFO", "Valor total da fatura: R$ " + fatura.getValorTotal(), "RECALCULAR_TOTAL");

	    // ========== 5. ADICIONAR OBSERVAÇÃO ==========
	    if (notificacao != null) {
	        BigDecimal valorNotificacoes = calcularValorTotalNotificacoes(notificacao);
	        String tipoFaturamento = isExtemporaneo ? "EXTEMPORÂNEO" : "NORMAL";
	        String observacao = String.format(
	                "📊 FATURAMENTO %s - %02d/%d\n" + "📱 SMS: %d total (%d sem / %d com enriquecimento)\n"
	                        + "📧 E-mails: %d total (%d sem / %d com enriquecimento)\n" + "📬 Cartas: %d\n"
	                        + "💰 Valor Total das Notificações: R$ %.2f",
	                tipoFaturamento, mesReferencia, anoReferencia, notificacao.getSmsTotal(),
	                notificacao.getSmsSemEnriquecimento(), notificacao.getSmsComEnriquecimento(),
	                notificacao.getEmailsTotal(), notificacao.getEmailsSemEnriquecimento(),
	                notificacao.getEmailsComEnriquecimento(), notificacao.getCartasTotal(), valorNotificacoes);
	        fatura.setObservacao(observacao);
	        log.info("📝 Observação adicionada à fatura");
	        adicionarLogFatura(fatura, "INFO", "Observação adicionada à fatura", "ADICIONAR_OBSERVACAO");
	    }

	    // ========== 6. APLICAR REGRAS ==========
	    log.info("⚖️ PASSO 6: APLICANDO REGRAS DE NEGÓCIO");
	    log.info("----------------------------------------");

	    // 6.1 Franquia
	    log.info("📊 6.1 Aplicando regra de franquia");
	    if (regua != null && Boolean.TRUE.equals(regua.getAplicarFranquia())) {
	        log.info("📊 Franquia: ATIVADA");
	        adicionarLogFatura(fatura, "INFO", "Aplicando regra de franquia (ATIVADA)", "APLICAR_FRANQUIA");

	        BigDecimal valorAntes = fatura.getValorTotal();
	        log.info("💰 Valor antes da franquia: R$ {}", valorAntes);

	        fatura = franquiaRule.aplicarRegraFranquia(fatura, associado);

	        BigDecimal valorDepois = fatura.getValorTotal();
	        log.info("💰 Valor depois da franquia: R$ {}", valorDepois);
	        log.info("📊 Diferença: R$ {}", valorDepois.subtract(valorAntes));

	        adicionarLogFatura(fatura, "INFO",
	                "Franquia aplicada - Antes: R$ " + valorAntes + ", Depois: R$ " + valorDepois, "APLICAR_FRANQUIA");
	    } else {
	        log.info("⏭️ Franquia: DESATIVADA");
	        adicionarLogFatura(fatura, "INFO", "Regra de franquia pulada (DESATIVADA)", "APLICAR_FRANQUIA");
	    }

	    // 6.2 Faturamento mínimo
	    log.info("📊 6.2 Aplicando regra de faturamento mínimo");
	    if (regua != null && Boolean.TRUE.equals(regua.getAplicarFaturamentoMinimo())) {
	        log.info("📊 Faturamento Mínimo: ATIVADO");
	        adicionarLogFatura(fatura, "INFO", "Aplicando regra de faturamento mínimo (ATIVADO)", "APLICAR_MINIMO");

	        BigDecimal valorAntes = fatura.getValorTotal();
	        log.info("💰 Valor antes do faturamento mínimo: R$ {}", valorAntes);

	        fatura = faturamentoMinimoRule.aplicarRegraComplemento(fatura, associado, true);

	        BigDecimal valorDepois = fatura.getValorTotal();
	        log.info("💰 Valor depois do faturamento mínimo: R$ {}", valorDepois);
	        log.info("📊 Diferença: R$ {}", valorDepois.subtract(valorAntes));

	        adicionarLogFatura(fatura, "INFO",
	                "Faturamento mínimo aplicado - Antes: R$ " + valorAntes + ", Depois: R$ " + valorDepois,
	                "APLICAR_MINIMO");
	    } else {
	        log.info("⏭️ Faturamento Mínimo: DESATIVADO");
	        adicionarLogFatura(fatura, "INFO", "Regra de faturamento mínimo pulada (DESATIVADO)", "APLICAR_MINIMO");
	    }

	    // 6.3 Cancelamentos
	    log.info("🗑️ 6.3 Verificando cancelamentos");
	    if (regua != null && Boolean.TRUE.equals(regua.getAplicarCancelamentos())) {
	        log.info("📊 Cancelamentos: ATIVADO");
	        adicionarLogFatura(fatura, "INFO", "Verificando cancelamentos (ATIVADO)", "APLICAR_CANCELAMENTOS");

	        List<CancelamentoImportacao> cancelamentos = cancelamentoService
	                .buscarCancelamentosPendentes(associado.getCodigoSpc(), mesReferencia, anoReferencia);

	        if (!cancelamentos.isEmpty()) {
	            log.info("📋 Encontrados {} cancelamentos pendentes", cancelamentos.size());
	            for (CancelamentoImportacao c : cancelamentos) {
	                log.info("   - ID: {}, Produto: {}", c.getId(), c.getProdutoPersonalizado());
	            }

	            adicionarLogFatura(fatura, "INFO", "Encontrados " + cancelamentos.size() + " cancelamentos pendentes",
	                    "APLICAR_CANCELAMENTOS");

	            BigDecimal valorAntes = fatura.getValorTotal();
	            log.info("💰 Valor antes dos cancelamentos: R$ {}", valorAntes);

	            List<CancelamentoProcessado> processados = cancelamentoService.aplicarCancelamentos(fatura,
	                    associado.getCodigoSpc(), mesReferencia, anoReferencia, usuario, simular);

	            BigDecimal valorDepois = fatura.getValorTotal();
	            log.info("💰 Valor depois dos cancelamentos: R$ {}", valorDepois);
	            log.info("📊 Diferença: R$ {}", valorDepois.subtract(valorAntes));

	            adicionarLogFatura(fatura, "INFO", "Cancelamentos aplicados: " + processados.size() + " - Antes: R$ "
	                    + valorAntes + ", Depois: R$ " + valorDepois, "APLICAR_CANCELAMENTOS");
	        } else {
	            log.info("ℹ️ Nenhum cancelamento pendente para o período");
	            adicionarLogFatura(fatura, "INFO", "Nenhum cancelamento pendente para o período",
	                    "APLICAR_CANCELAMENTOS");
	        }
	    } else {
	        log.info("⏭️ Cancelamentos: DESATIVADO");
	        adicionarLogFatura(fatura, "INFO", "Verificação de cancelamentos pulada (DESATIVADO)",
	                "APLICAR_CANCELAMENTOS");
	    }

	    // ========== 7. MARCAR NOTIFICAÇÕES COMO FATURADAS ==========
	    log.info("📱 PASSO 7: MARCANDO NOTIFICAÇÕES COMO FATURADAS");
	    log.info("----------------------------------------");

	    if (notificacao != null && !simular) {
	        notificacao.setProcessadoFatura(true);
	        notificacao.setFaturaId(fatura.getId());
	        notificacao.setDataProcessamento(LocalDateTime.now());
	        notificacaoAssociadoRepository.save(notificacao);
	        log.info("✅ Notificações marcadas como faturadas para associado {}", associado.getId());
	        adicionarLogFatura(fatura, "INFO", "Notificações marcadas como faturadas - ID: " + notificacao.getId(),
	                "MARCAR_NOTIFICACOES");
	    }

	    // ========== 8. SALVAR ==========
	    log.info("💾 PASSO 8: SALVANDO FATURA");
	    log.info("----------------------------------------");

	    if (simular) {
	    	
	        log.info("🔍 SIMULAÇÃO: Fatura NÃO salva no banco");
	        
	        adicionarLogFatura(fatura, "INFO", "SIMULAÇÃO - Fatura NÃO persistida", "FINALIZAR");

	        persistirLogs(fatura);

	        log.info("========================================");
	        log.info("✅ SIMULAÇÃO CONCLUÍDA");
	        log.info("========================================");
	        
	        return fatura;
	        
	    } else {
	       /* 
	    	Fatura faturaSalva = faturaRepository.save(fatura);
	        
	    	log.info("✅ Fatura salva com ID: {}", faturaSalva.getId());

	        persistirLogs(faturaSalva);

	        if (faturaSalva.getItens() != null && !faturaSalva.getItens().isEmpty()) {
	            for (FaturaItem item : faturaSalva.getItens()) {
	                item.setFatura(faturaSalva);
	                faturaItemRepository.save(item);
	            }
	            log.info("✅ {} itens da fatura salvos", faturaSalva.getItens().size());
	            adicionarLogFatura(faturaSalva, "INFO", faturaSalva.getItens().size() + " itens da fatura salvos",
	                    "PERSISTIR_ITENS");
	        }

	        adicionarLogFatura(faturaSalva, "INFO",
	                "FATURA GERADA COM SUCESSO - Valor: R$ " + faturaSalva.getValorTotal(), "FINALIZAR");

	        log.info("========================================");
	        log.info("✅ FATURA GERADA COM SUCESSO!");
	        log.info("📄 ID: {}, Número: {}", faturaSalva.getId(), faturaSalva.getNumeroFatura());
	        log.info("💰 Valor: R$ {}", faturaSalva.getValorTotal());
	        log.info("👤 Associado: {}", associado.getNomeRazao());
	        log.info("========================================");
			*/
	        //return faturaSalva;
	    	return fatura;
	    }
	}

	// ========== MÉTODO PARA VERIFICAR RÉGUA EXTEMPORÂNEA ==========

	private boolean isReguaExtemporanea(ReguaFaturamento regua) {
		if (regua == null || regua.getId() == null) {
			return false;
		}
		Long id = regua.getId();
		return id == 7L || id == 8L;
	}

	// ========== MÉTODOS DE NOTIFICAÇÃO ==========

	private Map<String, BigDecimal> buscarProdutosNotificacaoAssociado(Long associadoId) {
		Map<String, BigDecimal> produtos = new LinkedHashMap<>();

		try {
			List<Long> idsProdutos = Arrays.asList(17L, 18L, 19L, 20L, 21L);

			List<AssociadoProduto> lista = associadoProdutoRepository.findByAssociadoIdAndProdutoIdIn(associadoId,
					idsProdutos);

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

		} catch (Exception e) {
			log.error("❌ Erro ao buscar produtos de notificação para associado {}: {}", associadoId, e.getMessage());
		}

		return produtos;
	}

	private boolean associadoPossuiEnriquecimento(Map<String, BigDecimal> produtosAssociado) {
		return produtosAssociado.containsKey("04.01.03.94432") || produtosAssociado.containsKey("04.01.03.94431");
	}

	private List<FaturaItem> criarItensNotificacao(NotificacaoAssociado notificacao,
	        Map<String, BigDecimal> produtosAssociado, Associado associado, Fatura fatura) {

	    List<FaturaItem> itens = new ArrayList<>();

	    if (notificacao == null || produtosAssociado == null || produtosAssociado.isEmpty()) {
	        log.warn("⚠️ Notificação ou produtos do associado vazios");
	        return itens;
	    }

	    log.info("📊 Criando itens de notificação para associado: {}", associado.getNomeRazao());

	    int smsSemEnr = notificacao.getSmsSemEnriquecimento() != null ? notificacao.getSmsSemEnriquecimento() : 0;
	    int smsComEnr = notificacao.getSmsComEnriquecimento() != null ? notificacao.getSmsComEnriquecimento() : 0;
	    int emailSemEnr = notificacao.getEmailsSemEnriquecimento() != null ? notificacao.getEmailsSemEnriquecimento()
	            : 0;
	    int emailComEnr = notificacao.getEmailsComEnriquecimento() != null ? notificacao.getEmailsComEnriquecimento()
	            : 0;
	    int cartas = notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;

	    log.info("📊 Quantidades de notificação:");
	    log.info("   SMS SEM ENR: {}", smsSemEnr);
	    log.info("   SMS COM ENR: {}", smsComEnr);
	    log.info("   E-mail SEM ENR: {}", emailSemEnr);
	    log.info("   E-mail COM ENR: {}", emailComEnr);
	    log.info("   Cartas: {}", cartas);

	    boolean possuiEnriquecimento = associadoPossuiEnriquecimento(produtosAssociado);
	    boolean temNotificacoesComEnriquecimento = (smsComEnr > 0 || emailComEnr > 0);

	    log.info("📌 Associado {} - {}", associado.getNomeRazao(),
	            possuiEnriquecimento ? "COM ENRIQUECIMENTO" : "SEM ENRIQUECIMENTO");

	    if (temNotificacoesComEnriquecimento && !possuiEnriquecimento) {
	        log.warn("⚠️ Associado possui notificações COM enriquecimento, mas não tem produtos configurados!");
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
	            BigDecimal valorTotal = BigDecimal.valueOf(cartas).multiply(valorUnitario);

	            log.info("📬 CARTA: {} x R$ {} = R$ {}", cartas, valorUnitario, valorTotal);
	            adicionarLogFatura(fatura, "INFO",
	                    "Cartas: " + cartas + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                    "ADICIONAR_NOTIFICACOES");

	            FaturaItem item = criarItemFatura(codigoCarta, "NOTIFICAÇÃO SPC CARTA (" + cartas + " unid.)", cartas,
	                    valorUnitario, "D");
	            itens.add(item);
	            valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	            count++;
	            itensCriados.add("CARTA: " + cartas + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
	        }
	    }

	    // ========== 2. E-MAILS ==========
	    if (possuiEnriquecimento) {
	        String codigoEmailSem = "04.01.03.94341";
	        if (emailSemEnr > 0 && produtosAssociado.containsKey(codigoEmailSem)) {
	            BigDecimal valorUnitario = produtosAssociado.get(codigoEmailSem);
	            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
	                BigDecimal valorTotal = BigDecimal.valueOf(emailSemEnr).multiply(valorUnitario);

	                log.info("📧 E-MAIL SEM ENR: {} x R$ {} = R$ {}", emailSemEnr, valorUnitario, valorTotal);
	                adicionarLogFatura(fatura, "INFO",
	                        "E-mail sem ENR: " + emailSemEnr + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                        "ADICIONAR_NOTIFICACOES");

	                FaturaItem item = criarItemFatura(codigoEmailSem,
	                        "NOTIFICAÇÃO SPC E-MAIL (" + emailSemEnr + " unid.)", emailSemEnr, valorUnitario, "D");
	                itens.add(item);
	                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	                count++;
	                itensCriados.add("E-MAIL SEM ENR: " + emailSemEnr + " x R$ " + valorUnitario + " = R$ "
	                        + item.getValorTotal());
	            }
	        }

	        String codigoEmailCom = "04.01.03.94432";
	        if (emailComEnr > 0 && produtosAssociado.containsKey(codigoEmailCom)) {
	            BigDecimal valorUnitario = produtosAssociado.get(codigoEmailCom);
	            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
	                BigDecimal valorTotal = BigDecimal.valueOf(emailComEnr).multiply(valorUnitario);

	                log.info("📧 E-MAIL COM ENR: {} x R$ {} = R$ {}", emailComEnr, valorUnitario, valorTotal);
	                adicionarLogFatura(fatura, "INFO",
	                        "E-mail com ENR: " + emailComEnr + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                        "ADICIONAR_NOTIFICACOES");

	                FaturaItem item = criarItemFatura(codigoEmailCom,
	                        "NOTIFICAÇÃO SPC E-MAIL C/ ENRIQUECIMENTO (" + emailComEnr + " unid.)", emailComEnr,
	                        valorUnitario, "D");
	                itens.add(item);
	                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	                count++;
	                itensCriados.add("E-MAIL COM ENR: " + emailComEnr + " x R$ " + valorUnitario + " = R$ "
	                        + item.getValorTotal());
	            }
	        }
	    } else {
	        int totalEmails = emailSemEnr + emailComEnr;
	        String codigoEmail = "04.01.03.94341";
	        if (totalEmails > 0 && produtosAssociado.containsKey(codigoEmail)) {
	            BigDecimal valorUnitario = produtosAssociado.get(codigoEmail);
	            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
	                BigDecimal valorTotal = BigDecimal.valueOf(totalEmails).multiply(valorUnitario);

	                log.info("📧 E-MAIL TOTAL: {} x R$ {} = R$ {}", totalEmails, valorUnitario, valorTotal);
	                adicionarLogFatura(fatura, "INFO",
	                        "E-mail total: " + totalEmails + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                        "ADICIONAR_NOTIFICACOES");

	                FaturaItem item = criarItemFatura(codigoEmail, "NOTIFICAÇÃO SPC E-MAIL (" + totalEmails + " unid.)",
	                        totalEmails, valorUnitario, "D");
	                itens.add(item);
	                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	                count++;
	                itensCriados.add("E-MAIL TOTAL: " + totalEmails + " x R$ " + valorUnitario + " = R$ "
	                        + item.getValorTotal());
	            }
	        }
	    }

	    // ========== 3. SMS ==========
	    if (possuiEnriquecimento) {
	        String codigoSmsSem = "04.01.03.94342";
	        if (smsSemEnr > 0 && produtosAssociado.containsKey(codigoSmsSem)) {
	            BigDecimal valorUnitario = produtosAssociado.get(codigoSmsSem);
	            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
	                BigDecimal valorTotal = BigDecimal.valueOf(smsSemEnr).multiply(valorUnitario);

	                log.info("📱 SMS SEM ENR: {} x R$ {} = R$ {}", smsSemEnr, valorUnitario, valorTotal);
	                adicionarLogFatura(fatura, "INFO",
	                        "SMS sem ENR: " + smsSemEnr + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                        "ADICIONAR_NOTIFICACOES");

	                FaturaItem item = criarItemFatura(codigoSmsSem, "NOTIFICAÇÃO SPC SMS (" + smsSemEnr + " unid.)",
	                        smsSemEnr, valorUnitario, "D");
	                itens.add(item);
	                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	                count++;
	                itensCriados.add(
	                        "SMS SEM ENR: " + smsSemEnr + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
	            }
	        }

	        String codigoSmsCom = "04.01.03.94431";
	        if (smsComEnr > 0 && produtosAssociado.containsKey(codigoSmsCom)) {
	            BigDecimal valorUnitario = produtosAssociado.get(codigoSmsCom);
	            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
	                BigDecimal valorTotal = BigDecimal.valueOf(smsComEnr).multiply(valorUnitario);

	                log.info("📱 SMS COM ENR: {} x R$ {} = R$ {}", smsComEnr, valorUnitario, valorTotal);
	                adicionarLogFatura(fatura, "INFO",
	                        "SMS com ENR: " + smsComEnr + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                        "ADICIONAR_NOTIFICACOES");

	                FaturaItem item = criarItemFatura(codigoSmsCom,
	                        "NOTIFICAÇÃO SPC SMS C/ ENRIQUECIMENTO (" + smsComEnr + " unid.)", smsComEnr, valorUnitario,
	                        "D");
	                itens.add(item);
	                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	                count++;
	                itensCriados.add(
	                        "SMS COM ENR: " + smsComEnr + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
	            }
	        }
	    } else {
	        int totalSms = smsSemEnr + smsComEnr;
	        String codigoSms = "04.01.03.94342";
	        if (totalSms > 0 && produtosAssociado.containsKey(codigoSms)) {
	            BigDecimal valorUnitario = produtosAssociado.get(codigoSms);
	            if (valorUnitario != null && valorUnitario.compareTo(BigDecimal.ZERO) > 0) {
	                BigDecimal valorTotal = BigDecimal.valueOf(totalSms).multiply(valorUnitario);

	                log.info("📱 SMS TOTAL: {} x R$ {} = R$ {}", totalSms, valorUnitario, valorTotal);
	                adicionarLogFatura(fatura, "INFO",
	                        "SMS total: " + totalSms + " x R$ " + valorUnitario + " = R$ " + valorTotal,
	                        "ADICIONAR_NOTIFICACOES");

	                FaturaItem item = criarItemFatura(codigoSms, "NOTIFICAÇÃO SPC SMS (" + totalSms + " unid.)",
	                        totalSms, valorUnitario, "D");
	                itens.add(item);
	                valorTotalNotificacoes = valorTotalNotificacoes.add(item.getValorTotal());
	                count++;
	                itensCriados
	                        .add("SMS TOTAL: " + totalSms + " x R$ " + valorUnitario + " = R$ " + item.getValorTotal());
	            }
	        }
	    }

	    int totalItensCriados = 0;
	    for (FaturaItem item : itens) {
	        totalItensCriados += item.getQuantidade().intValue();
	    }

	    int totalDigitalEsperado = (notificacao.getSmsTotal() != null ? notificacao.getSmsTotal() : 0)
	            + (notificacao.getEmailsTotal() != null ? notificacao.getEmailsTotal() : 0)
	            + (notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0);

	    log.info("📊 VERIFICAÇÃO: Total itens criados: {}, Total digital esperado: {}", totalItensCriados,
	            totalDigitalEsperado);

	    if (totalItensCriados != totalDigitalEsperado) {
	        log.warn("⚠️ INCONSISTÊNCIA: Total itens criados ({}) difere do total digital esperado ({})",
	                totalItensCriados, totalDigitalEsperado);
	    }

	    if (count == 0) {
	        log.warn("⚠️ NENHUM item de notificação foi criado para associado {}", associado.getId());
	    } else {
	        log.info("✅ Criados {} itens de notificação - Valor total: R$ {}", count, valorTotalNotificacoes);
	        log.info("📋 Itens criados: {}", String.join(" | ", itensCriados));

	        adicionarLogFatura(fatura, "INFO",
	                "Itens de notificação criados: " + count + " - Valor total: R$ " + valorTotalNotificacoes,
	                "ADICIONAR_NOTIFICACOES");
	    }

	    return itens;
	}

	private BigDecimal calcularValorTotalNotificacoes(NotificacaoAssociado notificacao) {
		BigDecimal total = BigDecimal.ZERO;

		BigDecimal valorSmsSemEnr = notificacao.getValorUnitarioSmsSemEnriquecimento() != null
				? notificacao.getValorUnitarioSmsSemEnriquecimento()
				: BigDecimal.ZERO;
		BigDecimal valorSmsComEnr = notificacao.getValorUnitarioSmsComEnriquecimento() != null
				? notificacao.getValorUnitarioSmsComEnriquecimento()
				: BigDecimal.ZERO;
		BigDecimal valorEmailSemEnr = notificacao.getValorUnitarioEmailSemEnriquecimento() != null
				? notificacao.getValorUnitarioEmailSemEnriquecimento()
				: BigDecimal.ZERO;
		BigDecimal valorEmailComEnr = notificacao.getValorUnitarioEmailComEnriquecimento() != null
				? notificacao.getValorUnitarioEmailComEnriquecimento()
				: BigDecimal.ZERO;
		BigDecimal valorCarta = notificacao.getValorUnitarioCarta() != null ? notificacao.getValorUnitarioCarta()
				: BigDecimal.ZERO;

		total = total.add(BigDecimal.valueOf(notificacao.getSmsSemEnriquecimento()).multiply(valorSmsSemEnr));
		total = total.add(BigDecimal.valueOf(notificacao.getSmsComEnriquecimento()).multiply(valorSmsComEnr));
		total = total.add(BigDecimal.valueOf(notificacao.getEmailsSemEnriquecimento()).multiply(valorEmailSemEnr));
		total = total.add(BigDecimal.valueOf(notificacao.getEmailsComEnriquecimento()).multiply(valorEmailComEnr));
		total = total.add(BigDecimal.valueOf(notificacao.getCartasTotal()).multiply(valorCarta));

		return total;
	}

	private FaturaItem criarItemFatura(String codigo, String descricao, Integer quantidade, BigDecimal valorUnitario,
			String tipoLancamento) {
		FaturaItem item = new FaturaItem();
		item.setCodigoProduto(codigo);
		item.setDescricao(descricao);
		item.setQuantidade(BigDecimal.valueOf(quantidade != null ? quantidade : 0));
		item.setValorUnitario(valorUnitario != null ? valorUnitario : BigDecimal.ZERO);
		item.setValorTotal(item.getQuantidade().multiply(item.getValorUnitario()));
		item.setTipoLancamento(tipoLancamento != null ? tipoLancamento : "D");
		return item;
	}

	// ========== MÉTODOS AUXILIARES COM LOGS ==========

	private Map<String, List<ItemSPC>> processarNotasPorTipoArquivo(List<NotaDebitoSPC> notas, ReguaFaturamento regua,
			Fatura fatura) {
		log.info("📊 Processando notas por tipo de arquivo...");

		Map<String, List<ItemSPC>> itensPorTipo = new HashMap<>();
		itensPorTipo.put("PREVIA_ANTERIOR", new ArrayList<>());
		itensPorTipo.put("CONSOLIDACAO", new ArrayList<>());
		itensPorTipo.put("PREVIA_CORRENTE", new ArrayList<>());

		if (notas == null || notas.isEmpty()) {
			log.warn("⚠️ Nenhuma nota para processar");
			adicionarLogFatura(fatura, "WARN", "Nenhuma nota encontrada para processar", "PROCESSAR_NOTAS");
			return itensPorTipo;
		}

		adicionarLogFatura(fatura, "INFO", "Processando " + notas.size() + " notas por tipo de arquivo",
				"PROCESSAR_NOTAS");

		List<TipoArquivoRegua> tiposArquivo = regua != null ? regua.getTiposArquivo() : null;

		if (tiposArquivo != null && !tiposArquivo.isEmpty()) {
			tiposArquivo.sort(Comparator.comparing(TipoArquivoRegua::getOrdem));
			log.info("📋 Sequência de tipos de arquivo:");
			for (int i = 0; i < tiposArquivo.size(); i++) {
				log.info("   [{}] {}", i, tiposArquivo.get(i).getTipo());
			}

			for (int i = 0; i < notas.size() && i < tiposArquivo.size(); i++) {
				NotaDebitoSPC nota = notas.get(i);
				String tipo = tiposArquivo.get(i).getTipo();
				if (nota.getItens() != null && !nota.getItens().isEmpty()) {
					itensPorTipo.getOrDefault(tipo, new ArrayList<>()).addAll(nota.getItens());
					log.info("📋 Nota {} -> Tipo: {}, Itens: {}", i + 1, tipo, nota.getItens().size());
				}
			}
		} else {
			List<String> sequencia = regua != null ? regua.getSequenciaArquivosList() : new ArrayList<>();
			log.info("📋 Sequência de arquivos (fallback): {}", sequencia);

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
			ReguaFaturamento regua, Fatura fatura) {

		log.info("🧮 Iniciando cálculo de itens conforme fórmula");
		adicionarLogFatura(fatura, "INFO", "Iniciando cálculo de itens conforme fórmula", "CALCULAR_ITENS");

		List<ItemSPC> itensPreviaAnterior = itensPorTipo.getOrDefault("PREVIA_ANTERIOR", new ArrayList<>());
		List<ItemSPC> itensConsolidacao = itensPorTipo.getOrDefault("CONSOLIDACAO", new ArrayList<>());
		List<ItemSPC> itensPreviaCorrente = itensPorTipo.getOrDefault("PREVIA_CORRENTE", new ArrayList<>());

		log.info("📋 Itens para cálculo:");
		log.info("   - Prévia Anterior: {}", itensPreviaAnterior.size());
		log.info("   - Consolidação: {}", itensConsolidacao.size());
		log.info("   - Prévia Corrente: {}", itensPreviaCorrente.size());

		Map<String, ItemSPC> consolidacaoMap = agruparItensPorChaveComposta(itensConsolidacao);
		Map<String, ItemSPC> previaAnteriorMap = agruparItensPorChaveComposta(itensPreviaAnterior);
		Map<String, ItemSPC> previaCorrenteMap = agruparItensPorChaveComposta(itensPreviaCorrente);

		log.info("📊 Agrupamento por chave composta:");
		log.info("   - Consolidado: {} grupos", consolidacaoMap.size());
		log.info("   - Prévia Anterior: {} grupos", previaAnteriorMap.size());
		log.info("   - Prévia Corrente: {} grupos", previaCorrenteMap.size());

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

			log.info("📊 Calculando item: {}", chave);
			log.info("   - Consolidado: {}", qtdeConsolidada);
			log.info("   - Prévia Anterior: {}", qtdePreviaAnterior);
			log.info("   - Prévia Corrente: {}", qtdePreviaCorrente);
			log.info("   - Fórmula: ({}) - ({}) + ({}) = {}", qtdeConsolidada, qtdePreviaAnterior, qtdePreviaCorrente,
					qtdeCalculada);
			log.info("   - Resultado: {}",
					qtdeCalculada.compareTo(BigDecimal.ZERO) > 0 ? "✅ ADICIONADO" : "⏭️ IGNORADO");

			adicionarLogFatura(fatura, "DEBUG",
					String.format("Item '%s': Consolidado=%s, PréviaAnt=%s, PréviaCorr=%s, Resultado=%s", chave,
							qtdeConsolidada, qtdePreviaAnterior, qtdePreviaCorrente, qtdeCalculada),
					"CALCULAR_ITENS");

			if (qtdeCalculada.compareTo(BigDecimal.ZERO) <= 0) {
				adicionarLogFatura(fatura, "INFO", "Item ignorado (quantidade <= 0): " + chave, "CALCULAR_ITENS");
				continue;
			}

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

			log.info("   ✅ Item adicionado: '{}' - Qtd: {}, Valor: R$ {}", faturaItem.getDescricao(),
					faturaItem.getQuantidade(), faturaItem.getValorTotal());
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

					log.info("📊 Item apenas na prévia corrente: '{}' - Qtd: {}, Valor: R$ {}",
							faturaItem.getDescricao(), faturaItem.getQuantidade(), faturaItem.getValorTotal());
				}
			}
		}

		log.info("✅ Cálculo concluído: {} itens gerados", resultado.size());
		adicionarLogFatura(fatura, "INFO", "Cálculo concluído: " + resultado.size() + " itens gerados",
				"CALCULAR_ITENS");

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

	/*private String buscarCodigoProdutoRM(String descricaoServico) {
		
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
	*/
	
	/**
	 * 🔥 OTIMIZADO: Busca o código RM do produto usando CACHE em memória.
	 * Antes: produtoRepository.findAll() dentro do loop (15.000+ chamadas).
	 * Agora: lookup O(1) em ConcurrentHashMap.
	 */
	private String buscarCodigoProdutoRM(String descricaoServico) {
	    if (descricaoServico == null || descricaoServico.trim().isEmpty()) {
	        return null;
	    }

	    // 🔥 LOOKUP O(1) NO CACHE (sem I/O, sem findAll)
	    String codigoRM = cacheService.buscarCodigoRmPorDescricao(descricaoServico.trim());

	    if (codigoRM != null) {
	        log.debug("✅ Produto encontrado no cache: '{}' -> '{}'", descricaoServico, codigoRM);
	        return codigoRM;
	    }

	    log.warn("⚠️ Produto não encontrado no cache para descrição: '{}'", descricaoServico);
	    return null;
	}

	/*private String removerAcentos(String texto) {
		if (texto == null)
			return null;

		String normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
		return normalizado.replaceAll("[^\\p{ASCII}]", "");
	}*/

	private Object[] extrairPeriodoDosParametros(Associado associado) {
		Object[] resultado = new Object[4];

		try {
			String codigoSocio = associado.getCodigoSpc();
			if (codigoSocio == null || codigoSocio.isEmpty()) {
				log.warn("⚠️ Associado {} sem código SPC", associado.getId());
				return null;
			}

			Optional<ParametrosSPC> parametroOpt = parametrosSPCRepository.findUltimoByCodigoSocio(codigoSocio);

			if (parametroOpt.isPresent()) {
				ParametrosSPC parametro = parametroOpt.get();
				String dataInicioStr = parametro.getDataInicioPeriodoRef();
				String dataFimStr = parametro.getDataFimPeriodoRef();

				if (dataInicioStr != null && dataFimStr != null && !dataInicioStr.isEmpty() && !dataFimStr.isEmpty()) {

					LocalDate dataInicio = parseDate(dataInicioStr);
					LocalDate dataFim = parseDate(dataFimStr);

					if (dataInicio != null && dataFim != null) {
						log.info("📅 Período encontrado nos parâmetros: {} à {}", dataInicio, dataFim);
						log.info("   🔍 Código SPC: {}, IDs: {}", codigoSocio, parametro.getId());

						resultado[0] = dataFim.getMonthValue();
						resultado[1] = dataFim.getYear();
						resultado[2] = dataInicio;
						resultado[3] = dataFim;

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
	 * 🔥 Remove itens duplicados de notificação com logs detalhados
	 */
	private void removerItemNotificacaoDuplicado(List<FaturaItem> itens, NotificacaoAssociado notificacao,
	        Fatura fatura) {

	    log.info("========================================");
	    log.info("🔍 INICIANDO REMOÇÃO DE ITEM DUPLICADO");
	    log.info("========================================");

	    adicionarLogFatura(fatura, "INFO", "Iniciando remoção de itens duplicados", "REMOVER_DUPLICADOS");

	    if (itens == null || itens.isEmpty()) {
	        log.warn("⚠️ Lista de itens está vazia ou nula");
	        adicionarLogFatura(fatura, "WARN", "Lista de itens vazia", "REMOVER_DUPLICADOS");
	        return;
	    }

	    // ============================================================
	    // PASSO 1: DEDUP INTERNO (duplicatas entre itens do sistema)
	    // ============================================================
	    log.info("🔍 PASSO 1: Verificando duplicatas internas...");
	    int removidosInternos = removerDuplicatasInternas(itens, fatura);
	    if (removidosInternos > 0) {
	        log.info("🗑️ Removidos {} itens duplicados internos", removidosInternos);
	    } else {
	        log.info("✅ Nenhuma duplicata interna detectada");
	    }

	    // ============================================================
	    // PASSO 2: DEDUP SPC vs SISTEMA (nota vs itens criados)
	    // ============================================================
	    if (notificacao == null) {
	        log.warn("⚠️ Notificação é nula, pulando dedup SPC vs sistema");
	        return;
	    }

	    log.info("🔍 PASSO 2: Verificando duplicatas entre SPC e sistema...");

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
	        log.info("   [{}] Código: {}, Descrição: {}, Quantidade: {}", i, item.getCodigoProduto(),
	                item.getDescricao(), item.getQuantidade());
	    }

	    List<FaturaItem> itensNotificacaoNota = new ArrayList<>();

	    for (FaturaItem item : itens) {
	        String descricao = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
	        String codigo = item.getCodigoProduto();
	        BigDecimal qtd = item.getQuantidade();

	        if (qtd == null)
	            continue;

	        boolean isNotificacaoNota = false;

	        if (codigo != null && codigo.equals("04.01.03.07326")) {
	            isNotificacaoNota = true;
	        } else if (descricao.contains("REGISTRO / NOTIFICACAO") || descricao.contains("REGISTRO/NOTIFICACAO")) {
	            isNotificacaoNota = true;
	        } else if (descricao.equals("NOTIFICACAO SPC") || descricao.contains("NOTIFICACAO SPC/CN")
	                || (descricao.contains("NOTIFICACAO") && !descricao.contains("CARTA") && !descricao.contains("SMS")
	                        && !descricao.contains("E-MAIL") && !descricao.contains("ENRIQUECIMENTO"))) {
	            isNotificacaoNota = true;
	        }

	        if (isNotificacaoNota) {
	            itensNotificacaoNota.add(item);
	            log.info("🔍 Item de notificação da nota encontrado: '{}' - Qtd: {}", item.getDescricao(),
	                    qtd.intValue());
	        }
	    }

	    if (itensNotificacaoNota.isEmpty()) {
	        log.info("ℹ️ Nenhum item de notificação da nota encontrado");
	        adicionarLogFatura(fatura, "INFO", "Nenhum item duplicado encontrado", "REMOVER_DUPLICADOS");
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

	        boolean isNotificacaoSPC = desc.equals("NOTIFICACAO SPC") || desc.contains("NOTIFICACAO SPC/CN")
	                || (desc.contains("NOTIFICACAO") && !desc.contains("CARTA") && !desc.contains("SMS")
	                        && !desc.contains("E-MAIL") && !desc.contains("ENRIQUECIMENTO")
	                        && !desc.contains("REGISTRO"));

	        if (isNotificacaoSPC && qtdItem == totalDigital) {
	            if (!itensParaRemover.contains(item)) {
	                itensParaRemover.add(item);
	                log.info("📊 NOTIFICACAO SPC com quantidade {} igual ao total digital {} - será removido", qtdItem,
	                        totalDigital);
	            }
	        }
	    }

	    // Verificar REGISTRO / NOTIFICACAO com quantidade igual ao total digital
	    for (FaturaItem item : itensNotificacaoNota) {
	        String desc = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";
	        int qtdItem = item.getQuantidade().intValue();

	        boolean isRegistro = desc.contains("REGISTRO / NOTIFICACAO") || desc.contains("REGISTRO/NOTIFICACAO");

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
	        adicionarLogFatura(fatura, "INFO", "Nenhum item duplicado encontrado", "REMOVER_DUPLICADOS");
	        log.info("========================================");
	        return;
	    }

	    log.info("");
	    log.info("🗑️ REMOVENDO {} ITENS:", itensParaRemover.size());

	    for (FaturaItem item : itensParaRemover) {
	        if (itens.contains(item)) {
	            itens.remove(item);
	            log.info("   ✅ REMOVIDO: '{}' - Qtd: {}", item.getDescricao(), item.getQuantidade());
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

	    adicionarLogFatura(fatura, "INFO",
	            itensParaRemover.size() + " itens duplicados removidos. Itens restantes: " + itens.size(),
	            "REMOVER_DUPLICADOS");

	    log.info("========================================");
	    log.info("✅ REMOÇÃO DE DUPLICADOS CONCLUÍDA!");
	    log.info("========================================");
	}
	
	/**
	 * 🔥 NOVO MÉTODO: Remove duplicatas internas entre itens criados pelo sistema.
	 * 
	 * Estratégia: agrupa por (codigo_produto + quantidade). Mantém apenas
	 * a primeira ocorrência de cada grupo. Remove as demais.
	 * 
	 * @return Número de itens removidos
	 */
	private int removerDuplicatasInternas(List<FaturaItem> itens, Fatura fatura) {
	    if (itens == null || itens.isEmpty()) {
	        return 0;
	    }

	    Map<String, FaturaItem> unicos = new LinkedHashMap<>();
	    List<FaturaItem> duplicados = new ArrayList<>();

	    for (FaturaItem item : itens) {
	        String codigo = item.getCodigoProduto() != null ? item.getCodigoProduto() : "";
	        BigDecimal qtd = item.getQuantidade() != null ? item.getQuantidade() : BigDecimal.ZERO;
	        String desc = item.getDescricao() != null ? item.getDescricao().toUpperCase() : "";

	        // Identifica se é item de notificação do SISTEMA (criado pelo SGA)
	        boolean isNotificacaoSistema = 
	            desc.contains("NOTIFICAÇÃO SPC") || desc.contains("NOTIFICACAO SPC")
	            || (desc.startsWith("NOTIFICAÇÃO") && (desc.contains("CARTA") || desc.contains("SMS") 
	                || desc.contains("E-MAIL") || desc.contains("EMAIL") || desc.contains("ENRIQUECIMENTO")));

	        // Ignora itens que NÃO são notificações (mantém todos sem dedup)
	        if (!isNotificacaoSistema) {
	            // Chave única por item para não colidir
	            unicos.put("__NAO_DEDUP__" + System.nanoTime() + "_" + codigo, item);
	            continue;
	        }

	        // Chave de deduplicação: código + quantidade
	        String chave = codigo + "|" + qtd.toPlainString();

	        if (unicos.containsKey(chave)) {
	            duplicados.add(item);
	            log.warn("🔴 Duplicata interna detectada: '{}' | Código: {} | Qtd: {} | Valor: R$ {}",
	                    item.getDescricao(), codigo, qtd, item.getValorTotal());
	        } else {
	            unicos.put(chave, item);
	        }
	    }

	    if (duplicados.isEmpty()) {
	        return 0;
	    }

	    log.warn("🗑️ Removendo {} itens duplicados internos:", duplicados.size());
	    for (FaturaItem item : duplicados) {
	        log.warn("   - '{}' | Código: {} | Qtd: {} | Valor: R$ {}",
	                item.getDescricao(), item.getCodigoProduto(), item.getQuantidade(), item.getValorTotal());
	        itens.remove(item);
	    }

	    adicionarLogFatura(fatura, "WARN",
	            "Removidos " + duplicados.size() + " itens de notificação duplicados internamente",
	            "REMOVER_DUPLICADOS");

	    return duplicados.size();
	}
	
}