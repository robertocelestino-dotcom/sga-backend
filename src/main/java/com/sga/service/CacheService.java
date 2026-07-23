package com.sga.service;

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
                nomesProdutoCache.put("produto_" + id, produto.getNome() != null ? produto.getNome() : produto.getDescricao());
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
        });
        log.info("📦 {} produtos carregados em cache", produtos.size());
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
    
    @CacheEvict(value = {
        "produtos",
        "planos",
        "produto",
        "plano"
    }, allEntries = true)
    @Scheduled(cron = "0 0 3 * * ?")
    public void limparTodosCaches() {
        log.info("🧹 Limpando todos os caches (3h da manhã)");
        
        produtosPorId.clear();
        planosPorId.clear();
        nomesProdutoCache.clear();
        codigosProdutoCache.clear();
        nomesPlanoCache.clear();
        
        log.info("✅ Cache limpo com sucesso!");
    }
    
    @CacheEvict(value = {"produtos", "produto"}, allEntries = true)
    public void limparCacheProdutos() {
        log.info("🧹 Limpando cache de produtos");
        produtosPorId.clear();
        nomesProdutoCache.clear();
        codigosProdutoCache.clear();
    }
    
    @CacheEvict(value = {"planos", "plano"}, allEntries = true)
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
        return stats;
    }
}