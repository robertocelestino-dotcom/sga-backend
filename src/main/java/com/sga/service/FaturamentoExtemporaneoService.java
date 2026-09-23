package com.sga.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sga.model.FaturaItem;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;

@Service
public class FaturamentoExtemporaneoService {

	private static final Logger log = LoggerFactory.getLogger(FaturamentoExtemporaneoService.class);

	//@Autowired
	//private ProdutoRepository produtoRepository;
	
	@Autowired
	private CacheService cacheService;

	/**
	 * 🔥 PROCESSAR FATURAMENTO EXTEMPORÂNEO Réguas ID 7 e 8
	 * 
	 * Fórmula: QtdeFaturada = (Consolidado - Prévia do Dia/Mês Anterior) + Prévia
	 * do Dia/Mês Corrente
	 */
	public List<FaturaItem> processarExtemporaneo(List<NotaDebitoSPC> notas) {
		log.info("========================================");
		log.info("📋 PROCESSANDO FATURAMENTO EXTEMPORÂNEO");
		log.info("========================================");
		log.info("📊 Total de notas: {}", notas != null ? notas.size() : 0);

		if (notas == null || notas.isEmpty()) {
			log.warn("⚠️ Nenhuma nota para processar");
			return new ArrayList<>();
		}

		// 1. CLASSIFICAR NOTAS POR TIPO (baseado na data)
		Map<String, List<ItemSPC>> notasClassificadas = classificarNotasPorTipo(notas);

		List<ItemSPC> previaAnterior = notasClassificadas.getOrDefault("PREVIA_ANTERIOR", new ArrayList<>());
		List<ItemSPC> consolidado = notasClassificadas.getOrDefault("CONSOLIDACAO", new ArrayList<>());
		List<ItemSPC> previaCorrente = notasClassificadas.getOrDefault("PREVIA_CORRENTE", new ArrayList<>());

		log.info("📊 Classificação:");
		log.info("   ✅ Prévia Anterior: {} itens", previaAnterior.size());
		log.info("   ✅ Consolidação: {} itens", consolidado.size());
		log.info("   ✅ Prévia Corrente: {} itens", previaCorrente.size());

		// 2. AGRUPAR ITENS POR DESCRIÇÃO (chave composta)
		Map<String, ItemSPC> previaAnteriorMap = agruparItensPorDescricao(previaAnterior);
		Map<String, ItemSPC> consolidadoMap = agruparItensPorDescricao(consolidado);
		Map<String, ItemSPC> previaCorrenteMap = agruparItensPorDescricao(previaCorrente);

		log.info("📊 Agrupamento:");
		log.info("   ✅ Prévia Anterior: {} grupos", previaAnteriorMap.size());
		log.info("   ✅ Consolidação: {} grupos", consolidadoMap.size());
		log.info("   ✅ Prévia Corrente: {} grupos", previaCorrenteMap.size());

		// 3. CALCULAR QUANTIDADES
		List<FaturaItem> itensFatura = new ArrayList<>();

		// 3.1 Processar itens do consolidado
		log.info("📌 Processando itens do consolidado...");
		for (Map.Entry<String, ItemSPC> entry : consolidadoMap.entrySet()) {
			String chave = entry.getKey();
			ItemSPC itemConsolidado = entry.getValue();

			ItemSPC itemPreviaAnterior = previaAnteriorMap.get(chave);
			ItemSPC itemPreviaCorrente = previaCorrenteMap.get(chave);

			BigDecimal qtdeConsolidada = getQuantidade(itemConsolidado);
			BigDecimal qtdePreviaAnterior = getQuantidade(itemPreviaAnterior);
			BigDecimal qtdePreviaCorrente = getQuantidade(itemPreviaCorrente);

			// 🔥 FÓRMULA: (Consolidado - Prévia Anterior) + Prévia Corrente
			BigDecimal qtdeCalculada = qtdeConsolidada.subtract(qtdePreviaAnterior).add(qtdePreviaCorrente);

			log.debug("   🔢 {}: Consolidado={}, PréviaAnterior={}, PréviaCorrente={}, Calculada={}", chave,
					qtdeConsolidada, qtdePreviaAnterior, qtdePreviaCorrente, qtdeCalculada);

			// 🔥 SE QUANTIDADE > 0, ADICIONAR
			if (qtdeCalculada.compareTo(BigDecimal.ZERO) > 0) {
				FaturaItem item = criarFaturaItem(itemConsolidado, qtdeCalculada);
				itensFatura.add(item);

				log.info("   ✅ ADICIONADO: {} - Qtd: {}, Valor: R$ {}", item.getDescricao(), item.getQuantidade(),
						item.getValorTotal());
			} else {
				log.info("   ⏭️ IGNORADO (Qtd <= 0): {} - Qtde: {}", chave, qtdeCalculada);
			}
		}

		// 3.2 Processar itens APENAS na prévia corrente (não estão no consolidado)
		log.info("📌 Processando itens apenas na prévia corrente...");
		for (Map.Entry<String, ItemSPC> entry : previaCorrenteMap.entrySet()) {
			String chave = entry.getKey();

			// Se já foi processado no consolidado, ignorar
			if (consolidadoMap.containsKey(chave)) {
				continue;
			}

			ItemSPC itemPreviaCorrente = entry.getValue();

			// Verificar se existe na prévia anterior
			ItemSPC itemPreviaAnterior = previaAnteriorMap.get(chave);
			BigDecimal qtdePreviaAnterior = getQuantidade(itemPreviaAnterior);
			BigDecimal qtdePreviaCorrente = getQuantidade(itemPreviaCorrente);

			// 🔥 FÓRMULA: (0 - Prévia Anterior) + Prévia Corrente
			BigDecimal qtdeCalculada = qtdePreviaCorrente.subtract(qtdePreviaAnterior);

			log.debug("   🔢 {} (apenas corrente): PréviaAnterior={}, PréviaCorrente={}, Calculada={}", chave,
					qtdePreviaAnterior, qtdePreviaCorrente, qtdeCalculada);

			if (qtdeCalculada.compareTo(BigDecimal.ZERO) > 0) {
				FaturaItem item = criarFaturaItem(itemPreviaCorrente, qtdeCalculada);
				itensFatura.add(item);

				log.info("   ✅ ADICIONADO (corrente): {} - Qtd: {}, Valor: R$ {}", item.getDescricao(),
						item.getQuantidade(), item.getValorTotal());
			} else {
				log.info("   ⏭️ IGNORADO (Qtd <= 0): {} - Qtde: {}", chave, qtdeCalculada);
			}
		}

		log.info("========================================");
		log.info("📊 TOTAL DE ITENS CALCULADOS: {}", itensFatura.size());
		log.info("========================================");

		return itensFatura;
	}

	/**
	 * CLASSIFICA NOTAS POR TIPO BASEADO NA DATA
	 * 
	 * Regra: Ordena por data_fim_periodo e classifica como: - 3 notas: [Prévia
	 * Anterior, Consolidação, Prévia Corrente] - 2 notas: [Consolidação, Prévia
	 * Corrente] - 1 nota: [Consolidação]
	 */
	private Map<String, List<ItemSPC>> classificarNotasPorTipo(List<NotaDebitoSPC> notas) {
		Map<String, List<ItemSPC>> result = new LinkedHashMap<>();
		result.put("PREVIA_ANTERIOR", new ArrayList<>());
		result.put("CONSOLIDACAO", new ArrayList<>());
		result.put("PREVIA_CORRENTE", new ArrayList<>());

		if (notas == null || notas.isEmpty()) {
			return result;
		}

		// Ordenar por data_fim_periodo (mais antigo primeiro)
		List<NotaDebitoSPC> notasOrdenadas = notas.stream().filter(n -> n.getDataFimPeriodo() != null)
				.sorted(Comparator.comparing(NotaDebitoSPC::getDataFimPeriodo)).collect(Collectors.toList());

		if (notasOrdenadas.isEmpty()) {
			log.warn("⚠️ Nenhuma nota com data_fim_periodo válida");
			return result;
		}

		// 🔥 MAPEAMENTO EXPLÍCITO
		int size = notasOrdenadas.size();

		if (size >= 3) {
			// 3 notas: [Prévia Anterior, Consolidação, Prévia Corrente]
			addItensToMap(result, "PREVIA_ANTERIOR", notasOrdenadas.get(0));
			addItensToMap(result, "CONSOLIDACAO", notasOrdenadas.get(1));
			addItensToMap(result, "PREVIA_CORRENTE", notasOrdenadas.get(2));

			log.info("📋 3 notas:");
			log.info("   ✅ Prévia Anterior: {}", notasOrdenadas.get(0).getDataFimPeriodo());
			log.info("   ✅ Consolidação: {}", notasOrdenadas.get(1).getDataFimPeriodo());
			log.info("   ✅ Prévia Corrente: {}", notasOrdenadas.get(2).getDataFimPeriodo());
		} else if (size == 2) {
			// 2 notas: [Consolidação, Prévia Corrente]
			addItensToMap(result, "CONSOLIDACAO", notasOrdenadas.get(0));
			addItensToMap(result, "PREVIA_CORRENTE", notasOrdenadas.get(1));

			log.info("📋 2 notas:");
			log.info("   ✅ Consolidação: {}", notasOrdenadas.get(0).getDataFimPeriodo());
			log.info("   ✅ Prévia Corrente: {}", notasOrdenadas.get(1).getDataFimPeriodo());
		} else {
			// 1 nota: [Consolidado]
			addItensToMap(result, "CONSOLIDACAO", notasOrdenadas.get(0));
			log.info("📋 1 nota:");
			log.info("   ✅ Consolidação: {}", notasOrdenadas.get(0).getDataFimPeriodo());
		}

		return result;
	}

	/**
	 * AGRUPA ITENS POR DESCRIÇÃO (chave composta: código|descrição)
	 */
	private Map<String, ItemSPC> agruparItensPorDescricao(List<ItemSPC> itens) {
		Map<String, ItemSPC> mapa = new LinkedHashMap<>();

		if (itens == null || itens.isEmpty()) {
			return mapa;
		}

		for (ItemSPC item : itens) {
			String codigo = item.getCodigoProduto() != null ? item.getCodigoProduto() : "SEM_CODIGO";
			String descricao = item.getDescricaoServico() != null ? item.getDescricaoServico() : "SEM_DESCRICAO";
			String chave = codigo + "|" + descricao;

			if (mapa.containsKey(chave)) {
				ItemSPC existente = mapa.get(chave);
				BigDecimal qtdeExistente = getQuantidade(existente);
				BigDecimal qtdeAtual = getQuantidade(item);
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

	/**
	 * CRIA UM ITEM DE FATURA A PARTIR DO ITEM SPC
	 */
	private FaturaItem criarFaturaItem(ItemSPC item, BigDecimal quantidade) {
		FaturaItem faturaItem = new FaturaItem();

		// Buscar código RM do produto baseado na descrição
		String codigoRM = buscarCodigoProdutoRM(item.getDescricaoServico());

		faturaItem.setCodigoProduto(codigoRM != null ? codigoRM : item.getCodigoProduto());
		faturaItem.setDescricao(item.getDescricaoServico());
		faturaItem.setQuantidade(quantidade);
		faturaItem.setValorUnitario(item.getValorUnitario() != null ? item.getValorUnitario() : BigDecimal.ZERO);
		faturaItem.setValorTotal(quantidade.multiply(faturaItem.getValorUnitario()));
		faturaItem.setTipoLancamento("D");

		log.debug("   🔧 Item criado: {} | Código: {} | Qtd: {} | Valor: {}", faturaItem.getDescricao(),
				faturaItem.getCodigoProduto(), faturaItem.getQuantidade(), faturaItem.getValorTotal());

		return faturaItem;
	}

	/**
	 * BUSCA O CÓDIGO RM DO PRODUTO BASEADO NA DESCRIÇÃO
	 */
	/*
	 * private String buscarCodigoProdutoRM(String descricaoServico) { if
	 * (descricaoServico == null || descricaoServico.trim().isEmpty()) { return
	 * null; }
	 * 
	 * try { String descricaoSemAcento = removerAcentos(descricaoServico.trim());
	 * 
	 * List<Produto> todosProdutos = produtoRepository.findAll();
	 * 
	 * // Busca exata (ignorando acentos e maiúsculas) for (Produto produto :
	 * todosProdutos) { if (produto.getDescricao() != null) { String
	 * descricaoProdutoSemAcento = removerAcentos(produto.getDescricao().trim()); if
	 * (descricaoProdutoSemAcento.equalsIgnoreCase(descricaoSemAcento)) { String
	 * codigoRM = produto.getCodigoRm(); if (codigoRM != null &&
	 * !codigoRM.trim().isEmpty()) { log.debug("   ✅ Mapeado: '{}' -> '{}'",
	 * descricaoServico, codigoRM); return codigoRM; } } } }
	 * 
	 * // Busca por contém (fallback) for (Produto produto : todosProdutos) { if
	 * (produto.getDescricao() != null) { String descricaoProdutoSemAcento =
	 * removerAcentos(produto.getDescricao().trim()); if
	 * (descricaoProdutoSemAcento.contains(descricaoSemAcento) ||
	 * descricaoSemAcento.contains(descricaoProdutoSemAcento)) { String codigoRM =
	 * produto.getCodigoRm(); if (codigoRM != null && !codigoRM.trim().isEmpty()) {
	 * log.debug("   ✅ Mapeado (parcial): '{}' -> '{}'", descricaoServico,
	 * codigoRM); return codigoRM; } } } }
	 * 
	 * log.warn("   ⚠️ Produto não encontrado para: '{}'", descricaoServico); return
	 * null; } catch (Exception e) {
	 * log.error("   ❌ Erro ao buscar produto para '{}': {}", descricaoServico,
	 * e.getMessage()); return null; } }
	 */

	/**
	 * 🔥 OTIMIZADO: Busca o código RM do produto usando CACHE em memória.
	 */
	private String buscarCodigoProdutoRM(String descricaoServico) {
		if (descricaoServico == null || descricaoServico.trim().isEmpty()) {
			return null;
		}

		String codigoRM = cacheService.buscarCodigoRmPorDescricao(descricaoServico.trim());

		if (codigoRM != null) {
			log.debug("   ✅ Mapeado (cache): '{}' -> '{}'", descricaoServico, codigoRM);
			return codigoRM;
		}

		log.warn("   ⚠️ Produto não encontrado no cache para: '{}'", descricaoServico);
		return null;
	}

	/*private String removerAcentos(String texto) {
		if (texto == null)
			return null;
		String normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
		return normalizado.replaceAll("[^\\p{ASCII}]", "");
	}
	*/
	
	private void addItensToMap(Map<String, List<ItemSPC>> map, String key, NotaDebitoSPC nota) {
		if (nota != null && nota.getItens() != null) {
			map.get(key).addAll(nota.getItens());
		}
	}

	/**
	 * OBTÉM QUANTIDADE DO ITEM DE FORMA SEGURA
	 */
	private BigDecimal getQuantidade(ItemSPC item) {
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
}