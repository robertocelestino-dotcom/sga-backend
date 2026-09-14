package com.sga.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.sga.repository.FranquiaServicoCobertoRepository;

@Service
public class FranquiaCoberturaCache {

	private static final Logger log = LoggerFactory.getLogger(FranquiaCoberturaCache.class);

	private final FranquiaServicoCobertoRepository repository;

	public FranquiaCoberturaCache(FranquiaServicoCobertoRepository repository) {
		this.repository = repository;
	}

	@Cacheable(value = "franquiaCobertura", key = "#franquiaNomeBase.toUpperCase()")
	public List<String> obterServicosCobertos(String franquiaNomeBase) {
		log.debug("🔍 [CACHE MISS] Buscando serviços cobertos para franquia: {}", franquiaNomeBase);
		List<String> servicos = repository.findServicosByFranquia(franquiaNomeBase);

		if (servicos.isEmpty()) {
			log.warn("⚠️ Nenhum serviço coberto cadastrado para franquia: {}", franquiaNomeBase);
		} else {
			log.debug("   ✅ {} serviço(s) coberto(s): {}", servicos.size(), servicos);
		}
		return servicos;
	}

	@CacheEvict(value = "franquiaCobertura", allEntries = true)
	public void limparCache() {
		log.info("🗑️ Cache de cobertura de franquias invalidado");
	}
}