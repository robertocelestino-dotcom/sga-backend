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

/**
 * Serviço de verificação de importação SPC.
 *
 * Mantém as assinaturas públicas já utilizadas pelo frontend/controllers
 * (Map<String,Object> etc). Internamente organiza e documenta as verificações,
 * sem quebrar o que já funciona.
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

	@Autowired
	private PdfExportService pdfExportService;
	
	@Autowired
	private RmExportService rmExportService;


	/**
	 * Entrada pública: gera relatório completo de verificação (map com chaves).
	 * Cacheável para desempenho.
	 *
	 * Mantive assinatura: Map<String,Object> verificarImportacao(Long importacaoId)
	 */
	@Cacheable(value = "verificacaoCache", key = "#importacaoId")
	public Map<String, Object> verificarImportacao(Long importacaoId) {
		logger.info("=== INICIANDO VERIFICAÇÃO DA IMPORTAÇÃO ID: {} ===", importacaoId);
		Instant inicio = Instant.now();

		ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		/*
		 * Execução paralela de verificações principais. Mantive chamadas como
		 * CompletableFuture.supplyAsync para não alterar comportamento.
		 */
		List<CompletableFuture<VerificacaoResultadoDTO>> futures = Arrays.asList(
				CompletableFuture.supplyAsync(() -> verificarParametrosArquivo(importacao)),
				CompletableFuture.supplyAsync(() -> verificarProdutos(importacao)),
				// valores totais (opcional, comentado se não for necessário)
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
		// 7. BANCO → ARQUIVO — SOMENTE BANCO (mantido por compatibilidade)
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
		// ============================================================
		int diferenca = qtdTrailler - qtdArquivo;

		if (diferenca != 0) {
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

	/**
	 * Itens da Nota: quantidade e valor (DEBITOS - CREDITOS).
	 *
	 * Usa: - importacao (arquivo) para calcular valorArquivo = sum por nota
	 * (debitos - creditos) - itemSPCRepository.calcularValorCobrado(importacaoId)
	 * para o valorBanco (deve estar implementado) -
	 * itemSPCRepository.countByNotaDebito_Importacao_Id(importacaoId) para a
	 * quantidade no banco
	 */
	private VerificacaoResultadoDTO verificarItensNota(ImportacaoSPC importacao) {
		logger.info("Verificando itens da nota...");

		VerificacaoResultadoDTO r = new VerificacaoResultadoDTO("Itens da Nota");

		try {
			// ============================
			// Quantidade no ARQUIVO
			// ============================
			long qtdArquivo = importacao.getNotasDebito().stream().mapToLong(n -> n.getItens().size()).sum();

			// ============================
			// Quantidade no BANCO
			// ============================
			long qtdBanco = itemSPCRepository.countByNotaDebito_Importacao_Id(importacao.getId());

			// ============================
			// VALOR NO ARQUIVO (Debitos - Creditos por nota)
			// ============================
			BigDecimal valorArquivo = importacao.getNotasDebito().stream().map(nota -> {
				BigDecimal debitos = nota.getItens().stream().filter(i -> "D".equalsIgnoreCase(i.getCreditoDebito()))
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

				BigDecimal creditos = nota.getItens().stream().filter(i -> "C".equalsIgnoreCase(i.getCreditoDebito()))
						.map(ItemSPC::getValorTotal).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);

				return debitos.subtract(creditos);
			}).reduce(BigDecimal.ZERO, BigDecimal::add);

			// ============================
			// VALOR NO BANCO (calcular via repository)
			// ============================
			/*
			 * itemSPCRepository.calcularValorCobrado(importacaoId) deve executar a query:
			 *
			 * SELECT SUM(CASE WHEN ni.credito_debito = 'D' THEN ni.valor_total ELSE 0 END)
			 * - SUM(CASE WHEN ni.credito_debito = 'C' THEN ni.valor_total ELSE 0 END) FROM
			 * tb_importacao_spc im JOIN tb_nota_debito_spc nd ON im.id = nd.importacao_id
			 * JOIN tb_nota_itens_spc ni ON ni.nota_debito_id = nd.id WHERE im.id =
			 * :importacaoId
			 *
			 * Assegure que esse método exista no ItemSPCRepository.
			 */
			BigDecimal valorBanco = BigDecimal.ZERO;
			try {
				valorBanco = itemSPCRepository.calcularValorCobrado(importacao.getId());
				if (valorBanco == null)
					valorBanco = BigDecimal.ZERO;
			} catch (Exception e) {
				logger.warn("Método calcularValorCobrado não disponível no repository ou falhou: {}", e.getMessage());
				// fallback: calcular a partir dos itens do banco carregados (pode ser custoso)
				try {
					List<ItemSPC> itensBanco = itemSPCRepository.findByNotaDebito_Importacao_Id(importacao.getId());
					BigDecimal deb = itensBanco.stream().filter(i -> "D".equalsIgnoreCase(i.getCreditoDebito()))
							.map(ItemSPC::getValorTotal).filter(Objects::nonNull)
							.reduce(BigDecimal.ZERO, BigDecimal::add);

					BigDecimal cre = itensBanco.stream().filter(i -> "C".equalsIgnoreCase(i.getCreditoDebito()))
							.map(ItemSPC::getValorTotal).filter(Objects::nonNull)
							.reduce(BigDecimal.ZERO, BigDecimal::add);

					valorBanco = deb.subtract(cre);
				} catch (Exception ex) {
					logger.warn("Fallback para calcular valorBanco falhou: {}", ex.getMessage());
					valorBanco = BigDecimal.ZERO;
				}
			}

			// ============================
			// SET RESULTADOS
			// ============================
			r.setQuantidadeArquivo(qtdArquivo);
			r.setQuantidadeBanco(qtdBanco);
			r.setDiferenca(qtdArquivo - qtdBanco);

			r.setValorArquivo(valorArquivo);
			r.setValorBanco(valorBanco);

			r.setDiferencaValor(valorArquivo.subtract(valorBanco));

			r.setPossuiDivergencia(qtdArquivo != qtdBanco || r.getDiferencaValor().compareTo(BigDecimal.ZERO) != 0);

			logger.info("Itens da Nota - arquivo={}, banco={}, valorArquivo={}, valorBanco={}", qtdArquivo, qtdBanco,
					valorArquivo, valorBanco);

		} catch (Exception e) {
			logger.error("Erro ao verificar itens da nota: {}", e.getMessage());
			r.setPossuiDivergencia(true);
		}

		return r;
	}

	// -------------------------------------------
	// CATEGORIA: Produtos (comparação STRICT com DB)
	// -------------------------------------------
	private VerificacaoResultadoDTO verificarProdutos(ImportacaoSPC importacao) {
		logger.info("Verificando produtos (modo STRICT, igual ao banco)...");

		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Produtos");

		try {
			// 1) produtos do arquivo (distinct descricoes como estão)
			List<String> produtosArquivo = new ArrayList<>();
			if (importacao.getNotasDebito() != null) {
				produtosArquivo = importacao.getNotasDebito().stream().flatMap(n -> n.getItens().stream())
						.map(ItemSPC::getDescricaoServico).filter(Objects::nonNull).map(String::trim)
						.filter(s -> !s.isEmpty()).distinct().collect(Collectors.toList());
			}

			long qtdArquivo = produtosArquivo.size();
			resultado.setQuantidadeArquivo(qtdArquivo);

			// 2) produtos do banco (distinct)
			List<String> produtosBanco = Collections.emptyList();
			try {
				produtosBanco = itemSPCRepository.findDistinctProdutos(importacao.getId());
			} catch (Exception e) {
				logger.warn("Método findDistinctProdutos não disponível no repository: {}", e.getMessage());
				produtosBanco = Collections.emptyList();
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

			logger.info("Produtos - Arquivo: {}, Banco: {}, Diferença: {}", qtdArquivo, qtdBanco, diferenca);

		} catch (Exception e) {
			logger.error("Erro na verificação de produtos: {}", e.getMessage());
			resultado.setPossuiDivergencia(true);
		}

		return resultado;
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
	// CATEGORIA: Notas de Débito
	// --------------------------------------
	private VerificacaoResultadoDTO verificarNotasDebito(ImportacaoSPC importacao) {
		logger.info("Verificando notas de débito...");
		VerificacaoResultadoDTO resultado = new VerificacaoResultadoDTO("Notas de Débito");

		try {

			// 1) total do arquivo (trailler)
			// Long qtdTrailler = 0L;
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
				logger.warn("Falha ao ler trailler, usando notas do arquivo como fallback");
				// qtdTrailler = (long) importacao.getNotasDebito().size();
				qtdTrailler = importacao.getNotasDebito().size();
			}

			// 2) total encontrado no arquivo (notasPersistidas)
			long qtdArquivo = importacao.getNotasDebito() != null ? importacao.getNotasDebito().size() : 0;

			// 3) valor total do arquivo
			BigDecimal valorArquivo = BigDecimal.ZERO;
			if (importacao.getNotasDebito() != null) {
				valorArquivo = importacao.getNotasDebito().stream().map(NotaDebitoSPC::getValorNota)
						.filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
			}

			// preenche DTO
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

		// Também expõe resumoGeral (compatibilidade com frontend)
		rel.put("resumo", resultados);

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
			return todos.stream().map(Associado::getCnpjCpf).filter(Objects::nonNull).collect(Collectors.toList());
		} catch (Exception e) {
			logger.warn("Erro ao obter associados do banco: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	private List<String> obterProdutosDoBanco() {
		try {

			try {
				// Opção 1: Método que retorna apenas códigos
				return produtoRepository.findAllCodigosAtivos();
			} catch (Exception e) {
				logger.debug("Método findAllCodigosAtivos não disponível: {}", e.getMessage());

				// Opção 2: Método que retorna objetos
				try {
					List<Object[]> resultados = produtoRepository.findAllAtivosComCodigo();
					return resultados.stream().map(obj -> (String) obj[1]) // Segundo elemento é o código
							.filter(Objects::nonNull).collect(Collectors.toList());
				} catch (Exception e2) {
					logger.debug("Método findAllAtivosComCodigo também não disponível: {}", e2.getMessage());

					// Fallback: busca todos e filtra
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

		// 1) Total esperado no arquivo (trailler)
		int qtdTrailler = 0;
		try {
			if (importacao.getTraillers() != null && !importacao.getTraillers().isEmpty()) {
				qtdTrailler = importacao.getTraillers().get(0).getQtdeTotalRegistros();
			}
		} catch (Exception e) {
			qtdTrailler = importacao.getNotasDebito().size();
		}

		// 2) total no banco
		List<NotaDebitoSPC> notasBanco = notaDebitoSPCRepository.findByImportacao_Id(importacaoId);
		int qtdBanco = notasBanco.size();

		dto.setQuantidadeArquivo(qtdTrailler);
		dto.setQuantidadeBanco(qtdBanco);
		dto.setDiferenca(qtdTrailler - qtdBanco);

		Map<String, NotaDebitoSPC> mapaArquivo = importacao.getNotasDebito().stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		Map<String, NotaDebitoSPC> mapaBanco = notasBanco.stream()
				.collect(Collectors.toMap(NotaDebitoSPC::getNumeroNotaDebito, n -> n, (a, b) -> a));

		// notas somente no arquivo
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

		// notas somente no banco
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

		// divergências completas
		for (String numero : mapaArquivo.keySet()) {
			if (!mapaBanco.containsKey(numero))
				continue;

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

	public byte[] exportarResumoPdf(Long importacaoId) {
		logger.info("📄 Exportando PDF resumo da importação ID: {}", importacaoId);

		Map<String, Object> resumo = obterResumo(importacaoId);
		return pdfExportService.gerarPdfResumoImportacao(importacaoId, resumo);
	}

	public List<ImportacaoResumoDTO> listarImportacoes() {
		List<ImportacaoSPC> lista = importacaoSPCRepository.findAllByOrderByDataImportacaoDesc();

		return lista.stream().map(imp -> {
			// calcula quantidade de registros (qtde de notas)
			int qtdeRegistros = imp.getNotasDebito() != null ? imp.getNotasDebito().size() : 0;

			// calcula valor total como BigDecimal
			BigDecimal totalValor = BigDecimal.ZERO;
			if (imp.getNotasDebito() != null) {
				totalValor = imp.getNotasDebito().stream()
						.map(n -> n.getValorNota() != null ? n.getValorNota() : BigDecimal.ZERO)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
			}

			return new ImportacaoResumoDTO(imp.getId(), imp.getNomeArquivo(), imp.getStatus(), imp.getDataImportacao(),
					qtdeRegistros, totalValor.doubleValue());

		}).collect(Collectors.toList());
	}

	public List<Map<String, Object>> listarNotas(Long importacaoId) {
		logger.info("Listando notas da importação ID: {}", importacaoId);

		ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
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

			// Calcular total de itens (Débitos - Créditos)
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

	/**
	 * Método CORRIGIDO: Lista notas paginadas com filtro - Versão com BigDecimal
	 */
	public Page<NotaDebitoResumoDTO> listarNotasComFiltro(Long importacaoId, String filtro, Pageable pageable) {

		logger.info("Listando notas com filtro - importacaoId: {}, filtro: {}, page: {}", importacaoId, filtro,
				pageable.getPageNumber());

		try {
			Page<Object[]> page;

			// Se tiver filtro, usa o método com filtro
			if (filtro != null && !filtro.trim().isEmpty()) {
				page = notaDebitoSPCRepository.listarNotasResumoComFiltro(importacaoId, filtro.trim(), pageable);
			} else {
				// Sem filtro, usa o método padrão
				page = notaDebitoSPCRepository.listarNotasResumo(importacaoId, pageable);
			}

			return page.map(row -> {
				NotaDebitoResumoDTO dto = new NotaDebitoResumoDTO();

				// Índices baseados na ordem do SELECT
				dto.setId(((Number) row[0]).longValue()); // id
				dto.setNumeroNota((String) row[1]); // numeroNota
				dto.setCodigoSocio((String) row[2]); // codigoSocio
				dto.setNomeAssociado((String) row[3]); // nomeAssociado

				// CORREÇÃO: Converter para BigDecimal primeiro, depois para Double
				// Os valores vêm do banco como BigDecimal
				Object objDebitos = row[4];
				Object objCreditos = row[5];
				Object objFaturado = row[6];

				// Converter para BigDecimal e depois para Double
				if (objDebitos != null) {
					if (objDebitos instanceof BigDecimal) {
						dto.setTotalDebitos((BigDecimal) objDebitos);
					} else if (objDebitos instanceof Number) {
						dto.setTotalDebitos(BigDecimal.valueOf(((Number) objDebitos).doubleValue()));
					}
				} else {
					dto.setTotalDebitos(BigDecimal.ZERO);
				}

				if (objCreditos != null) {
					if (objCreditos instanceof BigDecimal) {
						dto.setTotalCreditos((BigDecimal) objCreditos);
					} else if (objCreditos instanceof Number) {
						dto.setTotalCreditos(BigDecimal.valueOf(((Number) objCreditos).doubleValue()));
					}
				} else {
					dto.setTotalCreditos(BigDecimal.ZERO);
				}

				if (objFaturado != null) {
					if (objFaturado instanceof BigDecimal) {
						dto.setValorFaturado((BigDecimal) objFaturado);
					} else if (objFaturado instanceof Number) {
						dto.setValorFaturado(BigDecimal.valueOf(((Number) objFaturado).doubleValue()));
					}
				} else {
					dto.setValorFaturado(BigDecimal.ZERO);
				}

				return dto;
			});

		} catch (Exception e) {
			logger.error("Erro ao listar notas com filtro: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao listar notas: " + e.getMessage());
		}
	}

	public Map<String, Object> obterResumo(Long importacaoId) {
		logger.info("Obtendo resumo da importação ID: {}", importacaoId);

		ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
				.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

		Map<String, Object> resumo = new LinkedHashMap<>();

		// Informações básicas
		resumo.put("id", importacao.getId());
		resumo.put("nomeArquivo", importacao.getNomeArquivo());
		resumo.put("dataImportacao", importacao.getDataImportacao());
		resumo.put("status", importacao.getStatus());

		// Estatísticas
		int totalNotas = importacao.getNotasDebito() != null ? importacao.getNotasDebito().size() : 0;
		resumo.put("totalNotas", totalNotas);

		long totalItens = 0;
		BigDecimal valorTotalDebitos = BigDecimal.ZERO;
		BigDecimal valorTotalCreditos = BigDecimal.ZERO;
		Set<String> associadosUnicos = new HashSet<>();
		Set<String> produtosUnicos = new HashSet<>();

		if (importacao.getNotasDebito() != null) {
			for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
				// Associados únicos
				if (nota.getCodigoSocio() != null) {
					associadosUnicos.add(nota.getCodigoSocio());
				}

				// Itens e produtos
				if (nota.getItens() != null) {
					totalItens += nota.getItens().size();

					for (ItemSPC item : nota.getItens()) {
						// Produtos únicos
						if (item.getCodigoProduto() != null) {
							produtosUnicos.add(item.getCodigoProduto());
						}

						// Totais por tipo
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

	public Page<NotaDebitoResumoDTO> listarNotas(Long importacaoId, Pageable pageable) {

		Page<Object[]> page = notaDebitoSPCRepository.listarNotasResumo(importacaoId, pageable);

		return page.map(row -> new NotaDebitoResumoDTO(((Number) row[0]).longValue(), // id
				(String) row[1], // numeroNota
				(String) row[2], // codigoSocio
				(String) row[3], // nomeAssociado
				(BigDecimal) row[4], // totalDebitos
				(BigDecimal) row[5], // totalCreditos
				(BigDecimal) row[6] // valorFaturado
		));
	}

	public byte[] exportarNotasExcel(Long importacaoId) {
		logger.info("Exportando notas Excel para importação ID: {}", importacaoId);
		// Por enquanto, usa CSV como fallback
		// Em produção, implementar com Apache POI
		return exportarResumoCsv(importacaoId);
	}

	/**
	 * Exporta resumo em CSV (usado pelo controller)
	 */
	public byte[] exportarResumoCsv(Long importacaoId) {
		logger.info("📊 Exportando resumo CSV para importação ID: {}", importacaoId);

		try {
			ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
					.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

			StringBuilder csv = new StringBuilder();

			// Cabeçalho
			csv.append("Código;Nome;Total Débito;Total Crédito;Valor Cobrado\n");

			// Dados das notas
			if (importacao.getNotasDebito() != null) {
				for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
					csv.append(nota.getCodigoSocio()).append(";").append("\"").append(nota.getNomeAssociado())
							.append("\"").append(";").append(nota.getValorNota() != null ? nota.getValorNota() : 0)
							.append(";").append("0").append(";") // Créditos (simplificado)
							.append(nota.getValorNota() != null ? nota.getValorNota() : 0).append("\n");
				}
			}

			return csv.toString().getBytes("UTF-8");

		} catch (Exception e) {
			logger.error("Erro ao exportar CSV: {}", e.getMessage());
			throw new RuntimeException("Erro ao exportar CSV: " + e.getMessage());
		}
	}

	/**
	 * Exporta notas em CSV (versão detalhada)
	 */
	public byte[] exportarNotasCsv(Long importacaoId) {
		logger.info("📊 Exportando notas CSV para importação ID: {}", importacaoId);

		try {
			ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
					.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

			StringBuilder csv = new StringBuilder();

			// Cabeçalho detalhado
			csv.append("ID Nota;Código;Nome;Débitos;Créditos;Valor Faturado;Data Vencimento\n");

			// Dados das notas
			if (importacao.getNotasDebito() != null) {
				for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
					// Calcular débitos e créditos
					BigDecimal debitos = BigDecimal.ZERO;
					BigDecimal creditos = BigDecimal.ZERO;

					if (nota.getItens() != null) {
						for (ItemSPC item : nota.getItens()) {
							if ("D".equalsIgnoreCase(item.getCreditoDebito())) {
								debitos = debitos
										.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
							} else {
								creditos = creditos
										.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
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

	// =========================================================
	// MÉTODOS FALTANTES PARA O CONTROLLER
	// =========================================================

	/**
	 * Método faltante: Listar importações paginadas
	 */
	public Page<ImportacaoResumoDTO> listarImportacoesPaginado(Pageable pageable) {
		logger.info("Listando importações paginadas...");

		try {
			Page<ImportacaoSPC> page = importacaoSPCRepository.findAll(pageable);

			List<ImportacaoResumoDTO> dtos = page.getContent().stream().map(this::converterParaImportacaoResumoDTO)
					.collect(Collectors.toList());

			// CORREÇÃO: Especificar explicitamente os tipos genéricos
			return new PageImpl<ImportacaoResumoDTO>(dtos, pageable, page.getTotalElements());

		} catch (Exception e) {
			logger.error("Erro ao listar importações paginadas: {}", e.getMessage());
			throw new RuntimeException("Erro ao listar importações paginadas: " + e.getMessage());
		}
	}

	/**
	 * Método faltante: Listar notas paginadas com filtro
	 */
	public Page<NotaFaturamentoGridDTO> listarNotasPaginadas(Long importacaoId, Pageable pageable, String filtro) {
		logger.info("Listando notas paginadas para importação ID: {}, filtro: {}", importacaoId, filtro);

		try {
			ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
					.orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

			List<NotaDebitoSPC> todasNotas = importacao.getNotasDebito();
			if (todasNotas == null) {
				todasNotas = new ArrayList<>();
			}

			// Aplicar filtro
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

			// Ordenar
			notasFiltradas.sort(Comparator.comparing(NotaDebitoSPC::getId));

			// Paginar
			int start = (int) pageable.getOffset();
			int end = Math.min((start + pageable.getPageSize()), notasFiltradas.size());

			if (start > notasFiltradas.size()) {
				// CORREÇÃO: Especificar explicitamente os tipos genéricos
				return new PageImpl<NotaFaturamentoGridDTO>(Collections.emptyList(), pageable, notasFiltradas.size());
			}

			List<NotaDebitoSPC> notasPagina = notasFiltradas.subList(start, end);

			// Converter para DTO
			List<NotaFaturamentoGridDTO> dtos = notasPagina.stream().map(this::converterParaNotaFaturamentoGridDTO)
					.collect(Collectors.toList());

			// CORREÇÃO: Especificar explicitamente os tipos genéricos
			return new PageImpl<NotaFaturamentoGridDTO>(dtos, pageable, notasFiltradas.size());

		} catch (Exception e) {
			logger.error("Erro ao listar notas paginadas: {}", e.getMessage());
			throw new RuntimeException("Erro ao listar notas paginadas: " + e.getMessage());
		}
	}

	/**
	 * Método faltante: Obter detalhes de uma nota específica
	 */
	/*
	 * public Map<String, Object> obterDetalhesNota(Long notaId) {
	 * logger.info("Obtendo detalhes da nota ID: {}", notaId);
	 * 
	 * try { NotaDebitoSPC nota = notaDebitoSPCRepository.findById(notaId)
	 * .orElseThrow(() -> new RuntimeException("Nota não encontrada: " + notaId));
	 * 
	 * List<ItemSPC> itens = nota.getItens(); if (itens == null) { itens = new
	 * ArrayList<>(); }
	 * 
	 * // Calcular totais BigDecimal totalDebitos = calcularTotalPorTipo(itens,
	 * "D"); BigDecimal totalCredito = calcularTotalPorTipo(itens, "C"); BigDecimal
	 * valorFaturado = totalDebitos.subtract(totalCredito);
	 * 
	 * // Preparar detalhes Map<String, Object> detalhes = new LinkedHashMap<>();
	 * detalhes.put("idNota", nota.getId()); detalhes.put("numeroNotaDebito",
	 * nota.getNumeroNotaDebito()); detalhes.put("codigoAssociado",
	 * nota.getCodigoSocio()); detalhes.put("nomeAssociado",
	 * nota.getNomeAssociado()); detalhes.put("cnpjCic", nota.getCnpjCic());
	 * detalhes.put("endereco", nota.getEnderecoCobranca()); detalhes.put("cidade",
	 * nota.getCidadeCobranca()); detalhes.put("uf", nota.getUfCobranca());
	 * detalhes.put("cep", nota.getCepCobranca()); detalhes.put("dataVencimento",
	 * nota.getDataVencimento()); detalhes.put("valorNota", nota.getValorNota() !=
	 * null ? nota.getValorNota().doubleValue() : 0.0); detalhes.put("totalDebitos",
	 * totalDebitos.doubleValue()); detalhes.put("totalCredito",
	 * totalCredito.doubleValue()); detalhes.put("valorFaturado",
	 * valorFaturado.doubleValue()); detalhes.put("dataImportacao",
	 * nota.getImportacao().getDataImportacao());
	 * 
	 * // Itens detalhados List<Map<String, Object>> itensDetalhados =
	 * itens.stream().map(item -> { Map<String, Object> itemMap = new
	 * LinkedHashMap<>(); itemMap.put("id", item.getId()); itemMap.put("codigo",
	 * item.getCodigoProduto()); itemMap.put("descricao",
	 * item.getDescricaoServico()); itemMap.put("quantidade",
	 * item.getQuantidadeServicos() != null ?
	 * item.getQuantidadeServicos().doubleValue() : 0.0);
	 * itemMap.put("valorUnitario", item.getValorUnitario() != null ?
	 * item.getValorUnitario().doubleValue() : 0.0); itemMap.put("valorTotal",
	 * item.getValorTotal() != null ? item.getValorTotal().doubleValue() : 0.0);
	 * itemMap.put("tipoLancamento", item.getCreditoDebito());
	 * itemMap.put("tipoLancamentoDesc",
	 * "D".equalsIgnoreCase(item.getCreditoDebito()) ? "Débito" : "Crédito"); return
	 * itemMap; }).collect(Collectors.toList());
	 * 
	 * detalhes.put("itens", itensDetalhados); detalhes.put("totalItens",
	 * itens.size());
	 * 
	 * return detalhes;
	 * 
	 * } catch (Exception e) { logger.error("Erro ao obter detalhes da nota: {}",
	 * e.getMessage()); throw new
	 * RuntimeException("Erro ao obter detalhes da nota: " + e.getMessage()); } }
	 */

	/**
	 * Método auxiliar: Escapar caracteres para CSV
	 */
	private String escapeCsv(String value) {
		if (value == null)
			return "";
		if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}

	/**
	 * Método auxiliar: Converter ImportacaoSPC para ImportacaoResumoDTO
	 */
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

	/**
	 * Método auxiliar: Converter NotaDebitoSPC para NotaFaturamentoGridDTO
	 */
	/**
	 * Método auxiliar: Converter NotaDebitoSPC para NotaFaturamentoGridDTO
	 */
	private NotaFaturamentoGridDTO converterParaNotaFaturamentoGridDTO(NotaDebitoSPC nota) {
		List<ItemSPC> itens = nota.getItens();
		if (itens == null) {
			itens = new ArrayList<>();
		}

		BigDecimal totalDebitos = calcularTotalPorTipo(itens, "D");
		BigDecimal totalCredito = calcularTotalPorTipo(itens, "C");
		BigDecimal valorFaturado = totalDebitos.subtract(totalCredito);

		// CORREÇÃO: Usar setters em vez de construtor com parâmetros
		NotaFaturamentoGridDTO dto = new NotaFaturamentoGridDTO();
		dto.setIdNota(nota.getId());
		dto.setCodigoAssociado(nota.getCodigoSocio());
		dto.setNomeAssociado(nota.getNomeAssociado());
		dto.setTotalDebitos(totalDebitos);
		dto.setTotalCredito(totalCredito);
		dto.setValorFaturado(valorFaturado);
		dto.setDataImportacao(nota.getImportacao().getDataImportacao());
		// O status já é definido como "PROCESSADO" no construtor padrão

		return dto;
	}

	/**
	 * Método auxiliar: Calcular total por tipo de lançamento
	 */
	private BigDecimal calcularTotalPorTipo(List<ItemSPC> itens, String tipo) {
		if (itens == null) {
			return BigDecimal.ZERO;
		}
		return itens.stream().filter(item -> tipo.equalsIgnoreCase(item.getCreditoDebito()))
				.map(item -> item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/**
	 * Método para obter a última importação realizada
	 */
	public Map<String, Object> obterUltimaImportacao() {
		logger.info("Obtendo última importação...");

		try {
			// Busca a última importação por data de importação (mais recente)
			List<ImportacaoSPC> importacoes = importacaoSPCRepository.findAllByOrderByDataImportacaoDesc();

			if (importacoes.isEmpty()) {
				Map<String, Object> resultado = new HashMap<>();
				resultado.put("mensagem", "Nenhuma importação encontrada");
				resultado.put("existeImportacao", false);
				return resultado;
			}

			ImportacaoSPC ultimaImportacao = importacoes.get(0);

			// Calcula estatísticas básicas
			int totalNotas = ultimaImportacao.getNotasDebito() != null ? ultimaImportacao.getNotasDebito().size() : 0;
			BigDecimal valorTotal = BigDecimal.ZERO;
			int associadosUnicos = 0;

			if (ultimaImportacao.getNotasDebito() != null) {
				Set<String> codigosAssociados = new HashSet<>();

				for (NotaDebitoSPC nota : ultimaImportacao.getNotasDebito()) {
					// Valor total
					if (nota.getValorNota() != null) {
						valorTotal = valorTotal.add(nota.getValorNota());
					}

					// Associados únicos
					if (nota.getCodigoSocio() != null) {
						codigosAssociados.add(nota.getCodigoSocio());
					}
				}

				associadosUnicos = codigosAssociados.size();
			}

			// Prepara o resultado
			Map<String, Object> resultado = new LinkedHashMap<>();
			resultado.put("existeImportacao", true);
			resultado.put("id", ultimaImportacao.getId());
			resultado.put("nomeArquivo", ultimaImportacao.getNomeArquivo());
			resultado.put("dataImportacao", ultimaImportacao.getDataImportacao());
			resultado.put("status", ultimaImportacao.getStatus());
			resultado.put("totalNotas", totalNotas);
			resultado.put("valorTotal", valorTotal);
			resultado.put("associadosUnicos", associadosUnicos);

			// Adiciona informações de parâmetros se existirem
			if (ultimaImportacao.getParametros() != null && !ultimaImportacao.getParametros().isEmpty()) {
				ParametrosSPC parametros = ultimaImportacao.getParametros().get(0);
				Map<String, Object> params = new HashMap<>();
				params.put("dataReferencia", parametros.getDataReferencia());
				params.put("dataInicioPeriodoRef", parametros.getDataInicioPeriodoRef());
				params.put("dataFimPeriodoRef", parametros.getDataFimPeriodoRef());
				params.put("data1oVencimento", parametros.getData1oVencimento());
				resultado.put("parametros", params);
			}

			// Adiciona informações do trailler se existir
			if (ultimaImportacao.getTraillers() != null && !ultimaImportacao.getTraillers().isEmpty()) {
				TraillerSPC trailler = ultimaImportacao.getTraillers().get(0);
				Map<String, Object> trail = new HashMap<>();
				trail.put("qtdeTotalBoletos", trailler.getQtdeTotalBoletos());
				trail.put("qtdeTotalRegistros", trailler.getQtdeTotalRegistros());
				trail.put("valorTotal", trailler.getValorTotalBoletos());
				resultado.put("trailler", trail);
			}

			logger.info("Última importação encontrada: ID={}, arquivo={}, totalNotas={}", ultimaImportacao.getId(),
					ultimaImportacao.getNomeArquivo(), totalNotas);

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

	/*
	 * public Page<NotaDebitoResumoDTO> listarNotas( Long importacaoId, String
	 * filtro, Pageable pageable ) {
	 * 
	 * Page<Object[]> page = notaDebitoSPCRepository
	 * .listarNotasPaginado(importacaoId, filtro, pageable);
	 * 
	 * return page.map(row -> new NotaDebitoResumoDTO( ((Number)
	 * row[0]).longValue(), (String) row[1], (String) row[2], (String) row[3],
	 * (BigDecimal) row[4], (BigDecimal) row[5], (BigDecimal) row[6] )); }
	 */

	/**
	 * MÉTODO OTIMIZADO: Obter detalhes completos de uma nota usando queries
	 * específicas
	 */
	public Map<String, Object> obterDetalhesNotaOtimizado(Long notaId) {
		logger.info("🔍 Buscando detalhes otimizados da nota ID: {}", notaId);

		try {
			// Buscar dados básicos da nota
			Optional<Map<String, Object>> notaOpt = notaDebitoSPCRepository.findDetalhesBasicosById(notaId);

			if (notaOpt.isEmpty()) {
				logger.error("Nota não encontrada: {}", notaId);
				throw new RuntimeException("Nota não encontrada: " + notaId);
			}

			Map<String, Object> detalhes = new LinkedHashMap<>(notaOpt.get());

			// Buscar itens da nota
			List<Map<String, Object>> itens = notaDebitoSPCRepository.findItensByNotaId(notaId);

			// Calcular totais
			BigDecimal totalDebitos = BigDecimal.ZERO;
			BigDecimal totalCreditos = BigDecimal.ZERO;

			for (Map<String, Object> item : itens) {
				String tipo = (String) item.get("tipoLancamento");
				BigDecimal valor = (BigDecimal) item.get("valorTotal");

				if (valor == null)
					valor = BigDecimal.ZERO;

				if ("D".equalsIgnoreCase(tipo)) {
					totalDebitos = totalDebitos.add(valor);
					item.put("tipoLancamentoDesc", "Débito");
				} else {
					totalCreditos = totalCreditos.add(valor);
					item.put("tipoLancamentoDesc", "Crédito");
				}

				// Garantir que valores numéricos sejam Double para o frontend
				item.put("quantidade",
						item.get("quantidade") != null ? ((Number) item.get("quantidade")).doubleValue() : 0.0);
				item.put("valorUnitario",
						item.get("valorUnitario") != null ? ((Number) item.get("valorUnitario")).doubleValue() : 0.0);
				item.put("valorTotal",
						item.get("valorTotal") != null ? ((Number) item.get("valorTotal")).doubleValue() : 0.0);
			}

			BigDecimal valorFaturado = totalDebitos.subtract(totalCreditos);

			// Adicionar campos calculados
			detalhes.put("itens", itens);
			detalhes.put("totalDebitos", totalDebitos);
			detalhes.put("totalCreditos", totalCreditos);
			detalhes.put("valorFaturado", valorFaturado);
			detalhes.put("quantidadeItens", itens.size());

			// Converter valores para Double (formato esperado pelo frontend)
			detalhes.put("valorNota",
					detalhes.get("valorNota") != null ? ((Number) detalhes.get("valorNota")).doubleValue() : 0.0);
			detalhes.put("totalDebitos", totalDebitos.doubleValue());
			detalhes.put("totalCreditos", totalCreditos.doubleValue());
			detalhes.put("valorFaturado", valorFaturado.doubleValue());

			// Buscar data de importação
			if (detalhes.get("importacaoId") != null) {
				Long importacaoId = ((Number) detalhes.get("importacaoId")).longValue();
				importacaoSPCRepository.findById(importacaoId)
						.ifPresent(imp -> detalhes.put("dataImportacao", imp.getDataImportacao()));
			}

			logger.info("✅ Detalhes da nota {} carregados com {} itens", notaId, itens.size());

			return detalhes;

		} catch (Exception e) {
			logger.error("❌ Erro ao buscar detalhes da nota {}: {}", notaId, e.getMessage(), e);
			throw new RuntimeException("Erro ao buscar detalhes da nota: " + e.getMessage());
		}
	}

	/**
	 * Manter o método antigo para compatibilidade, mas redirecionar para o novo
	 */
	public Map<String, Object> obterDetalhesNota(Long notaId) {
		return obterDetalhesNotaOtimizado(notaId);
	}
	
	/**
	 * Exporta dados no formato RM
	 */
	public byte[] exportarParaRm(Long importacaoId) {
	    logger.info("📤 Exportando para RM - Importação ID: {}", importacaoId);
	    return rmExportService.exportarParaRm(importacaoId);
	}
	

}
