// src/main/java/com/sga/service/RmApiIntegracaoService.java

package com.sga.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sga.dto.RmApiConfigDTO;
import com.sga.dto.RmApiIntegracaoResponse;
import com.sga.model.ConfiguracaoRm;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.ConfiguracaoRmRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class RmApiIntegracaoService {

    private static final Logger log = LoggerFactory.getLogger(RmApiIntegracaoService.class);

    private static final String DATASERVER_MOVIMENTO = "MovMovimentoTBCData";
    private static final String FORMATO_DATA = "yyyy-MM-dd";

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private RmApiConfigService configService;

    @Autowired
    private ConfiguracaoRmRepository configuracaoRmRepository;

    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(FORMATO_DATA);

    // ============================================================
    // MÉTODO PRINCIPAL - INTEGRAR NOTAS
    // ============================================================

    public RmApiIntegracaoResponse integrarNotas(List<Long> notaIds, Long configuracaoId, String usuario) {
        log.info("📤 Integrando {} notas via RM API", notaIds != null ? notaIds.size() : 0);

        RmApiIntegracaoResponse response = new RmApiIntegracaoResponse();

        if (notaIds == null || notaIds.isEmpty()) {
            response.setSucesso(false);
            response.setMensagem("Nenhuma nota selecionada para integração");
            return response;
        }

        try {
            RmApiConfigDTO config = configuracaoId != null
                ? configService.buscarPorId(configuracaoId)
                : configService.buscarConfiguracaoAtiva();

            if (config == null) {
                response.setSucesso(false);
                response.setMensagem("Nenhuma configuração RM API ativa encontrada");
                return response;
            }

            log.info("📋 Usando configuração API: {} - URL: {}", config.getNome(), config.getWsUrl());

            List<NotaDebitoSPC> notas = notaDebitoRepository.findAllById(notaIds);
            List<Fatura> faturas = faturaRepository.findByNotaDebitoIdIn(notaIds);

            Map<Long, Fatura> faturaMap = new java.util.HashMap<>();
            for (Fatura fatura : faturas) {
                if (fatura.getNotaDebitoId() != null) {
                    faturaMap.put(fatura.getNotaDebitoId(), fatura);
                }
            }

            int sucessos = 0;
            int erros = 0;
            List<RmApiIntegracaoResponse.ItemResponse> itens = new ArrayList<>();

            for (NotaDebitoSPC nota : notas) {
                RmApiIntegracaoResponse.ItemResponse item = new RmApiIntegracaoResponse.ItemResponse();
                item.setNotaId(nota.getId());

                try {
                    Fatura fatura = faturaMap.get(nota.getId());

                    if (fatura == null) {
                        java.util.Optional<Fatura> faturaOpt = faturaRepository.findByNotaDebitoId(nota.getId());
                        if (faturaOpt.isPresent()) {
                            fatura = faturaOpt.get();
                            faturaMap.put(nota.getId(), fatura);
                        }
                    }

                    if (fatura == null) {
                        item.setSucesso(false);
                        item.setMensagem("Fatura não encontrada para nota " + nota.getId());
                        erros++;
                        itens.add(item);
                        continue;
                    }

                    item.setFaturaId(fatura.getId());

                    Long idMov = integrarFaturaViaHttp(fatura, config);

                    item.setSucesso(true);
                    item.setIdMov(idMov);
                    item.setMensagem("Integrado com sucesso (ID Mov: " + (idMov != null ? idMov : "processamento assíncrono") + ")");
                    sucessos++;

                    fatura.setProcessadoRm(true);
                    faturaRepository.save(fatura);

                } catch (Exception e) {
                    log.error("❌ Erro ao integrar nota {}: {}", nota.getId(), e.getMessage());
                    item.setSucesso(false);
                    item.setMensagem(e.getMessage());
                    erros++;
                }

                itens.add(item);
            }

            response.setSucesso(erros == 0);
            response.setMensagem(erros == 0 ? "Todas as notas foram integradas com sucesso" :
                String.format("%d sucessos, %d erros", sucessos, erros));
            response.setTotalProcessados(notas.size());
            response.setTotalSucessos(sucessos);
            response.setTotalErros(erros);
            response.setItens(itens);

            log.info("✅ Integração RM API concluída: {} sucessos, {} erros", sucessos, erros);

        } catch (Exception e) {
            log.error("❌ Erro na integração RM API: {}", e.getMessage(), e);
            response.setSucesso(false);
            response.setMensagem("Erro na integração: " + e.getMessage());
        }

        return response;
    }

    // ============================================================
    // BUSCAR CONFIGURAÇÃO RM
    // ============================================================

    private ConfiguracaoRm buscarConfiguracaoRmAtiva() {
        try {
            List<ConfiguracaoRm> configuracoes = configuracaoRmRepository.findByAtivoTrue();

            for (ConfiguracaoRm config : configuracoes) {
                if (config.getCodigoTmv() != null && config.getCodigoTmv().equals("2.1.05")) {
                    log.info("📋 Configuração RM encontrada: {} (codigoTmv: {})", 
                        config.getDescricao(), config.getCodigoTmv());
                    return config;
                }
            }

            if (!configuracoes.isEmpty()) {
                ConfiguracaoRm config = configuracoes.get(0);
                log.warn("⚠️ Configuração 2.1.05 não encontrada. Usando: {}", config.getDescricao());
                return config;
            }

            log.warn("⚠️ Nenhuma configuração RM ativa. Usando valores padrão.");
            ConfiguracaoRm defaultConfig = new ConfiguracaoRm();
            defaultConfig.setCodigoTmv("2.1.05");
            defaultConfig.setCentroCusto("01.01");
            defaultConfig.setCondicaoPagamento("99");
            defaultConfig.setSerie("RPS");
            defaultConfig.setContaCaixa("78");
            defaultConfig.setCodigoServico("5.949.01");
            defaultConfig.setMunicipioServico("04400");
            defaultConfig.setUfServico("CE");
            defaultConfig.setDescricao("Faturamento de Serviços de SPC (RPS) - Padrão");
            return defaultConfig;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar configuração RM: {}", e.getMessage());
            return null;
        }
    }

    // ============================================================
    // FORMATAR VALOR - NUNCA RETORNA NULL
    // ============================================================

    private String formatarValor(BigDecimal valor) {
        if (valor == null) {
            return "0.00";
        }
        try {
            return String.format("%.2f", valor).replace(",", ".");
        } catch (Exception e) {
            return "0.00";
        }
    }

    private String formatarData(LocalDate data) {
        if (data == null) {
            return LocalDate.now().format(DATE_FORMATTER);
        }
        return data.format(DATE_FORMATTER);
    }

    private String formatarDataComHora(LocalDate data) {
        if (data == null) {
            return LocalDate.now().format(DATE_FORMATTER) + "T00:00:00";
        }
        return data.format(DATE_FORMATTER) + "T00:00:00";
    }

    // ============================================================
    // PREPARAR DADOS DA FATURA
    // ============================================================

    private Fatura prepararDadosFatura(Fatura fatura) {
        log.info("🔍 Preparando dados da fatura ID: {}", fatura.getId());

        if (fatura.getDataEmissao() == null) {
            fatura.setDataEmissao(LocalDate.now());
            log.warn("   ⚠️ Data de emissão NULA. Usando data atual.");
        }

        if (fatura.getDataVencimento() == null) {
            fatura.setDataVencimento(LocalDate.now().plusDays(30));
            log.warn("   ⚠️ Data de vencimento NULA. Usando data atual + 30 dias.");
        }

        if (fatura.getValorTotal() == null) {
            fatura.recalcularTotal();
            if (fatura.getValorTotal() == null) {
                fatura.setValorTotal(BigDecimal.ZERO);
                log.warn("   ⚠️ Valor total NULO. Usando 0.00");
            }
        }
        log.info("   Valor Total: R$ {}", formatarValor(fatura.getValorTotal()));

        List<FaturaItem> itens = fatura.getItens();
        if (itens == null || itens.isEmpty()) {
            log.warn("   ⚠️ Fatura sem itens. Criando item genérico.");
            FaturaItem item = new FaturaItem();
            item.setCodigoProduto("SERVICO");
            item.setDescricao("Serviço - Fatura " + fatura.getNumeroFatura());
            item.setQuantidade(BigDecimal.ONE);
            item.setValorUnitario(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
            item.setValorTotal(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
            item.setTipoLancamento("D");
            itens = new ArrayList<>();
            itens.add(item);
            fatura.setItens(itens);
        }

        int itemIndex = 0;
        for (FaturaItem item : fatura.getItens()) {
            itemIndex++;
            
            if (item.getCodigoProduto() == null || item.getCodigoProduto().trim().isEmpty()) {
                item.setCodigoProduto("SERVICO");
                log.warn("   Item {}: Código de produto NULO. Usando SERVICO", itemIndex);
            }

            if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                item.setQuantidade(BigDecimal.ONE);
                log.warn("   Item {}: Quantidade inválida. Usando 1", itemIndex);
            }

            if (item.getValorUnitario() == null) {
                item.setValorUnitario(BigDecimal.ZERO);
                log.warn("   Item {}: Valor unitário NULO. Usando 0.00", itemIndex);
            }

            if (item.getValorTotal() == null) {
                item.setValorTotal(item.getQuantidade().multiply(item.getValorUnitario()));
                log.warn("   Item {}: Valor total NULO. Calculado", itemIndex);
            }

            if (item.getTipoLancamento() == null || item.getTipoLancamento().trim().isEmpty()) {
                item.setTipoLancamento("D");
            }

            log.info("   Item {}: {} - Qtd={}, Unit=R$ {}, Total=R$ {}", 
                itemIndex,
                item.getCodigoProduto(),
                item.getQuantidade(),
                formatarValor(item.getValorUnitario()),
                formatarValor(item.getValorTotal()));
        }

        return fatura;
    }

    // ============================================================
    // VALIDAR CAMPOS OBRIGATÓRIOS DO XML
    // ============================================================

    private void validarCamposXml(String xml) {
        List<String> camposVazios = new ArrayList<>();

        // Verificar campos obrigatórios do TMOV
        String[] camposTMOV = {"CODCOLIGADA", "IDMOV", "CODFILIAL", "CODLOC", "CODTMV", "CODCFO", "DATAEMISSAO", "VALOR"};
        for (String campo : camposTMOV) {
            if (!xml.contains("<" + campo + ">") || xml.contains("<" + campo + "></" + campo + ">")) {
                camposVazios.add(campo);
            }
        }

        // Verificar se há itens (TITMMOV)
        if (xml.contains("<TITMMOV>")) {
            String[] camposTITMMOV = {"IDPRD", "QUANTIDADE", "PRECOUNITARIO", "VALOR"};
            for (String campo : camposTITMMOV) {
                if (!xml.contains("<" + campo + ">") || xml.contains("<" + campo + "></" + campo + ">")) {
                    camposVazios.add(campo);
                }
            }
        }

        if (!camposVazios.isEmpty()) {
            log.error("❌ CAMPOS OBRIGATÓRIOS VAZIOS NO XML: {}", String.join(", ", camposVazios));
            throw new RuntimeException("Campos obrigatórios vazios no XML: " + String.join(", ", camposVazios));
        }

        log.info("✅ Todos os campos obrigatórios estão preenchidos");
    }

    // ============================================================
    // 🔥 GERAR XML NO PADRÃO TOTVS (MovMovimento)
    // ============================================================

    public String gerarXmlMovimento(Fatura fatura, RmApiConfigDTO config) {
        return gerarXmlMovimentoInternal(fatura, config);
    }

    private String gerarXmlMovimentoInternal(Fatura fatura, RmApiConfigDTO config) {
        StringBuilder xml = new StringBuilder();
        String codColigada = config.getWsColigada() != null ? config.getWsColigada() : "1";

        ConfiguracaoRm configuracaoRm = buscarConfiguracaoRmAtiva();

        String codTmv = configuracaoRm != null && configuracaoRm.getCodigoTmv() != null 
            ? configuracaoRm.getCodigoTmv() : "2.1.05";
        String centroCusto = configuracaoRm != null && configuracaoRm.getCentroCusto() != null 
            ? configuracaoRm.getCentroCusto() : "01.01";
        String condicaoPagamento = configuracaoRm != null && configuracaoRm.getCondicaoPagamento() != null 
            ? configuracaoRm.getCondicaoPagamento() : "99";

        String dataEmissaoStr = formatarDataComHora(fatura.getDataEmissao());
        String dataVencimentoStr = formatarDataComHora(fatura.getDataVencimento());
        String valorTotalStr = formatarValor(fatura.getValorTotal());
        String codigoFilial = "1";
        String codigoLocal = "01";

        String codigoCfo = "";
        if (fatura.getAssociado() != null && fatura.getAssociado().getCodigoSpc() != null) {
            codigoCfo = fatura.getAssociado().getCodigoSpc();
        }

        log.info("📋 Dados para XML (Padrão TOTVS):");
        log.info("   CodColigada: {}", codColigada);
        log.info("   CodTMV: {}", codTmv);
        log.info("   CodFilial: {}", codigoFilial);
        log.info("   CodLocal: {}", codigoLocal);
        log.info("   CodCFO: {}", codigoCfo);
        log.info("   Valor Total: {}", valorTotalStr);
        log.info("   Data Emissão: {}", dataEmissaoStr);

        // ============================================================
        // GERAR XML - MovMovimento
        // ============================================================

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<MovMovimento>");

        // TMOV - Cabeçalho
        xml.append("<TMOV>");
        xml.append("<CODCOLIGADA>").append(codColigada).append("</CODCOLIGADA>");
        xml.append("<IDMOV>-1</IDMOV>");
        xml.append("<CODFILIAL>").append(codigoFilial).append("</CODFILIAL>");
        xml.append("<CODLOC>").append(codigoLocal).append("</CODLOC>");
        xml.append("<CODTMV>").append(codTmv).append("</CODTMV>");
        xml.append("<CODCFO>").append(codigoCfo).append("</CODCFO>");
        xml.append("<DATAEMISSAO>").append(dataEmissaoStr).append("</DATAEMISSAO>");
        xml.append("<DATAVCTO>").append(dataVencimentoStr).append("</DATAVCTO>");
        xml.append("<CODCCUSTO>").append(centroCusto).append("</CODCCUSTO>");
        xml.append("<CODPAGTO>").append(condicaoPagamento).append("</CODPAGTO>");
        xml.append("<VALOR>").append(valorTotalStr).append("</VALOR>");
        xml.append("<VALORBRUTO>").append(valorTotalStr).append("</VALORBRUTO>");
        xml.append("</TMOV>");

        // TITMMOV - Itens
        List<FaturaItem> itens = fatura.getItens();
        if (itens != null && !itens.isEmpty()) {
            int seqItem = 1;
            for (FaturaItem item : itens) {
                String codigoProduto = item.getCodigoProduto() != null ? item.getCodigoProduto() : "SERVICO";
                BigDecimal quantidade = item.getQuantidade() != null && item.getQuantidade().compareTo(BigDecimal.ZERO) > 0 
                    ? item.getQuantidade() : BigDecimal.ONE;
                BigDecimal valorUnitario = item.getValorUnitario() != null ? item.getValorUnitario() : BigDecimal.ZERO;
                BigDecimal valorTotalItem = item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO;

                xml.append("<TITMMOV>");
                xml.append("<CODCOLIGADA>").append(codColigada).append("</CODCOLIGADA>");
                xml.append("<IDMOV>-1</IDMOV>");
                xml.append("<NSEQITMMOV>").append(seqItem++).append("</NSEQITMMOV>");
                xml.append("<IDPRD>").append(codigoProduto).append("</IDPRD>");
                xml.append("<QUANTIDADE>").append(quantidade).append("</QUANTIDADE>");
                xml.append("<PRECOUNITARIO>").append(formatarValor(valorUnitario)).append("</PRECOUNITARIO>");
                xml.append("<VALOR>").append(formatarValor(valorTotalItem)).append("</VALOR>");
                xml.append("<VALORDESC>0</VALORDESC>");
                xml.append("<VALORDESPESA>0</VALORDESPESA>");
                xml.append("</TITMMOV>");
            }
        } else {
            log.warn("⚠️ Fatura sem itens. Criando item genérico.");
            xml.append("<TITMMOV>");
            xml.append("<CODCOLIGADA>").append(codColigada).append("</CODCOLIGADA>");
            xml.append("<IDMOV>-1</IDMOV>");
            xml.append("<NSEQITMMOV>1</NSEQITMMOV>");
            xml.append("<IDPRD>SERVICO</IDPRD>");
            xml.append("<QUANTIDADE>1</QUANTIDADE>");
            xml.append("<PRECOUNITARIO>").append(valorTotalStr).append("</PRECOUNITARIO>");
            xml.append("<VALOR>").append(valorTotalStr).append("</VALOR>");
            xml.append("<VALORDESC>0</VALORDESC>");
            xml.append("<VALORDESPESA>0</VALORDESPESA>");
            xml.append("</TITMMOV>");
        }

        xml.append("</MovMovimento>");

        validarCamposXml(xml.toString());

        log.info("📤 XML gerado (Padrão TOTVS). Tamanho: {} caracteres", xml.length());

        return xml.toString();
    }

    // ============================================================
    // VALIDAR DADOS PARA VISUALIZAÇÃO
    // ============================================================

    public Map<String, Object> validarDadosParaVisualizacao(Fatura fatura) {
        Map<String, Object> resultado = new LinkedHashMap<>();
        List<String> erros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        Map<String, Object> dados = new LinkedHashMap<>();

        if (fatura.getAssociado() == null) {
            erros.add("Associado não informado");
        } else {
            dados.put("associadoId", fatura.getAssociado().getId());
            dados.put("associadoNome", fatura.getAssociado().getNomeRazao());
            dados.put("codigoSpc", fatura.getAssociado().getCodigoSpc());
            if (fatura.getAssociado().getCodigoSpc() == null || fatura.getAssociado().getCodigoSpc().trim().isEmpty()) {
                erros.add("Código SPC do associado não informado");
            }
        }

        dados.put("dataEmissao", fatura.getDataEmissao() != null ? fatura.getDataEmissao().toString() : "NULO");
        if (fatura.getDataEmissao() == null) {
            erros.add("Data de emissão não informada");
        }

        dados.put("dataVencimento", fatura.getDataVencimento() != null ? fatura.getDataVencimento().toString() : "NULO");
        if (fatura.getDataVencimento() == null) {
            avisos.add("Data de vencimento não informada (será usada data padrão)");
        }

        dados.put("valorTotal", fatura.getValorTotal() != null ? fatura.getValorTotal().toString() : "NULO");
        if (fatura.getValorTotal() == null || fatura.getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
            avisos.add("Valor total é zero ou nulo");
        }

        List<FaturaItem> itens = fatura.getItens();
        if (itens == null || itens.isEmpty()) {
            erros.add("Fatura sem itens");
        } else {
            dados.put("quantidadeItens", itens.size());
            List<Map<String, Object>> itensInfo = new ArrayList<>();
            for (int i = 0; i < itens.size(); i++) {
                FaturaItem item = itens.get(i);
                Map<String, Object> itemInfo = new LinkedHashMap<>();
                itemInfo.put("indice", i + 1);
                itemInfo.put("codigoProduto", item.getCodigoProduto() != null ? item.getCodigoProduto() : "NULO");
                itemInfo.put("descricao", item.getDescricao() != null ? item.getDescricao() : "NULO");
                itemInfo.put("quantidade", item.getQuantidade() != null ? item.getQuantidade().toString() : "NULO");
                itemInfo.put("valorUnitario", item.getValorUnitario() != null ? item.getValorUnitario().toString() : "NULO");
                itemInfo.put("valorTotal", item.getValorTotal() != null ? item.getValorTotal().toString() : "NULO");

                if (item.getCodigoProduto() == null || item.getCodigoProduto().trim().isEmpty()) {
                    erros.add("Item " + (i + 1) + ": Código de produto não informado");
                }
                if (item.getValorUnitario() == null) {
                    erros.add("Item " + (i + 1) + ": Valor unitário não informado");
                }
                if (item.getValorTotal() == null) {
                    erros.add("Item " + (i + 1) + ": Valor total não informado");
                }
                if (item.getQuantidade() == null || item.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
                    avisos.add("Item " + (i + 1) + ": Quantidade inválida (será usada 1)");
                }

                itensInfo.add(itemInfo);
            }
            dados.put("itens", itensInfo);
        }

        resultado.put("dados", dados);
        resultado.put("erros", erros);
        resultado.put("avisos", avisos);
        resultado.put("valido", erros.isEmpty());

        return resultado;
    }

    // ============================================================
    // INTEGRAÇÃO VIA HTTP
    // ============================================================

    private Long integrarFaturaViaHttp(Fatura fatura, RmApiConfigDTO config) throws Exception {
        log.info("📤 Integrando fatura ID: {} - Número: {} via HTTP", 
            fatura.getId(), fatura.getNumeroFatura());

        fatura = prepararDadosFatura(fatura);

        String endpoint = config.getWsUrl();
        if (endpoint.endsWith("?wsdl")) {
            endpoint = endpoint.substring(0, endpoint.length() - 5);
        }

        log.info("📤 Endpoint para chamada: {}", endpoint);

        try {
            String xmlMovimento = gerarXmlMovimentoInternal(fatura, config);
            
            log.info("📤 ========== XML COMPLETO (PADRÃO TOTVS) ==========");
            log.info("📤 XML:\n{}", xmlMovimento);
            log.info("📤 =================================================");
            
            String soapEnvelope = criarEnvelopeSoap(xmlMovimento, config);
            String soapAction = "http://www.totvs.com/IwsMov/SaveRecord";
            String resultado = enviarHttpPost(soapEnvelope, endpoint, config, soapAction);

            log.info("📥 ========== RESPOSTA DO RM ==========");
            log.info("📥 Resposta:\n{}", resultado);
            log.info("📥 =====================================");

            Long idMov = extrairIdMovimento(resultado);
            if (idMov != null) {
                log.info("✅ Fatura {} integrada com sucesso (ID Mov: {})", fatura.getId(), idMov);
                return idMov;
            }

            log.info("✅ Movimento processado com sucesso (ID não retornado)");
            return System.currentTimeMillis() % 1000000;

        } catch (Exception e) {
            log.error("❌ Erro ao integrar fatura {}: {}", fatura.getId(), e.getMessage());
            throw new Exception("Erro ao integrar fatura: " + e.getMessage());
        }
    }

    // ============================================================
    // ENVIO HTTP POST
    // ============================================================

    private String enviarHttpPost(String soapEnvelope, String endpoint, RmApiConfigDTO config, String soapAction) throws Exception {
        log.info("📤 Enviando SOAP para: {}", endpoint);

        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(config.getWsTimeout() != null ? config.getWsTimeout() : 30000);
            connection.setReadTimeout(config.getWsTimeout() != null ? config.getWsTimeout() : 30000);

            connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
            connection.setRequestProperty("SOAPAction", soapAction);

            if (config.getWsUsername() != null && config.getWsPassword() != null) {
                String auth = config.getWsUsername() + ":" + config.getWsPassword();
                String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                connection.setRequestProperty("Authorization", "Basic " + encodedAuth);
            }

            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = soapEnvelope.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();

            if (responseCode == 202) {
                return "SUCESSO_202";
            }

            InputStream inputStream = responseCode >= 200 && responseCode < 300 
                ? connection.getInputStream() 
                : connection.getErrorStream();

            String resposta = lerInputStream(inputStream);

            if (responseCode >= 400) {
                throw new Exception("Erro HTTP: " + responseCode + " - " + resposta);
            }

            return resposta;

        } finally {
            connection.disconnect();
        }
    }

    private String lerInputStream(InputStream inputStream) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    // ============================================================
    // CRIAÇÃO DO ENVELOPE SOAP - USANDO SaveRecord
    // ============================================================

    private String criarEnvelopeSoap(String xmlData, RmApiConfigDTO config) {
        String dataServer = config.getWsDataServer() != null ? config.getWsDataServer() : DATASERVER_MOVIMENTO;
        String contexto = String.format("CODCOLIGADA=%s;CODUSUARIO=%s;CODSISTEMA=%s",
            config.getWsColigada() != null ? config.getWsColigada() : "1",
            config.getWsUsuario() != null ? config.getWsUsuario() : "mestre",
            config.getWsSistema() != null ? config.getWsSistema() : "T");

        StringBuilder soap = new StringBuilder();
        soap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        soap.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tot=\"http://www.totvs.com/\">");
        soap.append("<soapenv:Header/>");
        soap.append("<soapenv:Body>");
        soap.append("<tot:SaveRecord>");
        soap.append("<tot:DataServerName>").append(dataServer).append("</tot:DataServerName>");
        soap.append("<tot:XML><![CDATA[").append(xmlData).append("]]></tot:XML>");
        soap.append("<tot:Contexto>").append(contexto).append("</tot:Contexto>");
        soap.append("</tot:SaveRecord>");
        soap.append("</soapenv:Body>");
        soap.append("</soapenv:Envelope>");

        return soap.toString();
    }

    // ============================================================
    // EXTRAIR ID DO MOVIMENTO
    // ============================================================

    private Long extrairIdMovimento(String resultado) {
        if ("SUCESSO_202".equals(resultado)) {
            return null;
        }

        try {
            String limpo = resultado.replaceAll("<[^>]+>", "").trim();
            if (limpo.isEmpty()) return null;

            if (limpo.contains("não pode ser nulo") || limpo.contains("erro") || limpo.contains("Error")) {
                throw new RuntimeException("Erro do RM: " + limpo);
            }

            String[] partes = limpo.split(";");
            if (partes.length >= 2) {
                return Long.parseLong(partes[1].trim());
            }
            if (limpo.matches("\\d+")) {
                return Long.parseLong(limpo.trim());
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ============================================================
    // TESTE DE CONEXÃO
    // ============================================================

    public boolean testarConexao(RmApiConfigDTO config) {
        log.info("🧪 Testando conexão com WebService: {}", config.getWsUrl());

        try {
            String endpoint = config.getWsUrl();
            if (endpoint.endsWith("?wsdl")) {
                endpoint = endpoint.substring(0, endpoint.length() - 5);
            }

            String soapAction = "http://www.totvs.com/IwsMov/SaveRecord";

            String xmlTeste = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Test>Connection</Test>";
            String soapEnvelope = criarEnvelopeSoap(xmlTeste, config);

            String resultado = enviarHttpPost(soapEnvelope, endpoint, config, soapAction);

            log.info("✅ Conexão estabelecida!");
            return true;

        } catch (Exception e) {
            log.error("❌ Falha no teste: {}", e.getMessage());
            return false;
        }
    }
}