// src/main/java/com/sga/service/NotificacaoIntegracaoService.java

package com.sga.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;  // 🔥 ADICIONAR IMPORT
import java.util.List;
import java.util.Map;      // 🔥 ADICIONAR IMPORT
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.NotificacaoAssociadoDTO;
import com.sga.dto.NotificacaoSumarizadaDTO;
import com.sga.model.Associado;
import com.sga.model.NotificacaoAssociado;
import com.sga.model.SincronizacaoNotificacao;  // 🔥 ADICIONAR IMPORT
import com.sga.model.notificacao.NotificacaoSumarizada;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.NotificacaoAssociadoRepository;
import com.sga.repository.notificacao.NotificacaoSumarizadaRepository;

@Service
public class NotificacaoIntegracaoService {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoIntegracaoService.class);

    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = 
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private NotificacaoSumarizadaRepository notificacaoSumarizadaRepository;

    @Autowired
    private NotificacaoAssociadoRepository notificacaoAssociadoRepository;

    @Autowired
    private AssociadoRepository associadoRepository;

    @Autowired
    private SincronizacaoNotificacaoService sincronizacaoService;  // 🔥 ADICIONAR DEPENDÊNCIA

    @Value("${notificacao.scheduler.dias-retrospectiva:30}")
    private Integer diasRetrospectiva;

    // ========== MÉTODO AUXILIAR PARA CONVERTER CÓDIGO SPC EM ID SGA ==========

    private Long converterCodigoSpcParaIdSga(String codigoSpc) {
        if (codigoSpc == null || codigoSpc.isEmpty()) {
            return null;
        }
        
        String codigoLimpo = codigoSpc.replaceAll("^0+", "");
        
        try {
            Associado associado = associadoRepository.findByCodigoSpc(codigoLimpo).orElse(null);
            
            if (associado != null) {
                log.debug("✅ Código SPC {} convertido para ID SGA: {}", codigoSpc, associado.getId());
                return associado.getId();
            } else {
                log.warn("⚠️ Associado não encontrado para o código SPC: {} (limpo: {})", codigoSpc, codigoLimpo);
                return null;
            }
        } catch (Exception e) {
            log.error("❌ Erro ao converter código SPC {} para ID SGA: {}", codigoSpc, e.getMessage());
            return null;
        }
    }

    // ========== SINCRONIZAÇÕES ==========

    /**
     * 🔥 SINCRONIZA POR PERÍODO - DATAS OBRIGATÓRIAS
     * Formato esperado: dd/MM/yyyy
     * 🔥 CORRIGIDO: Retorna Map com processados e totalRegistros
     */
    @Transactional
    public Map<String, Object> sincronizarNotificacoesPorPeriodo(LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        // 🔥 VALIDAR DATAS OBRIGATÓRIAS
        if (dataInicio == null || dataFim == null) {
            log.error("❌ Datas são obrigatórias para sincronização");
            resultado.put("processados", 0);
            resultado.put("totalRegistros", 0);
            resultado.put("mensagem", "Datas são obrigatórias");
            return resultado;
        }
        
        if (dataInicio.isAfter(dataFim)) {
            log.error("❌ Data de início ({}) não pode ser maior que data de fim ({})", 
                    dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER));
            resultado.put("processados", 0);
            resultado.put("totalRegistros", 0);
            resultado.put("mensagem", "Data de início não pode ser maior que data de fim");
            return resultado;
        }
        
        log.info("🔄 SINCRONIZANDO do MS-SQL - Início: {}, Fim: {}, Código: {}", 
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), codigoAssociado);

        // 🔥 BUSCAR DO MS-SQL (APENAS PARA SINCRONIZAÇÃO)
        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesMSSQL(dataInicio, dataFim, codigoAssociado);

        if (notificacoes.isEmpty()) {
            log.warn("⚠️ Nenhuma notificação encontrada no MS-SQL para o período");
            resultado.put("processados", 0);
            resultado.put("totalRegistros", 0);
            resultado.put("mensagem", "Nenhuma notificação encontrada");
            return resultado;
        }

        int mes = dataFim.getMonthValue();
        int ano = dataFim.getYear();

        log.info("📅 Período: {} à {} - Competência: {}/{}", 
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), mes, ano);
        log.info("📊 Total de registros encontrados no MS-SQL: {}", notificacoes.size());

        int processados = 0;
        int salvos = 0;
        int erros = 0;
        int totalRegistros = notificacoes.size();

        for (NotificacaoSumarizada n : notificacoes) {
            try {
                String codigoSpc = String.valueOf(n.getCodigoAssociado());
                
                // 🔥 CONVERTER CÓDIGO SPC PARA ID SGA
                Long idSga = converterCodigoSpcParaIdSga(codigoSpc);
                
                if (idSga == null) {
                    log.warn("⚠️ Associado não encontrado para código SPC: {}, ignorando", codigoSpc);
                    erros++;
                    continue;
                }

                // 🔥 BUSCAR OU CRIAR NOTIFICAÇÃO DO ASSOCIADO
                NotificacaoAssociado notificacao = notificacaoAssociadoRepository
                        .findByAssociadoIdAndMesReferenciaAndAnoReferencia(idSga, mes, ano)
                        .orElse(new NotificacaoAssociado());

                // 🔥 PREENCHER DADOS
                notificacao.setAssociadoId(idSga);
                notificacao.setCodigoSpc(codigoSpc);
                notificacao.setMesReferencia(mes);
                notificacao.setAnoReferencia(ano);
                notificacao.setPeriodoInicio(dataInicio);
                notificacao.setPeriodoFim(dataFim);
                notificacao.setTotalRegistros(n.getTotalRegistrosDigital() != null ? n.getTotalRegistrosDigital() : 0);

                // 🔥 ATUALIZAR SMS
                int smsSemEnr = notificacao.getSmsSemEnriquecimento() != null ? notificacao.getSmsSemEnriquecimento() : 0;
                int smsComEnr = notificacao.getSmsComEnriquecimento() != null ? notificacao.getSmsComEnriquecimento() : 0;
                int novoSmsSemEnr = n.getSmsSemEnriquecimento() != null ? n.getSmsSemEnriquecimento() : 0;
                int novoSmsComEnr = n.getSmsComEnriquecimento() != null ? n.getSmsComEnriquecimento() : 0;
                
                notificacao.setSmsSemEnriquecimento(smsSemEnr + novoSmsSemEnr);
                notificacao.setSmsComEnriquecimento(smsComEnr + novoSmsComEnr);

                // 🔥 ATUALIZAR E-MAIL
                int emailSemEnr = notificacao.getEmailsSemEnriquecimento() != null ? notificacao.getEmailsSemEnriquecimento() : 0;
                int emailComEnr = notificacao.getEmailsComEnriquecimento() != null ? notificacao.getEmailsComEnriquecimento() : 0;
                int novoEmailSemEnr = n.getEmailsSemEnriquecimento() != null ? n.getEmailsSemEnriquecimento() : 0;
                int novoEmailComEnr = n.getEmailsComEnriquecimento() != null ? n.getEmailsComEnriquecimento() : 0;
                
                notificacao.setEmailsSemEnriquecimento(emailSemEnr + novoEmailSemEnr);
                notificacao.setEmailsComEnriquecimento(emailComEnr + novoEmailComEnr);

                // 🔥 ATUALIZAR CARTAS
                int cartasTotal = notificacao.getCartasTotal() != null ? notificacao.getCartasTotal() : 0;
                int novasCartas = n.getCartasEnviadas() != null ? n.getCartasEnviadas() : 0;
                notificacao.setCartasTotal(cartasTotal + novasCartas);

                // 🔥 ATUALIZAR NÃO ENVIADAS
                int naoEnviadas = notificacao.getNaoEnviadas() != null ? notificacao.getNaoEnviadas() : 0;
                int novasNaoEnviadas = n.getNaoEnviada() != null ? n.getNaoEnviada() : 0;
                notificacao.setNaoEnviadas(naoEnviadas + novasNaoEnviadas);

                // 🔥 CALCULAR TOTAIS E VALORES
                notificacao.calcularTotais();
                notificacao.calcularValoresTotais();

                // 🔥 SALVAR NO BANCO LOCAL
                notificacaoAssociadoRepository.save(notificacao);
                salvos++;
                processados++;

                log.debug("✅ Associado {} (SPC: {}) processado: SMS={}, E-mail={}, Cartas={}", 
                        idSga, codigoSpc, 
                        notificacao.getSmsTotal(), 
                        notificacao.getEmailsTotal(), 
                        notificacao.getCartasTotal());

            } catch (Exception e) {
                log.error("❌ Erro ao processar associado {}: {}", n.getCodigoAssociado(), e.getMessage(), e);
                erros++;
            }
        }

        // 🔥 REGISTRAR NA TABELA DE SINCRONIZAÇÃO
        try {
            SincronizacaoNotificacao sincronizacao = sincronizacaoService.salvarSincronizacao(
                    dataInicio,
                    dataFim,
                    codigoAssociado,
                    processados,
                    totalRegistros,
                    "SISTEMA"
            );
            log.info("✅ Registro de sincronização salvo - ID: {}", sincronizacao.getId());
        } catch (Exception e) {
            log.error("❌ Erro ao salvar registro de sincronização: {}", e.getMessage());
        }

        log.info("✅ Sincronização por período concluída!");
        log.info("   📊 Total registros MS-SQL: {}", totalRegistros);
        log.info("   📊 Total processados: {}", processados);
        log.info("   💾 Total salvos: {}", salvos);
        log.info("   ❌ Total erros: {}", erros);
        
        // 🔥 RETORNAR MAP COM TODOS OS DADOS
        resultado.put("processados", processados);
        resultado.put("totalRegistros", totalRegistros);
        resultado.put("salvos", salvos);
        resultado.put("erros", erros);
        resultado.put("mensagem", "Sincronização concluída com sucesso");
        
        return resultado;
    }

    // ========== MÉTODOS LEGADO (MANTIDOS) ==========

    @Transactional
    public int sincronizarNotificacoes(Integer mes, Integer ano, String codigoAssociado) {
        log.info("🔄 Sincronizando notificações - {}/{} - Código: {}", mes, ano, codigoAssociado);

        LocalDate dataInicio = LocalDate.of(ano, mes, 1);
        LocalDate dataFim = dataInicio.withDayOfMonth(dataInicio.lengthOfMonth());

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesSumarizadas(dataInicio, dataFim, codigoAssociado);

        log.info("📊 Encontradas {} notificações", notificacoes.size());

        if (notificacoes.isEmpty()) {
            log.warn("⚠️ Nenhuma notificação encontrada para {}/{}", mes, ano);
            return 0;
        }

        Map<Integer, List<NotificacaoSumarizada>> notificacoesPorAssociado = notificacoes.stream()
                .filter(n -> n.getCodigoAssociado() != null)
                .collect(Collectors.groupingBy(NotificacaoSumarizada::getCodigoAssociado));

        int processados = 0;
        for (Map.Entry<Integer, List<NotificacaoSumarizada>> entry : notificacoesPorAssociado.entrySet()) {
            Integer codigoSpc = entry.getKey();
            List<NotificacaoSumarizada> lista = entry.getValue();

            try {
                Long idSga = converterCodigoSpcParaIdSga(String.valueOf(codigoSpc));
                
                if (idSga == null) {
                    log.warn("⚠️ Associado não encontrado para código SPC: {}, ignorando", codigoSpc);
                    continue;
                }
                
                processarNotificacoesAssociado(idSga, String.valueOf(codigoSpc), mes, ano, dataInicio, dataFim, lista);
                processados++;
            } catch (Exception e) {
                log.error("❌ Erro ao processar associado {}: {}", codigoSpc, e.getMessage(), e);
            }
        }

        log.info("✅ Sincronização concluída! {} associados processados", processados);
        return processados;
    }

    @Transactional
    public int sincronizarNotificacoesAgrupadas(Integer mes, Integer ano, String codigoAssociado) {
        log.info("🔄 Sincronizando notificações agrupadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesAgrupadasPorMesAno(mes, ano, codigoAssociado);

        if (notificacoes.isEmpty()) {
            log.warn("⚠️ Nenhuma notificação encontrada para {}/{}", mes, ano);
            return 0;
        }

        int processados = 0;
        for (NotificacaoSumarizada n : notificacoes) {
            try {
                String codigoSpc = String.valueOf(n.getCodigoAssociado());
                
                Long idSga = converterCodigoSpcParaIdSga(codigoSpc);
                
                if (idSga == null) {
                    log.warn("⚠️ Associado não encontrado para código SPC: {}, ignorando", codigoSpc);
                    continue;
                }

                NotificacaoAssociado notificacao = notificacaoAssociadoRepository
                        .findByAssociadoIdAndMesReferenciaAndAnoReferencia(idSga, mes, ano)
                        .orElse(new NotificacaoAssociado());

                notificacao.setAssociadoId(idSga);
                notificacao.setCodigoSpc(codigoSpc);
                notificacao.setMesReferencia(mes);
                notificacao.setAnoReferencia(ano);
                notificacao.setSmsSemEnriquecimento(n.getSmsSemEnriquecimento());
                notificacao.setSmsComEnriquecimento(n.getSmsComEnriquecimento());
                notificacao.setEmailsSemEnriquecimento(n.getEmailsSemEnriquecimento());
                notificacao.setEmailsComEnriquecimento(n.getEmailsComEnriquecimento());
                notificacao.setCartasTotal(n.getCartasEnviadas());
                notificacao.setNaoEnviadas(n.getNaoEnviada());
                notificacao.calcularTotais();
                notificacao.calcularValoresTotais();

                notificacaoAssociadoRepository.save(notificacao);
                processados++;

            } catch (Exception e) {
                log.error("❌ Erro ao processar associado {}: {}", n.getCodigoAssociado(), e.getMessage(), e);
            }
        }

        log.info("✅ Sincronização agrupada concluída! {} associados processados", processados);
        return processados;
    }

    @Transactional
    public int sincronizarNotificacoesAgrupadasPorPeriodo(LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {
        log.info("🔄 Sincronizando notificações agrupadas por período - Início: {}, Fim: {}", dataInicio, dataFim);

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesAgrupadasPorPeriodo(dataInicio, dataFim, codigoAssociado);

        if (notificacoes.isEmpty()) {
            log.warn("⚠️ Nenhuma notificação encontrada para o período");
            return 0;
        }

        int mes = dataFim.getMonthValue();
        int ano = dataFim.getYear();

        log.info("📅 Período: {} à {} - Competência: {}/{}", dataInicio, dataFim, mes, ano);

        int processados = 0;
        for (NotificacaoSumarizada n : notificacoes) {
            try {
                String codigoSpc = String.valueOf(n.getCodigoAssociado());
                
                Long idSga = converterCodigoSpcParaIdSga(codigoSpc);
                
                if (idSga == null) {
                    log.warn("⚠️ Associado não encontrado para código SPC: {}, ignorando", codigoSpc);
                    continue;
                }

                NotificacaoAssociado notificacao = notificacaoAssociadoRepository
                        .findByAssociadoIdAndMesReferenciaAndAnoReferencia(idSga, mes, ano)
                        .orElse(new NotificacaoAssociado());

                notificacao.setAssociadoId(idSga);
                notificacao.setCodigoSpc(codigoSpc);
                notificacao.setMesReferencia(mes);
                notificacao.setAnoReferencia(ano);
                notificacao.setPeriodoInicio(dataInicio);
                notificacao.setPeriodoFim(dataFim);

                notificacao.setSmsSemEnriquecimento(
                    notificacao.getSmsSemEnriquecimento() + (n.getSmsSemEnriquecimento() != null ? n.getSmsSemEnriquecimento() : 0)
                );
                notificacao.setSmsComEnriquecimento(
                    notificacao.getSmsComEnriquecimento() + (n.getSmsComEnriquecimento() != null ? n.getSmsComEnriquecimento() : 0)
                );
                notificacao.setEmailsSemEnriquecimento(
                    notificacao.getEmailsSemEnriquecimento() + (n.getEmailsSemEnriquecimento() != null ? n.getEmailsSemEnriquecimento() : 0)
                );
                notificacao.setEmailsComEnriquecimento(
                    notificacao.getEmailsComEnriquecimento() + (n.getEmailsComEnriquecimento() != null ? n.getEmailsComEnriquecimento() : 0)
                );
                notificacao.setCartasTotal(
                    notificacao.getCartasTotal() + (n.getCartasEnviadas() != null ? n.getCartasEnviadas() : 0)
                );
                notificacao.setNaoEnviadas(
                    notificacao.getNaoEnviadas() + (n.getNaoEnviada() != null ? n.getNaoEnviada() : 0)
                );

                notificacao.calcularTotais();
                notificacao.calcularValoresTotais();

                notificacaoAssociadoRepository.save(notificacao);
                processados++;

            } catch (Exception e) {
                log.error("❌ Erro ao processar associado {}: {}", n.getCodigoAssociado(), e.getMessage(), e);
            }
        }

        log.info("✅ Sincronização agrupada por período concluída! {} associados processados", processados);
        return processados;
    }

    @Transactional
    public void processarNotificacoesAssociado(
            Long idSga,
            String codigoSpc,
            Integer mes,
            Integer ano,
            LocalDate periodoInicio,
            LocalDate periodoFim,
            List<NotificacaoSumarizada> notificacoes) {

        log.info("📝 Processando notificações do associado - ID SGA: {}, Código SPC: {}", idSga, codigoSpc);

        NotificacaoAssociado notificacao = notificacaoAssociadoRepository
                .findByAssociadoIdAndMesReferenciaAndAnoReferencia(idSga, mes, ano)
                .orElse(new NotificacaoAssociado());

        notificacao.setAssociadoId(idSga);
        notificacao.setCodigoSpc(codigoSpc);
        notificacao.setMesReferencia(mes);
        notificacao.setAnoReferencia(ano);
        notificacao.setPeriodoInicio(periodoInicio);
        notificacao.setPeriodoFim(periodoFim);

        calcularTotais(notificacao, notificacoes);
        notificacao.calcularTotais();
        notificacao.calcularValoresTotais();

        notificacaoAssociadoRepository.save(notificacao);

        log.info("✅ Notificações atualizadas para associado {} (Código SPC: {}): SMS: {} ({} sem / {} com), E-mail: {} ({} sem / {} com), Cartas: {}, Não enviadas: {}",
                idSga, codigoSpc,
                notificacao.getSmsTotal(),
                notificacao.getSmsSemEnriquecimento(),
                notificacao.getSmsComEnriquecimento(),
                notificacao.getEmailsTotal(),
                notificacao.getEmailsSemEnriquecimento(),
                notificacao.getEmailsComEnriquecimento(),
                notificacao.getCartasTotal(),
                notificacao.getNaoEnviadas());
    }

    // ========== CONSULTAS ==========

    public List<NotificacaoSumarizadaDTO> buscarNotificacoesSumarizadas(String codigoAssociado) {
        log.info("🔍 Buscando notificações sumarizadas - Código: {}", codigoAssociado);

        LocalDate dataInicio = LocalDate.now().minusDays(diasRetrospectiva);
        LocalDate dataFim = LocalDate.now();

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesSumarizadas(dataInicio, dataFim, codigoAssociado);

        return notificacoes.stream()
                .map(this::toSumarizadaDTO)
                .collect(Collectors.toList());
    }

    public List<NotificacaoSumarizadaDTO> buscarNotificacoesPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        log.info("📅 Buscando notificações por período - Início: {}, Fim: {}", dataInicio, dataFim);

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesSumarizadas(dataInicio, dataFim, codigoAssociado);

        return notificacoes.stream()
                .map(this::toSumarizadaDTO)
                .collect(Collectors.toList());
    }

    public List<NotificacaoSumarizadaDTO> buscarNotificacoesAgrupadas(Integer mes, Integer ano, String codigoAssociado) {
        log.info("📊 Buscando notificações agrupadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesAgrupadasPorMesAno(mes, ano, codigoAssociado);

        return notificacoes.stream()
                .map(this::toSumarizadaDTO)
                .collect(Collectors.toList());
    }

    public List<NotificacaoSumarizadaDTO> buscarNotificacoesAgrupadasPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        log.info("📊 Buscando notificações agrupadas por período - Início: {}, Fim: {}", dataInicio, dataFim);

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesAgrupadasPorPeriodo(dataInicio, dataFim, codigoAssociado);

        return notificacoes.stream()
                .map(this::toSumarizadaDTO)
                .collect(Collectors.toList());
    }

    public List<NotificacaoSumarizadaDTO> buscarNotificacoesDetalhadas(
            Integer mes, Integer ano, String codigoAssociado) {

        log.info("📋 Buscando notificações detalhadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        if (codigoAssociado == null || codigoAssociado.isEmpty()) {
            log.warn("⚠️ Código do associado é obrigatório para consulta detalhada");
            return new ArrayList<>();
        }

        List<NotificacaoSumarizada> notificacoes = notificacaoSumarizadaRepository
                .buscarNotificacoesDetalhadasPorAssociado(mes, ano, codigoAssociado);

        return notificacoes.stream()
                .map(this::toSumarizadaDTO)
                .collect(Collectors.toList());
    }

    public NotificacaoAssociadoDTO buscarNotificacoesPorAssociadoPeriodo(Long associadoId, Integer mes, Integer ano) {
        log.info("🔍 Buscando notificações do associado {} - {}/{}", associadoId, mes, ano);

        return notificacaoAssociadoRepository
                .findByAssociadoIdAndMesReferenciaAndAnoReferencia(associadoId, mes, ano)
                .map(this::toAssociadoDTO)
                .orElse(null);
    }

    public List<NotificacaoAssociadoDTO> buscarNaoProcessados(Integer mes, Integer ano) {
        log.info("🔍 Buscando notificações não processadas - {}/{}", mes, ano);

        return notificacaoAssociadoRepository
                .findNaoProcessados(mes, ano)
                .stream()
                .map(this::toAssociadoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarComoProcessado(Long notificacaoId, Long faturaId) {
        log.info("📌 Marcando notificação {} como processada na fatura {}", notificacaoId, faturaId);
        notificacaoAssociadoRepository.marcarComoProcessado(notificacaoId, faturaId);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void calcularTotais(NotificacaoAssociado notificacao, List<NotificacaoSumarizada> notificacoes) {
        int smsSemEnr = 0, smsComEnr = 0;
        int emailSemEnr = 0, emailComEnr = 0;
        int cartas = 0, naoEnviadas = 0;

        for (NotificacaoSumarizada n : notificacoes) {
            smsSemEnr += n.getSmsSemEnriquecimento() != null ? n.getSmsSemEnriquecimento() : 0;
            smsComEnr += n.getSmsComEnriquecimento() != null ? n.getSmsComEnriquecimento() : 0;
            emailSemEnr += n.getEmailsSemEnriquecimento() != null ? n.getEmailsSemEnriquecimento() : 0;
            emailComEnr += n.getEmailsComEnriquecimento() != null ? n.getEmailsComEnriquecimento() : 0;
            cartas += n.getCartasEnviadas() != null ? n.getCartasEnviadas() : 0;
            naoEnviadas += n.getNaoEnviada() != null ? n.getNaoEnviada() : 0;
        }

        notificacao.setSmsSemEnriquecimento(smsSemEnr);
        notificacao.setSmsComEnriquecimento(smsComEnr);
        notificacao.setEmailsSemEnriquecimento(emailSemEnr);
        notificacao.setEmailsComEnriquecimento(emailComEnr);
        notificacao.setCartasTotal(cartas);
        notificacao.setNaoEnviadas(naoEnviadas);
    }

    private NotificacaoSumarizadaDTO toSumarizadaDTO(NotificacaoSumarizada n) {
        NotificacaoSumarizadaDTO dto = new NotificacaoSumarizadaDTO();
        dto.setIdRemessa(n.getIdRemessa());
        dto.setTipoEnvio(n.getTipoEnvio());
        dto.setCompetencia(n.getCompetencia());
        dto.setDataMovimento(n.getDataMovimento());
        dto.setCodigoAssociado(n.getCodigoAssociado());
        dto.setNomeAssociado(n.getNomeAssociado());
        dto.setTotalRegistrosDigital(n.getTotalRegistrosDigital());
        dto.setSmsSemEnriquecimento(n.getSmsSemEnriquecimento());
        dto.setSmsComEnriquecimento(n.getSmsComEnriquecimento());
        dto.setTotalSms(n.getTotalSms());
        dto.setEmailsSemEnriquecimento(n.getEmailsSemEnriquecimento());
        dto.setEmailsComEnriquecimento(n.getEmailsComEnriquecimento());
        dto.setTotalEmail(n.getTotalEmail());
        dto.setCartasEnviadas(n.getCartasEnviadas());
        dto.setNaoEnviada(n.getNaoEnviada());
        return dto;
    }

    private NotificacaoAssociadoDTO toAssociadoDTO(NotificacaoAssociado n) {
        NotificacaoAssociadoDTO dto = new NotificacaoAssociadoDTO();
        dto.setId(n.getId());
        dto.setAssociadoId(n.getAssociadoId());
        dto.setReguaId(n.getReguaId());
        dto.setMesReferencia(n.getMesReferencia());
        dto.setAnoReferencia(n.getAnoReferencia());
        dto.setPeriodoInicio(n.getPeriodoInicio());
        dto.setPeriodoFim(n.getPeriodoFim());
        dto.setCodigoSpc(n.getCodigoSpc());
        dto.setTotalRegistros(n.getTotalRegistros());
        dto.setSmsSemEnriquecimento(n.getSmsSemEnriquecimento());
        dto.setSmsComEnriquecimento(n.getSmsComEnriquecimento());
        dto.setSmsTotal(n.getSmsTotal());
        dto.setEmailsSemEnriquecimento(n.getEmailsSemEnriquecimento());
        dto.setEmailsComEnriquecimento(n.getEmailsComEnriquecimento());
        dto.setEmailsTotal(n.getEmailsTotal());
        dto.setCartasTotal(n.getCartasTotal());
        dto.setNaoEnviadas(n.getNaoEnviadas());
        dto.setValorTotal(n.getValorTotal());
        dto.setProcessadoFatura(n.getProcessadoFatura());
        dto.setFaturaId(n.getFaturaId());
        return dto;
    }
}