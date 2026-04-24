// src/main/java/com/sga/service/FaturaService.java

package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.repository.FaturaRepository;

@Service
@Transactional
public class FaturaService {

    private static final Logger log = LoggerFactory.getLogger(FaturaService.class);

    @Autowired
    private FaturaRepository faturaRepository;
    
    @Autowired
    private AssociadoService associadoService;

    // ========== CRUD BÁSICO ==========

    public Fatura salvar(Fatura fatura) {
        log.info("Salvando fatura: {}", fatura.getNumeroFatura());
        if (fatura.getCriadoEm() == null) {
            fatura.setCriadoEm(LocalDateTime.now());
        }
        fatura.setAtualizadoEm(LocalDateTime.now());
        return faturaRepository.save(fatura);
    }

    public Optional<Fatura> buscarPorId(Long id) {
        log.info("Buscando fatura por ID: {}", id);
        return faturaRepository.findById(id);
    }

    public List<Fatura> buscarTodas() {
        log.info("Buscando todas as faturas");
        return faturaRepository.findAll();
    }

    public Page<Fatura> buscarTodasPaginado(Pageable pageable) {
        log.info("Buscando todas as faturas paginado");
        return faturaRepository.findAll(pageable);
    }

    // ========== MÉTODOS PARA O CONTROLLER ==========

    public Page<Fatura> listarFaturas(Pageable pageable) {
        log.info("Listando todas as faturas com paginação");
        return faturaRepository.findAll(pageable);
    }

    public Page<Fatura> listarPorAssociado(Long associadoId, Pageable pageable) {
        log.info("Listando faturas do associado ID: {} com paginação", associadoId);
        return faturaRepository.findByAssociadoId(associadoId, pageable);
    }

    public Page<Fatura> listarPorLoteProcessamento(Long loteProcessamentoId, Pageable pageable) {
        log.info("Listando faturas do lote: {} com paginação", loteProcessamentoId);
        return faturaRepository.findByLoteProcessamentoId(loteProcessamentoId, pageable);
    }

    public List<Fatura> listarFaturasPendentesRM() {
        log.info("Listando faturas pendentes de integração com RM");
        return faturaRepository.findByProcessadoRmFalse();
    }

    // ========== BUSCAS POR ASSOCIADO ==========

    public Page<Fatura> buscarPorAssociadoId(Long associadoId, Pageable pageable) {
        log.info("Buscando faturas do associado ID: {} - página: {}", associadoId, pageable.getPageNumber());
        return faturaRepository.findByAssociadoId(associadoId, pageable);
    }

    public List<Fatura> buscarPorAssociadoId(Long associadoId) {
        log.info("Buscando todas as faturas do associado ID: {}", associadoId);
        return faturaRepository.findByAssociadoId(associadoId);
    }

    public Page<Fatura> buscarPorAssociadoIdEStatus(Long associadoId, String status, Pageable pageable) {
        log.info("Buscando faturas do associado ID: {} com status: {}", associadoId, status);
        return faturaRepository.findByAssociadoIdAndStatus(associadoId, status, pageable);
    }

    // ========== BUSCAS POR PERÍODO ==========

    public List<Fatura> buscarPorPeriodo(Integer mes, Integer ano) {
        log.info("Buscando faturas do período: {}/{}", mes, ano);
        return faturaRepository.findByPeriodo(mes, ano);
    }

    public List<Fatura> buscarPorAssociadoEPeriodo(Long associadoId, Integer mes, Integer ano) {
        log.info("Buscando faturas do associado {} para período {}/{}", associadoId, mes, ano);
        if (mes == null || ano == null) {
            return List.of();
        }
        return faturaRepository.findByAssociadoIdAndMesReferenciaAndAnoReferencia(associadoId, mes, ano);
    }

    // ========== BUSCAS PARA INTEGRAÇÃO RM ==========

    public List<Fatura> buscarNaoProcessadasRm() {
        log.info("Buscando faturas não processadas no RM");
        return faturaRepository.findByProcessadoRmFalse();
    }

    // ========== BUSCAS POR STATUS ==========

    public List<Fatura> buscarPorStatus(String status) {
        log.info("Buscando faturas com status: {}", status);
        return faturaRepository.findByStatus(status);
    }

    public Long contarPorStatus(String status) {
        log.info("Contando faturas com status: {}", status);
        return faturaRepository.countByStatus(status);
    }

    public BigDecimal somarValorTotalPorStatus(String status) {
        log.info("Somando valor total das faturas com status: {}", status);
        return faturaRepository.sumValorTotalByStatus(status);
    }

    // ========== BUSCAS POR DATA ==========

    public List<Fatura> buscarPorDataVencimento(LocalDate data) {
        log.info("Buscando faturas com vencimento em: {}", data);
        return faturaRepository.findByDataVencimento(data);
    }

    public List<Fatura> buscarPorVencimentoAte(LocalDate data) {
        log.info("Buscando faturas com vencimento até: {}", data);
        return faturaRepository.findByDataVencimentoBefore(data);
    }

    public List<Fatura> buscarPorIntervaloVencimento(LocalDate inicio, LocalDate fim) {
        log.info("Buscando faturas com vencimento entre: {} e {}", inicio, fim);
        return faturaRepository.findByDataVencimentoBetween(inicio, fim);
    }

    // ========== BUSCAS POR LOTE ==========

    public List<Fatura> buscarPorLoteProcessamento(Long loteId) {
        log.info("Buscando faturas do lote de processamento: {}", loteId);
        return faturaRepository.findByLoteProcessamentoId(loteId);
    }

    // ========== ATUALIZAÇÕES ==========

    public Fatura atualizarStatus(Long id, String status) {
        log.info("Atualizando status da fatura {} para: {}", id, status);
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        fatura.setStatus(status);
        fatura.setAtualizadoEm(LocalDateTime.now());
        return faturaRepository.save(fatura);
    }

    public Fatura marcarComoProcessadoRm(Long id) {
        log.info("Marcando fatura {} como processada no RM", id);
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        fatura.setProcessadoRm(true);
        fatura.setDataProcessamento(LocalDateTime.now());
        fatura.setAtualizadoEm(LocalDateTime.now());
        return faturaRepository.save(fatura);
    }

    public void marcarLoteComoProcessadoRm(List<Long> ids) {
        log.info("Marcando lote de {} faturas como processadas no RM", ids.size());
        for (Long id : ids) {
            marcarComoProcessadoRm(id);
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    public void recalcularTotalFatura(Long id) {
        Fatura fatura = faturaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + id));
        fatura.recalcularTotal();
        faturaRepository.save(fatura);
    }

    public BigDecimal getValorTotalPeriodo(Integer mes, Integer ano) {
        log.info("Calculando valor total do período: {}/{}", mes, ano);
        return faturaRepository.sumValorTotalByPeriodo(mes, ano);
    }

    public Fatura processarFaturamento(Long associadoId, LocalDate dataEmissao, String usuario) {
        log.info("Processando faturamento para associado: {}, data: {}", associadoId, dataEmissao);
        
        Associado associado = associadoService.buscarPorIdEntity(associadoId);
        
        Fatura fatura = new Fatura();
        fatura.setAssociado(associado);
        fatura.setNumeroFatura(gerarNumeroFatura(associadoId, dataEmissao));
        fatura.setDataEmissao(dataEmissao);
        fatura.setDataVencimento(calcularDataVencimento(dataEmissao));
        fatura.setStatus("PENDENTE");
        fatura.setProcessadoRm(false);
        fatura.setUsuarioCriacao(usuario);
        fatura.setMesReferencia(dataEmissao.getMonthValue());
        fatura.setAnoReferencia(dataEmissao.getYear());
        
        return faturaRepository.save(fatura);
    }

    private String gerarNumeroFatura(Long associadoId, LocalDate dataEmissao) {
        String ano = String.valueOf(dataEmissao.getYear());
        String mes = String.format("%02d", dataEmissao.getMonthValue());
        String seq = String.format("%06d", System.currentTimeMillis() % 1000000);
        return "FAT-" + ano + mes + "-" + associadoId + "-" + seq;
    }

    private LocalDate calcularDataVencimento(LocalDate dataEmissao) {
        return dataEmissao.plusDays(10);
    }
}