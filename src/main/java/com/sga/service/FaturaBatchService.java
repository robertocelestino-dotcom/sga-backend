package com.sga.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.CancelamentoProcessado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.repository.CancelamentoProcessadoRepository;
import com.sga.repository.FaturaItemRepository;
import com.sga.repository.FaturaRepository;

@Service
public class FaturaBatchService {

    private static final Logger log = LoggerFactory.getLogger(FaturaBatchService.class);
    
    private static final int BATCH_SIZE_FATURAS = 100;
    private static final int BATCH_SIZE_ITENS = 500;
    
    @Autowired
    private FaturaRepository faturaRepository;
    
    @Autowired
    private FaturaItemRepository faturaItemRepository;

    // 🔥 NOVO: Repository para persistir cancelamentos
    @Autowired
    private CancelamentoProcessadoRepository cancelamentoProcessadoRepository;
    
    /**
     * Salva faturas em lote
     */
    @Transactional
    public List<Fatura> salvarFaturasEmLote(List<Fatura> faturas) {
        if (faturas == null || faturas.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("📝 Salvando {} faturas em lote (batch size: {})", 
            faturas.size(), BATCH_SIZE_FATURAS);
        
        long inicio = System.currentTimeMillis();
        List<Fatura> faturasSalvas = new ArrayList<>();
        int total = faturas.size();
        int processados = 0;
        
        for (int i = 0; i < faturas.size(); i += BATCH_SIZE_FATURAS) {
            int end = Math.min(i + BATCH_SIZE_FATURAS, faturas.size());
            List<Fatura> batch = faturas.subList(i, end);
            
            // Preparar os itens antes de salvar
            batch.forEach(fatura -> {
                if (fatura.getItens() != null) {
                    fatura.getItens().forEach(item -> item.setFatura(fatura));
                }
            });
            
            List<Fatura> batchSalvo = faturaRepository.saveAll(batch);
            faturasSalvas.addAll(batchSalvo);
            processados += batch.size();
            
            if (processados % 500 == 0) {
                faturaRepository.flush();
                log.info("📊 {}/{} faturas salvas", processados, total);
            }
        }
        
        long tempo = System.currentTimeMillis() - inicio;
        log.info("✅ {} faturas salvas em {} ms (média: {} ms/fatura)", 
            faturasSalvas.size(), tempo, tempo / Math.max(1, faturasSalvas.size()));
        
        return faturasSalvas;
    }
    
    /**
     * Salva itens de fatura em lote
     */
    @Transactional
    public List<FaturaItem> salvarItensEmLote(List<FaturaItem> itens) {
        if (itens == null || itens.isEmpty()) {
            return new ArrayList<>();
        }
        
        log.info("📝 Salvando {} itens de fatura em lote", itens.size());
        
        long inicio = System.currentTimeMillis();
        List<FaturaItem> itensSalvos = new ArrayList<>();
        int total = itens.size();
        int processados = 0;
        
        for (int i = 0; i < itens.size(); i += BATCH_SIZE_ITENS) {
            int end = Math.min(i + BATCH_SIZE_ITENS, itens.size());
            List<FaturaItem> batch = itens.subList(i, end);
            
            List<FaturaItem> batchSalvo = faturaItemRepository.saveAll(batch);
            itensSalvos.addAll(batchSalvo);
            processados += batch.size();
            
            if (processados % 1000 == 0) {
                faturaItemRepository.flush();
                log.info("📊 {}/{} itens salvos", processados, total);
            }
        }
        
        long tempo = System.currentTimeMillis() - inicio;
        log.info("✅ {} itens salvos em {} ms", itensSalvos.size(), tempo);
        
        return itensSalvos;
    }
    
    /**
     * Salva faturas com seus itens em lote
     * 
     * 🔥 CORREÇÃO: Após salvar faturas e itens, persiste também os cancelamentos
     * que estavam pendentes (fatura.getId() == null no momento da aplicação).
     */
    @Transactional
    public void salvarFaturasComItens(List<Fatura> faturas) {
        if (faturas == null || faturas.isEmpty()) {
            return;
        }
        
        log.info("📝 Salvando {} faturas com itens em lote", faturas.size());
        long inicioTotal = System.currentTimeMillis();
        
        // 1. Salvar faturas
        List<Fatura> faturasSalvas = salvarFaturasEmLote(faturas);
        
        // 2. Coletar todos os itens
        List<FaturaItem> todosItens = new ArrayList<>();
        for (Fatura fatura : faturasSalvas) {
            if (fatura.getItens() != null) {
                for (FaturaItem item : fatura.getItens()) {
                    item.setFatura(fatura);
                    todosItens.add(item);
                }
            }
        }
        
        // 3. Salvar itens em lote
        if (!todosItens.isEmpty()) {
            salvarItensEmLote(todosItens);
        }
        
        // 🔥 4. NOVO: Persistir cancelamentos processados que ficaram pendentes
        // Neste ponto, as faturas e itens JÁ TÊM IDs, então as FKs funcionam.
        List<CancelamentoProcessado> todosCancelamentos = new ArrayList<>();
        for (Fatura fatura : faturasSalvas) {
            List<CancelamentoProcessado> pendentes = fatura.getCancelamentosPendentesPersistencia();
            if (pendentes != null && !pendentes.isEmpty()) {
                for (CancelamentoProcessado cp : pendentes) {
                    // Garantir que a fatura está setada
                    cp.setFatura(fatura);
                    
                    // 🔥 Ajustar referência do item: 
                    // o FaturaItem em memória tem o ID após o saveAll, mas precisamos
                    // garantir que o objeto apontado por cp.faturaItem seja o mesmo
                    // que foi salvo (com ID). O saveAll() do Hibernate propaga o ID
                    // para o mesmo objeto, então deve funcionar automaticamente.
                    // 
                    // Como segurança, verificamos se o faturaItem ainda está sem ID
                    // e, se estiver, buscamos pelo (fatura_id, codigo_produto, descricao).
                    if (cp.getFaturaItem() != null && cp.getFaturaItem().getId() == null) {
                        log.warn("⚠️ faturaItem sem ID para cancelamento {}. Buscando por código/descrição...",
                                cp.getCancelamentoImportacao().getId());
                        
                        FaturaItem itemEncontrado = localizarItemPorCodigoEDescricao(
                                fatura, 
                                cp.getFaturaItem().getCodigoProduto(), 
                                cp.getFaturaItem().getDescricao());
                        
                        if (itemEncontrado != null) {
                            cp.setFaturaItem(itemEncontrado);
                        } else {
                            log.warn("   ⚠️ Item não encontrado. Salvando cancelamento sem referência ao item.");
                            cp.setFaturaItem(null);
                        }
                    }
                    
                    todosCancelamentos.add(cp);
                }
            }
        }
        
        if (!todosCancelamentos.isEmpty()) {
            log.info("🗑️ Persistindo {} cancelamentos processados em lote...", todosCancelamentos.size());
            cancelamentoProcessadoRepository.saveAll(todosCancelamentos);
            cancelamentoProcessadoRepository.flush();
            log.info("✅ {} cancelamentos processados persistidos", todosCancelamentos.size());
        } else {
            log.debug("ℹ️ Nenhum cancelamento processado para persistir neste lote.");
        }
        
        long tempoTotal = System.currentTimeMillis() - inicioTotal;
        log.info("✅ {} faturas, {} itens e {} cancelamentos salvos em {} ms", 
            faturasSalvas.size(), todosItens.size(), todosCancelamentos.size(), tempoTotal);
    }

    /**
     * 🔥 Localiza um FaturaItem dentro da fatura pelo código + descrição.
     * Usado quando o item em memória não tem ID após o saveAll (caso raro).
     */
    private FaturaItem localizarItemPorCodigoEDescricao(Fatura fatura, String codigo, String descricao) {
        if (fatura.getItens() == null) return null;
        
        for (FaturaItem item : fatura.getItens()) {
            boolean codigoMatch = (codigo == null && item.getCodigoProduto() == null) 
                    || (codigo != null && codigo.equals(item.getCodigoProduto()));
            boolean descricaoMatch = (descricao == null && item.getDescricao() == null)
                    || (descricao != null && descricao.equals(item.getDescricao()));
            
            if (codigoMatch && descricaoMatch) {
                return item;
            }
        }
        return null;
    }
}