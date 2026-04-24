package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.*;
import com.sga.repository.*;
import com.sga.rules.FaturamentoMinimoRule;
import com.sga.rules.FranquiaRule;

@Service
public class FaturaGeracaoService {

    private static final Logger log = LoggerFactory.getLogger(FaturaGeracaoService.class);

    @Autowired
    private FaturaRepository faturaRepository;
    
    @Autowired
    private FaturaItemRepository faturaItemRepository;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;

    @Autowired
    private ItemSPCRepository itemSPCRepository;
    
    @Autowired
    private AssociadoService associadoService;

    @Autowired
    private ReguaFaturamentoService reguaService;

    @Autowired
    private FranquiaRule franquiaRule;

    @Autowired
    private FaturamentoMinimoRule faturamentoMinimoRule;
    
    @Autowired
    private CancelamentoImportacaoRepository cancelamentoRepository;

    @Transactional
    public Fatura gerarFatura(Associado associado, List<NotaDebitoSPC> notas, 
                               ReguaFaturamento regua, Integer mes, Integer ano, 
                               boolean simular, String usuario) {
        
        log.info("========== INICIANDO GERAÇÃO DE FATURA ==========");
        log.info("📄 Associado: {} (ID: {}, Código SPC: {})", 
            associado.getNomeRazao(), associado.getId(), associado.getCodigoSpc());
        log.info("📅 Período: {}/{} (simular={})", mes, ano, simular);
        log.info("📋 Régua: {} (ID: {})", regua != null ? regua.getNome() : "N/A", regua != null ? regua.getId() : "N/A");
        
        if (notas == null || notas.isEmpty()) {
            log.warn("⚠️ Nenhuma nota encontrada para o associado");
            return null;
        }
        
        log.info("📊 Total de notas recebidas: {}", notas.size());
        
        // Verificar se já existe fatura para o período (exceto simulação)
        if (!simular) {
            List<Fatura> faturasExistentes = faturaRepository.findByAssociadoIdAndMesReferenciaAndAnoReferencia(
                associado.getId(), mes, ano);
            
            if (!faturasExistentes.isEmpty()) {
                log.warn("Já existe fatura para o associado {} no período {}/{}", associado.getId(), mes, ano);
                return faturasExistentes.get(0);
            }
        }
        
        // 🔥 PASSO 1: Processar notas por tipo de arquivo conforme régua
        log.info("========== PASSO 1: Processando notas por tipo de arquivo ==========");
        Map<String, List<ItemSPC>> itensPorTipo = processarNotasPorTipoArquivo(notas, regua);
        
        // 🔥 PASSO 2: Calcular quantidades conforme a fórmula usando chave composta
        log.info("========== PASSO 2: Calculando itens conforme fórmula ==========");
        List<FaturaItem> itensCalculados = calcularItensConformeFormula(itensPorTipo, regua);
        
        if (itensCalculados.isEmpty()) {
            log.warn("⚠️ Nenhum item calculado para o associado: {}", associado.getNomeRazao());
            return null;
        }
        
        // 🔥 PASSO 3: Criar fatura
        Fatura fatura = new Fatura();
        fatura.setAssociado(associado);
        fatura.setNumeroFatura(gerarNumeroFatura(associado.getId(), mes, ano));
        fatura.setDataEmissao(calcularDataEmissao(regua));
        fatura.setDataVencimento(calcularDataVencimento(regua));
        fatura.setStatus(simular ? "SIMULADO" : "PENDENTE");
        fatura.setProcessadoRm(false);
        fatura.setMesReferencia(mes);
        fatura.setAnoReferencia(ano);
        fatura.setUsuarioCriacao(usuario);
        fatura.setCriadoEm(LocalDateTime.now());
        fatura.setItens(itensCalculados);
        
        for (FaturaItem item : itensCalculados) {
            item.setFatura(fatura);
        }
        
        fatura.recalcularTotal();
        log.info("💰 Valor total após cálculo dos itens: R$ {}", fatura.getValorTotal());
        
        // 🔥 PASSO 4: Aplicar regra de franquia (ANTES do faturamento mínimo)
        log.info("========== PASSO 4: Aplicando regra de franquia ==========");
        if (regua != null && Boolean.TRUE.equals(regua.getAplicarFranquia())) {
            BigDecimal valorAntes = fatura.getValorTotal();
            fatura = franquiaRule.aplicarRegraFranquia(fatura, associado);
            log.info("💰 Valor antes da franquia: R$ {}, depois: R$ {} (diferença: R$ {})", 
                valorAntes, fatura.getValorTotal(), valorAntes.subtract(fatura.getValorTotal()));
        } else {
            log.info("📊 Regra de franquia NÃO aplicada (aplicarFranquia={})", 
                regua != null ? regua.getAplicarFranquia() : "regua nula");
        }
        
        // 🔥 PASSO 5: Aplicar regra de faturamento mínimo (DEPOIS da franquia)
        log.info("========== PASSO 5: Aplicando regra de faturamento mínimo ==========");
        boolean aplicarMinimo = regua != null && 
                                 Boolean.TRUE.equals(regua.getAplicarFaturamentoMinimo()) && 
                                 regua.isExtemporaneo();
        
        if (aplicarMinimo) {
            BigDecimal valorAntes = fatura.getValorTotal();
            fatura = faturamentoMinimoRule.aplicarRegraComplemento(fatura, associado, true);
            log.info("💰 Valor antes do faturamento mínimo: R$ {}, depois: R$ {} (diferença: R$ {})", 
                valorAntes, fatura.getValorTotal(), fatura.getValorTotal().subtract(valorAntes));
        } else {
            log.info("📊 Regra de faturamento mínimo NÃO aplicada (aplicarFaturamentoMinimo={}, extemporaneo={})", 
                regua != null ? regua.getAplicarFaturamentoMinimo() : "regua nula",
                regua != null ? regua.isExtemporaneo() : "regua nula");
        }
        
        // 🔥 PASSO 6: Remover serviços cancelados (DEPOIS das regras)
        log.info("========== PASSO 6: Removendo serviços cancelados ==========");
        if (regua != null && Boolean.TRUE.equals(regua.getAplicarCancelamentos())) {
            List<CancelamentoImportacao> cancelamentos = buscarCancelamentos(associado, mes, ano);
            if (!cancelamentos.isEmpty()) {
                BigDecimal valorAntes = fatura.getValorTotal();
                itensCalculados = removerServicosCancelados(fatura.getItens(), cancelamentos);
                fatura.setItens(itensCalculados);
                fatura.recalcularTotal();
                log.info("💰 Valor antes dos cancelamentos: R$ {}, depois: R$ {} (diferença: R$ {})", 
                    valorAntes, fatura.getValorTotal(), valorAntes.subtract(fatura.getValorTotal()));
            }
        } else {
            log.info("📊 Cancelamentos NÃO aplicados (aplicarCancelamentos={})", 
                regua != null ? regua.getAplicarCancelamentos() : "regua nula");
        }
        
        log.info("========== RESUMO FINAL ==========");
        log.info("✅ Fatura: {} - Valor total: R$ {}", fatura.getNumeroFatura(), fatura.getValorTotal());
        log.info("📊 Total de itens na fatura: {}", fatura.getItens() != null ? fatura.getItens().size() : 0);
        
        for (FaturaItem item : fatura.getItens()) {
            log.info("  - {} | {} | Quantidade: {} | Valor Unitário: {} | Total: {}", 
                item.getCodigoProduto(), item.getDescricao(), item.getQuantidade(), 
                item.getValorUnitario(), item.getValorTotal());
        }
        
        if (simular) {
            log.info("🔍 SIMULAÇÃO - Fatura seria: {} - Valor: R$ {}", 
                fatura.getNumeroFatura(), fatura.getValorTotal());
            return fatura;
        } else {
            Fatura faturaSalva = faturaRepository.save(fatura);
            if (fatura.getItens() != null && !fatura.getItens().isEmpty()) {
                for (FaturaItem item : fatura.getItens()) {
                    item.setFatura(faturaSalva);
                    faturaItemRepository.save(item);
                }
            }
            log.info("✅ Fatura persistida: {} - Valor: R$ {}", 
                faturaSalva.getNumeroFatura(), faturaSalva.getValorTotal());
            return faturaSalva;
        }
    }
    
    /**
     * 🔥 Processa notas por tipo de arquivo conforme configuração da régua
     */
    private Map<String, List<ItemSPC>> processarNotasPorTipoArquivo(List<NotaDebitoSPC> notas, ReguaFaturamento regua) {
        Map<String, List<ItemSPC>> itensPorTipo = new HashMap<>();
        itensPorTipo.put("PREVIA_ANTERIOR", new ArrayList<>());
        itensPorTipo.put("CONSOLIDACAO", new ArrayList<>());
        itensPorTipo.put("PREVIA_CORRENTE", new ArrayList<>());
        
        if (notas == null || notas.isEmpty()) {
            return itensPorTipo;
        }
        
        // Buscar tipos de arquivo configurados na régua
        List<TipoArquivoRegua> tiposArquivo = regua != null ? regua.getTiposArquivo() : null;
        
        if (tiposArquivo != null && !tiposArquivo.isEmpty()) {
            // Ordenar por ordem
            tiposArquivo.sort(Comparator.comparing(TipoArquivoRegua::getOrdem));
            
            for (int i = 0; i < notas.size() && i < tiposArquivo.size(); i++) {
                NotaDebitoSPC nota = notas.get(i);
                String tipo = tiposArquivo.get(i).getTipo();
                if (nota.getItens() != null && !nota.getItens().isEmpty()) {
                    itensPorTipo.getOrDefault(tipo, new ArrayList<>()).addAll(nota.getItens());
                    log.info("  Nota {} classificada como: {} ({} itens)", i+1, tipo, nota.getItens().size());
                }
            }
        } else {
            // Usar ordem padrão baseada no dia de emissão
            List<String> sequencia = regua != null ? regua.getSequenciaArquivosList() : new ArrayList<>();
            for (int i = 0; i < notas.size() && i < sequencia.size(); i++) {
                NotaDebitoSPC nota = notas.get(i);
                String tipo = sequencia.get(i);
                if (nota.getItens() != null && !nota.getItens().isEmpty()) {
                    itensPorTipo.getOrDefault(tipo, new ArrayList<>()).addAll(nota.getItens());
                    log.info("  Nota {} classificada como: {} ({} itens)", i+1, tipo, nota.getItens().size());
                }
            }
        }
        
        // Log detalhado dos itens por tipo
        for (Map.Entry<String, List<ItemSPC>> entry : itensPorTipo.entrySet()) {
            log.info("📋 Tipo: {} - {} itens", entry.getKey(), entry.getValue().size());
            for (ItemSPC item : entry.getValue()) {
                log.info("    - Código: {}, Descrição: {}, Quantidade: {}, Valor Unitário: {}", 
                    item.getCodigoProduto(), item.getDescricaoServico(), 
                    item.getQuantidadeServicos(), item.getValorUnitario());
            }
        }
        
        return itensPorTipo;
    }
    
    /**
     * 🔥 Calcula itens conforme fórmula: (consolidada - previa_anterior) + previa_corrente
     * Usa chave composta (código + descrição) para agrupamento correto
     */
    private List<FaturaItem> calcularItensConformeFormula(Map<String, List<ItemSPC>> itensPorTipo, 
                                                           ReguaFaturamento regua) {
        
        List<ItemSPC> itensPreviaAnterior = itensPorTipo.getOrDefault("PREVIA_ANTERIOR", new ArrayList<>());
        List<ItemSPC> itensConsolidacao = itensPorTipo.getOrDefault("CONSOLIDACAO", new ArrayList<>());
        List<ItemSPC> itensPreviaCorrente = itensPorTipo.getOrDefault("PREVIA_CORRENTE", new ArrayList<>());
        
        log.info("📊 Itens brutos - Prévia Anterior: {}, Consolidação: {}, Prévia Corrente: {}", 
            itensPreviaAnterior.size(), itensConsolidacao.size(), itensPreviaCorrente.size());
        
        // 🔥 Usar chave composta (código + descrição)
        Map<String, ItemSPC> consolidacaoMap = agruparItensPorChaveComposta(itensConsolidacao);
        Map<String, ItemSPC> previaAnteriorMap = agruparItensPorChaveComposta(itensPreviaAnterior);
        Map<String, ItemSPC> previaCorrenteMap = agruparItensPorChaveComposta(itensPreviaCorrente);
        
        log.info("📊 Produtos únicos - Consolidação: {}", consolidacaoMap.keySet());
        log.info("📊 Produtos únicos - Prévia Anterior: {}", previaAnteriorMap.keySet());
        log.info("📊 Produtos únicos - Prévia Corrente: {}", previaCorrenteMap.keySet());
        
        List<FaturaItem> resultado = new ArrayList<>();
        
        // Processar cada produto da consolidação
        for (Map.Entry<String, ItemSPC> entry : consolidacaoMap.entrySet()) {
            String chave = entry.getKey();
            ItemSPC itemConsolidacao = entry.getValue();
            
            ItemSPC itemPreviaAnterior = previaAnteriorMap.get(chave);
            ItemSPC itemPreviaCorrente = previaCorrenteMap.get(chave);
            
            BigDecimal qtdeConsolidada = getQuantidadeAsBigDecimal(itemConsolidacao);
            BigDecimal qtdePreviaAnterior = getQuantidadeAsBigDecimal(itemPreviaAnterior);
            BigDecimal qtdePreviaCorrente = getQuantidadeAsBigDecimal(itemPreviaCorrente);
            
            log.info("🔢 Produto: {} - Consolidada: {}, Prévia Anterior: {}, Prévia Corrente: {}", 
                chave, qtdeConsolidada, qtdePreviaAnterior, qtdePreviaCorrente);
            
            // 🔥 FÓRMULA: (consolidada - previa_anterior) + previa_corrente
            BigDecimal qtdeCalculada = (qtdeConsolidada.subtract(qtdePreviaAnterior)).add(qtdePreviaCorrente);
            
            log.info("  📐 Fórmula: ({}) - ({}) + ({}) = {}", 
                qtdeConsolidada, qtdePreviaAnterior, qtdePreviaCorrente, qtdeCalculada);
            
            if (qtdeCalculada.compareTo(BigDecimal.ZERO) <= 0) {
                log.info("  ⏭️ Quantidade calculada <= 0, ignorando produto");
                continue;
            }
            
            FaturaItem faturaItem = new FaturaItem();
            faturaItem.setDescricao(itemConsolidacao.getDescricaoServico());
            faturaItem.setCodigoProduto(itemConsolidacao.getCodigoProduto());
            faturaItem.setQuantidade(qtdeCalculada);
            faturaItem.setValorUnitario(itemConsolidacao.getValorUnitario());
            faturaItem.setValorTotal(qtdeCalculada.multiply(itemConsolidacao.getValorUnitario()));
            faturaItem.setTipoLancamento("D");
            
            resultado.add(faturaItem);
            log.info("  ✅ Item adicionado: {} - Quantidade: {}, Valor Unitário: {}, Total: {}", 
                itemConsolidacao.getDescricaoServico(), qtdeCalculada, 
                itemConsolidacao.getValorUnitario(), faturaItem.getValorTotal());
        }
        
        // Adicionar itens que só existem na prévia corrente (não estão na consolidação)
        for (Map.Entry<String, ItemSPC> entry : previaCorrenteMap.entrySet()) {
            String chave = entry.getKey();
            if (!consolidacaoMap.containsKey(chave)) {
                ItemSPC item = entry.getValue();
                BigDecimal qtde = getQuantidadeAsBigDecimal(item);
                
                if (qtde.compareTo(BigDecimal.ZERO) > 0) {
                    FaturaItem faturaItem = new FaturaItem();
                    faturaItem.setDescricao(item.getDescricaoServico());
                    faturaItem.setCodigoProduto(item.getCodigoProduto());
                    faturaItem.setQuantidade(qtde);
                    faturaItem.setValorUnitario(item.getValorUnitario());
                    faturaItem.setValorTotal(qtde.multiply(item.getValorUnitario()));
                    faturaItem.setTipoLancamento("D");
                    resultado.add(faturaItem);
                    log.info("  ✅ Item apenas na prévia corrente: {} - Quantidade: {}, Total: {}", 
                        item.getDescricaoServico(), qtde, faturaItem.getValorTotal());
                }
            }
        }
        
        log.info("📊 Total de itens calculados: {}", resultado.size());
        return resultado;
    }
    
    /**
     * 🔥 Agrupa itens por chave composta (código + descrição)
     */
    private Map<String, ItemSPC> agruparItensPorChaveComposta(List<ItemSPC> itens) {
        Map<String, ItemSPC> mapa = new LinkedHashMap<>();
        
        for (ItemSPC item : itens) {
            String codigo = item.getCodigoProduto();
            String descricao = item.getDescricaoServico();
            
            if (codigo == null || codigo.trim().isEmpty()) {
                codigo = "SEM_CODIGO";
            }
            if (descricao == null || descricao.trim().isEmpty()) {
                descricao = "SEM_DESCRICAO";
            }
            
            // 🔥 Chave composta: código + descrição
            String chave = codigo + "|" + descricao;
            
            if (mapa.containsKey(chave)) {
                ItemSPC existente = mapa.get(chave);
                BigDecimal qtdeExistente = getQuantidadeAsBigDecimal(existente);
                BigDecimal qtdeAtual = getQuantidadeAsBigDecimal(item);
                BigDecimal qtdeSomada = qtdeExistente.add(qtdeAtual);
                existente.setQuantidadeServicos(qtdeSomada.intValue());
                
                BigDecimal valorExistente = existente.getValorTotal() != null ? existente.getValorTotal() : BigDecimal.ZERO;
                BigDecimal valorAtual = item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO;
                existente.setValorTotal(valorExistente.add(valorAtual));
                
                log.debug("  Agrupando: {} - quantidade {} + {} = {}", 
                    descricao, qtdeExistente, qtdeAtual, qtdeSomada);
            } else {
                ItemSPC copia = new ItemSPC();
                copia.setCodigoProduto(codigo);
                copia.setDescricaoServico(descricao);
                copia.setQuantidadeServicos(item.getQuantidadeServicos());
                copia.setValorUnitario(item.getValorUnitario());
                copia.setValorTotal(item.getValorTotal());
                mapa.put(chave, copia);
                log.debug("  Novo produto: {} - {}", chave, descricao);
            }
        }
        
        return mapa;
    }
    
    /**
     * Converte quantidade para BigDecimal de forma segura
     */
    private BigDecimal getQuantidadeAsBigDecimal(ItemSPC item) {
        if (item == null) return BigDecimal.ZERO;
        if (item.getQuantidadeServicos() == null) return BigDecimal.ZERO;
        
        Object qtde = item.getQuantidadeServicos();
        if (qtde instanceof BigDecimal) {
            return (BigDecimal) qtde;
        } else if (qtde instanceof Integer) {
            return BigDecimal.valueOf((Integer) qtde);
        } else if (qtde instanceof Long) {
            return BigDecimal.valueOf((Long) qtde);
        } else if (qtde instanceof String) {
            try {
                return new BigDecimal((String) qtde);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    /**
     * Busca cancelamentos de serviços para o associado
     */
    private List<CancelamentoImportacao> buscarCancelamentos(Associado associado, Integer mes, Integer ano) {
        try {
            String codigoAssociado = associado.getCodigoSpc();
            if (codigoAssociado == null || codigoAssociado.isEmpty()) {
                log.warn("Associado {} não possui código SPC para buscar cancelamentos", associado.getId());
                return new ArrayList<>();
            }
            
            log.debug("Buscando cancelamentos para código associado: {}, período: {}/{}", 
                codigoAssociado, mes, ano);
            
            return cancelamentoRepository.findByCodigoAssociadoAndPeriodo(codigoAssociado, mes, ano);
        } catch (Exception e) {
            log.warn("Erro ao buscar cancelamentos: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Remove serviços que foram cancelados
     */
    private List<FaturaItem> removerServicosCancelados(List<FaturaItem> itens, List<CancelamentoImportacao> cancelamentos) {
        if (cancelamentos == null || cancelamentos.isEmpty()) {
            return itens;
        }
        
        Set<String> codigosCancelados = new HashSet<>();
        for (CancelamentoImportacao cancelamento : cancelamentos) {
            if (cancelamento.getCodigoServico() != null) {
                codigosCancelados.add(cancelamento.getCodigoServico());
                log.info("  Serviço cancelado: Código={}", cancelamento.getCodigoServico());
            }
            if (cancelamento.getDescricaoProduto() != null) {
                codigosCancelados.add(cancelamento.getDescricaoProduto());
                log.info("  Serviço cancelado (descrição): {}", cancelamento.getDescricaoProduto());
            }
        }
        
        if (codigosCancelados.isEmpty()) {
            return itens;
        }
        
        List<FaturaItem> itensFiltrados = new ArrayList<>();
        for (FaturaItem item : itens) {
            boolean cancelado = false;
            
            // Verificar por código
            if (item.getCodigoProduto() != null && codigosCancelados.contains(item.getCodigoProduto())) {
                cancelado = true;
            }
            // Verificar por descrição
            if (item.getDescricao() != null && codigosCancelados.contains(item.getDescricao())) {
                cancelado = true;
            }
            
            if (cancelado) {
                log.info("  🗑️ Removendo item cancelado: {} - Código: {}", 
                    item.getDescricao(), item.getCodigoProduto());
            } else {
                itensFiltrados.add(item);
            }
        }
        
        log.info("🗑️ Removidos {} itens cancelados, restam {}", 
            itens.size() - itensFiltrados.size(), itensFiltrados.size());
        
        return itensFiltrados;
    }
    
    /**
     * Gera número da fatura
     */
    private String gerarNumeroFatura(Long associadoId, Integer mes, Integer ano) {
        String anoStr = ano != null ? String.valueOf(ano) : String.valueOf(LocalDate.now().getYear());
        String mesStr = mes != null ? String.format("%02d", mes) : String.format("%02d", LocalDate.now().getMonthValue());
        String seq = String.format("%06d", System.currentTimeMillis() % 1000000);
        return "FAT-" + anoStr + mesStr + "-" + associadoId + "-" + seq;
    }
    
    /**
     * Calcula data de emissão baseada na régua
     */
    private LocalDate calcularDataEmissao(ReguaFaturamento regua) {
        LocalDate hoje = LocalDate.now();
        if (regua != null && regua.getDiaEmissao() != null) {
            int diaEmissao = regua.getDiaEmissao();
            try {
                LocalDate dataEmissao = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaEmissao);
                if (dataEmissao.isBefore(hoje)) {
                    dataEmissao = dataEmissao.plusMonths(1);
                }
                return dataEmissao;
            } catch (Exception e) {
                return hoje;
            }
        }
        return hoje;
    }
    
    /**
     * Calcula data de vencimento baseada na régua
     */
    private LocalDate calcularDataVencimento(ReguaFaturamento regua) {
        if (regua != null && regua.getDiaVencimento() != null) {
            LocalDate hoje = LocalDate.now();
            int diaVencimento = regua.getDiaVencimento();
            try {
                LocalDate dataVencimento = LocalDate.of(hoje.getYear(), hoje.getMonth(), diaVencimento);
                if (dataVencimento.isBefore(hoje)) {
                    dataVencimento = dataVencimento.plusMonths(1);
                }
                return dataVencimento;
            } catch (Exception e) {
                return hoje.plusDays(10);
            }
        }
        return LocalDate.now().plusDays(10);
    }
}