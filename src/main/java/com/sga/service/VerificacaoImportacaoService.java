package com.sga.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.ImportacaoResumoDTO;
import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.dto.NotaFaturamentoGridDTO;
import com.sga.dto.VerificacaoAssociadosCompletoDTO;
import com.sga.dto.VerificacaoAssociadosDTO;
import com.sga.dto.VerificacaoResultadoDTO;
import com.sga.model.Associado;
import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ParametrosSPC;
import com.sga.model.Produto;
import com.sga.model.TraillerSPC;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.ImportacaoSPCRepository;
import com.sga.repository.ItemSPCRepository;
import com.sga.repository.NotaDebitoSPCRepository;
import com.sga.repository.ProdutoRepository;

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

	@Autowired
	private PdfExportService pdfExportService;
	
	@Autowired
	private RmExportService rmExportService;


	/**
	 * Entrada pública: gera relatório completo de verificação (map com chaves).
	 */
	@Transactional(readOnly = true)
	public Map<String, Object> verificarImportacao(Long importacaoId) {
		logger.info("=== INICIANDO VERIFICAÇÃO DA IMPORTAÇÃO ID: {} ===", importacaoId);
		Instant inicio = Instant.now();

		// 🔥 USAR O MÉTODO COM @EntityGraph
		ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithRelations(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		List<CompletableFuture<VerificacaoResultadoDTO>> futures = Arrays.asList(
				CompletableFuture.supplyAsync(() -> verificarParametrosArquivo(importacao)),
				CompletableFuture.supplyAsync(() -> verificarProdutos(importacao)),
				CompletableFuture.supplyAsync(() -> verificarValoresTotais(importacao)),
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

	@Transactional(readOnly = true)
	public Map<String, Object> verificarDivergenciasDetalhadas(Long importacaoId) {
		logger.info("Buscando divergências detalhadas para importação: {}", importacaoId);
		Map<String, Object> divergencias = new HashMap<>();

		// 🔥 USAR O MÉTODO COM @EntityGraph (notas + itens)
		ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithNotasAndItens(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		try {
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

	// ============================================================
	// 🔥 MÉTODOS PRIVADOS DE VERIFICAÇÃO (não precisam de @Transactional
	// porque já estão dentro de um método transacional)
	// ============================================================

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
			resultado.setQuantidadeBanco(4L);
			resultado.setDiferenca(resultado.getQuantidadeArquivo() - resultado.getQuantidadeBanco());
			resultado.setPossuiDivergencia(resultado.getDiferenca() != 0);

			logger.info("Parâmetros - encontrados: {}, esperados: 4", encontrados);

		} catch (Exception e) {
			logger.error("Erro ao verificar parâmetros: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	@Transactional(readOnly = true)
	public VerificacaoAssociadosDTO verificarAssociados(Long importacaoId) {

		ImportacaoSPC importacao = importacaoRepository.findByIdWithRelations(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		VerificacaoAssociadosDTO dto = new VerificacaoAssociadosDTO();

		int qtdTrailler = importacao.getTraillers().stream().map(TraillerSPC::getQtdeTotalBoletos)
				.filter(Objects::nonNull).findFirst().orElse(0L).intValue();

		Map<String, NotaDebitoSPC> notasArquivo = importacao.getNotasDebito().stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		int qtdArquivo = notasArquivo.size();

		List<NotaDebitoSPC> listaBanco = notaDebitoSPCRepository.findByImportacao_Id(importacaoId);

		Map<String, NotaDebitoSPC> notasBanco = listaBanco.stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		int qtdBanco = notasBanco.size();

		dto.setQuantidadeTrailler(qtdTrailler);
		dto.setQuantidadeArquivo(qtdArquivo);
		dto.setQuantidadeBanco(qtdBanco);
		dto.setDiferencaTraillerArquivo(qtdTrailler - qtdArquivo);
		dto.setDiferencaArquivoBanco(qtdArquivo - qtdBanco);

		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> faltandoPorTrailler = new ArrayList<>();
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> somenteArquivo = new ArrayList<>();
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> somenteBanco = new ArrayList<>();
		List<VerificacaoAssociadosDTO.AssociadoDivergenteDTO> divergentes = new ArrayList<>();

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

		int diferenca = qtdTrailler - qtdArquivo;
		if (diferenca != 0) {
			dto.setDiferencaTraillerArquivo(diferenca);
		}

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
			long qtdArquivo = importacao.getNotasDebito().stream().mapToLong(n -> n.getItens().size()).sum();
			long qtdBanco = itemSPCRepository.countByNotaDebito_Importacao_Id(importacao.getId());

			BigDecimal valorArquivo = importacao.getNotasDebito().stream().map(nota -> {
				BigDecimal debitos = nota.getItens().stream().filter(i -> "D".equalsIgnoreCase(i.getCreditoDebito()))
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal creditos = nota.getItens().stream().filter(i -> "C".equalsIgnoreCase(i.getCreditoDebito()))
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				return debitos.subtract(creditos);
			}).reduce(BigDecimal.ZERO, BigDecimal::add);

			BigDecimal valorBanco = BigDecimal.ZERO;
			try {
				valorBanco = itemSPCRepository.calcularValorCobrado(importacao.getId());
				if (valorBanco == null) valorBanco = BigDecimal.ZERO;
			} catch (Exception e) {
				logger.warn("Método calcularValorCobrado não disponível: {}", e.getMessage());
			}

			r.setQuantidadeArquivo(qtdArquivo);
			r.setQuantidadeBanco(qtdBanco);
			r.setDiferenca(qtdArquivo - qtdBanco);
			r.setValorArquivo(valorArquivo);
			r.setValorBanco(valorBanco);
			r.setDiferencaValor(valorArquivo.subtract(valorBanco));
			r.setPossuiDivergencia(qtdArquivo != qtdBanco || r.getDiferencaValor().compareTo(BigDecimal.ZERO) != 0);

		} catch (Exception e) {
			logger.error("Erro ao verificar itens da nota: {}", e.getMessage());
			r.setPossuiDivergencia(true);
		}

		return r;
	}

	private VerificacaoResultadoDTO verificarProdutos(ImportacaoSPC importacao) {
		logger.info("Verificando produtos...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Produtos");

		try {
			List<String> produtosArquivo = new ArrayList<>();
			if (importacao.getNotasDebito() != null) {
				produtosArquivo = importacao.getNotasDebito().stream().flatMap(n -> n.getItens().stream())
						.map(ItemSPC::getDescricaoServico).filter(Objects::nonNull).map(String::trim)
						.filter(s -> !s.isEmpty()).distinct().collect(Collectors.toList());
			}

			long qtdArquivo = produtosArquivo.size();
			resultado.setQuantidadeArquivo(qtdArquivo);

			List<String> produtosBanco = Collections.emptyList();
			try {
				produtosBanco = itemSPCRepository.findDistinctProdutos(importacao.getId());
			} catch (Exception e) {
				logger.warn("Método findDistinctProdutos não disponível: {}", e.getMessage());
			}

			long qtdBanco = produtosBanco.size();
			resultado.setQuantidadeBanco(qtdBanco);

			long diferenca = qtdArquivo - qtdBanco;
			resultado.setDiferenca(diferenca);
			resultado.setPossuiDivergencia(diferenca != 0);

			Map<String, Object> detalhes = new HashMap<>();
			detalhes.put("produtos_arquivo", produtosArquivo);
			detalhes.put("produtos_banco", produtosBanco);
			resultado.setDetalhes(detalhes);

		} catch (Exception e) {
			logger.error("Erro na verificação de produtos: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

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

		} catch (Exception e) {
			logger.error("Erro na verificação de valores: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

	private VerificacaoResultadoDTO verificarNotasDebito(ImportacaoSPC importacao) {
		logger.info("Verificando notas de débito...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Notas de Débito");

		try {
			Integer qtdTrailler = 0;
			try {
				if (importacao.getTraillers() != null && !importacao.getTraillers().isEmpty()) {
					TraillerSPC t = importacao.getTraillers().get(0);
					if (t.getQtdeTotalBoletos() != null) {
						qtdTrailler = t.getQtdeTotalBoletos().intValue();
					} else if (t.getQtdeTotalRegistros() != null) {
						qtdTrailler = t.getQtdeTotalRegistros();
					}
				}
			} catch (Exception e) {
				logger.warn("Falha ao ler trailler");
				qtdTrailler = importacao.getNotasDebito().size();
			}

			long qtdArquivo = importacao.getNotasDebito() != null ? importacao.getNotasDebito().size() : 0;

			BigDecimal valorArquivo = BigDecimal.ZERO;
			if (importacao.getNotasDebito() != null) {
				valorArquivo = importacao.getNotasDebito().stream().map(NotaDebitoSPC::getValorNota)
						.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
			}

			resultado.setQuantidadeArquivo((long) qtdTrailler);
			resultado.setQuantidadeBanco(qtdArquivo);
			resultado.setDiferenca(qtdTrailler - qtdArquivo);
			resultado.setValorArquivo(valorArquivo);
			resultado.setValorBanco(valorArquivo);
			resultado.setDiferencaValor(BigDecimal.ZERO);
			resultado.setPossuiDivergencia(qtdTrailler != qtdArquivo);

		} catch (Exception e) {
			logger.error("Erro na verificação das notas: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

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

		} catch (Exception e) {
			logger.error("Erro na consistência: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

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

		} catch (Exception e) {
			logger.error("Erro na verificação da estrutura: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
	}

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

		rel.put("resumo", resultados);

		logger.info("Relatório: arquivo={} divergencias={} taxaSucesso={} score={}", importacao.getNomeArquivo(),
				totalDivergencias, Math.round(taxaSucesso), score);

		return rel;
	}

	private int calcularScoreConfianca(List<VerificacaoResultadoDTO> resultados) {
		if (resultados.isEmpty()) return 100;
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
		if (score >= 90) return "MUITO ALTA";
		if (score >= 75) return "ALTA";
		if (score >= 60) return "MÉDIA";
		if (score >= 40) return "BAIXA";
		return "MUITO BAIXA";
	}

	private List<String> obterAssociadosDoBanco() {
		try {
			try {
				return associadoRepository.findAllCnpjCpfAtivos();
			} catch (Exception e) {
				logger.debug("Método findAllCnpjCpfAtivos não disponível: {}", e.getMessage());
			}
			List<Associado> todos = associadoRepository.findAll();
			return todos.stream().map(Associado::getCnpjCpf).filter(Objects::nonNull).collect(Collectors.toList());
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
				try {
					List<Object[]> resultados = produtoRepository.findAllAtivosComCodigo();
					return resultados.stream().map(obj -> (String) obj[1]).filter(Objects::nonNull)
							.collect(Collectors.toList());
				} catch (Exception e2) {
					logger.debug("Método findAllAtivosComCodigo também não disponível: {}", e2.getMessage());
					List<Produto> todos = produtoRepository.findAll();
					return todos.stream().map(Produto::getCodigo).filter(Objects::nonNull).collect(Collectors.toList());
				}
			}
		} catch (Exception e) {
			logger.warn("Erro ao obter produtos do banco: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	@Async
	@Transactional(readOnly = true)
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

	@Transactional(readOnly = true)
	public VerificacaoAssociadosCompletoDTO verificarAssociadosCompleto(Long importacaoId) {

		ImportacaoSPC importacao = importacaoRepository.findByIdWithRelations(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada"));

		VerificacaoAssociadosCompletoDTO dto = new VerificacaoAssociadosCompletoDTO();

		int qtdTrailler = 0;
		try {
			if (importacao.getTraillers() != null && !importacao.getTraillers().isEmpty()) {
				qtdTrailler = importacao.getTraillers().get(0).getQtdeTotalRegistros();
			}
		} catch (Exception e) {
			qtdTrailler = importacao.getNotasDebito().size();
		}

		List<NotaDebitoSPC> notasBanco = notaDebitoSPCRepository.findByImportacao_Id(importacaoId);
		int qtdBanco = notasBanco.size();

		dto.setQuantidadeArquivo(qtdTrailler);
		dto.setQuantidadeBanco(qtdBanco);
		dto.setDiferenca(qtdTrailler - qtdBanco);

		Map<String, NotaDebitoSPC> mapaArquivo = importacao.getNotasDebito().stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		Map<String, NotaDebitoSPC> mapaBanco = notasBanco.stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		List<String> notasSomenteArquivo = mapaArquivo.keySet().stream().filter(n -> !mapaBanco.containsKey(n))
				.collect(Collectors.toList());
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

		for (String numero : mapaArquivo.keySet()) {
			if (!mapaBanco.containsKey(numero)) continue;

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

	public byte[] exportarNotaPdf(Long notaId) {
		logger.info("📄 Exportando PDF da nota ID: {}", notaId);
		return pdfExportService.gerarPdfNota(notaId);
	}

	@Transactional(readOnly = true)
	public byte[] exportarResumoPdf(Long importacaoId) {
		logger.info("📄 Exportando PDF resumo da importação ID: {}", importacaoId);
		Map<String, Object> resumo = obterResumo(importacaoId);
		return pdfExportService.gerarPdfResumoImportacao(importacaoId, resumo);
	}

	@Transactional(readOnly = true)
	public List<ImportacaoResumoDTO> listarImportacoes() {
		logger.info("📋 Listando importações (com notas pré-carregadas)");

		// 🔥 USAR O MÉTODO COM @EntityGraph
		List<ImportacaoSPC> lista = importacaoSPCRepository.findAllWithNotasOrderByDataImportacaoDesc();

		return lista.stream().map(imp -> {
			int qtdeRegistros = imp.getNotasDebito() != null ? imp.getNotasDebito().size() : 0;

			BigDecimal totalValor = BigDecimal.ZERO;
			if (imp.getNotasDebito() != null) {
				totalValor = imp.getNotasDebito().stream()
						.map(n -> n.getValorNota() != null ? n.getValorNota() : BigDecimal.ZERO)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
			}

			return new ImportacaoResumoDTO(imp.getId(), imp.getNomeArquivo(), imp.getStatus(),
					imp.getDataImportacao(), qtdeRegistros, totalValor.doubleValue());
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<Map<String, Object>> listarNotas(Long importacaoId) {
		logger.info("Listando notas da importação ID: {}", importacaoId);

		ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithNotasAndItens(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		return importacao.getNotasDebito().stream().map(nota -> {
			Map<String, Object> notaMap = new LinkedHashMap<>();
			notaMap.put("id", nota.getId());
			notaMap.put("numeroNotaDebito", nota.getNumeroNotaDebito());
			notaMap.put("codigoSocio", nota.getCodigoSocio());
			notaMap.put("nomeAssociado", nota.getNomeAssociado());
			notaMap.put("cnpjCic", nota.getCnpjCic());
			notaMap.put("valorNota", nota.getValorNota());
			notaMap.put("dataVencimento", nota.getDataVencimento());
			notaMap.put("quantidadeItens", nota.getItens() != null ? nota.getItens().size() : 0);

			BigDecimal totalItens = BigDecimal.ZERO;
			if (nota.getItens() != null) {
				BigDecimal debitos = nota.getItens().stream().filter(i -> "D".equalsIgnoreCase(i.getCreditoDebito()))
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal creditos = nota.getItens().stream().filter(i -> "C".equalsIgnoreCase(i.getCreditoDebito()))
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
				totalItens = debitos.subtract(creditos);
			}
			notaMap.put("totalItensCalculado", totalItens);

			return notaMap;
		}).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<NotaDebitoResumoDTO> listarNotasComFiltro(Long importacaoId, String filtro, Pageable pageable) {
		logger.info("Listando notas com filtro - importacaoId: {}, filtro: {}, page: {}", importacaoId, filtro,
				pageable.getPageNumber());

		try {
			Page<Object[]> page;
			if (filtro != null && !filtro.trim().isEmpty()) {
				page = notaDebitoSPCRepository.listarNotasResumoComFiltro(importacaoId, filtro.trim(), pageable);
			} else {
				page = notaDebitoSPCRepository.listarNotasResumo(importacaoId, pageable);
			}

			return page.map(row -> {
				NotaDebitoResumoDTO dto = new NotaDebitoResumoDTO();
				dto.setId(((Number) row[0]).longValue());
				dto.setNumeroNota((String) row[1]);
				dto.setCodigoSocio((String) row[2]);
				dto.setNomeAssociado((String) row[3]);

				Object objDebitos = row[4];
				Object objCreditos = row[5];
				Object objFaturado = row[6];

				if (objDebitos != null) {
					if (objDebitos instanceof BigDecimal) dto.setTotalDebitos((BigDecimal) objDebitos);
					else if (objDebitos instanceof Number) dto.setTotalDebitos(BigDecimal.valueOf(((Number) objDebitos).doubleValue()));
				} else dto.setTotalDebitos(BigDecimal.ZERO);

				if (objCreditos != null) {
					if (objCreditos instanceof BigDecimal) dto.setTotalCreditos((BigDecimal) objCreditos);
					else if (objCreditos instanceof Number) dto.setTotalCreditos(BigDecimal.valueOf(((Number) objCreditos).doubleValue()));
				} else dto.setTotalCreditos(BigDecimal.ZERO);

				if (objFaturado != null) {
					if (objFaturado instanceof BigDecimal) dto.setValorFaturado((BigDecimal) objFaturado);
					else if (objFaturado instanceof Number) dto.setValorFaturado(BigDecimal.valueOf(((Number) objFaturado).doubleValue()));
				} else dto.setValorFaturado(BigDecimal.ZERO);

				return dto;
			});

		} catch (Exception e) {
			logger.error("Erro ao listar notas com filtro: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao listar notas: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public Map<String, Object> obterResumo(Long importacaoId) {
		logger.info("Obtendo resumo da importação ID: {}", importacaoId);

		ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithNotasAndItens(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		Map<String, Object> resumo = new LinkedHashMap<>();

		resumo.put("id", importacao.getId());
		resumo.put("nomeArquivo", importacao.getNomeArquivo());
		resumo.put("dataImportacao", importacao.getDataImportacao());
		resumo.put("status", importacao.getStatus());

		int totalNotas = importacao.getNotasDebito() != null ? importacao.getNotasDebito().size() : 0;
		resumo.put("totalNotas", totalNotas);

		long totalItens = 0;
		BigDecimal valorTotalDebitos = BigDecimal.ZERO;
		BigDecimal valorTotalCreditos = BigDecimal.ZERO;
		Set<String> associadosUnicos = new HashSet<>();
		Set<String> produtosUnicos = new HashSet<>();

		if (importacao.getNotasDebito() != null) {
			for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
				if (nota.getCodigoSocio() != null) {
					associadosUnicos.add(nota.getCodigoSocio());
				}

				if (nota.getItens() != null) {
					totalItens += nota.getItens().size();

					for (ItemSPC item : nota.getItens()) {
						if (item.getCodigoProduto() != null) {
							produtosUnicos.add(item.getCodigoProduto());
						}

						if ("D".equalsIgnoreCase(item.getCreditoDebito())) {
							valorTotalDebitos = valorTotalDebitos
									.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
						} else if ("C".equalsIgnoreCase(item.getCreditoDebito())) {
							valorTotalCreditos = valorTotalCreditos
									.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
						}
					}
				}
			}
		}

		resumo.put("totalItens", totalItens);
		resumo.put("valorTotalDebitos", valorTotalDebitos);
		resumo.put("valorTotalCreditos", valorTotalCreditos);
		resumo.put("valorCobrado", valorTotalDebitos.subtract(valorTotalCreditos));
		resumo.put("associadosUnicos", associadosUnicos.size());
		resumo.put("produtosUnicos", produtosUnicos.size());

		return resumo;
	}

	@Transactional(readOnly = true)
	public Page<NotaDebitoResumoDTO> listarNotas(Long importacaoId, Pageable pageable) {
		Page<Object[]> page = notaDebitoSPCRepository.listarNotasResumo(importacaoId, pageable);

		return page.map(row -> new NotaDebitoResumoDTO(((Number) row[0]).longValue(),
				(String) row[1], (String) row[2], (String) row[3],
				(BigDecimal) row[4], (BigDecimal) row[5], (BigDecimal) row[6]));
	}

	public byte[] exportarNotasExcel(Long importacaoId) {
		logger.info("Exportando notas Excel para importação ID: {}", importacaoId);
		return exportarResumoCsv(importacaoId);
	}

	@Transactional(readOnly = true)
	public byte[] exportarResumoCsv(Long importacaoId) {
		logger.info("📊 Exportando resumo CSV para importação ID: {}", importacaoId);

		try {
			ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithNotasAndItens(importacaoId)
					.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

			StringBuilder csv = new StringBuilder();
			csv.append("Código;Nome;Total Débito;Total Crédito;Valor Cobrado\n");

			if (importacao.getNotasDebito() != null) {
				for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
					csv.append(nota.getCodigoSocio()).append(";").append("\"").append(nota.getNomeAssociado())
							.append("\"").append(";").append(nota.getValorNota() != null ? nota.getValorNota() : 0)
							.append(";").append("0").append(";")
							.append(nota.getValorNota() != null ? nota.getValorNota() : 0).append("\n");
				}
			}

			return csv.toString().getBytes("UTF-8");

		} catch (Exception e) {
			logger.error("Erro ao exportar CSV: {}", e.getMessage());
			throw new RuntimeException("Erro ao exportar CSV: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public byte[] exportarNotasCsv(Long importacaoId) {
		logger.info("📊 Exportando notas CSV para importação ID: {}", importacaoId);

		try {
			ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithNotasAndItens(importacaoId)
					.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

			StringBuilder csv = new StringBuilder();
			csv.append("ID Nota;Código;Nome;Débitos;Créditos;Valor Faturado;Data Vencimento\n");

			if (importacao.getNotasDebito() != null) {
				for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
					BigDecimal debitos = BigDecimal.ZERO;
					BigDecimal creditos = BigDecimal.ZERO;

					if (nota.getItens() != null) {
						for (ItemSPC item : nota.getItens()) {
							if ("D".equalsIgnoreCase(item.getCreditoDebito())) {
								debitos = debitos.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
							} else {
								creditos = creditos.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
							}
						}
					}

					BigDecimal valorFaturado = debitos.subtract(creditos);

					csv.append(nota.getId()).append(";").append(nota.getCodigoSocio()).append(";").append("\"")
							.append(nota.getNomeAssociado()).append("\"").append(";").append(debitos).append(";")
							.append(creditos).append(";").append(valorFaturado).append(";")
							.append(nota.getDataVencimento()).append("\n");
				}
			}

			return csv.toString().getBytes("UTF-8");

		} catch (Exception e) {
			logger.error("Erro ao exportar notas CSV: {}", e.getMessage());
			throw new RuntimeException("Erro ao exportar notas CSV: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public Page<ImportacaoResumoDTO> listarImportacoesPaginado(Pageable pageable) {
		logger.info("Listando importações paginadas...");

		try {
			Page<ImportacaoSPC> page = importacaoSPCRepository.findAll(pageable);

			List<ImportacaoResumoDTO> dtos = page.getContent().stream()
					.map(this::converterParaImportacaoResumoDTO)
					.collect(Collectors.toList());

			return new PageImpl<ImportacaoResumoDTO>(dtos, pageable, page.getTotalElements());

		} catch (Exception e) {
			logger.error("Erro ao listar importações paginadas: {}", e.getMessage());
			throw new RuntimeException("Erro ao listar importações paginadas: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public Page<NotaFaturamentoGridDTO> listarNotasPaginadas(Long importacaoId, Pageable pageable, String filtro) {
		logger.info("Listando notas paginadas para importação ID: {}, filtro: {}", importacaoId, filtro);

		try {
			ImportacaoSPC importacao = importacaoSPCRepository.findByIdWithNotasAndItens(importacaoId)
					.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

			List<NotaDebitoSPC> todasNotas = importacao.getNotasDebito();
			if (todasNotas == null) todasNotas = new ArrayList<>();

			List<NotaDebitoSPC> notasFiltradas;
			if (filtro != null && !filtro.trim().isEmpty()) {
				String filtroLower = filtro.toLowerCase().trim();
				notasFiltradas = todasNotas.stream().filter(nota -> {
					boolean matchCodigo = nota.getCodigoSocio() != null
							&& nota.getCodigoSocio().toLowerCase().contains(filtroLower);
					boolean matchNome = nota.getNomeAssociado() != null
							&& nota.getNomeAssociado().toLowerCase().contains(filtroLower);
					boolean matchNota = nota.getNumeroNotaDebito() != null
							&& nota.getNumeroNotaDebito().toLowerCase().contains(filtroLower);
					return matchCodigo || matchNome || matchNota;
				}).collect(Collectors.toList());
			} else {
				notasFiltradas = todasNotas;
			}

			notasFiltradas.sort(Comparator.comparing(NotaDebitoSPC::getId));

			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), notasFiltradas.size());

			if (start > notasFiltradas.size()) {
				return new PageImpl<NotaFaturamentoGridDTO>(Collections.emptyList(), pageable, notasFiltradas.size());
			}

			List<NotaDebitoSPC> notasPagina = notasFiltradas.subList(start, end);

			List<NotaFaturamentoGridDTO> dtos = notasPagina.stream().map(this::converterParaNotaFaturamentoGridDTO)
					.collect(Collectors.toList());

			return new PageImpl<NotaFaturamentoGridDTO>(dtos, pageable, notasFiltradas.size());

		} catch (Exception e) {
			logger.error("Erro ao listar notas paginadas: {}", e.getMessage());
			throw new RuntimeException("Erro ao listar notas paginadas: " + e.getMessage());
		}
	}

	private String escapeCsv(String value) {
		if (value == null) return "";
		if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}

	private ImportacaoResumoDTO converterParaImportacaoResumoDTO(ImportacaoSPC importacao) {
		int qtdeRegistros = importacao.getNotasDebito() != null ? importacao.getNotasDebito().size() : 0;

		BigDecimal totalValor = BigDecimal.ZERO;
		if (importacao.getNotasDebito() != null) {
			totalValor = importacao.getNotasDebito().stream()
					.map(n -> n.getValorNota() != null ? n.getValorNota() : BigDecimal.ZERO)
					.reduce(BigDecimal.ZERO, BigDecimal::add);
		}

		return new ImportacaoResumoDTO(importacao.getId(), importacao.getNomeArquivo(), importacao.getStatus(),
				importacao.getDataImportacao(), qtdeRegistros, totalValor.doubleValue());
	}

	private NotaFaturamentoGridDTO converterParaNotaFaturamentoGridDTO(NotaDebitoSPC nota) {
		List<ItemSPC> itens = nota.getItens();
		if (itens == null) itens = new ArrayList<>();

		BigDecimal totalDebitos = calcularTotalPorTipo(itens, "D");
		BigDecimal totalCredito = calcularTotalPorTipo(itens, "C");
		BigDecimal valorFaturado = totalDebitos.subtract(totalCredito);

		NotaFaturamentoGridDTO dto = new NotaFaturamentoGridDTO();
		dto.setIdNota(nota.getId());
		dto.setCodigoAssociado(nota.getCodigoSocio());
		dto.setNomeAssociado(nota.getNomeAssociado());
		dto.setTotalDebitos(totalDebitos);
		dto.setTotalCredito(totalCredito);
		dto.setValorFaturado(valorFaturado);
		dto.setDataImportacao(nota.getImportacao().getDataImportacao());

		return dto;
	}

	private BigDecimal calcularTotalPorTipo(List<ItemSPC> itens, String tipo) {
		if (itens == null) return BigDecimal.ZERO;
		return itens.stream().filter(item -> tipo.equalsIgnoreCase(item.getCreditoDebito()))
				.map(item -> item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Transactional(readOnly = true)
	public Map<String, Object> obterUltimaImportacao() {
		logger.info("Obtendo última importação...");

		try {
			List<ImportacaoSPC> importacoes = importacaoSPCRepository.findAllWithNotasOrderByDataImportacaoDesc();

			if (importacoes.isEmpty()) {
				Map<String, Object> resultado = new HashMap<>();
				resultado.put("mensagem", "Nenhuma importação encontrada");
				resultado.put("existeImportacao", false);
				return resultado;
			}

			ImportacaoSPC ultimaImportacao = importacoes.get(0);

			int totalNotas = ultimaImportacao.getNotasDebito() != null ? ultimaImportacao.getNotasDebito().size() : 0;
			BigDecimal valorTotal = BigDecimal.ZERO;
			int associadosUnicos = 0;

			if (ultimaImportacao.getNotasDebito() != null) {
				Set<String> codigosAssociados = new HashSet<>();
				for (NotaDebitoSPC nota : ultimaImportacao.getNotasDebito()) {
					if (nota.getValorNota() != null) {
						valorTotal = valorTotal.add(nota.getValorNota());
					}
					if (nota.getCodigoSocio() != null) {
						codigosAssociados.add(nota.getCodigoSocio());
					}
				}
				associadosUnicos = codigosAssociados.size();
			}

			Map<String, Object> resultado = new LinkedHashMap<>();
			resultado.put("existeImportacao", true);
			resultado.put("id", ultimaImportacao.getId());
			resultado.put("nomeArquivo", ultimaImportacao.getNomeArquivo());
			resultado.put("dataImportacao", ultimaImportacao.getDataImportacao());
			resultado.put("status", ultimaImportacao.getStatus());
			resultado.put("totalNotas", totalNotas);
			resultado.put("valorTotal", valorTotal);
			resultado.put("associadosUnicos", associadosUnicos);

			if (ultimaImportacao.getParametros() != null && !ultimaImportacao.getParametros().isEmpty()) {
				ParametrosSPC parametros = ultimaImportacao.getParametros().get(0);
				Map<String, Object> params = new HashMap<>();
				params.put("dataReferencia", parametros.getDataReferencia());
				params.put("dataInicioPeriodoRef", parametros.getDataInicioPeriodoRef());
				params.put("dataFimPeriodoRef", parametros.getDataFimPeriodoRef());
				params.put("data1oVencimento", parametros.getData1oVencimento());
				resultado.put("parametros", params);
			}

			if (ultimaImportacao.getTraillers() != null && !ultimaImportacao.getTraillers().isEmpty()) {
				TraillerSPC trailler = ultimaImportacao.getTraillers().get(0);
				Map<String, Object> trail = new HashMap<>();
				trail.put("qtdeTotalBoletos", trailler.getQtdeTotalBoletos());
				trail.put("qtdeTotalRegistros", trailler.getQtdeTotalRegistros());
				trail.put("valorTotal", trailler.getValorTotalBoletos());
				resultado.put("trailler", trail);
			}

			return resultado;

		} catch (Exception e) {
			logger.error("Erro ao obter última importação: {}", e.getMessage(), e);

			Map<String, Object> erro = new HashMap<>();
			erro.put("existeImportacao", false);
			erro.put("mensagem", "Erro ao buscar última importação: " + e.getMessage());
			erro.put("erro", true);

			return erro;
		}
	}

	public Map<String, Object> obterDetalhesNotaOtimizado(Long notaId) {
		logger.info("🔍 Buscando detalhes otimizados da nota ID: {}", notaId);

		try {
			Optional<Map<String, Object>> notaOpt = notaDebitoSPCRepository.findDetalhesBasicosById(notaId);

			if (notaOpt.isEmpty()) {
				logger.error("Nota não encontrada: {}", notaId);
				throw new RuntimeException("Nota não encontrada: " + notaId);
			}

			Map<String, Object> detalhes = new LinkedHashMap<>(notaOpt.get());

			List<Map<String, Object>> itens = notaDebitoSPCRepository.findItensByNotaId(notaId);

			BigDecimal totalDebitos = BigDecimal.ZERO;
			BigDecimal totalCreditos = BigDecimal.ZERO;

			for (Map<String, Object> item : itens) {
				String tipo = (String) item.get("tipoLancamento");
				BigDecimal valor = (BigDecimal) item.get("valorTotal");

				if (valor == null) valor = BigDecimal.ZERO;

				if ("D".equalsIgnoreCase(tipo)) {
					totalDebitos = totalDebitos.add(valor);
					item.put("tipoLancamentoDesc", "Débito");
				} else {
					totalCreditos = totalCreditos.add(valor);
					item.put("tipoLancamentoDesc", "Crédito");
				}

				item.put("quantidade", item.get("quantidade") != null ? ((Number) item.get("quantidade")).doubleValue() : 0.0);
				item.put("valorUnitario", item.get("valorUnitario") != null ? ((Number) item.get("valorUnitario")).doubleValue() : 0.0);
				item.put("valorTotal", item.get("valorTotal") != null ? ((Number) item.get("valorTotal")).doubleValue() : 0.0);
			}

			BigDecimal valorFaturado = totalDebitos.subtract(totalCreditos);

			detalhes.put("itens", itens);
			detalhes.put("totalDebitos", totalDebitos);
			detalhes.put("totalCreditos", totalCreditos);
			detalhes.put("valorFaturado", valorFaturado);
			detalhes.put("quantidadeItens", itens.size());

			detalhes.put("valorNota", detalhes.get("valorNota") != null ? ((Number) detalhes.get("valorNota")).doubleValue() : 0.0);
			detalhes.put("totalDebitos", totalDebitos.doubleValue());
			detalhes.put("totalCreditos", totalCreditos.doubleValue());
			detalhes.put("valorFaturado", valorFaturado.doubleValue());

			if (detalhes.get("importacaoId") != null) {
				Long importacaoId = ((Number) detalhes.get("importacaoId")).longValue();
				importacaoSPCRepository.findById(importacaoId)
						.ifPresent(imp -> detalhes.put("dataImportacao", imp.getDataImportacao()));
			}

			return detalhes;

		} catch (Exception e) {
			logger.error("❌ Erro ao buscar detalhes da nota {}: {}", notaId, e.getMessage(), e);
			throw new RuntimeException("Erro ao buscar detalhes da nota: " + e.getMessage());
		}
	}

	public Map<String, Object> obterDetalhesNota(Long notaId) {
		return obterDetalhesNotaOtimizado(notaId);
	}
	
	public byte[] exportarParaRm(Long importacaoId) {
	    logger.info("📤 Exportando para RM - Importação ID: {}", importacaoId);
	    return rmExportService.exportarParaRm(importacaoId);
	}
}