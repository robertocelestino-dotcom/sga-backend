package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.CancelamentoImportacao;
import com.sga.model.CancelamentoProcessado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.repository.CancelamentoImportacaoRepository;
import com.sga.repository.CancelamentoProcessadoRepository;

@Service
public class CancelamentoService {
    
    private static final Logger log = LoggerFactory.getLogger(CancelamentoService.class);
    
    @Autowired
    private CancelamentoImportacaoRepository cancelamentoImportacaoRepository;
    
    @Autowired
    private CancelamentoProcessadoRepository cancelamentoProcessadoRepository;
    
    /**
     * Aplica cancelamentos na fatura
     * 
     * @param fatura Fatura a ser processada
     * @param codigoAssociado Código SPC do associado
     * @param mes Mês de referência
     * @param ano Ano de referência
     * @param usuario Usuário que está processando
     * @param isSimulacao Se é simulação (NÃO persiste no banco)
     * @return Lista de cancelamentos aplicados
     */
    @Transactional
    public List<CancelamentoProcessado> aplicarCancelamentos(
            Fatura fatura, 
            String codigoAssociado, 
            Integer mes, 
            Integer ano,
            String usuario,
            boolean isSimulacao) {
        
        log.info("========================================");
        log.info("🗑️ APLICANDO CANCELAMENTOS");
        log.info("========================================");
        log.info("📄 Associado: {}", codigoAssociado);
        log.info("📄 Período: {}/{}", mes, ano);
        log.info("📄 Modo: {}", isSimulacao ? "🔍 SIMULAÇÃO" : "💾 PROCESSAMENTO REAL");
        log.info("📄 Itens na fatura antes: {}", fatura.getItens().size());
        
        List<CancelamentoProcessado> cancelamentosProcessados = new ArrayList<>();
        
        // 1. Buscar cancelamentos pendentes
        List<CancelamentoImportacao> cancelamentos = cancelamentoImportacaoRepository
                .findPendentesByCodigoAssociadoAndPeriodo(codigoAssociado, mes, ano);
        
        if (cancelamentos.isEmpty()) {
            log.info("ℹ️ Nenhum cancelamento pendente encontrado para o período");
            log.info("========================================");
            return cancelamentosProcessados;
        }
        
        log.info("📋 Encontrados {} cancelamentos pendentes:", cancelamentos.size());
        for (CancelamentoImportacao c : cancelamentos) {
            log.info("   - ID: {}, Produto: {}", c.getId(), c.getProdutoPersonalizado());
        }
        
        // 2. IDENTIFICAR ITENS PARA REMOVER
        List<FaturaItem> itensParaRemover = new ArrayList<>();
        List<CancelamentoImportacao> cancelamentosEncontrados = new ArrayList<>();
        List<CancelamentoImportacao> cancelamentosNaoEncontrados = new ArrayList<>();
        
        for (CancelamentoImportacao cancelamento : cancelamentos) {
            String produtoCancelar = cancelamento.getProdutoPersonalizado();
            if (produtoCancelar == null || produtoCancelar.trim().isEmpty()) {
                log.warn("⚠️ Cancelamento sem produto_personalizado: ID {}", cancelamento.getId());
                cancelamentosNaoEncontrados.add(cancelamento);
                continue;
            }
            
            String busca = produtoCancelar.toUpperCase().trim();
            log.info("🔍 Buscando item com '{}' na descrição...", busca);
            boolean encontrou = false;
            
            for (FaturaItem item : fatura.getItens()) {
                if (item.getDescricao() == null) continue;
                
                String descricao = item.getDescricao().toUpperCase();
                if (descricao.contains(busca)) {
                    if (!itensParaRemover.contains(item)) {
                        itensParaRemover.add(item);
                        cancelamentosEncontrados.add(cancelamento);
                        log.info("   ✅ ITEM ENCONTRADO: '{}' (Valor: R$ {})", 
                                item.getDescricao(), item.getValorTotal());
                        encontrou = true;
                    }
                }
            }
            
            if (!encontrou) {
                log.warn("   ❌ Produto '{}' NÃO encontrado na fatura", produtoCancelar);
                cancelamentosNaoEncontrados.add(cancelamento);
            }
        }
        
        // 3. REMOVER ITENS DA FATURA (SEMPRE, tanto simulação quanto real)
        log.info("");
        log.info("🗑️ REMOVENDO {} ITENS DA FATURA:", itensParaRemover.size());
        BigDecimal valorRemovido = BigDecimal.ZERO;
        
        for (FaturaItem item : itensParaRemover) {
            if (fatura.getItens().contains(item)) {
                valorRemovido = valorRemovido.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
                fatura.getItens().remove(item);
                log.info("   ✅ REMOVIDO: '{}' (Valor: R$ {})", 
                        item.getDescricao(), item.getValorTotal());
            }
        }
        
        // 4. RECALCULAR VALOR (SEMPRE)
        BigDecimal valorAnterior = fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO;
        fatura.recalcularTotal();
        BigDecimal novoValor = fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO;
        
        log.info("");
        log.info("💰 Valor da fatura: R$ {} → R$ {} (Removido: R$ {})", 
                valorAnterior, novoValor, valorRemovido);
        log.info("📋 Itens restantes na fatura: {}", fatura.getItens().size());
        
        // 5. 🔥 PERSISTIR NO BANCO (APENAS SE NÃO FOR SIMULAÇÃO)
        if (!isSimulacao) {
            log.info("");
            log.info("💾 PERSISTINDO CANCELAMENTOS NO BANCO...");
            
            // 5.1 Marcar cancelamentos como processados
            for (CancelamentoImportacao cancelamento : cancelamentos) {
                cancelamento.setStatus("PROCESSADO");
                cancelamento.setProcessado(true);
                cancelamento.setDataProcessamento(LocalDateTime.now());
                
                // Verificar se o produto foi encontrado
                boolean foiEncontrado = cancelamentosEncontrados.contains(cancelamento);
                if (!foiEncontrado) {
                    cancelamento.setMensagemErro("Produto não encontrado na fatura do associado");
                }
            }
            
            // Salvar todos os cancelamentos
            cancelamentoImportacaoRepository.saveAll(cancelamentos);
            log.info("✅ {} cancelamentos atualizados para PROCESSADO", cancelamentos.size());
            
            // 5.2 Criar registros em tb_cancelamento_processado (apenas se fatura tiver ID)
            if (fatura.getId() != null) {
                // Para cancelamentos com produto encontrado
                for (int i = 0; i < itensParaRemover.size() && i < cancelamentosEncontrados.size(); i++) {
                    FaturaItem item = itensParaRemover.get(i);
                    CancelamentoImportacao cancelamento = cancelamentosEncontrados.get(i);
                    
                    CancelamentoProcessado processado = new CancelamentoProcessado();
                    processado.setCancelamentoImportacao(cancelamento);
                    processado.setFatura(fatura);
                    processado.setFaturaItem(item);
                    processado.setProcessadoEm(LocalDateTime.now());
                    processado.setObservacao("Cancelamento aplicado na fatura " + 
                            (fatura.getNumeroFatura() != null ? fatura.getNumeroFatura() : "ID: " + fatura.getId()) +
                            " - Produto removido: " + item.getDescricao());
                    
                    cancelamentosProcessados.add(processado);
                }
                
                // Para cancelamentos sem produto encontrado
                for (CancelamentoImportacao cancelamento : cancelamentosNaoEncontrados) {
                    CancelamentoProcessado processado = new CancelamentoProcessado();
                    processado.setCancelamentoImportacao(cancelamento);
                    processado.setFatura(fatura);
                    processado.setFaturaItem(null);
                    processado.setProcessadoEm(LocalDateTime.now());
                    processado.setObservacao("Cancelamento processado - Produto não encontrado na fatura: " + 
                            cancelamento.getProdutoPersonalizado());
                    
                    cancelamentosProcessados.add(processado);
                }
                
                if (!cancelamentosProcessados.isEmpty()) {
                    cancelamentoProcessadoRepository.saveAll(cancelamentosProcessados);
                    log.info("✅ {} registros criados em tb_cancelamento_processado", cancelamentosProcessados.size());
                }
            } else {
                log.warn("⚠️ Fatura sem ID, não foi possível salvar CancelamentoProcessado");
            }
            
            log.info("💾 PERSISTÊNCIA CONCLUÍDA!");
        } else {
            log.info("");
            log.info("🔍 SIMULAÇÃO: NENHUM dado foi persistido no banco!");
            log.info("   Cancelamentos NÃO foram atualizados");
            log.info("   tb_cancelamento_processado NÃO foi inserido");
        }
        
        log.info("");
        log.info("📊 Resumo:");
        log.info("   ✅ Encontrados e removidos: {}", cancelamentosEncontrados.size());
        log.info("   ❌ Não encontrados: {}", cancelamentosNaoEncontrados.size());
        log.info("   💾 Persistido: {}", isSimulacao ? "NÃO (simulação)" : "SIM");
        log.info("========================================");
        
        return cancelamentosProcessados;
    }
    
    public boolean temCancelamentosPendentes(String codigoAssociado, Integer mes, Integer ano) {
        List<CancelamentoImportacao> cancelamentos = cancelamentoImportacaoRepository
                .findPendentesByCodigoAssociadoAndPeriodo(codigoAssociado, mes, ano);
        return !cancelamentos.isEmpty();
    }
    
    public List<CancelamentoImportacao> buscarCancelamentosPendentes(String codigoAssociado, Integer mes, Integer ano) {
        return cancelamentoImportacaoRepository.findPendentesByCodigoAssociadoAndPeriodo(codigoAssociado, mes, ano);
    }
}