package com.sga.service;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sga.model.Planos;
import com.sga.model.Produto;
import com.sga.repository.PlanosRepository;
import com.sga.repository.ProdutoRepository;

@Service
public class CacheService {

	private static final Logger log = LoggerFactory.getLogger(CacheService.class);

	@Autowired
	private ProdutoRepository produtoRepository;

	@Autowired
	private PlanosRepository planosRepository;

	// ============================================================
	// CACHES EM MEMÓRIA PARA ACESSO RÁPIDO
	// ============================================================

	private final Map<Long, Produto> produtosPorId = new ConcurrentHashMap<>();
	private final Map<Long, Planos> planosPorId = new ConcurrentHashMap<>();

	private final Map<String, String> nomesProdutoCache = new ConcurrentHashMap<>();
	private final Map<String, String> codigosProdutoCache = new ConcurrentHashMap<>();
	private final Map<String, String> nomesPlanoCache = new ConcurrentHashMap<>();

	// ============================================================
	// 🔥 NOVOS ÍNDICES PARA FATURAMENTO
	// ============================================================

	/** Descrição normalizada → Produto */
	private final Map<String, Produto> produtosPorDescricao = new ConcurrentHashMap<>();

	/** Código RM → Produto */
	private final Map<String, Produto> produtosPorCodigoRm = new ConcurrentHashMap<>();

	/** Código (SPC) → Produto */
	private final Map<String, Produto> produtosPorCodigo = new ConcurrentHashMap<>();

	// ============================================================
	// INICIALIZAÇÃO DO CACHE
	// ============================================================

	@PostConstruct
	public void init() {
		log.info("🚀 Inicializando CacheService (versão otimizada para faturamento)...");
		carregarTodosCaches();
		log.info("✅ CacheService inicializado com sucesso!");
	}

	private void carregarTodosCaches() {
		carregarProdutos();
		carregarPlanos();
	}

	// ============================================================
	// CACHE DE PRODUTOS
	// ============================================================

	@Cacheable(value = "produtos", unless = "#result == null || #result.isEmpty()")
	public List<Produto> getProdutos() {
		log.info("📦 Carregando produtos do banco (cache vazio)");
		long inicio = System.currentTimeMillis();

		List<Produto> produtos = produtoRepository.findAll();

		produtos.forEach(p -> {
			produtosPorId.put(p.getId(), p);
			nomesProdutoCache.put("produto_" + p.getId(), p.getNome() != null ? p.getNome() : p.getDescricao());
			codigosProdutoCache.put("produto_" + p.getId(), p.getCodigo() != null ? p.getCodigo() : "");

			// 🔥 Indexa por descrição
			if (p.getDescricao() != null) {
				produtosPorDescricao.put(normalizar(p.getDescricao()), p);
			}
			// 🔥 Indexa por código RM
			if (p.getCodigoRm() != null && !p.getCodigoRm().trim().isEmpty()) {
				produtosPorCodigoRm.put(p.getCodigoRm(), p);
			}
			// 🔥 Indexa por código
			if (p.getCodigo() != null && !p.getCodigo().trim().isEmpty()) {
				produtosPorCodigo.put(p.getCodigo(), p);
			}
		});

		long tempo = System.currentTimeMillis() - inicio;
		log.info("✅ {} produtos carregados em {} ms", produtos.size(), tempo);

		return produtos;
	}

	@Cacheable(value = "produto", key = "#id", unless = "#result == null")
	public Produto getProdutoPorId(Long id) {
		Produto produto = produtosPorId.get(id);
		if (produto == null) {
			produto = produtoRepository.findById(id).orElse(null);
			if (produto != null) {
				produtosPorId.put(id, produto);
				nomesProdutoCache.put("produto_" + id,
						produto.getNome() != null ? produto.getNome() : produto.getDescricao());
				codigosProdutoCache.put("produto_" + id, produto.getCodigo() != null ? produto.getCodigo() : "");
			}
		}
		return produto;
	}

	public String getNomeProduto(Long produtoId) {
		String key = "produto_" + produtoId;
		return nomesProdutoCache.computeIfAbsent(key, k -> {
			Produto p = getProdutoPorId(produtoId);
			return p != null ? (p.getNome() != null ? p.getNome() : p.getDescricao()) : "Produto " + produtoId;
		});
	}

	public String getCodigoProduto(Long produtoId) {
		String key = "produto_" + produtoId;
		return codigosProdutoCache.computeIfAbsent(key, k -> {
			Produto p = getProdutoPorId(produtoId);
			return p != null ? (p.getCodigo() != null ? p.getCodigo() : "") : "";
		});
	}

	private void carregarProdutos() {
		List<Produto> produtos = produtoRepository.findAll();
		produtos.forEach(p -> {
			produtosPorId.put(p.getId(), p);
			nomesProdutoCache.put("produto_" + p.getId(), p.getNome() != null ? p.getNome() : p.getDescricao());
			codigosProdutoCache.put("produto_" + p.getId(), p.getCodigo() != null ? p.getCodigo() : "");

			if (p.getDescricao() != null) {
				produtosPorDescricao.put(normalizar(p.getDescricao()), p);
			}
			if (p.getCodigoRm() != null && !p.getCodigoRm().trim().isEmpty()) {
				produtosPorCodigoRm.put(p.getCodigoRm(), p);
			}
			if (p.getCodigo() != null && !p.getCodigo().trim().isEmpty()) {
				produtosPorCodigo.put(p.getCodigo(), p);
			}
		});
		log.info("📦 {} produtos carregados em cache", produtos.size());
	}

	// ============================================================
	// 🔥 NOVOS MÉTODOS — BUSCA POR DESCRIÇÃO / CÓDIGO RM / CÓDIGO
	// ============================================================

	/**
	 * Busca produto por descrição (normalizada). Complexidade: O(1).
	 */
	public Produto buscarPorDescricao(String descricao) {
		if (descricao == null || descricao.trim().isEmpty())
			return null;
		return produtosPorDescricao.get(normalizar(descricao));
	}

	/**
	 * Busca produto por código RM. Complexidade: O(1).
	 */
	public Produto buscarPorCodigoRm(String codigoRm) {
		if (codigoRm == null || codigoRm.trim().isEmpty())
			return null;
		return produtosPorCodigoRm.get(codigoRm);
	}

	/**
	 * Busca produto por código (SPC). Complexidade: O(1).
	 */
	public Produto buscarPorCodigo(String codigo) {
		if (codigo == null || codigo.trim().isEmpty())
			return null;
		return produtosPorCodigo.get(codigo);
	}

	/**
	 * Busca parcial (fallback) — usa contains sobre o cache em memória.
	 * Complexidade: O(N), mas sem I/O.
	 */
	public Produto buscarParcialPorDescricao(String descricao) {
		if (descricao == null || descricao.trim().isEmpty())
			return null;

		String chave = normalizar(descricao);

		// Primeiro: contain direto (descrição do banco contém a buscada)
		for (Map.Entry<String, Produto> entry : produtosPorDescricao.entrySet()) {
			if (entry.getKey().contains(chave)) {
				return entry.getValue();
			}
		}
		// Segundo: busca reversa (a buscada contém a descrição do banco)
		for (Map.Entry<String, Produto> entry : produtosPorDescricao.entrySet()) {
			if (chave.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}

	// ============================================================
	// CACHE DE PLANOS
	// ============================================================

	@Cacheable(value = "planos", unless = "#result == null || #result.isEmpty()")
	public List<Planos> getPlanos() {
		log.info("📦 Carregando planos do banco (cache vazio)");
		long inicio = System.currentTimeMillis();

		List<Planos> planos = planosRepository.findAll();

		planos.forEach(p -> {
			planosPorId.put(p.getId(), p);
			nomesPlanoCache.put("plano_" + p.getId(), p.getPlano() != null ? p.getPlano() : p.getPlano());
		});

		long tempo = System.currentTimeMillis() - inicio;
		log.info("✅ {} planos carregados em {} ms", planos.size(), tempo);

		return planos;
	}

	@Cacheable(value = "plano", key = "#id", unless = "#result == null")
	public Planos getPlanoPorId(Long id) {
		Planos plano = planosPorId.get(id);
		if (plano == null) {
			plano = planosRepository.findById(id).orElse(null);
			if (plano != null) {
				planosPorId.put(id, plano);
				nomesPlanoCache.put("plano_" + id, plano.getPlano() != null ? plano.getPlano() : plano.getPlano());
			}
		}
		return plano;
	}

	public String getNomePlano(Long planoId) {
		String key = "plano_" + planoId;
		return nomesPlanoCache.computeIfAbsent(key, k -> {
			Planos p = getPlanoPorId(planoId);
			return p != null ? (p.getPlano() != null ? p.getPlano() : p.getPlano()) : "Plano " + planoId;
		});
	}

	private void carregarPlanos() {
		List<Planos> planos = planosRepository.findAll();
		planos.forEach(p -> {
			planosPorId.put(p.getId(), p);
			nomesPlanoCache.put("plano_" + p.getId(), p.getPlano() != null ? p.getPlano() : p.getPlano());
		});
		log.info("📦 {} planos carregados em cache", planos.size());
	}

	// ============================================================
	// UTILITÁRIOS
	// ============================================================

	public boolean isProdutoEmCache(Long produtoId) {
		return produtosPorId.containsKey(produtoId);
	}

	public boolean isPlanoEmCache(Long planoId) {
		return planosPorId.containsKey(planoId);
	}

	public int getProdutosCacheSize() {
		return produtosPorId.size();
	}

	public int getPlanosCacheSize() {
		return planosPorId.size();
	}

	// ============================================================
	// LIMPEZA DE CACHE (PROGRAMADA)
	// ============================================================

	@CacheEvict(value = { "produtos", "planos", "produto", "plano" }, allEntries = true)
	@Scheduled(cron = "0 0 3 * * ?")
	public void limparTodosCaches() {
		log.info("🧹 Limpando todos os caches (3h da manhã)");

		produtosPorId.clear();
		planosPorId.clear();
		nomesProdutoCache.clear();
		codigosProdutoCache.clear();
		nomesPlanoCache.clear();
		produtosPorDescricao.clear();
		produtosPorCodigoRm.clear();
		produtosPorCodigo.clear();

		log.info("✅ Cache limpo com sucesso!");
	}

	@CacheEvict(value = { "produtos", "produto" }, allEntries = true)
	public void limparCacheProdutos() {
		log.info("🧹 Limpando cache de produtos");
		produtosPorId.clear();
		nomesProdutoCache.clear();
		codigosProdutoCache.clear();
		produtosPorDescricao.clear();
		produtosPorCodigoRm.clear();
		produtosPorCodigo.clear();
	}

	@CacheEvict(value = { "planos", "plano" }, allEntries = true)
	public void limparCachePlanos() {
		log.info("🧹 Limpando cache de planos");
		planosPorId.clear();
		nomesPlanoCache.clear();
	}

	// ============================================================
	// ESTATÍSTICAS DO CACHE
	// ============================================================

	public Map<String, Object> getCacheStats() {
		Map<String, Object> stats = new ConcurrentHashMap<>();
		stats.put("produtos", produtosPorId.size());
		stats.put("planos", planosPorId.size());
		stats.put("nomesProduto", nomesProdutoCache.size());
		stats.put("codigosProduto", codigosProdutoCache.size());
		stats.put("nomesPlano", nomesPlanoCache.size());
		stats.put("produtosPorDescricao", produtosPorDescricao.size());
		stats.put("produtosPorCodigoRm", produtosPorCodigoRm.size());
		stats.put("produtosPorCodigo", produtosPorCodigo.size());
		return stats;
	}

	// ============================================================
	// UTILITÁRIO INTERNO
	// ============================================================

	private String normalizar(String texto) {
		if (texto == null)
			return "";
		String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		return semAcento.trim().toUpperCase();
	}

	// ============================================================
	// 🔥 NOVO MÉTODO — BUSCA ROBUSTA COM FALLBACK
	// ============================================================
	/**
	 * Busca produto por descrição usando TODOS os índices disponíveis.
	 * Complexidade: O(1) na maioria dos casos, O(N) só no último fallback.
	 *
	 * Ordem de tentativa: 1. Lookup direto por descrição normalizada (O(1)) 2.
	 * Lookup parcial (contains) em memória (O(N) sem I/O) 3. Retorna null se não
	 * encontrar
	 *
	 * @param descricaoServico descrição vinda do SPC
	 * @return Produto encontrado ou null
	 */
	public Produto buscarPorDescricaoRobusto(String descricaoServico) {
		if (descricaoServico == null || descricaoServico.trim().isEmpty()) {
			return null;
		}

		// 1. Lookup direto (O(1))
		Produto p = buscarPorDescricao(descricaoServico);
		if (p != null) {
			return p;
		}

		// 2. Lookup parcial em memória (O(N) mas sem I/O)
		p = buscarParcialPorDescricao(descricaoServico);
		if (p != null) {
			return p;
		}

		return null;
	}

	/**
	 * Versão que retorna o código RM diretamente, já tratando vazio/null.
	 */
	public String buscarCodigoRmPorDescricao(String descricaoServico) {
		Produto p = buscarPorDescricaoRobusto(descricaoServico);
		if (p == null) {
			return null;
		}
		String codigoRm = p.getCodigoRm();
		if (codigoRm == null || codigoRm.trim().isEmpty()) {
			return null;
		}
		return codigoRm;
	}

	/**
	 * Força recarga dos caches de produtos (útil para endpoint admin).
	 */
	public synchronized void recarregarProdutos() {
		log.info("🔄 Forçando recarga do cache de produtos...");
		limparCacheProdutos();
		carregarProdutos();
		log.info("✅ Cache de produtos recarregado ({} produtos)", produtosPorId.size());
	}
}