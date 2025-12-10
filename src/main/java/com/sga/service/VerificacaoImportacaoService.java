package com.sga.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.sga.dto.VerificacaoAssociadosCompletoDTO;
import com.sga.dto.VerificacaoAssociadosDTO;
import com.sga.dto.VerificacaoResultadoDTO;
import com.sga.model.Associado;
import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ParametrosSPC;
import com.sga.model.TraillerSPC;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.ImportacaoSPCRepository;
import com.sga.repository.ItemSPCRepository;
import com.sga.repository.NotaDebitoSPCRepository;
import com.sga.repository.ProdutoRepository;

/**
 * Serviço de verificação de importação SPC. Versão atualizada: adiciona
 * "Parâmetros do Arquivo" e corrige verificação de associados (Arquivo =
 * Trailler.qtdeTotalBoletos ; Banco = count(codigoSocio)).
 */
@Service
public class VerificacaoImportacaoService {

	private static final Logger logger = LoggerFactory.getLogger(VerificacaoImportacaoService.class);

	@Autowired
	private AssociadoRepository associadoRepository;

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private ImportacaoSPCRepository importacaoSPCRepository;

	@Autowired
	private ImportacaoSPCRepository importacaoRepository;

	@Autowired
	private NotaDebitoSPCRepository notaDebitoSPCRepository;

	@Autowired
	private ItemSPCRepository itemSPCRepository;

	/**
	 * Entrada pública: gera relatório completo de verificação (map com chaves).
	 * Cacheável para desempenho.
	 */
	@Cacheable(value = "verificacaoCache", key = "#importacaoId")
	public Map<String, Object> verificarImportacao(Long importacaoId) {
		logger.info("=== INICIANDO VERIFICAÇÃO DA IMPORTAÇÃO ID: {} ===", importacaoId);
		Instant inicio = Instant.now();

		ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		// Execução paralela — parâmetros primeiro para priorizar essa categoria
		List<CompletableFuture<VerificacaoResultadoDTO>> futures = Arrays.asList(
				CompletableFuture.supplyAsync(() -> verificarParametrosArquivo(importacao)),
				CompletableFuture.supplyAsync(() -> verificarProdutos(importacao)),
				// CompletableFuture.supplyAsync(() -> verificarValoresTotais(importacao)),
				CompletableFuture.supplyAsync(() -> verificarNotasDebito(importacao)),
				CompletableFuture.supplyAsync(() -> verificarItensNota(importacao)),
				CompletableFuture.supplyAsync(() -> verificarConsistenciaDados(importacao)),
				CompletableFuture.supplyAsync(() -> verificarEstruturaArquivo(importacao)));

		List<VerificacaoResultadoDTO> resultados = futures.stream().map(CompletableFuture::join)
				.collect(Collectors.toList());

		Map<String, Object> relatorio = gerarRelatorio(importacao, resultados);

		Instant fim = Instant.now();
		long durMs = Duration.between(inicio, fim).toMillis();
		logger.info("=== VERIFICAÇÃO CONCLUÍDA ({} ms) ===", durMs);

		return relatorio;
	}

	/**
	 * Retorna divergências detalhadas (associados e produtos novos/faltantes).
	 */
	public Map<String, Object> verificarDivergenciasDetalhadas(Long importacaoId) {
		logger.info("Buscando divergências detalhadas para importação: {}", importacaoId);
		Map<String, Object> divergencias = new HashMap<>();

		ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		try {
			// --- ASSOCIADOS ---
			Set<String> arquivoCodigos = importacao.getNotasDebito().stream().map(NotaDebitoSPC::getCodigoSocio)
					.filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());

			List<String> sistemaCodigos = obterAssociadosDoBanco();

			List<String> associadosNovos = arquivoCodigos.stream().filter(c -> !sistemaCodigos.contains(c)).sorted()
					.collect(Collectors.toList());

			List<String> associadosFaltantes = sistemaCodigos.stream().filter(c -> !arquivoCodigos.contains(c)).sorted()
					.collect(Collectors.toList());

			divergencias.put("associadosNovos", associadosNovos);
			divergencias.put("associadosFaltantes", associadosFaltantes);
			divergencias.put("totalAssociadosNovos", associadosNovos.size());
			divergencias.put("totalAssociadosFaltantes", associadosFaltantes.size());

			// --- PRODUTOS ---
			Set<String> produtosArquivo = importacao.getNotasDebito().stream().flatMap(n -> n.getItens().stream())
					.map(ItemSPC::getCodigoProduto).filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty())
					.collect(Collectors.toSet());

			List<String> produtosBanco = obterProdutosDoBanco();

			List<String> produtosNovos = produtosArquivo.stream().filter(c -> !produtosBanco.contains(c)).sorted()
					.collect(Collectors.toList());

			List<String> produtosFaltantes = produtosBanco.stream().filter(c -> !produtosArquivo.contains(c)).sorted()
					.collect(Collectors.toList());

			divergencias.put("produtosNovos", produtosNovos);
			divergencias.put("produtosFaltantes", produtosFaltantes);
			divergencias.put("totalProdutosNovos", produtosNovos.size());
			divergencias.put("totalProdutosFaltantes", produtosFaltantes.size());

			logger.info("Divergências detalhadas calculadas: associadosNovos={}, produtosNovos={}",
					associadosNovos.size(), produtosNovos.size());

		} catch (Exception e) {
			logger.error("Erro ao buscar divergências detalhadas: {}", e.getMessage());
			// Retorna estrutura vazia, mas consistente
			divergencias.put("associadosNovos", new ArrayList<>());
			divergencias.put("associadosFaltantes", new ArrayList<>());
			divergencias.put("totalAssociadosNovos", 0);
			divergencias.put("totalAssociadosFaltantes", 0);
			divergencias.put("produtosNovos", new ArrayList<>());
			divergencias.put("produtosFaltantes", new ArrayList<>());
			divergencias.put("totalProdutosNovos", 0);
			divergencias.put("totalProdutosFaltantes", 0);
		}

		return divergencias;
	}

	// ----------------------------------
	// CATEGORIA: Parâmetros do arquivo
	// ----------------------------------
	private VerificacaoResultadoDTO verificarParametrosArquivo(ImportacaoSPC importacao) {
		logger.info("Verificando parâmetros do arquivo...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Parâmetros do Arquivo");

		try {
			int encontrados = 0;
			Map<String, Object> detalhes = new LinkedHashMap<>();

			if (importacao.getParametros() != null && !importacao.getParametros().isEmpty()) {
				ParametrosSPC p = importacao.getParametros().get(0);

				if (p.getDataReferencia() != null) {
					detalhes.put("Referencia", p.getDataReferencia());
					encontrados++;
				}
				if (p.getDataInicioPeriodoRef() != null) {
					detalhes.put("Periodo_inicial", p.getDataInicioPeriodoRef());
					encontrados++;
				}
				if (p.getDataFimPeriodoRef() != null) {
					detalhes.put("Periodo_final", p.getDataFimPeriodoRef());
					encontrados++;
				}
				if (p.getData1oVencimento() != null) {
					detalhes.put("Vencimento", p.getData1oVencimento());
					encontrados++;
				}
			}

			resultado.setDetalhes(detalhes);
			resultado.setQuantidadeArquivo((long) encontrados);
			resultado.setQuantidadeBanco(4L); // 4 campos esperados
			resultado.setDiferenca(resultado.getQuantidadeArquivo() - resultado.getQuantidadeBanco());
			resultado.setPossuiDivergencia(resultado.getDiferenca() != 0);

			logger.info("Parâmetros - encontrados: {}, esperados: 4", encontrados);

		} catch (Exception e) {
			logger.error("Erro ao verificar parâmetros: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	public VerificacaoAssociadosDTO verificarAssociados(Long importacaoId) {

		ImportacaoSPC importacao = importacaoRepository.findById(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		VerificacaoAssociadosDTO dto = new VerificacaoAssociadosDTO();

		// ============================================================
		// 1. QUANTIDADE TRAILLER (verdade absoluta do arquivo)
		// ============================================================
		int qtdTrailler = importacao.getTraillers().stream().map(TraillerSPC::getQtdeTotalBoletos)
				.filter(Objects::nonNull).findFirst().orElse(0L).intValue();

		// ============================================================
		// 2. MAPAS DE NOTAS (arquivo)
		// ============================================================
		Map<String, NotaDebitoSPC> notasArquivo = importacao.getNotasDebito().stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		int qtdArquivo = notasArquivo.size();

		// ============================================================
		// 3. MAPAS DE NOTAS (banco)
		// ============================================================
		List<NotaDebitoSPC> listaBanco = notaDebitoSPCRepository.findByImportacao_Id(importacaoId);

		Map<String, NotaDebitoSPC> notasBanco = listaBanco.stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		int qtdBanco = notasBanco.size();

		// ============================================================
		// 4. PREENCHER DTO COM AS MESMAS REGRAS DO SEU SELECT
		// ============================================================
		dto.setQuantidadeTrailler(qtdTrailler);
		dto.setQuantidadeArquivo(qtdArquivo);
		dto.setQuantidadeBanco(qtdBanco);

		dto.setDiferencaTraillerArquivo(qtdTrailler - qtdArquivo);
		dto.setDiferencaArquivoBanco(qtdArquivo - qtdBanco);

		// ============================================================
		// LISTAS
		// ============================================================
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> faltandoPorTrailler = new ArrayList<>();
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> somenteArquivo = new ArrayList<>();
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> somenteBanco = new ArrayList<>();
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> divergentes = new ArrayList<>();

		// ============================================================
		// 5. ARQUIVO → BANCO (somenteArquivo)
		// Nota existe no arquivo, mas não existe no banco
		// ============================================================
		for (String numero : notasArquivo.keySet()) {
			if (!notasBanco.containsKey(numero)) {

				NotaDebitoSPC n = notasArquivo.get(numero);

				VerificacaoAssociadosDTO.AssociadoDivergenteDTO d = new VerificacaoAssociadosDTO.AssociadoDivergenteDTO();

				d.setStatus("SOMENTE_NO_ARQUIVO");
				d.setNumeroNota(n.getNumeroNotaDebito());
				d.setCodigoSocio(n.getCodigoSocio());
				d.setNomeAssociado(n.getNomeAssociado());
				d.setValorNota(n.getValorNota());
				d.setTotalItens(n.getItens().size());

				BigDecimal totalItens = n.getItens().stream().map(ItemSPC::getValorTotal).filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);

				d.setValorTotalItens(totalItens);

				somenteArquivo.add(d);
			}
		}

		// ============================================================
		// 6. BANCO → ARQUIVO (somenteBanco)
		// Nota existe no banco, mas não existe no arquivo
		// ============================================================
		for (String numero : notasBanco.keySet()) {
			if (!notasArquivo.containsKey(numero)) {

				NotaDebitoSPC n = notasBanco.get(numero);

				VerificacaoAssociadosDTO.AssociadoDivergenteDTO d = new VerificacaoAssociadosDTO.AssociadoDivergenteDTO();

				d.setStatus("SOMENTE_NO_BANCO");
				d.setNumeroNota(n.getNumeroNotaDebito());
				d.setCodigoSocio(n.getCodigoSocio());
				d.setNomeAssociado(n.getNomeAssociado());
				d.setValorNota(n.getValorNota());
				d.setTotalItens(n.getItens().size());

				BigDecimal totalItens = n.getItens().stream().map(ItemSPC::getValorTotal).filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);

				d.setValorTotalItens(totalItens);

				somenteBanco.add(d);
			}
		}

		// ============================================================
		// 7. BANCO → ARQUIVO — SOMENTE BANCO
		// ============================================================
		for (String numero : notasBanco.keySet()) {
			if (!notasArquivo.containsKey(numero)) {

				NotaDebitoSPC n = notasBanco.get(numero);

				VerificacaoAssociadosDTO.AssociadoDivergenteDTO d = new VerificacaoAssociadosDTO.AssociadoDivergenteDTO();

				d.setStatus("SOMENTE_BANCO");
				d.setNumeroNota(n.getNumeroNotaDebito());
				d.setCodigoSocio(n.getCodigoSocio());
				d.setNomeAssociado(n.getNomeAssociado());
				d.setValorNota(n.getValorNota());
				d.setTotalItens(n.getItens().size());

				BigDecimal total = n.getItens().stream().map(ItemSPC::getValorTotal).filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);

				d.setValorTotalItens(total);

				somenteBanco.add(d);
			}
		}

		// ============================================================
		// 8. FALTANDO POR TRAILLER
		// (quando Trailler != qtdArquivo, mostrar diferença real)
		// ============================================================
		int diferenca = qtdTrailler - qtdArquivo;

		if (diferenca != 0) {
			// Apenas informar a diferença, sem assumir que está no banco
			// porque quem manda é o TRAILLER
			// exemplo: trailler=2645 arquivo=2644 → faltando 1
			// NÃO usar notasBanco aqui
			dto.setDiferencaTraillerArquivo(diferenca);
		}

		// ============================================================
		// ATRIBUI AO DTO
		// ============================================================
		dto.setFaltandoPorTrailler(faltandoPorTrailler);
		dto.setSomenteArquivo(somenteArquivo);
		dto.setSomenteBanco(somenteBanco);
		dto.setDivergentes(divergentes);

		return dto;
	}

	private VerificacaoResultadoDTO verificarItensNota(ImportacaoSPC importacao) {
		logger.info("Verificando itens da nota...");

		VerificacaoResultadoDTO r = new VerificacaoResultadoDTO("Itens da Nota");

		try {
			// Quantidade no ARQUIVO
			long qtdArquivo = importacao.getNotasDebito().stream().mapToLong(n -> n.getItens().size()).sum();

			// Quantidade no BANCO
			long qtdBanco = itemSPCRepository.countByNotaDebito_Importacao_Id(importacao.getId());

			// Valor no ARQUIVO
			BigDecimal valorArquivo = importacao.getNotasDebito().stream().flatMap(n -> n.getItens().stream())
					.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

			// Banco usa a mesma lógica (controle virá depois se necessário)
			BigDecimal valorBanco = valorArquivo;

			r.setQuantidadeArquivo(qtdArquivo);
			r.setQuantidadeBanco(qtdBanco);
			r.setDiferenca(qtdArquivo - qtdBanco);

			r.setValorArquivo(valorArquivo);
			r.setValorBanco(valorBanco);
			r.setDiferencaValor(valorArquivo.subtract(valorBanco));

			r.setPossuiDivergencia(qtdArquivo != qtdBanco || r.getDiferencaValor().compareTo(BigDecimal.ZERO) != 0);

			logger.info("Itens da Nota - arquivo={}, banco={}, valor={}", qtdArquivo, qtdBanco, valorArquivo);

		} catch (Exception e) {
			logger.error("Erro ao verificar itens da nota: {}", e.getMessage());
			r.setPossuiDivergencia(true);
		}

		return r;
	}

	// -------------------------------------------
	// CATEGORIA: Produtos (mantido / limpo)
	// ------------------------------------------
	private VerificacaoResultadoDTO verificarProdutos(ImportacaoSPC importacao) {
	    logger.info("Verificando produtos (modo STRICT, igual ao banco)...");

	    VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Produtos");

	    try {
	        // ------------------------------
	        // 1) COLETAR PRODUTOS DO ARQUIVO
	        // ------------------------------
	        List<String> produtosArquivo = new ArrayList<>();

	        if (importacao.getNotasDebito() != null) {
	            produtosArquivo = importacao.getNotasDebito().stream()
	                    .flatMap(n -> n.getItens().stream())
	                    .map(ItemSPC::getDescricaoServico)
	                    .filter(Objects::nonNull)
	                    .map(String::trim)
	                    .filter(s -> !s.isEmpty())
	                    .distinct() // mantém distintos exatamente como estão
	                    .collect(Collectors.toList());
	        }

	        long qtdArquivo = produtosArquivo.size();
	        resultado.setQuantidadeArquivo(qtdArquivo);

	        // ----------------------------------------
	        // 2) COLETAR PRODUTOS DO BANCO (DISTINCT)
	        // ----------------------------------------
	        List<String> produtosBanco = itemSPCRepository.findDistinctProdutos(importacao.getId());

	        long qtdBanco = produtosBanco.size();
	        resultado.setQuantidadeBanco(qtdBanco);

	        // ------------------------------
	        // 3) DIFERENÇA
	        // ------------------------------
	        long diferenca = qtdArquivo - qtdBanco;
	        resultado.setDiferenca(diferenca);
	        resultado.setPossuiDivergencia(diferenca != 0);

	        // ------------------------------
	        // 4) DETALHES OPCIONAIS
	        // ------------------------------
	        Map<String, Object> detalhes = new HashMap<>();
	        detalhes.put("produtos_arquivo", produtosArquivo);
	        detalhes.put("produtos_banco", produtosBanco);
	        resultado.setDetalhes(detalhes);

	        logger.info("Produtos - Arquivo: {}, Banco: {}, Diferença: {}",
	                qtdArquivo, qtdBanco, diferenca);

	    } catch (Exception e) {
	        logger.error("Erro na verificação de produtos: {}", e.getMessage());
	        resultado.setPossuiDivergencia(true);
	    }

	    return resultado;
	}


	private String processarDescricaoProduto(String descricao) {
		if (descricao == null)
			return "";
		String d = descricao.trim();
		String upper = d.toUpperCase();
		if (upper.contains("INTERNET"))
			return d.substring(0, upper.indexOf("INTERNET")).trim();
		if (upper.contains("INT"))
			return d.substring(0, upper.indexOf("INT")).trim();
		if (upper.contains("WEBSERVICE"))
			return d.substring(0, upper.indexOf("WEBSERVICE")).trim();
		if (upper.contains("HOST A HOST"))
			return d.substring(0, upper.indexOf("HOST A HOST")).trim();
		return d.replace(".", "").trim();
	}

	// ----------------------------
	// CATEGORIA: Valores Totais
	// ----------------------------
	private VerificacaoResultadoDTO verificarValoresTotais(ImportacaoSPC importacao) {
		logger.info("Verificando valores totais...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Valor Total");

		try {
			BigDecimal valorArquivo = BigDecimal.ZERO;
			if (importacao.getNotasDebito() != null) {
				valorArquivo = importacao.getNotasDebito().stream().flatMap(n -> n.getItens().stream())
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
			}
			resultado.setValorArquivo(valorArquivo);
			resultado.setValorBanco(valorArquivo);
			resultado.setDiferencaValor(BigDecimal.ZERO);
			resultado.setPossuiDivergencia(false);

			long qtdItensArquivo = 0L;
			if (importacao.getNotasDebito() != null) {
				qtdItensArquivo = importacao.getNotasDebito().stream().mapToLong(n -> n.getItens().size()).sum();
			}
			resultado.setQuantidadeArquivo(qtdItensArquivo);

			logger.info("Valores - totalArquivo R$ {}", valorArquivo);

		} catch (Exception e) {
			logger.error("Erro na verificação de valores: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	// --------------------------------------
	// CATEGORIA: Notas de Débito (CORRIGIDO)
	// --------------------------------------
	private VerificacaoResultadoDTO verificarNotasDebito(ImportacaoSPC importacao) {
		logger.info("Verificando notas de débito...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Notas de Débito");

		try {

			// ------------------------------
			// 1) TOTAL DO ARQUIVO (Trailler)
			// ------------------------------
			Long qtdTrailler = 0L;

			try {
				if (importacao.getTraillers() != null && !importacao.getTraillers().isEmpty()) {

					// Usa qtdeTotalRegistros se existir, senão qtdeTotalBoletos
					TraillerSPC t = importacao.getTraillers().get(0);

					if (t.getQtdeTotalBoletos() != null) {
						qtdTrailler = t.getQtdeTotalBoletos(); // t.getQtdeTotalRegistros();
					} else if (t.getQtdeTotalBoletos() != null) {
						// qtdTrailler = t.getQtdeTotalBoletos().intValue();
						qtdTrailler = t.getQtdeTotalBoletos();
					}
				}
			} catch (Exception e) {
				logger.warn("Falha ao ler trailler, usando notas do arquivo como fallback");
				qtdTrailler = (long) importacao.getNotasDebito().size();
			}

			// ------------------------------------------------------
			// 2) TOTAL ENCONTRADO NO ARQUIVO (NotasDebito persisted)
			// ------------------------------------------------------
			long qtdArquivo = importacao.getNotasDebito() != null ? importacao.getNotasDebito().size() : 0;

			// ------------------------------
			// 3) VALOR TOTAL DO ARQUIVO
			// ------------------------------
			BigDecimal valorArquivo = BigDecimal.ZERO;
			if (importacao.getNotasDebito() != null) {
				valorArquivo = importacao.getNotasDebito().stream().map(NotaDebitoSPC::getValorNota)
						.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
			}

			// -----------------------------------------
			// 4) Preenche DTO com os valores corretos
			// -----------------------------------------
			resultado.setQuantidadeArquivo((long) qtdTrailler); // ARQUIVO = TRAILLER
			resultado.setQuantidadeBanco(qtdArquivo); // BANCO = notas persistidas
			resultado.setDiferenca(qtdTrailler - qtdArquivo);

			resultado.setValorArquivo(valorArquivo);
			resultado.setValorBanco(valorArquivo);
			resultado.setDiferencaValor(BigDecimal.ZERO);

			resultado.setPossuiDivergencia(qtdTrailler != qtdArquivo);

			logger.info("Notas de Débito - trailler={}, arquivo={}, valorTotal={}", qtdTrailler, qtdArquivo,
					valorArquivo);

		} catch (Exception e) {
			logger.error("Erro na verificação das notas: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	// ---------------------------------
	// CATEGORIA: Consistência de Dados
	// ---------------------------------
	private VerificacaoResultadoDTO verificarConsistenciaDados(ImportacaoSPC importacao) {
		logger.info("Verificando consistência de dados...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Consistência de Dados");

		try {
			int problemas = 0;
			List<String> detalhesList = new ArrayList<>();

			long notasSemItens = importacao.getNotasDebito().stream()
					.filter(n -> n.getItens() == null || n.getItens().isEmpty()).count();
			if (notasSemItens > 0) {
				problemas++;
				detalhesList.add(notasSemItens + " nota(s) sem itens");
			}

			long itensValorZero = importacao.getNotasDebito().stream().flatMap(n -> n.getItens().stream())
					.filter(i -> i.getValorTotal() != null && i.getValorTotal().compareTo(BigDecimal.ZERO) == 0)
					.count();
			if (itensValorZero > 0) {
				problemas++;
				detalhesList.add(itensValorZero + " item(s) com valor zero");
			}

			long documentosInvalidos = importacao.getNotasDebito().stream().map(NotaDebitoSPC::getCnpjCic)
					.filter(c -> !isDocumentoValido(c)).count();
			if (documentosInvalidos > 0) {
				problemas++;
				detalhesList.add(documentosInvalidos + " documento(s) inválido(s)");
			}

			long notasDuplicadas = importacao.getNotasDebito().stream()
					.collect(Collectors.groupingBy(NotaDebitoSPC::getNumeroNotaDebito, Collectors.counting()))
					.entrySet().stream().filter(e -> e.getValue() > 1).count();
			if (notasDuplicadas > 0) {
				problemas++;
				detalhesList.add(notasDuplicadas + " nota(s) duplicada(s)");
			}

			resultado.setQuantidadeArquivo((long) problemas);
			resultado.setQuantidadeBanco(0L);
			resultado.setDiferenca((long) problemas);
			resultado.setPossuiDivergencia(problemas > 0);

			if (!detalhesList.isEmpty()) {
				Map<String, Object> detalhes = new HashMap<>();
				detalhes.put("inconsistencias", detalhesList);
				detalhes.put("total", problemas);
				resultado.setDetalhes(detalhes);
			}

			logger.info("Consistência - problemas: {}", problemas);

		} catch (Exception e) {
			logger.error("Erro na consistência: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	// ----------------------------
	// CATEGORIA: Estrutura do Arquivo
	// ----------------------------
	private VerificacaoResultadoDTO verificarEstruturaArquivo(ImportacaoSPC importacao) {
		logger.info("Verificando estrutura do arquivo...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Estrutura do Arquivo");

		try {
			int problemas = 0;
			List<String> detalhes = new ArrayList<>();

			if (importacao.getHeaders() == null || importacao.getHeaders().isEmpty()) {
				problemas++;
				detalhes.add("Header não encontrado");
			}

			if (importacao.getTraillers() == null || importacao.getTraillers().isEmpty()) {
				problemas++;
				detalhes.add("Trailler não encontrado");
			}

			long totalItens = importacao.getNotasDebito().stream().mapToLong(n -> n.getItens().size()).sum();
			if (totalItens == 0) {
				problemas++;
				detalhes.add("Nenhum item processado");
			}

			resultado.setQuantidadeArquivo((long) problemas);
			resultado.setQuantidadeBanco(0L);
			resultado.setDiferenca((long) problemas);
			resultado.setPossuiDivergencia(problemas > 0);

			if (!detalhes.isEmpty()) {
				Map<String, Object> det = new HashMap<>();
				det.put("problemas", detalhes);
				resultado.setDetalhes(det);
			}

			logger.info("Estrutura - problemas: {}", problemas);

		} catch (Exception e) {
			logger.error("Erro na verificação da estrutura: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	// ----------------------------
	// Utils & helpers
	// ----------------------------
	private boolean isDocumentoValido(String documento) {
		if (documento == null || documento.trim().isEmpty())
			return false;
		String doc = documento.replaceAll("\\D", "");
		return doc.length() == 11 || doc.length() == 14;
	}

	private Map<String, Object> gerarRelatorio(ImportacaoSPC importacao, List<VerificacaoResultadoDTO> resultados) {
		Map<String, Object> rel = new HashMap<>();
		rel.put("importacaoId", importacao.getId());
		rel.put("nomeArquivo", importacao.getNomeArquivo());
		rel.put("dataImportacao", importacao.getDataImportacao());
		rel.put("status", importacao.getStatus());
		rel.put("resultados", resultados);

		boolean possuiDivergencias = resultados.stream().anyMatch(VerificacaoResultadoDTO::isPossuiDivergencia);
		rel.put("possuiDivergencias", possuiDivergencias);

		long totalDivergencias = resultados.stream().filter(VerificacaoResultadoDTO::isPossuiDivergencia).count();
		rel.put("totalDivergencias", totalDivergencias);

		double taxaSucesso = resultados.isEmpty() ? 100.0
				: resultados.stream().filter(r -> !r.isPossuiDivergencia()).count() / (double) resultados.size()
						* 100.0;
		rel.put("taxaSucesso", Math.round(taxaSucesso));

		int score = calcularScoreConfianca(resultados);
		rel.put("scoreConfianca", score);
		rel.put("nivelConfianca", getNivelConfianca(score));

		// log resumo
		logger.info("Relatório: arquivo={} divergencias={} taxaSucesso={} score={}", importacao.getNomeArquivo(),
				totalDivergencias, Math.round(taxaSucesso), score);

		return rel;
	}

	private int calcularScoreConfianca(List<VerificacaoResultadoDTO> resultados) {
		if (resultados.isEmpty())
			return 100;
		long ok = resultados.stream().filter(r -> !r.isPossuiDivergencia()).count();
		double penalidade = 0;
		for (VerificacaoResultadoDTO r : resultados) {
			if (r.isPossuiDivergencia()) {
				switch (r.getCategoria()) {
				case "Consistência de Dados":
					penalidade += 15;
					break;
				case "Estrutura do Arquivo":
					penalidade += 20;
					break;
				default:
					penalidade += 10;
					break;
				}
			}
		}
		double base = (ok / (double) resultados.size()) * 100.0;
		return (int) Math.max(0, base - penalidade);
	}

	private String getNivelConfianca(int score) {
		if (score >= 90)
			return "MUITO ALTA";
		if (score >= 75)
			return "ALTA";
		if (score >= 60)
			return "MÉDIA";
		if (score >= 40)
			return "BAIXA";
		return "MUITO BAIXA";
	}

	private List<String> obterAssociadosDoBanco() {
		try {
			// tenta método customizado (se existir)
			try {
				return associadoRepository.findAllCnpjCpfAtivos();
			} catch (Exception e) {
				logger.debug("Método findAllCnpjCpfAtivos não disponível: {}", e.getMessage());
			}
			// fallback: coleta códigos a partir de todos os associados
			List<Associado> todos = associadoRepository.findAll();
			return (List<String>) todos.stream().map(Associado::getCnpjCpf); // .map(Associado::getCodigo).filter(Objects::nonNull).collect(Collectors.toList());
		} catch (Exception e) {
			logger.warn("Erro ao obter associados do banco: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	private List<String> obterProdutosDoBanco() {
		try {
			try {
				return produtoRepository.findAllCodigosAtivos();
			} catch (Exception e) {
				logger.debug("Método findAllCodigosAtivos não disponível: {}", e.getMessage());
			}
			return Collections.emptyList();
		} catch (Exception e) {
			logger.warn("Erro ao obter produtos do banco: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	@Async
	public CompletableFuture<Map<String, Object>> verificarImportacaoAsync(Long importacaoId) {
		return CompletableFuture.completedFuture(verificarImportacao(importacaoId));
	}

	public Map<String, Object> healthCheck() {
		Map<String, Object> health = new HashMap<>();
		health.put("status", "UP");
		health.put("service", "VerificacaoImportacaoService");
		health.put("timestamp", System.currentTimeMillis());

		try {
			health.put("importacoesCount", importacaoSPCRepository.count());
		} catch (Exception e) {
			health.put("importacaoRepository", "ERROR: " + e.getMessage());
		}

		return health;
	}

	public void limparCache(Long importacaoId) {
		logger.info("Cache limpo (placeholder) para importacao: {}", importacaoId);
	}

	public VerificacaoAssociadosCompletoDTO verificarAssociadosCompleto(Long importacaoId) {

		ImportacaoSPC importacao = importacaoRepository.findById(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada"));

		VerificacaoAssociadosCompletoDTO dto = new VerificacaoAssociadosCompletoDTO();

		// ------------------------------
		// 1) Total esperado no ARQUIVO (Trailler)
		// ------------------------------
		int qtdTrailler = 0;

		try {
			if (importacao.getTraillers() != null && !importacao.getTraillers().isEmpty()) {
				qtdTrailler = importacao.getTraillers().get(0).getQtdeTotalRegistros();
			}
		} catch (Exception e) {
			qtdTrailler = importacao.getNotasDebito().size();
		}

		// ------------------------------
		// 2) Total encontrado no BANCO (Notas)
		// ------------------------------
		List<NotaDebitoSPC> notasBanco = notaDebitoSPCRepository.findByImportacao_Id(importacaoId);
		int qtdBanco = notasBanco.size();

		dto.setQuantidadeArquivo(qtdTrailler);
		dto.setQuantidadeBanco(qtdBanco);
		dto.setDiferenca(qtdTrailler - qtdBanco);

		// ------------------------------
		// Mapas para facilitar a comparação
		// ------------------------------
		Map<String, NotaDebitoSPC> mapaArquivo = importacao.getNotasDebito().stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		Map<String, NotaDebitoSPC> mapaBanco = notasBanco.stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		// ------------------------------
		// 3) Notas faltantes (Arquivo -> Banco)
		// ------------------------------
		List<String> notasSomenteArquivo = mapaArquivo.keySet().stream().filter(n -> !mapaBanco.containsKey(n))
				.collect(Collectors.toList());

		// Dados completos do associado para o frontend
		for (String numero : notasSomenteArquivo) {
			NotaDebitoSPC n = mapaArquivo.get(numero);

			VerificacaoAssociadosCompletoDTO.NotaBasicaDTO nb = new VerificacaoAssociadosCompletoDTO.NotaBasicaDTO();
			nb.numeroNota = numero;
			nb.codigoSocio = n.getCodigoSocio();
			nb.nomeAssociado = n.getNomeAssociado();
			nb.valorNota = n.getValorNota();
			nb.totalItens = n.getItens().size();

			dto.getNotasSomenteNoArquivo().add(nb);
		}

		// ------------------------------
		// 4) Notas que existem no banco mas não existem no arquivo
		// ------------------------------
		List<String> notasSomenteBanco = mapaBanco.keySet().stream().filter(n -> !mapaArquivo.containsKey(n))
				.collect(Collectors.toList());

		for (String numero : notasSomenteBanco) {
			NotaDebitoSPC n = mapaBanco.get(numero);

			VerificacaoAssociadosCompletoDTO.NotaBasicaDTO nb = new VerificacaoAssociadosCompletoDTO.NotaBasicaDTO();
			nb.numeroNota = numero;
			nb.codigoSocio = n.getCodigoSocio();
			nb.nomeAssociado = n.getNomeAssociado();
			nb.valorNota = n.getValorNota();
			nb.totalItens = n.getItens().size();

			dto.getNotasSomenteNoBanco().add(nb);
		}

		// ------------------------------
		// 5) Divergências completas
		// ------------------------------
		for (String numero : mapaArquivo.keySet()) {

			if (!mapaBanco.containsKey(numero)) {
				// já tratado nas notas faltantes
				continue;
			}

			NotaDebitoSPC arq = mapaArquivo.get(numero);
			NotaDebitoSPC banco = mapaBanco.get(numero);

			VerificacaoAssociadosCompletoDTO.DivergenciaAssociadoDTO div = new VerificacaoAssociadosCompletoDTO.DivergenciaAssociadoDTO();

			div.numeroNota = numero;
			div.codigoSocio = arq.getCodigoSocio();

			div.nomeArquivo = arq.getNomeAssociado();
			div.nomeBanco = banco.getNomeAssociado();
			div.nomeDivergente = !arq.getNomeAssociado().trim().equalsIgnoreCase(banco.getNomeAssociado().trim());

			div.valorArquivo = arq.getValorNota();
			div.valorBanco = banco.getValorNota();
			div.valorDivergente = arq.getValorNota() != null && banco.getValorNota() != null
					&& arq.getValorNota().compareTo(banco.getValorNota()) != 0;

			div.itensArquivo = arq.getItens().size();
			div.itensBanco = banco.getItens().size();
			div.itensDivergentes = div.itensArquivo != div.itensBanco;

			if (div.nomeDivergente || div.valorDivergente || div.itensDivergentes) {
				dto.getDivergencias().add(div);
			}
		}

		return dto;
	}

}
