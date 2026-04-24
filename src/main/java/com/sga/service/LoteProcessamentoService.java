package com.sga.service;

import com.sga.model.LoteProcessamento;
import com.sga.repository.LoteProcessamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LoteProcessamentoService {

    private static final Logger logger = LoggerFactory.getLogger(LoteProcessamentoService.class);

    @Autowired
    private LoteProcessamentoRepository loteRepository;

    public Page<LoteProcessamento> listarLotes(Pageable pageable) {
        logger.info("📋 Listando lotes de processamento");
        return loteRepository.findAllOrderByCriadoEmDesc(pageable);
    }

    public List<LoteProcessamento> listarEmProcessamento() {
        logger.info("📋 Listando lotes em processamento");
        return loteRepository.findEmProcessamento();
    }

    public Optional<LoteProcessamento> buscarPorId(Long id) {
        logger.info("🔍 Buscando lote ID: {}", id);
        return loteRepository.findById(id);
    }

    public List<LoteProcessamento> buscarPorMesReferencia(String mesReferencia) {
        logger.info("📅 Buscando lotes por mês: {}", mesReferencia);
        // Implementar busca por mês
        return loteRepository.findAll();
    }
}