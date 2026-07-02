package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.ProcessamentoRequest;
import com.sga.dto.ResultadoProcessamento;
import com.sga.model.Associado;
import com.sga.model.CancelamentoImportacao;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.Produto;
import com.sga.model.ReguaFaturamento;
import com.sga.model.TipoArquivoRegua;
import com.sga.repository.CancelamentoImportacaoRepository;
import com.sga.repository.FaturaItemRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.ItemSPCRepository;
import com.sga.repository.NotaDebitoSPCRepository;
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

	/*
	 * @Autowired private ProcessamentoFaturamentoService
	 * processamentoFaturamentoService;
	 */
	
	// ========== PROCESSAR FATURAMENTO ==========

	/**
	 * Processa o faturamento para os associados selecionados Este método delega
	 * para o ProcessamentoFaturamentoService
	 */
	/*@Transactional
	public ResultadoProcessamento processarFaturamento(ProcessamentoRequest request) {
		log.info("🚀 Processando faturamento via FaturaGeracaoService: {}", request);

		try {
			// Delegar para o service especializado
			ResultadoProcessamento resultado = processamentoFaturamentoService.processarFaturamento(request);

			log.info("✅ Processamento concluído: {} processados, {} erros, {} faturas geradas",
					resultado.getAssociadosProcessados(), resultado.getAssociadosComErro(),
					resultado.getTotalNotasGeradas());

			return resultado;

		} catch (Exception e) {
			log.error("❌ Erro ao processar faturamento: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao processar faturamento: " + e.getMessage());
		}
	}
	 */
	
	// ========== MÉTODO PRINCIPAL DE GERAÇÃO DE FATURA ==========

	@Transactional
	public Fatura gerarFatura(Associado associado, List<NotaDebitoSPC> notas, ReguaFaturamento regua, Integer mes,
			Integer ano, LocalDate dataEmissao, LocalDate dataVencimento, boolean simular, String usuario) {

		log.info("========== INICIANDO GERAÇÃO DE FATURA ==========");
		log.info("📄 Associado: {} (ID: {}, Código SPC: {})", associado.getNomeRazao(), associado.getId(),
				associado.getCodigoSpc());
		log.info("📅 Período: {}/{} (simular={})", mes, ano, simular);
		log.info("📅 Datas recebidas - Emissão: {}, Vencimento: {}", dataEmissao, dataVencimento);

		// 🔥 CORREÇÃO: Se mês/ano vierem nulos, extrair da data de emissão
		if ((mes == null || ano == null) && dataEmissao != null) {
			mes = dataEmissao.getMonthValue();
			ano = dataEmissao.getYear();
			log.info("📅 Mês/Ano extraídos da data de emissão: {}/{}", mes, ano);
		}

		if (notas == null || notas.isEmpty()) {
			log.warn("⚠️ Nenhuma nota encontrada para o associado");
			return null;
		}

		// Verificar se já existe fatura
		if (!simular) {
			List<Fatura> faturasExistentes = faturaRepository
					.findByAssociadoIdAndMesReferenciaAndAnoReferencia(associado.getId(), mes, ano);
			if (!faturasExistentes.isEmpty()) {
				log.warn("Já existe fatura para o associado {} no período {}/{}", associado.getId(), mes, ano);
				return faturasExistentes.get(0);
			}
		}

		// Processar notas
		Map<String, List<ItemSPC>> itensPorTipo = processarNotasPorTipoArquivo(notas, regua);
		List<FaturaItem> itensCalculados = calcularItensConformeFormula(itensPorTipo, regua);

		if (itensCalculados.isEmpty()) {
			log.warn("⚠️ Nenhum item calculado para o associado: {}", associado.getNomeRazao());
			return null;
		}

		// Criar fatura
		Fatura fatura = new Fatura();
		fatura.setAssociado(associado);
		fatura.setNumeroFatura(gerarNumeroFatura(associado.getId(), mes, ano));

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
		if (mes != null && ano != null) {
			fatura.setMesReferencia(mes);
			fatura.setAnoReferencia(ano);
		} else if (dataEmissao != null) {
			fatura.setMesReferencia(dataEmissao.getMonthValue());
			fatura.setAnoReferencia(dataEmissao.getYear());
		} else {
			LocalDate hoje = LocalDate.now();
			fatura.setMesReferencia(hoje.getMonthValue());
			fatura.setAnoReferencia(hoje.getYear());
		}

		fatura.setUsuarioCriacao(usuario);
		fatura.setCriadoEm(LocalDateTime.now());
		fatura.setItens(itensCalculados);

		for (FaturaItem item : itensCalculados) {
			item.setFatura(fatura);
		}

		fatura.recalcularTotal();

		// Aplicar regras
		if (regua != null && Boolean.TRUE.equals(regua.getAplicarFranquia())) {
			fatura = franquiaRule.aplicarRegraFranquia(fatura, associado);
		}

		boolean aplicarMinimo = regua != null && Boolean.TRUE.equals(regua.getAplicarFaturamentoMinimo())
				&& regua.isExtemporaneo();
		if (aplicarMinimo) {
			fatura = faturamentoMinimoRule.aplicarRegraComplemento(fatura, associado, true);
		}

		if (regua != null && Boolean.TRUE.equals(regua.getAplicarCancelamentos())) {
			List<CancelamentoImportacao> cancelamentos = buscarCancelamentos(associado, mes, ano);
			if (!cancelamentos.isEmpty()) {
				List<FaturaItem> itensAposCancelamento = removerServicosCancelados(fatura.getItens(), cancelamentos);
				fatura.setItens(itensAposCancelamento);
				fatura.recalcularTotal();
			}
		}

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
			return faturaSalva;
		}
	}

	// Método sobrecarregado para compatibilidade
	@Transactional
	public Fatura gerarFatura(Associado associado, List<NotaDebitoSPC> notas, ReguaFaturamento regua, Integer mes,
			Integer ano, boolean simular, String usuario) {
		return gerarFatura(associado, notas, regua, mes, ano, null, null, simular, usuario);
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

			// 🔥 BUSCAR O CÓDIGO DO PRODUTO RM BASEADO NA DESCRIÇÃO
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

			log.info(
					"  ✅ Item adicionado: {} | Código Original: {} | Código RM: {} | Quantidade: {} | Valor Unitário: {} | Total: {}",
					itemConsolidacao.getDescricaoServico(), itemConsolidacao.getCodigoProduto(),
					faturaItem.getCodigoProduto(), qtdeCalculada, itemConsolidacao.getValorUnitario(),
					faturaItem.getValorTotal());
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

					log.info(
							"  ✅ Item apenas na prévia corrente: {} | Código Original: {} | Código RM: {} | Quantidade: {} | Total: {}",
							item.getDescricaoServico(), item.getCodigoProduto(), faturaItem.getCodigoProduto(), qtde,
							faturaItem.getValorTotal());
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

	/**
	 * Busca o código do produto RM baseado na descrição do serviço Ignora acentos e
	 * case
	 */
	private String buscarCodigoProdutoRM(String descricaoServico) {
		if (descricaoServico == null || descricaoServico.trim().isEmpty()) {
			return null;
		}

		try {
			String descricaoSemAcento = removerAcentos(descricaoServico.trim());
			log.debug("🔍 Buscando produto para descrição original: '{}' -> sem acento: '{}'", descricaoServico,
					descricaoSemAcento);

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

			log.debug("📦 Encontrados {} produtos para a descrição '{}'", produtos.size(), descricaoServico);

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

	/**
	 * Remove acentos de uma string
	 */
	private String removerAcentos(String texto) {
		if (texto == null)
			return null;

		String normalizado = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
		return normalizado.replaceAll("[^\\p{ASCII}]", "");
	}
}