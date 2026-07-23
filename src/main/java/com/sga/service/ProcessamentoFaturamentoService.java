package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoProcessamentoDTO;
import com.sga.dto.FaturaItemDTO;
import com.sga.dto.ProcessamentoRequest;
import com.sga.dto.ResultadoProcessamento;
import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.LoteProcessamento;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ReguaFaturamento;
import com.sga.repository.FaturaRepository;
import com.sga.repository.LoteProcessamentoRepository;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class ProcessamentoFaturamentoService {

    private static final Logger log = LoggerFactory.getLogger(ProcessamentoFaturamentoService.class);

    @Autowired
    private ReguaFaturamentoService reguaService;

    @Autowired
    private AssociadoService associadoService;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;

    @Autowired
    private FaturaGeracaoService faturaGeracaoService;

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private LoteProcessamentoRepository loteProcessamentoRepository;

    // ============================================================
    // 🔥 CACHE SERVICE E BATCH SERVICE INJETADOS
    // ============================================================

    @Autowired
    private CacheService cacheService;

    @Autowired
    private FaturaBatchService faturaBatchService;

    @Autowired
    private PdfBatchService pdfBatchService;

    @Autowired
    private RmExportBatchService rmExportBatchService;

    // ============================================================
    // 🔥 EXECUTOR PARA PROCESSAMENTO PARALELO
    // ============================================================

    @Autowired
    @Qualifier("faturamentoExecutor")
    private Executor faturamentoExecutor;

    // ============================================================
    // MÉTODO PRINCIPAL - PROCESSAR FATURAMENTO
    // ============================================================

    @Transactional
    public ResultadoProcessamento processarFaturamento(ProcessamentoRequest request) {

        log.info("🚀 Processando faturamento: {}", request);

        ResultadoProcessamento resultado = new ResultadoProcessamento();
        resultado.setDataProcessamento(LocalDateTime.now());
        resultado.setErros(new ArrayList<>());
        resultado.setDetalhes(new ArrayList<>());
        resultado.setAssociadosJaFaturados(0L);

        if (request.getAssociadosIds() == null || request.getAssociadosIds().isEmpty()) {
            resultado.getErros().add("Nenhum associado selecionado para processamento");
            return resultado;
        }

        resultado.setTotalAssociados((long) request.getAssociadosIds().size());

        // ========== 1. BUSCAR A RÉGUA ==========
        ReguaFaturamento regua = null;
        if (request.getReguaId() != null) {
            var reguaOpt = reguaService.buscarPorId(request.getReguaId());
            if (reguaOpt.isPresent()) {
                regua = reguaOpt.get();
                log.info("✅ Régua encontrada: {} (ID: {})", regua.getNome(), regua.getId());
            } else {
                resultado.getErros().add("Régua não encontrada: " + request.getReguaId());
                return resultado;
            }
        } else {
            var reguaPadraoOpt = reguaService.buscarReguaPadrao();
            if (reguaPadraoOpt.isPresent()) {
                regua = reguaPadraoOpt.get();
                log.info("✅ Usando régua padrão: {} (ID: {})", regua.getNome(), regua.getId());
            } else {
                resultado.getErros().add("Nenhuma régua selecionada e nenhuma régua padrão encontrada");
                return resultado;
            }
        }

        // ========== 2. DEFINIR MÊS REFERÊNCIA ==========
        LocalDate mesReferencia;
        if (request.getMes() != null && request.getAno() != null) {
            mesReferencia = LocalDate.of(request.getAno(), request.getMes(), 1);
        } else if (request.getDataEmissao() != null) {
            mesReferencia = request.getDataEmissao().withDayOfMonth(1);
        } else {
            mesReferencia = LocalDate.now().withDayOfMonth(1);
        }
        log.info("📅 Mês referência: {}", mesReferencia);

        // ========== 3. VERIFICAR SE JÁ EXISTE LOTE ==========
        LoteProcessamento lote = null;
        Integer mes = mesReferencia.getMonthValue();
        Integer ano = mesReferencia.getYear();

        log.info("📅 Buscando lote por MÊS/ANO: {}/{} (ignorando dia)", mes, ano);

        List<LoteProcessamento> lotesExistentes = loteProcessamentoRepository.findByMesAnoAndReguaId(mes, ano,
                regua.getId());

        lotesExistentes = lotesExistentes.stream().filter(l -> !"ERRO".equals(l.getStatus()))
                .collect(Collectors.toList());

        if (!lotesExistentes.isEmpty()) {
            lote = lotesExistentes.get(lotesExistentes.size() - 1);
            log.info("♻️ Reutilizando lote existente ID: {}, Régua: {}, Mês: {}, Status: {}, Faturas: {}", lote.getId(),
                    lote.getRegua().getNome(), lote.getMesReferencia(), lote.getStatus(),
                    lote.getTotalFaturasGeradas());

            if ("CONCLUIDO".equals(lote.getStatus())) {
                log.info("🔄 Lote já concluído, reiniciando processamento...");
                lote.setStatus("EM_PROCESSAMENTO");
                lote.setDataInicioProcessamento(LocalDateTime.now());
                lote.setTotalFaturasGeradas(0);
                lote.setTotalErros(0);
                lote.setTotalCancelamentosAplicados(0);
                loteProcessamentoRepository.save(lote);
            }
        } else {
            lote = new LoteProcessamento();
            lote.setDataInicioProcessamento(LocalDateTime.now());
            lote.setUsuarioProcessamento(request.getUsuario() != null ? request.getUsuario() : "SISTEMA");
            lote.setStatus("EM_PROCESSAMENTO");
            lote.setCriadoEm(LocalDateTime.now());
            lote.setTotalNotasProcessadas(0);
            lote.setTotalFaturasGeradas(0);
            lote.setTotalCancelamentosAplicados(0);
            lote.setTotalErros(0);
            lote.setRegua(regua);
            lote.setMesReferencia(mesReferencia);
            loteProcessamentoRepository.save(lote);
            log.info("✅ NOVO lote criado com ID: {}, Régua: {} (ID: {}), Mês: {}", lote.getId(),
                    lote.getRegua().getNome(), lote.getRegua().getId(), lote.getMesReferencia());
        }

        // ========== 4. PRÉ-CARREGAR CACHES ==========
        log.info("📦 Pré-carregando caches para otimização...");
        long inicioCache = System.currentTimeMillis();

        cacheService.getProdutos();
        cacheService.getPlanos();

        long tempoCache = System.currentTimeMillis() - inicioCache;
        log.info("✅ Caches pré-carregados em {} ms", tempoCache);

        // ========== 5. PROCESSAR ASSOCIADOS ==========
        long processados = 0;
        long erros = 0;
        long jaFaturados = 0;
        List<Fatura> faturasGeradas = new ArrayList<>();
        BigDecimal totalDebito = BigDecimal.ZERO;

        boolean isSimulacao = request.getSimular() != null && request.getSimular();

        for (Long associadoId : request.getAssociadosIds()) {
            AssociadoProcessamentoDTO detalhe = new AssociadoProcessamentoDTO();
            detalhe.setAssociadoId(associadoId);

            try {
                Associado associado = associadoService.buscarPorIdEntity(associadoId);

                detalhe.setAssociadoNome(associado.getNomeRazao());
                detalhe.setCnpjCpf(associado.getCnpjCpf());
                detalhe.setCodigoSpc(associado.getCodigoSpc());
                detalhe.setCodigoRm(associado.getCodigoRm());

                String codigoSpc = associado.getCodigoSpc();
                if (codigoSpc == null || codigoSpc.trim().isEmpty()) {
                    log.warn("Associado {} sem código SPC", associadoId);
                    detalhe.setMensagemErro("Associado sem código SPC");
                    detalhe.setProcessado(false);
                    resultado.getDetalhes().add(detalhe);
                    resultado.getErros().add("Associado " + associadoId + " sem código SPC");
                    erros++;
                    continue;
                }

                List<NotaDebitoSPC> notas = notaDebitoRepository.findByCodigoSocioWithPadding(codigoSpc);

                if (notas == null || notas.isEmpty()) {
                    log.warn("Nenhuma nota encontrada para associado: {} - código SPC: {}", associado.getNomeRazao(),
                            codigoSpc);
                    detalhe.setMensagemErro("Nenhuma nota de débito encontrada para o código SPC: " + codigoSpc);
                    detalhe.setProcessado(false);
                    resultado.getDetalhes().add(detalhe);
                    erros++;
                    continue;
                }

                log.info("📄 Encontradas {} notas para o associado: {}", notas.size(), associado.getNomeRazao());

                Fatura fatura = faturaGeracaoService.gerarFatura(associado, notas, regua, request.getMes(),
                        request.getAno(), request.getDataEmissao(), request.getDataVencimento(), isSimulacao,
                        request.getUsuario() != null ? request.getUsuario() : "SISTEMA");

                if (fatura != null) {
                    if (!isSimulacao) {
                        fatura.setLoteProcessamentoId(lote.getId());
                        faturasGeradas.add(fatura);
                    } else {
                        faturasGeradas.add(fatura);
                    }

                    processados++;

                    detalhe.setValorNota(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
                    detalhe.setValorDebito(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
                    detalhe.setProcessado(true);
                    detalhe.setGerouNota(true);

                    if (fatura.getItens() != null && !fatura.getItens().isEmpty()) {
                        for (FaturaItem item : fatura.getItens()) {
                            FaturaItemDTO itemDTO = new FaturaItemDTO();
                            itemDTO.setCodigoProduto(item.getCodigoProduto());
                            itemDTO.setDescricao(item.getDescricao());
                            itemDTO.setQuantidade(item.getQuantidade());
                            itemDTO.setValorUnitario(item.getValorUnitario());
                            itemDTO.setValorTotal(item.getValorTotal());
                            itemDTO.setTipoLancamento(item.getTipoLancamento());
                            detalhe.addItemFatura(itemDTO);
                        }
                    }

                    totalDebito = totalDebito.add(detalhe.getValorDebito());
                    log.info("✅ Fatura gerada para {}: R$ {}", associado.getNomeRazao(), detalhe.getValorNota());
                } else {
                    detalhe.setMensagemErro("Associado já possui fatura para este período");
                    detalhe.setProcessado(false);
                    jaFaturados++;
                    resultado.getErros().add("Associado " + associadoId + " já possui fatura para o período");
                }

                resultado.getDetalhes().add(detalhe);

            } catch (Exception e) {
                log.error("Erro ao processar associado {}: {}", associadoId, e.getMessage());
                detalhe.setAssociadoNome("Associado ID: " + associadoId);
                detalhe.setMensagemErro(e.getMessage());
                detalhe.setProcessado(false);
                resultado.getDetalhes().add(detalhe);
                erros++;
                resultado.getErros().add("Erro no associado " + associadoId + ": " + e.getMessage());
            }
        }

        // ============================================================
        // 🔥 SALVAR FATURAS EM LOTE (BATCH INSERT)
        // ============================================================
        if (!isSimulacao && !faturasGeradas.isEmpty()) {
            log.info("📝 Salvando {} faturas em lote...", faturasGeradas.size());
            long inicioBatch = System.currentTimeMillis();

            faturaBatchService.salvarFaturasComItens(faturasGeradas);

            long tempoBatch = System.currentTimeMillis() - inicioBatch;
            log.info("✅ {} faturas salvas em lote em {} ms", faturasGeradas.size(), tempoBatch);
        }

        // ========== 6. ATUALIZAR LOTE ==========
        lote.setTotalFaturasGeradas((int) faturasGeradas.size());
        lote.setTotalErros((int) erros);
        lote.setDataFimProcessamento(LocalDateTime.now());
        lote.setStatus("CONCLUIDO");
        loteProcessamentoRepository.save(lote);

        log.info("📊 Lote {} finalizado: {} faturas, {} erros", lote.getId(), faturasGeradas.size(), erros);

        // ============================================================
        // 🔥 GERAR PDFS EM PARALELO (APÓS SALVAR FATURAS)
        // ============================================================
        if (!isSimulacao && !faturasGeradas.isEmpty()) {
            log.info("📄 Iniciando geração paralela de PDFs para {} faturas...", faturasGeradas.size());
            long inicioPdf = System.currentTimeMillis();

            try {
                // Buscar notas para gerar PDFs
                List<NotaDebitoSPC> notas = notaDebitoRepository
                        .findByFaturaIds(faturasGeradas.stream().map(Fatura::getId).collect(Collectors.toList()));

                if (!notas.isEmpty()) {
                    CompletableFuture<List<PdfBatchService.PdfResultado>> pdfFuture = pdfBatchService
                            .gerarPdfsEmLote(faturasGeradas, notas);

                    List<PdfBatchService.PdfResultado> pdfResultados = pdfFuture.join();

                    long tempoPdf = System.currentTimeMillis() - inicioPdf;
                    log.info("✅ {} PDFs gerados em {} ms", pdfResultados.size(), tempoPdf);
                } else {
                    log.warn("⚠️ Nenhuma nota encontrada para gerar PDFs");
                }
            } catch (Exception e) {
                log.error("❌ Erro ao gerar PDFs: {}", e.getMessage(), e);
                resultado.getErros().add("Erro ao gerar PDFs: " + e.getMessage());
            }
        }

        // ============================================================
        // 🔥 EXPORTAR PARA RM EM PARALELO (APÓS PDFs)
        // ============================================================
        if (!isSimulacao && !faturasGeradas.isEmpty() 
                && request.getIntegrarRm() != null && request.getIntegrarRm()) {
            log.info("📤 Iniciando exportação paralela para RM de {} faturas...", faturasGeradas.size());
            long inicioRm = System.currentTimeMillis();

            try {
                // Buscar notas para exportação
                List<NotaDebitoSPC> notas = notaDebitoRepository
                        .findByFaturaIds(faturasGeradas.stream().map(Fatura::getId).collect(Collectors.toList()));

                if (!notas.isEmpty()) {
                    CompletableFuture<RmExportBatchService.RmExportResultado> rmFuture = rmExportBatchService
                            .exportarParaRmEmLote(notas, request.getUsuario());

                    RmExportBatchService.RmExportResultado rmResultado = rmFuture.join();

                    long tempoRm = System.currentTimeMillis() - inicioRm;
                    log.info("✅ Exportação RM concluída: {} sucessos, {} erros em {} ms",
                            rmResultado.getSucessos(), rmResultado.getErros(), tempoRm);
                } else {
                    log.warn("⚠️ Nenhuma nota encontrada para exportação RM");
                }
            } catch (Exception e) {
                log.error("❌ Erro na exportação RM: {}", e.getMessage(), e);
                resultado.getErros().add("Erro na exportação RM: " + e.getMessage());
            }
        }

        // ========== 7. PREENCHER RESULTADO ==========
        resultado.setAssociadosProcessados(processados);
        resultado.setAssociadosComErro(erros);
        resultado.setAssociadosJaFaturados(jaFaturados);
        resultado.setTotalNotasGeradas((long) faturasGeradas.size());
        resultado.setValorTotalDebito(totalDebito);

        log.info(
                "✅ Processamento concluído: {} processados, {} erros, {} já faturados, {} faturas geradas, valor total: R$ {}",
                processados, erros, jaFaturados, faturasGeradas.size(), totalDebito);

        log.info("📊 Detalhes preenchidos: {} registros", resultado.getDetalhes().size());

        return resultado;
    }

    // ============================================================
    // MÉTODO COM NOTIFICAÇÕES (COM CACHE E BATCH)
    // ============================================================

    @Transactional
    public ResultadoProcessamento processarFaturamentoComNotificacoes(ProcessamentoRequest request, String usuario) {

        log.info("🚀 Processando faturamento COM NOTIFICAÇÕES: {}", request);

        ResultadoProcessamento resultado = new ResultadoProcessamento();
        resultado.setDataProcessamento(LocalDateTime.now());
        resultado.setErros(new ArrayList<>());
        resultado.setDetalhes(new ArrayList<>());
        resultado.setAssociadosJaFaturados(0L);

        if (request.getAssociadosIds() == null || request.getAssociadosIds().isEmpty()) {
            resultado.getErros().add("Nenhum associado selecionado para processamento");
            return resultado;
        }

        resultado.setTotalAssociados((long) request.getAssociadosIds().size());

        // ========== 1. BUSCAR A RÉGUA ==========
        ReguaFaturamento regua = null;
        if (request.getReguaId() != null) {
            var reguaOpt = reguaService.buscarPorId(request.getReguaId());
            if (reguaOpt.isPresent()) {
                regua = reguaOpt.get();
                log.info("✅ Régua encontrada: {} (ID: {})", regua.getNome(), regua.getId());
            } else {
                resultado.getErros().add("Régua não encontrada: " + request.getReguaId());
                return resultado;
            }
        } else {
            var reguaPadraoOpt = reguaService.buscarReguaPadrao();
            if (reguaPadraoOpt.isPresent()) {
                regua = reguaPadraoOpt.get();
                log.info("✅ Usando régua padrão: {} (ID: {})", regua.getNome(), regua.getId());
            } else {
                resultado.getErros().add("Nenhuma régua selecionada e nenhuma régua padrão encontrada");
                return resultado;
            }
        }

        // ========== 2. DEFINIR MÊS REFERÊNCIA ==========
        LocalDate mesReferencia;
        if (request.getMes() != null && request.getAno() != null) {
            mesReferencia = LocalDate.of(request.getAno(), request.getMes(), 1);
        } else if (request.getDataEmissao() != null) {
            mesReferencia = request.getDataEmissao().withDayOfMonth(1);
        } else {
            mesReferencia = LocalDate.now().withDayOfMonth(1);
        }
        log.info("📅 Mês referência: {}", mesReferencia);

        Integer mes = mesReferencia.getMonthValue();
        Integer ano = mesReferencia.getYear();

        // ========== 3. VERIFICAR SE JÁ EXISTE LOTE ==========
        LoteProcessamento lote = null;
        List<LoteProcessamento> lotesExistentes = loteProcessamentoRepository.findByMesAnoAndReguaId(mes, ano,
                regua.getId());

        lotesExistentes = lotesExistentes.stream().filter(l -> !"ERRO".equals(l.getStatus()))
                .collect(Collectors.toList());

        if (!lotesExistentes.isEmpty()) {
            lote = lotesExistentes.get(lotesExistentes.size() - 1);
            log.info("♻️ Reutilizando lote existente ID: {}", lote.getId());

            if ("CONCLUIDO".equals(lote.getStatus())) {
                log.info("🔄 Lote já concluído, reiniciando processamento...");
                lote.setStatus("EM_PROCESSAMENTO");
                lote.setDataInicioProcessamento(LocalDateTime.now());
                lote.setTotalFaturasGeradas(0);
                lote.setTotalErros(0);
                lote.setTotalCancelamentosAplicados(0);
                loteProcessamentoRepository.save(lote);
            }
        } else {
            lote = new LoteProcessamento();
            lote.setDataInicioProcessamento(LocalDateTime.now());
            lote.setUsuarioProcessamento(request.getUsuario() != null ? request.getUsuario() : "SISTEMA");
            lote.setStatus("EM_PROCESSAMENTO");
            lote.setCriadoEm(LocalDateTime.now());
            lote.setTotalNotasProcessadas(0);
            lote.setTotalFaturasGeradas(0);
            lote.setTotalCancelamentosAplicados(0);
            lote.setTotalErros(0);
            lote.setRegua(regua);
            lote.setMesReferencia(mesReferencia);
            loteProcessamentoRepository.save(lote);
            log.info("✅ NOVO lote criado ID: {}", lote.getId());
        }

        // ========== 4. PRÉ-CARREGAR CACHES ==========
        log.info("📦 Pré-carregando caches para otimização (com notificações)...");
        long inicioCache = System.currentTimeMillis();

        cacheService.getProdutos();
        cacheService.getPlanos();

        long tempoCache = System.currentTimeMillis() - inicioCache;
        log.info("✅ Caches pré-carregados em {} ms", tempoCache);

        // ========== 5. PROCESSAR ASSOCIADOS ==========
        long processados = 0;
        long erros = 0;
        long jaFaturados = 0;
        List<Fatura> faturasGeradas = new ArrayList<>();
        BigDecimal totalDebito = BigDecimal.ZERO;
        boolean isSimulacao = request.getSimular() != null && request.getSimular();

        for (Long associadoId : request.getAssociadosIds()) {
            AssociadoProcessamentoDTO detalhe = new AssociadoProcessamentoDTO();
            detalhe.setAssociadoId(associadoId);

            try {
                Associado associado = associadoService.buscarPorIdEntity(associadoId);
                detalhe.setAssociadoNome(associado.getNomeRazao());
                detalhe.setCnpjCpf(associado.getCnpjCpf());
                detalhe.setCodigoSpc(associado.getCodigoSpc());
                detalhe.setCodigoRm(associado.getCodigoRm());

                String codigoSpc = associado.getCodigoSpc();
                if (codigoSpc == null || codigoSpc.trim().isEmpty()) {
                    log.warn("Associado {} sem código SPC", associadoId);
                    detalhe.setMensagemErro("Associado sem código SPC");
                    detalhe.setProcessado(false);
                    resultado.getDetalhes().add(detalhe);
                    resultado.getErros().add("Associado " + associadoId + " sem código SPC");
                    erros++;
                    continue;
                }

                List<NotaDebitoSPC> notas = notaDebitoRepository.findByCodigoSocioWithPadding(codigoSpc);

                if (notas == null || notas.isEmpty()) {
                    log.warn("Nenhuma nota encontrada para associado: {}", associado.getNomeRazao());
                    detalhe.setMensagemErro("Nenhuma nota de débito encontrada para o código SPC: " + codigoSpc);
                    detalhe.setProcessado(false);
                    resultado.getDetalhes().add(detalhe);
                    erros++;
                    continue;
                }

                log.info("📄 Encontradas {} notas para o associado: {}", notas.size(), associado.getNomeRazao());

                Fatura fatura = faturaGeracaoService.gerarFatura(associado, notas, regua, mes, ano,
                        request.getDataEmissao(), request.getDataVencimento(), isSimulacao,
                        request.getUsuario() != null ? request.getUsuario() : "SISTEMA");

                if (fatura != null) {
                    if (!isSimulacao) {
                        fatura.setLoteProcessamentoId(lote.getId());
                        faturasGeradas.add(fatura);
                    } else {
                        faturasGeradas.add(fatura);
                    }

                    processados++;

                    detalhe.setValorNota(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
                    detalhe.setValorDebito(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
                    detalhe.setProcessado(true);
                    detalhe.setGerouNota(true);

                    if (fatura.getItens() != null && !fatura.getItens().isEmpty()) {
                        for (FaturaItem item : fatura.getItens()) {
                            FaturaItemDTO itemDTO = new FaturaItemDTO();
                            itemDTO.setCodigoProduto(item.getCodigoProduto());
                            itemDTO.setDescricao(item.getDescricao());
                            itemDTO.setQuantidade(item.getQuantidade());
                            itemDTO.setValorUnitario(item.getValorUnitario());
                            itemDTO.setValorTotal(item.getValorTotal());
                            itemDTO.setTipoLancamento(item.getTipoLancamento());
                            detalhe.addItemFatura(itemDTO);
                        }
                    }

                    totalDebito = totalDebito.add(detalhe.getValorDebito());
                    log.info("✅ Fatura gerada para {}: R$ {}", associado.getNomeRazao(), detalhe.getValorNota());
                } else {
                    detalhe.setMensagemErro("Associado já possui fatura para este período");
                    detalhe.setProcessado(false);
                    jaFaturados++;
                    resultado.getErros().add("Associado " + associadoId + " já possui fatura para o período");
                }

                resultado.getDetalhes().add(detalhe);

            } catch (Exception e) {
                log.error("Erro ao processar associado {}: {}", associadoId, e.getMessage());
                detalhe.setAssociadoNome("Associado ID: " + associadoId);
                detalhe.setMensagemErro(e.getMessage());
                detalhe.setProcessado(false);
                resultado.getDetalhes().add(detalhe);
                erros++;
                resultado.getErros().add("Erro no associado " + associadoId + ": " + e.getMessage());
            }
        }

        // ============================================================
        // 🔥 SALVAR FATURAS EM LOTE (BATCH INSERT)
        // ============================================================
        if (!isSimulacao && !faturasGeradas.isEmpty()) {
            log.info("📝 Salvando {} faturas em lote (com notificações)...", faturasGeradas.size());
            long inicioBatch = System.currentTimeMillis();

            faturaBatchService.salvarFaturasComItens(faturasGeradas);

            long tempoBatch = System.currentTimeMillis() - inicioBatch;
            log.info("✅ {} faturas salvas em lote em {} ms", faturasGeradas.size(), tempoBatch);
        }

        // ========== 6. ATUALIZAR LOTE ==========
        lote.setTotalFaturasGeradas((int) faturasGeradas.size());
        lote.setTotalErros((int) erros);
        lote.setDataFimProcessamento(LocalDateTime.now());
        lote.setStatus("CONCLUIDO");
        loteProcessamentoRepository.save(lote);

        log.info("📊 Lote {} finalizado: {} faturas, {} erros", lote.getId(), faturasGeradas.size(), erros);

        // ============================================================
        // 🔥 GERAR PDFS EM PARALELO (APÓS SALVAR FATURAS)
        // ============================================================
        if (!isSimulacao && !faturasGeradas.isEmpty()) {
            log.info("📄 Iniciando geração paralela de PDFs para {} faturas...", faturasGeradas.size());
            long inicioPdf = System.currentTimeMillis();

            try {
                // 🔥 CORRIGIDO: Extrair os notaDebitoId das faturas
                List<Long> notaIds = faturasGeradas.stream()
                    .map(Fatura::getNotaDebitoId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

                if (!notaIds.isEmpty()) {
                    List<NotaDebitoSPC> notas = notaDebitoRepository.findByIdIn(notaIds);

                    if (!notas.isEmpty()) {
                        CompletableFuture<List<PdfBatchService.PdfResultado>> pdfFuture = pdfBatchService
                                .gerarPdfsEmLote(faturasGeradas, notas);

                        List<PdfBatchService.PdfResultado> pdfResultados = pdfFuture.join();

                        long tempoPdf = System.currentTimeMillis() - inicioPdf;
                        log.info("✅ {} PDFs gerados em {} ms", pdfResultados.size(), tempoPdf);
                    } else {
                        log.warn("⚠️ Nenhuma nota encontrada para gerar PDFs");
                    }
                } else {
                    log.warn("⚠️ Nenhuma fatura com notaDebitoId associado");
                }
            } catch (Exception e) {
                log.error("❌ Erro ao gerar PDFs: {}", e.getMessage(), e);
                resultado.getErros().add("Erro ao gerar PDFs: " + e.getMessage());
            }
        }

        // ============================================================
        // 🔥 EXPORTAR PARA RM EM PARALELO (APÓS PDFs)
        // ============================================================
        if (!isSimulacao && !faturasGeradas.isEmpty() 
                && request.getIntegrarRm() != null && request.getIntegrarRm()) {
            log.info("📤 Iniciando exportação paralela para RM de {} faturas...", faturasGeradas.size());
            long inicioRm = System.currentTimeMillis();

            try {
                // 🔥 CORRIGIDO: Extrair os notaDebitoId das faturas
                List<Long> notaIds = faturasGeradas.stream()
                    .map(Fatura::getNotaDebitoId)
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

                if (!notaIds.isEmpty()) {
                    List<NotaDebitoSPC> notas = notaDebitoRepository.findByIdIn(notaIds);

                    if (!notas.isEmpty()) {
                        CompletableFuture<RmExportBatchService.RmExportResultado> rmFuture = rmExportBatchService
                                .exportarParaRmEmLote(notas, request.getUsuario());

                        RmExportBatchService.RmExportResultado rmResultado = rmFuture.join();

                        long tempoRm = System.currentTimeMillis() - inicioRm;
                        log.info("✅ Exportação RM concluída: {} sucessos, {} erros em {} ms",
                                rmResultado.getSucessos(), rmResultado.getErros(), tempoRm);
                    } else {
                        log.warn("⚠️ Nenhuma nota encontrada para exportação RM");
                    }
                } else {
                    log.warn("⚠️ Nenhuma fatura com notaDebitoId associado");
                }
            } catch (Exception e) {
                log.error("❌ Erro na exportação RM: {}", e.getMessage(), e);
                resultado.getErros().add("Erro na exportação RM: " + e.getMessage());
            }
        }

        // ========== 7. PREENCHER RESULTADO ==========
        resultado.setAssociadosProcessados(processados);
        resultado.setAssociadosComErro(erros);
        resultado.setAssociadosJaFaturados(jaFaturados);
        resultado.setTotalNotasGeradas((long) faturasGeradas.size());
        resultado.setValorTotalDebito(totalDebito);

        log.info(
                "✅ Processamento COM NOTIFICAÇÕES concluído: {} processados, {} erros, {} já faturados, {} faturas geradas, valor total: R$ {}",
                processados, erros, jaFaturados, faturasGeradas.size(), totalDebito);

        return resultado;
    }

    // ============================================================
    // 🔥 PROCESSAMENTO PARALELO
    // ============================================================

    @Async("faturamentoExecutor")
    public CompletableFuture<ResultadoProcessamento> processarEmParalelo(ProcessamentoRequest request) {

        log.info("🚀 Iniciando processamento paralelo para {} associados", request.getAssociadosIds().size());

        long inicioTotal = System.currentTimeMillis();

        List<List<Long>> lotes = dividirEmLotes(request.getAssociadosIds(), 300);
        log.info("📦 Dividido em {} lotes de até 300 associados", lotes.size());

        List<CompletableFuture<ResultadoProcessamento>> futures = lotes.stream()
                .map(lote -> CompletableFuture.supplyAsync(() -> {
                    ProcessamentoRequest subRequest = cloneRequest(request, lote);
                    return processarFaturamento(subRequest);
                }, faturamentoExecutor))
                .collect(Collectors.toList());

        List<ResultadoProcessamento> resultados = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        ResultadoProcessamento resultadoFinal = consolidarResultados(resultados);

        long tempoTotal = System.currentTimeMillis() - inicioTotal;
        log.info("✅ Processamento paralelo concluído em {} ms ({} min)", tempoTotal, tempoTotal / 60000);

        return CompletableFuture.completedFuture(resultadoFinal);
    }

    private List<List<Long>> dividirEmLotes(List<Long> lista, int tamanhoLote) {
        List<List<Long>> lotes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamanhoLote) {
            int end = Math.min(i + tamanhoLote, lista.size());
            lotes.add(lista.subList(i, end));
        }
        return lotes;
    }

    private ProcessamentoRequest cloneRequest(ProcessamentoRequest original, List<Long> ids) {
        ProcessamentoRequest clone = new ProcessamentoRequest();
        clone.setAssociadosIds(ids);
        clone.setReguaId(original.getReguaId());
        clone.setMes(original.getMes());
        clone.setAno(original.getAno());
        clone.setDataEmissao(original.getDataEmissao());
        clone.setDataVencimento(original.getDataVencimento());
        clone.setSimular(original.getSimular());
        clone.setUsuario(original.getUsuario());
        return clone;
    }

    private ResultadoProcessamento consolidarResultados(List<ResultadoProcessamento> resultados) {
        ResultadoProcessamento consolidado = new ResultadoProcessamento();
        consolidado.setDataProcessamento(LocalDateTime.now());
        consolidado.setErros(new ArrayList<>());
        consolidado.setDetalhes(new ArrayList<>());

        long totalProcessados = 0;
        long totalErros = 0;
        long totalJaFaturados = 0;
        long totalNotas = 0;
        BigDecimal valorTotal = BigDecimal.ZERO;

        for (ResultadoProcessamento r : resultados) {
            totalProcessados += r.getAssociadosProcessados() != null ? r.getAssociadosProcessados() : 0;
            totalErros += r.getAssociadosComErro() != null ? r.getAssociadosComErro() : 0;
            totalJaFaturados += r.getAssociadosJaFaturados() != null ? r.getAssociadosJaFaturados() : 0;
            totalNotas += r.getTotalNotasGeradas() != null ? r.getTotalNotasGeradas() : 0;

            if (r.getValorTotalDebito() != null) {
                valorTotal = valorTotal.add(r.getValorTotalDebito());
            }

            if (r.getDetalhes() != null) {
                consolidado.getDetalhes().addAll(r.getDetalhes());
            }

            if (r.getErros() != null) {
                consolidado.getErros().addAll(r.getErros());
            }
        }

        consolidado.setAssociadosProcessados(totalProcessados);
        consolidado.setAssociadosComErro(totalErros);
        consolidado.setAssociadosJaFaturados(totalJaFaturados);
        consolidado.setTotalNotasGeradas(totalNotas);
        consolidado.setValorTotalDebito(valorTotal);
        consolidado.setTotalAssociados(totalProcessados + totalErros + totalJaFaturados);

        return consolidado;
    }
}