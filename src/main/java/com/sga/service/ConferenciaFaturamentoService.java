package com.sga.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sga.dto.ConferenciaFaturaDetalheDTO;
import com.sga.dto.ConferenciaFaturamentoDTO;
import com.sga.dto.ConferenciaResumoDTO;
import com.sga.repository.FaturaRepository;
import com.sga.util.CastUtils;

@Service
public class ConferenciaFaturamentoService {

    private static final Logger log = LoggerFactory.getLogger(ConferenciaFaturamentoService.class);

    @Autowired
    private FaturaRepository faturaRepository;

    // ============================================================
    // MÉTODOS PRINCIPAIS
    // ============================================================

    public Page<ConferenciaFaturamentoDTO> listarConferencia(
            Long reguaId,
            LocalDate dataInicio,
            LocalDate dataFim,
            String codigoSpc,
            String statusFiltro,
            Pageable pageable) {

        log.info("📊 Listando conferência de faturamento - Régua: {}, Período: {} à {}, Código: {}, Status: {}",
                reguaId, dataInicio, dataFim, codigoSpc, statusFiltro);

        List<Object[]> resultados = faturaRepository.findConferenciaFaturamento(
                reguaId, dataInicio, dataFim, codigoSpc);

        log.info("📊 Registros encontrados: {}", resultados != null ? resultados.size() : 0);

        List<ConferenciaFaturamentoDTO> lista = new ArrayList<>();

        if (resultados != null) {
            for (Object[] row : resultados) {
                if (row == null || row.length < 18) {
                    log.warn("⚠️ Linha inválida ou com tamanho insuficiente: {}",
                            row != null ? row.length : "null");
                    continue;
                }

                try {
                    ConferenciaFaturamentoDTO dto = new ConferenciaFaturamentoDTO();

                    int idx = 0;
                    dto.setFaturaId(CastUtils.toLong(row[idx++]));
                    dto.setNumeroFatura(CastUtils.toStringSafe(row[idx++]));
                    dto.setDataEmissao(converterParaLocalDate(row[idx++]));
                    dto.setStatusFatura(CastUtils.toStringSafe(row[idx++]));
                    dto.setValorFatura(CastUtils.toBigDecimal(row[idx++]));

                    dto.setNotaId(CastUtils.toLong(row[idx++]));
                    dto.setNumeroNotaDebito(CastUtils.toStringSafe(row[idx++]));
                    dto.setDataVencimento(converterParaLocalDate(row[idx++]));
                    dto.setValorNota(CastUtils.toBigDecimal(row[idx++]));
                    dto.setTipoArquivo(CastUtils.toStringSafe(row[idx++]));

                    dto.setAssociadoId(CastUtils.toLong(row[idx++]));
                    dto.setCodigoSpc(CastUtils.toStringSafe(row[idx++]));
                    dto.setCodigoRm(CastUtils.toStringSafe(row[idx++]));
                    dto.setNomeRazao(CastUtils.toStringSafe(row[idx++]));

                    dto.setQtdItensNota(CastUtils.toInteger(row[idx++]));
                    dto.setQtdItensFatura(CastUtils.toInteger(row[idx++]));
                    dto.setQtdFranquiasNota(CastUtils.toInteger(row[idx++]));
                    dto.setQtdFranquiasFatura(CastUtils.toInteger(row[idx++]));

                    dto.setDiferencaValor(dto.getValorFatura().subtract(dto.getValorNota()));
                    dto.setDiferencaItens(dto.getQtdItensFatura() - dto.getQtdItensNota());
                    dto.setDiferencaFranquias(dto.getQtdFranquiasFatura() - dto.getQtdFranquiasNota());

                    dto.setStatusConferencia(determinarStatus(dto));
                    dto.setObservacao(gerarObservacao(dto));

                    lista.add(dto);

                } catch (Exception e) {
                    log.error("❌ Erro ao processar linha: {}", e.getMessage(), e);
                }
            }
        }

        log.info("📊 Total de faturas processadas: {}", lista.size());
        
        // 🔥 Debug: conta status antes do filtro
        long qtdOK = lista.stream().filter(d -> "OK".equals(d.getStatusConferencia())).count();
        long qtdDIF = lista.stream().filter(d -> "DIFERENCA".equals(d.getStatusConferencia())).count();
        log.info("📊 Distribuição antes do filtro: OK={}, DIFERENCA={}, TOTAL={}",
                qtdOK, qtdDIF, lista.size());
        

        // 🔥 Filtro por status (aplicado em memória)
        if (statusFiltro != null && !statusFiltro.isEmpty() && !"Todos".equals(statusFiltro)) {
            List<ConferenciaFaturamentoDTO> filtrados = lista.stream()
                    .filter(dto -> statusFiltro.equals(dto.getStatusConferencia()))
                    .collect(Collectors.toList());
            log.info("📊 Filtro por status '{}' aplicado: {} resultados (antes: {})",
                    statusFiltro, filtrados.size(), lista.size());
            lista = filtrados;
        }

        // Paginação em memória
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), lista.size());
        if (start > lista.size()) {
            start = 0;
            end = Math.min(pageable.getPageSize(), lista.size());
        }

        List<ConferenciaFaturamentoDTO> subList = lista.subList(start, end);

        return new PageImpl<>(subList, pageable, lista.size());
    }

    public ConferenciaFaturaDetalheDTO detalharConferencia(Long faturaId) {
        log.info("📊 Detalhando conferência da fatura ID: {}", faturaId);

        List<Object[]> resultados = faturaRepository.findFaturaDetalheConferencia(faturaId);

        if (resultados == null || resultados.isEmpty()) {
            throw new RuntimeException("Fatura não encontrada: " + faturaId);
        }

        Object[] dados = resultados.get(0);

        // 🔥 Desempacotar se vier aninhado
        if (dados.length == 1 && dados[0] instanceof Object[]) {
            log.warn("⚠️ Driver retornou Object[] aninhado. Desempacotando...");
            dados = (Object[]) dados[0];
        }

        if (dados.length < 13) {
            throw new RuntimeException(
                "Query retornou " + dados.length + " colunas; esperado 13");
        }

        ConferenciaFaturaDetalheDTO detalhe = new ConferenciaFaturaDetalheDTO();

        try {
            int idx = 0;
            detalhe.setFaturaId(CastUtils.toLong(dados[idx++]));
            detalhe.setNumeroFatura(CastUtils.toStringSafe(dados[idx++]));
            detalhe.setDataEmissao(converterParaLocalDate(dados[idx++]));
            detalhe.setStatusFatura(CastUtils.toStringSafe(dados[idx++]));
            detalhe.setValorFatura(CastUtils.toBigDecimal(dados[idx++]));

            detalhe.setNotaId(CastUtils.toLong(dados[idx++]));
            detalhe.setNumeroNotaDebito(CastUtils.toStringSafe(dados[idx++]));
            detalhe.setDataVencimento(converterParaLocalDate(dados[idx++]));
            detalhe.setValorNota(CastUtils.toBigDecimal(dados[idx++]));

            detalhe.setAssociadoId(CastUtils.toLong(dados[idx++]));
            detalhe.setCodigoSpc(CastUtils.toStringSafe(dados[idx++]));
            detalhe.setCodigoRm(CastUtils.toStringSafe(dados[idx++]));
            detalhe.setNomeRazao(CastUtils.toStringSafe(dados[idx++]));

            detalhe.setDiferencaValor(detalhe.getValorFatura().subtract(detalhe.getValorNota()));

            // Itens da nota
            List<Object[]> itensNota = faturaRepository.findItensNotaByFaturaId(faturaId);
            List<ConferenciaFaturaDetalheDTO.ItemComparacaoDTO> itensNotaList = new ArrayList<>();
            if (itensNota != null) {
                for (Object[] item : itensNota) {
                    if (item == null || item.length < 5) continue;

                    ConferenciaFaturaDetalheDTO.ItemComparacaoDTO dto =
                            new ConferenciaFaturaDetalheDTO.ItemComparacaoDTO();
                    dto.setCodigo(CastUtils.toStringSafe(item[0]));
                    dto.setDescricao(CastUtils.toStringSafe(item[1]));
                    dto.setQuantidade(CastUtils.toBigDecimal(item[2]));
                    dto.setValorUnitario(CastUtils.toBigDecimal(item[3]));
                    dto.setValorTotal(CastUtils.toBigDecimal(item[4]));
                    dto.setTipo("NOTA");
                    dto.setStatus("OK");
                    itensNotaList.add(dto);
                }
            }
            detalhe.setItensNota(itensNotaList);

            // Itens da fatura
            List<Object[]> itensFatura = faturaRepository.findItensFaturaByFaturaId(faturaId);
            List<ConferenciaFaturaDetalheDTO.ItemComparacaoDTO> itensFaturaList = new ArrayList<>();
            if (itensFatura != null) {
                for (Object[] item : itensFatura) {
                    if (item == null || item.length < 5) continue;

                    ConferenciaFaturaDetalheDTO.ItemComparacaoDTO dto =
                            new ConferenciaFaturaDetalheDTO.ItemComparacaoDTO();
                    dto.setCodigo(CastUtils.toStringSafe(item[0]));
                    dto.setDescricao(CastUtils.toStringSafe(item[1]));
                    dto.setQuantidade(CastUtils.toBigDecimal(item[2]));
                    dto.setValorUnitario(CastUtils.toBigDecimal(item[3]));
                    dto.setValorTotal(CastUtils.toBigDecimal(item[4]));
                    dto.setTipo("FATURA");
                    dto.setStatus("OK");
                    itensFaturaList.add(dto);
                }
            }
            detalhe.setItensFatura(itensFaturaList);

            detalhe.setDiferencaItens(itensFaturaList.size() - itensNotaList.size());
            detalhe.setStatusConferencia(determinarStatus(detalhe));
            detalhe.setObservacao(gerarObservacao(detalhe));

        } catch (Exception e) {
            log.error("❌ Erro ao processar detalhe da fatura {}: {}", faturaId, e.getMessage(), e);
            throw new RuntimeException("Erro ao processar detalhe da fatura: " + e.getMessage(), e);
        }

        return detalhe;
    }

    public ConferenciaResumoDTO resumoConferencia(Long reguaId, LocalDate dataInicio, LocalDate dataFim) {
        log.info("📊 Resumo da conferência - Régua: {}, Período: {} à {}", reguaId, dataInicio, dataFim);

        List<Object[]> resultados = faturaRepository.findResumoConferencia(reguaId, dataInicio, dataFim);

        ConferenciaResumoDTO resumo = new ConferenciaResumoDTO();

        if (resultados == null || resultados.isEmpty() || resultados.get(0) == null) {
            return resumo;
        }

        Object[] row = resultados.get(0);
        int idx = 0;

        BigDecimal somaDiferencas = CastUtils.toBigDecimal(row[idx++]);
        Integer totalItensFatura = CastUtils.toInteger(row[idx++]);
        Integer totalItensNota = CastUtils.toInteger(row[idx++]);
        Integer totalFranquiasFatura = CastUtils.toInteger(row[idx++]);
        Integer totalFranquiasNota = CastUtils.toInteger(row[idx++]);
        BigDecimal somaValorNotas = CastUtils.toBigDecimal(row[idx++]);
        BigDecimal somaValorFaturas = CastUtils.toBigDecimal(row[idx++]);

        resumo.setSomaDiferencas(somaDiferencas);
        resumo.setTotalItensFatura(totalItensFatura);
        resumo.setTotalItensNota(totalItensNota);
        resumo.setTotalFranquiasFatura(totalFranquiasFatura);
        resumo.setTotalFranquiasNota(totalFranquiasNota);
        resumo.setTotalFranquiasRemovidas(totalFranquiasNota - totalFranquiasFatura);
        resumo.setSomaValorNotas(somaValorNotas);
        resumo.setSomaValorFaturas(somaValorFaturas);

        // Conta OK vs Diferença
        List<Object[]> faturas = faturaRepository.findConferenciaFaturamento(
                reguaId, dataInicio, dataFim, null);
        int totalFaturas = faturas != null ? faturas.size() : 0;
        int totalComDiferenca = 0;
        int totalSemDiferenca = 0;
        int totalComAtencao = 0;

        if (faturas != null) {
            for (Object[] f : faturas) {
                if (f == null || f.length < 9) continue;

                BigDecimal valorFatura = CastUtils.toBigDecimal(f[4]);
                BigDecimal valorNota = CastUtils.toBigDecimal(f[8]);
                BigDecimal diff = valorFatura.subtract(valorNota);

                if (diff.compareTo(BigDecimal.ZERO) == 0
                        || diff.abs().compareTo(new BigDecimal("0.01")) <= 0) {
                    totalSemDiferenca++;
                } else if (diff.abs().compareTo(new BigDecimal("10")) > 0) {
                    totalComAtencao++;
                } else {
                    totalComDiferenca++;
                }
            }
        }

        resumo.setTotalFaturas(totalFaturas);
        resumo.setTotalComDiferenca(totalComDiferenca);
        resumo.setTotalSemDiferenca(totalSemDiferenca);
        resumo.setTotalAssociados(totalFaturas);

        return resumo;
    }

    public ConferenciaResumoDTO resumoConferenciaComFiltros(
            Long reguaId, LocalDate dataInicio, LocalDate dataFim,
            String codigoSpc, String statusFiltro) {

        log.info("📊 Resumo da conferência com filtros - Régua: {}, Período: {} à {}, Status: {}",
                reguaId, dataInicio, dataFim, statusFiltro);

        ConferenciaResumoDTO resumo = new ConferenciaResumoDTO();

        List<Object[]> contagemStatus = faturaRepository.contarFaturasPorStatusConferencia(
                reguaId, dataInicio, dataFim, codigoSpc);

        int totalOK = 0;
        int totalDiferenca = 0;

        if (contagemStatus != null && !contagemStatus.isEmpty()) {
            Object[] row = contagemStatus.get(0);
            if (row != null) {
                totalOK = CastUtils.toInteger(row[0]);
                totalDiferenca = CastUtils.toInteger(row[1]);
            }
        }

        List<Object[]> somas = faturaRepository.somarDiferencasConferencia(
                reguaId, dataInicio, dataFim, codigoSpc);

        BigDecimal somaDiferencas = BigDecimal.ZERO;
        int totalFaturas = 0;

        if (somas != null && !somas.isEmpty()) {
            Object[] row = somas.get(0);
            if (row != null) {
                somaDiferencas = CastUtils.toBigDecimal(row[0]);
                totalFaturas = CastUtils.toInteger(row[1]);
            }
        }

        if (statusFiltro != null && !statusFiltro.isEmpty() && !"Todos".equals(statusFiltro)) {
            if ("OK".equals(statusFiltro)) {
                totalFaturas = totalOK;
                totalDiferenca = 0;
                somaDiferencas = BigDecimal.ZERO;
            } else if ("DIFERENCA".equals(statusFiltro)) {
                totalFaturas = totalDiferenca;
                totalOK = 0;
            }
        }

        resumo.setTotalFaturas(totalFaturas);
        resumo.setTotalSemDiferenca(totalOK);
        resumo.setTotalComDiferenca(totalDiferenca);
        resumo.setSomaDiferencas(somaDiferencas);
        resumo.setTotalAssociados(totalFaturas);

        return resumo;
    }

    // ============================================================
    // 🔥 EXPORTAÇÃO CSV
    // ============================================================

    /**
     * Exporta conferência para CSV (todos os registros filtrados).
     */
    public String exportarCSV(Long reguaId, LocalDate dataInicio, LocalDate dataFim,
                              String codigoSpcFiltro, String statusFiltro) {

        log.info("📊 [CSV] Exportando conferência - Régua: {}, Período: {} à {}, Código: {}, Status: {}",
                reguaId, dataInicio, dataFim, codigoSpcFiltro, statusFiltro);

        List<Object[]> resultados = faturaRepository.findConferenciaParaExportacao(
                reguaId, dataInicio, dataFim, codigoSpcFiltro);

        StringBuilder csv = new StringBuilder();
        // BOM para Excel reconhecer UTF-8
        csv.append("\uFEFF");
        // Cabeçalho
        csv.append("Associado;Código SPC;Código RM;CNPJ/CPF;");
        csv.append("Nº Nota Débito;Valor Nota;Data Vencimento;");
        csv.append("Nº Fatura;Valor Fatura;Data Emissão;");
        csv.append("Diferença;Status Conferência\n");

        int linhas = 0;
        if (resultados != null) {
            for (Object[] row : resultados) {
                try {
                    // Índices conforme a query findConferenciaParaExportacao:
                    // 0=fatura_id, 1=numero_fatura, 2=data_emissao, 3=data_vencimento,
                    // 4=status_fatura, 5=valor_fatura, 6=nota_id, 7=numero_nota_debito,
                    // 8=valor_nota, 9=associado_id, 10=codigo_spc, 11=codigo_rm,
                    // 12=nome_razao, 13=cnpj_cpf
                    String numeroFatura = CastUtils.toStringSafe(row[1]);
                    LocalDate dataEmissao = converterParaLocalDate(row[2]);
                    LocalDate dataVencimento = converterParaLocalDate(row[3]);
                    BigDecimal valorFatura = CastUtils.toBigDecimal(row[5]);
                    String numeroNota = CastUtils.toStringSafe(row[7]);
                    BigDecimal valorNota = CastUtils.toBigDecimal(row[8]);
                    String codigoSpcRow = CastUtils.toStringSafe(row[10]);
                    String codigoRmRow = CastUtils.toStringSafe(row[11]);
                    String nomeRazao = CastUtils.toStringSafe(row[12]);
                    String cnpjCpf = CastUtils.toStringSafe(row[13]);

                    BigDecimal diff = valorFatura.subtract(valorNota);
                    String status = diff.abs().compareTo(new BigDecimal("0.01")) <= 0
                            ? "OK" : "DIFERENCA";

                    // Aplicar filtro de status se houver
                    if (statusFiltro != null && !statusFiltro.isEmpty()
                            && !"Todos".equals(statusFiltro)
                            && !statusFiltro.equals(status)) {
                        continue;
                    }

                    csv.append(escaparCSV(nomeRazao)).append(";");
                    csv.append(escaparCSV(codigoSpcRow)).append(";");
                    csv.append(escaparCSV(codigoRmRow)).append(";");
                    csv.append(escaparCSV(cnpjCpf)).append(";");
                    csv.append(escaparCSV(numeroNota)).append(";");
                    csv.append(formatarBigDecimal(valorNota)).append(";");
                    csv.append(dataVencimento != null ? dataVencimento.toString() : "").append(";");
                    csv.append(escaparCSV(numeroFatura)).append(";");
                    csv.append(formatarBigDecimal(valorFatura)).append(";");
                    csv.append(dataEmissao != null ? dataEmissao.toString() : "").append(";");
                    csv.append(formatarBigDecimal(diff)).append(";");
                    csv.append(status).append("\n");
                    linhas++;
                } catch (Exception e) {
                    log.error("❌ Erro ao processar linha para CSV: {}", e.getMessage());
                }
            }
        }

        log.info("✅ [CSV] {} linhas exportadas", linhas);
        return csv.toString();
    }

    /**
     * Exporta apenas faturas selecionadas por IDs.
     */
    public String exportarCSVSelecionados(List<Long> faturaIds) {
        log.info("📊 [CSV] Exportando {} faturas selecionadas", faturaIds.size());

        List<Object[]> resultados = faturaRepository.findFaturasParaExportacaoPorIds(faturaIds);

        StringBuilder csv = new StringBuilder();
        csv.append("\uFEFF");
        csv.append("Associado;Código SPC;Código RM;CNPJ/CPF;");
        csv.append("Nº Nota Débito;Valor Nota;Data Vencimento;");
        csv.append("Nº Fatura;Valor Fatura;Data Emissão;");
        csv.append("Diferença;Status Conferência\n");

        int linhas = 0;
        if (resultados != null) {
            for (Object[] row : resultados) {
                try {
                    String numeroFatura = CastUtils.toStringSafe(row[1]);
                    LocalDate dataEmissao = converterParaLocalDate(row[2]);
                    LocalDate dataVencimento = converterParaLocalDate(row[3]);
                    BigDecimal valorFatura = CastUtils.toBigDecimal(row[5]);
                    String numeroNota = CastUtils.toStringSafe(row[7]);
                    BigDecimal valorNota = CastUtils.toBigDecimal(row[8]);
                    String codigoSpcRow = CastUtils.toStringSafe(row[10]);
                    String codigoRmRow = CastUtils.toStringSafe(row[11]);
                    String nomeRazao = CastUtils.toStringSafe(row[12]);
                    String cnpjCpf = CastUtils.toStringSafe(row[13]);

                    BigDecimal diff = valorFatura.subtract(valorNota);
                    String status = diff.abs().compareTo(new BigDecimal("0.01")) <= 0
                            ? "OK" : "DIFERENCA";

                    csv.append(escaparCSV(nomeRazao)).append(";");
                    csv.append(escaparCSV(codigoSpcRow)).append(";");
                    csv.append(escaparCSV(codigoRmRow)).append(";");
                    csv.append(escaparCSV(cnpjCpf)).append(";");
                    csv.append(escaparCSV(numeroNota)).append(";");
                    csv.append(formatarBigDecimal(valorNota)).append(";");
                    csv.append(dataVencimento != null ? dataVencimento.toString() : "").append(";");
                    csv.append(escaparCSV(numeroFatura)).append(";");
                    csv.append(formatarBigDecimal(valorFatura)).append(";");
                    csv.append(dataEmissao != null ? dataEmissao.toString() : "").append(";");
                    csv.append(formatarBigDecimal(diff)).append(";");
                    csv.append(status).append("\n");
                    linhas++;
                } catch (Exception e) {
                    log.error("❌ Erro ao processar linha (selecionadas): {}", e.getMessage());
                }
            }
        }

        log.info("✅ [CSV] {} linhas exportadas (selecionadas)", linhas);
        return csv.toString();
    }

    private String escaparCSV(String valor) {
        if (valor == null) return "";
        String v = valor.replace("\"", "\"\"");
        if (v.contains(";") || v.contains("\n") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private String formatarBigDecimal(BigDecimal valor) {
        if (valor == null) return "0,00";
        return valor.setScale(2, RoundingMode.HALF_UP)
                .toString().replace(".", ",");
    }

    // ============================================================
    // MÉTODOS AUXILIARES
    // ============================================================

    private LocalDate converterParaLocalDate(Object dataObj) {
        if (dataObj == null) return null;

        try {
            if (dataObj instanceof Object[]) {
                Object[] arr = (Object[]) dataObj;
                return arr.length > 0 ? converterParaLocalDate(arr[0]) : null;
            }

            if (dataObj instanceof java.sql.Date) {
                return ((java.sql.Date) dataObj).toLocalDate();
            }

            if (dataObj instanceof java.sql.Timestamp) {
                return ((java.sql.Timestamp) dataObj).toLocalDateTime().toLocalDate();
            }

            if (dataObj instanceof LocalDate) {
                return (LocalDate) dataObj;
            }

            if (dataObj instanceof String) {
                String dataStr = ((String) dataObj).trim();
                if (dataStr.isEmpty()) return null;

                if (dataStr.matches("\\d{8}")) {
                    return converterDDMMYYYY(dataStr);
                }

                try {
                    return LocalDate.parse(dataStr);
                } catch (DateTimeParseException e) {
                    try {
                        return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (DateTimeParseException e2) {
                        try {
                            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                        } catch (DateTimeParseException e3) {
                            log.warn("⚠️ Não foi possível converter data: {}", dataStr);
                            return null;
                        }
                    }
                }
            }

            return LocalDate.parse(dataObj.toString());
        } catch (Exception e) {
            log.warn("⚠️ Erro ao converter data: {}", e.getMessage());
            return null;
        }
    }

    private LocalDate converterDDMMYYYY(String dataStr) {
        try {
            String dataLimpa = dataStr.replaceAll("[^0-9]", "");
            if (dataLimpa.length() != 8) return null;

            int dia = Integer.parseInt(dataLimpa.substring(0, 2));
            int mes = Integer.parseInt(dataLimpa.substring(2, 4));
            int ano = Integer.parseInt(dataLimpa.substring(4, 8));

            return LocalDate.of(ano, mes, dia);
        } catch (Exception e) {
            log.warn("⚠️ Erro ao converter DDMMYYYY '{}': {}", dataStr, e.getMessage());
            return null;
        }
    }

    private String determinarStatus(ConferenciaFaturamentoDTO dto) {
        BigDecimal diffValor = dto.getDiferencaValor() != null 
                ? dto.getDiferencaValor() 
                : BigDecimal.ZERO;

        Integer difItens = dto.getDiferencaItens() != null ? dto.getDiferencaItens() : 0;

        // 🔥 REGRA ÚNICA: só é OK se AMBOS (valor e itens) forem iguais
        boolean valorIgual = diffValor.abs().compareTo(new BigDecimal("0.01")) <= 0;
        boolean itensIguais = difItens == 0;

        if (valorIgual && itensIguais) {
            return "OK";
        }

        // Log para debug
        log.debug("🔍 Status DIFERENCA - Fatura {}: diffValor={}, diffItens={}",
                dto.getFaturaId(), diffValor, difItens);

        return "DIFERENCA";
    }

    private String determinarStatus(ConferenciaFaturaDetalheDTO dto) {
        BigDecimal diffValor = dto.getDiferencaValor();

        if (diffValor.compareTo(BigDecimal.ZERO) == 0 && dto.getDiferencaItens() == 0) {
            return "OK";
        }

        if (diffValor.abs().compareTo(new BigDecimal("0.01")) <= 0) {
            return "OK";
        }

        return "DIFERENCA";
    }

    private String gerarObservacao(ConferenciaFaturamentoDTO dto) {
        StringBuilder obs = new StringBuilder();

        if (dto.getDiferencaValor().compareTo(BigDecimal.ZERO) == 0 && dto.getDiferencaItens() == 0) {
            return "✅ Fatura idêntica à nota de débito";
        }

        if (dto.getDiferencaFranquias() != null && dto.getDiferencaFranquias() < 0) {
            obs.append(String.format("🔴 %d franquia(s) removida(s). ",
                    Math.abs(dto.getDiferencaFranquias())));
        }

        if (dto.getDiferencaItens() != null && dto.getDiferencaItens() < 0) {
            obs.append(String.format("📉 %d item(ns) removido(s). ",
                    Math.abs(dto.getDiferencaItens())));
        }

        if (dto.getDiferencaItens() != null && dto.getDiferencaItens() > 0) {
            obs.append(String.format("📈 %d item(ns) adicionado(s). ",
                    dto.getDiferencaItens()));
        }

        if (dto.getDiferencaValor().compareTo(BigDecimal.ZERO) < 0) {
            obs.append(String.format("💰 Valor reduzido em R$ %.2f",
                    dto.getDiferencaValor().abs()));
        } else if (dto.getDiferencaValor().compareTo(BigDecimal.ZERO) > 0) {
            obs.append(String.format("💰 Valor aumentado em R$ %.2f",
                    dto.getDiferencaValor()));
        }

        return obs.length() > 0 ? obs.toString() : "⚠️ Verificar diferenças";
    }

    private String gerarObservacao(ConferenciaFaturaDetalheDTO dto) {
        StringBuilder obs = new StringBuilder();

        if (dto.getDiferencaValor().compareTo(BigDecimal.ZERO) == 0 && dto.getDiferencaItens() == 0) {
            return "✅ Fatura idêntica à nota de débito";
        }

        if (dto.getDiferencaValor().compareTo(BigDecimal.ZERO) < 0) {
            obs.append(String.format("💰 Valor reduzido em R$ %.2f",
                    dto.getDiferencaValor().abs()));
        } else if (dto.getDiferencaValor().compareTo(BigDecimal.ZERO) > 0) {
            obs.append(String.format("💰 Valor aumentado em R$ %.2f",
                    dto.getDiferencaValor()));
        }

        if (dto.getItensNota() != null && dto.getItensFatura() != null) {
            obs.append(String.format(" 📊 Itens: %d nota → %d fatura",
                    dto.getItensNota().size(), dto.getItensFatura().size()));
        }

        return obs.length() > 0 ? obs.toString() : "⚠️ Verificar diferenças";
    }
}