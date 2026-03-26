package com.sga.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sga.model.Associado;
import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.Produto;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.ImportacaoSPCRepository;
import com.sga.repository.ProdutoRepository;

@Service
public class RmExportService {

    private static final Logger logger = LoggerFactory.getLogger(RmExportService.class);

    @Autowired
    private ImportacaoSPCRepository importacaoSPCRepository;
    
    @Autowired
    private AssociadoRepository associadoRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;

    // Formatadores
    private final DecimalFormat valorFormat = new DecimalFormat("#0.00", 
        new DecimalFormatSymbols(Locale.US));
    private final NumberFormat quantidadeFormat = NumberFormat.getInstance(Locale.US);
    
    // Controle sequencial para números de RPS
    private int ultimoNumeroRPS = 0;
    
    private static final String DATA_EMISSAO_PADRAO = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    
    {
        quantidadeFormat.setMinimumFractionDigits(2);
        quantidadeFormat.setMaximumFractionDigits(2);
        quantidadeFormat.setGroupingUsed(false);
    }

    /**
     * Exporta notas no formato RM (layout fixo)
     */
    public byte[] exportarParaRm(Long importacaoId) {
        logger.info("📤 Exportando dados para RM - Importação ID: {}", importacaoId);
        
        ultimoNumeroRPS = buscarUltimoNumeroRPS();

        try {
            ImportacaoSPC importacao = importacaoSPCRepository.findById(importacaoId)
                    .orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(baos);

            int sequencialNota = 1;

            if (importacao.getNotasDebito() != null) {
                for (NotaDebitoSPC nota : importacao.getNotasDebito()) {
                    writer.println(gerarLinhaM(nota, sequencialNota++));
                    
                    if (nota.getItens() != null) {
                        int itemSeq = 1;
                        for (ItemSPC item : nota.getItens()) {
                            writer.println(gerarLinhaI(nota, item, itemSeq++));
                        }
                    }
                }
            }

            writer.flush();
            byte[] resultado = baos.toByteArray();
            logger.info("✅ Arquivo RM gerado com sucesso! Tamanho: {} bytes", resultado.length);
            return resultado;

        } catch (Exception e) {
            logger.error("❌ Erro ao exportar para RM: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao exportar para RM: " + e.getMessage());
        }
    }

    private int buscarUltimoNumeroRPS() {
        try {
            return 1000;
        } catch (Exception e) {
            logger.warn("Erro ao buscar último número RPS: {}", e.getMessage());
            return 1000;
        }
    }

    private String obterProximoNumeroRPS() {
        ultimoNumeroRPS++;
        return String.format("%06d", ultimoNumeroRPS);
    }

    private String buscarCodigoRmAssociado(String codigoSpc) {
        try {
            if (codigoSpc == null || codigoSpc.trim().isEmpty()) return "";
            return associadoRepository.findByCodigoSpc(codigoSpc)
                    .map(Associado::getCodigoRm)
                    .orElse(codigoSpc);
        } catch (Exception e) {
            logger.warn("Erro ao buscar código RM do associado {}: {}", codigoSpc, e.getMessage());
            return codigoSpc;
        }
    }

    private String buscarCodigoRmProduto(String descricaoServico) {
        try {
            if (descricaoServico == null || descricaoServico.trim().isEmpty()) return "";
            
            Optional<Produto> produto = produtoRepository.findByDescricao(descricaoServico);
            
            if (produto.isPresent()) {
                String codigoRm = produto.get().getCodigoRm();
                if (codigoRm != null && !codigoRm.trim().isEmpty()) {
                    return codigoRm;
                }
            }
            return descricaoServico;
            
        } catch (Exception e) {
            logger.warn("Erro ao buscar código RM do produto '{}': {}", descricaoServico, e.getMessage());
            return descricaoServico;
        }
    }

    /**
     * 🔥 MÉTODO AUXILIAR: Preenche campo alfanumérico (espaços à direita)
     */
    private void appendAlfa(StringBuilder sb, String valor, int tamanho) {
        if (valor == null) valor = "";
        if (valor.length() > tamanho) {
            valor = valor.substring(0, tamanho);
        }
        sb.append(valor);
        for (int i = valor.length(); i < tamanho; i++) {
            sb.append(' ');
        }
    }

    /**
     * 🔥 MÉTODO AUXILIAR: Preenche campo numérico (zeros à esquerda)
     */
    private void appendNumero(StringBuilder sb, String valor, int tamanho) {
        if (valor == null) valor = "0";
        
        // Remover pontos e vírgulas para contar apenas dígitos
        String numeros = valor.replaceAll("[^0-9]", "");
        
        if (numeros.length() > tamanho) {
            numeros = numeros.substring(numeros.length() - tamanho);
        }
        
        // Preencher com zeros à esquerda
        for (int i = numeros.length(); i < tamanho; i++) {
            sb.append('0');
        }
        sb.append(numeros);
    }

    /**
     * 🔥 MÉTODO AUXILIAR: Preenche campo numérico (zeros à esquerda) - Versão corrigida
     */
    private void appendNumeroZero(StringBuilder sb, String valor, int tamanho) {
        if (valor == null) valor = "0";
        
        // Remover todos os caracteres não numéricos
        String numeros = valor.replaceAll("[^0-9]", "");
        
        // Se ficou vazio, usar "0"
        if (numeros.isEmpty()) {
            numeros = "0";
        }
        
        // Truncar se maior que o tamanho (pegar os últimos dígitos)
        if (numeros.length() > tamanho) {
            numeros = numeros.substring(numeros.length() - tamanho);
        }
        
        // Preencher com zeros à esquerda
        for (int i = numeros.length(); i < tamanho; i++) {
            sb.append('0');
        }
        sb.append(numeros);
    }    
    
    /**
     * 🔥 CORRIGIDO: Preenche campo valor monetário (alinhado à direita com zeros, incluindo ponto decimal)
     * Formato: 00000000000001128.00 (20 caracteres: 15 inteiros + 1 ponto + 2 decimais = 18, mas vamos usar 20)
     */
    private void appendValorMonetario(StringBuilder sb, BigDecimal valor, int tamanhoTotal) {
        if (valor == null) valor = BigDecimal.ZERO;
        
        // Formatar com 2 casas decimais e ponto decimal
        String valorStr = String.format("%.2f", valor);
        
        // Substituir vírgula por ponto (garantir ponto decimal)
        valorStr = valorStr.replace(',', '.');
        
        // Separar parte inteira e decimal
        String[] partes = valorStr.split("\\.");
        String parteInteira = partes[0];
        String parteDecimal = partes.length > 1 ? partes[1] : "00";
        
        // Garantir 2 dígitos decimais
        if (parteDecimal.length() > 2) {
            parteDecimal = parteDecimal.substring(0, 2);
        } else if (parteDecimal.length() < 2) {
            parteDecimal = String.format("%-2s", parteDecimal).replace(' ', '0');
        }
        
        // Parte inteira com zeros à esquerda (tamanho total - 3 (ponto + 2 decimais))
        int tamanhoInteiro = tamanhoTotal - 3;
        String inteiroFormatado = String.format("%" + tamanhoInteiro + "s", parteInteira).replace(' ', '0');
        
        // Montar o valor completo: inteiro + ponto + decimal
        String valorFormatado = inteiroFormatado + "." + parteDecimal;
        
        logger.debug("Valor original: {}, formatado: {}", valor, valorFormatado);
        
        sb.append(valorFormatado);
    }

    /**
     * Gera linha tipo M (Movimento/Cabeçalho)
     */
    private String gerarLinhaM(NotaDebitoSPC nota, int sequencial) {
        
    	StringBuilder sb = new StringBuilder(2134);
        
        // Marca (pos 1-1) - Alfanumérico
        appendAlfa(sb, "M", 1);
        
        // CodFilial (pos 2-6) - Numérico
        appendNumero(sb, "1", 5);
        
        // CodDepar (pos 7-36) - Alfanumérico
        appendAlfa(sb, "1", 30);
        
        // Codloc (pos 37-51) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // CodlocEnt (pos 52-66) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // CodlocDest (pos 67-81) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // Codcfo (pos 82-106) - Alfanumérico
        String codigoRmAssociado = buscarCodigoRmAssociado(nota.getCodigoSocio());
        appendAlfa(sb, codigoRmAssociado, 25);
        
        // CodcfoNat (pos 107-131) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // 🔥 CORRIGIDO: NumeroMov (pos 132-166) - ALFANUMÉRICO (com espaços à direita)
        String numeroRPS = obterProximoNumeroRPS();
        appendAlfa(sb, numeroRPS, 35);
        
        // Serie (pos 167-174) - Alfanumérico
        appendAlfa(sb, "RPS", 8);
        
        // CodTmv (pos 175-184) - Alfanumérico
        appendAlfa(sb, "2.1.05", 10);
        
        // Tipo (pos 185-185) - Alfanumérico
        appendAlfa(sb, "S", 1);
        
        // Status (pos 186-186) - Alfanumérico
        appendAlfa(sb, "A", 1);
        
        // MovImpresso (pos 187-187) - Numérico
        appendNumero(sb, "0", 1);
        
        // DocImpresso (pos 188-188) - Numérico
        appendNumero(sb, "0", 1);
        
        // FatImpressa (pos 189-189) - Numérico
        appendNumero(sb, "0", 1);
        
        // DataEmissao (pos 190-199) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // DataSaida (pos 200-209) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // DataExtra1 (pos 210-219) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // DataExtra2 (pos 220-229) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // Codrpr (pos 230-244) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // ComissaoRepres (pos 245-264) - Numérico
        appendAlfa(sb, "0.00", 20);
        
        // Nordem (pos 265-284) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // Codcpg (pos 285-289) - Alfanumérico
        appendAlfa(sb, "99", 5);
        
        // NumeroTributos (pos 290-294) - Numérico
        appendNumero(sb, "0", 5);
        
        // ValorBruto (pos 295-314) - Numérico (monetário)
        appendValorMonetario(sb, nota.getValorNota(), 20);
        
        // ValorLiquido (pos 315-334) - Numérico (monetário)
        appendValorMonetario(sb, nota.getValorNota(), 20);
        
        // ValorOutros (pos 335-354) - Numérico
        appendValorMonetario(sb, nota.getValorNota(), 20);
        
        // Observacao (pos 355-414) - Alfanumérico
        appendAlfa(sb, "", 60);
        
       BigDecimal PercentualFrete = BigDecimal.valueOf(0);
        // PercentualFrete (pos 415-434) - Numérico
       appendValorMonetario(sb, PercentualFrete, 20);
        
       BigDecimal ValorFrete = BigDecimal.valueOf(0);
       // ValorFrete (pos 435-454) - Numérico
       appendValorMonetario(sb, ValorFrete, 20);
        
       BigDecimal PercentualSeguro = BigDecimal.valueOf(0);  
       // PercentualSeguro (pos 455-474) - Numérico
       appendValorMonetario(sb, PercentualSeguro, 20);
        
        BigDecimal ValorSeguro = BigDecimal.valueOf(0);
       // ValorSeguro (pos 475-494) - Numérico
        appendValorMonetario(sb, ValorSeguro, 20);
        
        BigDecimal PercentualDesc = BigDecimal.valueOf(0);
        // PercentualDesc (pos 495-514) - Numérico
        appendValorMonetario(sb, PercentualDesc, 20);
        
        BigDecimal ValorDesc = BigDecimal.valueOf(0);
        // ValorDesc (pos 515-534) - Numérico
        appendValorMonetario(sb, ValorDesc, 20);
        
        BigDecimal PercentualDesp = BigDecimal.valueOf(0);
        // PercentualDesp (pos 535-554) - Numérico
        appendValorMonetario(sb, PercentualDesp, 20);
        
        // ValorDesp (pos 555-574) - Numérico
        BigDecimal ValorDesp = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorDesp, 20);
        
        // PercentualExtra1 (pos 575-594) - Numérico
        BigDecimal PercentualExtra1 = BigDecimal.valueOf(0);
        appendValorMonetario(sb, PercentualExtra1, 20);
        
        // ValorExtra1 (pos 595-614) - Numérico
        BigDecimal ValorExtra1 = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorExtra1, 20);
        
        // PercentualExtra2 (pos 615-634) - Numérico
        BigDecimal PercentualExtra2 = BigDecimal.valueOf(0);
        appendValorMonetario(sb, PercentualExtra2, 20);
        
        // ValorExtra2 (pos 635-654) - Numérico
        BigDecimal ValorExtra2 = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorExtra2, 20);
        
        // CodVen1 (pos 655-670) - Alfanumérico
        appendAlfa(sb, "", 16);
        
        // Perccomissao (pos 671-690) - Numérico
        BigDecimal Perccomissao = BigDecimal.valueOf(0);
        appendValorMonetario(sb, Perccomissao, 20);
        
        // CodMen (pos 691-695) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // CodMen2 (pos 696-700) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // ViaDeTransporte (pos 701-715) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // Placa (pos 716-725) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // CodetdPlaca (pos 726-727) - Alfanumérico
        appendAlfa(sb, "", 2);
        
        // PesoLiquido (pos 728-747) - Numérico
        appendAlfa(sb, "", 20);
        
        // PesoBruto (pos 748-767) - Numérico
        appendAlfa(sb, "", 20);
        
        // Marca (pos 768-777) - Numérico
        appendAlfa(sb, "", 10);
        
        // Numero (pos 778-787) - Numérico
        appendAlfa(sb, "0", 10);
        
        // Quantidade (pos 788-797) - Numérico
        appendAlfa(sb, "0", 10);
        
        // Especie (pos 798-812) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // Codtb1fat (pos 813-822) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb2fat (pos 823-832) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb3fat (pos 833-842) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb4fat (pos 843-852) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb5fat (pos 853-862) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb1flx (pos 863-887) - Alfanumérico - Código do Serviço.
        appendAlfa(sb, "0001", 25);
        
        // Codtb2flx (pos 888-912) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb3flx (pos 913-937) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb4flx (pos 938-962) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb5flx (pos 963-987) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // CampoLivre1 (pos 988-1087) - Alfanumérico
        appendAlfa(sb, "", 100);
        
        // CampoLivre2 (pos 1088-1187) - Alfanumérico
        appendAlfa(sb, "", 100);
        
        // CampoLivre3 (pos 1188-1287) - Alfanumérico
        appendAlfa(sb, "", 100);
        
        // CodmoeValorLiquido (pos 1288-1297) - Alfanumérico
        appendAlfa(sb, "R$", 10);
        
        // DataBaseMov (pos 1298-1307) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // DataMovimento (pos 1308-1317) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // NumeroLctGerado (pos 1318-1322) - Numérico
        appendNumero(sb, "1", 5);
        
        // GerouFatura (pos 1323-1327) - Alfanumérico
        appendNumero(sb, "0", 5);
        
        // NumeroLctAberto (pos 1328-1332) - Numérico
        appendNumero(sb, "1", 5);
        
        // CodVen2 (pos 1333-1348) - Alfanumérico
        appendAlfa(sb, "", 16);
        
        // CodUsuario (pos 1349-1368) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // FlagExportacao (pos 1369-1373) - Numérico
        appendNumero(sb, "0", 5);
        
        // EmiteBoleta (pos 1374-1374) - Alfanumérico
        appendAlfa(sb, "", 1);
        
        // CodMenDesconto (pos 1375-1379) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // CodmenDespesa (pos 1380-1384) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // CodmenFrete (pos 1385-1389) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // FreteCifoufob (pos 1390-1394) - Numérico
        appendAlfa(sb, "0", 5);
        
        // UsaDespfinanc (pos 1395-1399) - Numérico
        appendNumero(sb, "0", 5);
        
        // IdNat (pos 1400-1404) - Numérico
        appendNumero(sb, "9", 5);
        
        // FlagExporFisc (pos 1405-1409) - Numérico
        appendNumero(sb, "0", 5);
        
        // FlagExporFazenda (pos 1410-1414) - Numérico
        appendNumero(sb, "0", 5);
        
        // CodVen3 (pos 1415-1430) - Alfanumérico
        appendAlfa(sb, "", 16);
        
        // CodVen4 (pos 1431-1446) - Alfanumérico
        appendAlfa(sb, "", 16);
        
        // PercComissaoVenc2 (pos 1447-1466) - Numérico
        appendNumero(sb, "0", 20);
        
        // IdNat2 (pos 1467-1468) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // ValorAdiantamento (pos 1472-1492) - Numérico
        appendNumero(sb, "0", 20);
        
        // CodTra (pos 1492-1497) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // CodTra2 (pos 1497-1502) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // StatusLiberacao (pos 1503-1508) - Numérico
        appendNumero(sb, "0", 5);
        
        // ItensAgrupados (pos 1504-1508) - Numérico
        appendNumero(sb, "0", 5);
        
        // FlagImpressaoFat (pos 1509-1513) - Numérico
        appendAlfa(sb, "", 1);
        
        // DataCancelamentoMov (pos 1514-1523) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // SegundoNumero (pos 1524-1543) - Alfanumérico
        appendNumeroZero(sb, "0", 20);
        
        // CodCcusto (pos 1544-1568) - Alfanumérico
        String CodCcusto = "01.01";
        appendAlfa(sb, CodCcusto, 25);
        
        // CodColCxa (pos 1569-1573) - Numérico
        appendAlfa(sb, "1", 5);
        
        // CodCxa (pos 1574-1583) - Numérico
        appendAlfa(sb, "78", 10);
        
        // CodFilialDestino (pos 1584-1588) - Numérico
        appendAlfa(sb, "1", 5);
        
        // CodCustoDestino (pos 1589-1613) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // CodColCFO (pos 1614-1618) - Numérico
        appendAlfa(sb, "1", 5);
        
        // CodColCFONatureza (pos 1619-1623) - Numérico
        appendAlfa(sb, "1", 5);
        
        // IdMovLctFluxs (pos 1624-1633) - Numérico
        appendAlfa(sb, "0", 10);
        
        // DataEntrega (pos 1634-1643) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // NumeroCaixa (pos 1644-1653) - Numérico
        appendNumero(sb, "0", 10);
        
        // NumeroCupom (pos 1654-1663) - Numérico
        appendNumero(sb, "0", 10);
        
        // HorarioEmissao (pos 1664-1683) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // CodFilialEntrega (pos 1684-1688) - Numérico
        appendAlfa(sb, "", 5);
        
        // CodNat (pos 1689-1708) - Alfanumérico
        appendAlfa(sb, "5.949.01", 20);
        
        // CodNat2 (pos 1709-1728) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // CgcCFO (pos 1729-1748) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // DataContabilizacao (pos 1749-1758) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // CodigoServico (pos 1759-1773) - Alfanumérico
        appendAlfa(sb, "17.21/829110001", 15);
        
        // CodMunServico (pos 1774-1793) - Alfanumérico
        appendAlfa(sb, "04400", 20);
        
        // CodEtdMunServ (pos 1794-1795) - Alfanumérico
        appendAlfa(sb, "CE", 2);
        
        // PontoVenda (pos 1796-1805) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // CodEvento (pos 1806-1815) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // CodTdo (pos 1816-1825) - Alfanumérico
        appendAlfa(sb, "39", 10);
        
        // CodigoIRRF (pos 1826-1835) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // DeducaoIRRF (pos 1836-1855) - Numérico
        appendNumero(sb, "0", 20);
        
        // PercBaseInssEmpregado (pos 1856-1875) - Numérico
        appendNumero(sb, "0", 20);
        
        // PercentBaseInss (pos 1876-1895) - Numérico
        appendNumero(sb, "0", 20);
        
        // CodPrj (pos 1896-1919) - Alfanumérico
        appendAlfa(sb, "", 24);
        
        // Revisao (pos 1920-1924) - Numérico
        appendNumero(sb, "0", 5);
        
        // SubSerie (pos 1925-1932) - Alfanumérico
        appendAlfa(sb, "", 8);
        
        // IdContatoEntrega (pos 1933-1942) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // InnsEmOutraEmpresa (pos 1943-1962) - Numérico
        appendNumero(sb, "0", 20);
        
        // Apropriado (pos 1963-1967) - Alfanumérico
        appendAlfa(sb, "0", 5);
        
        // DataLancamento (pos 1968-1977) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // Cro (pos 1978-1987) - Alfanumérico
        appendAlfa(sb, "0", 10);
        
        // Coo (pos 1988-1997) - Alfanumérico
        appendAlfa(sb, "0", 10);
        
        // NumeroReducao (pos 1998-2007) - Numérico
        appendNumero(sb, "0", 10);
        
        // HoraSaida (pos 2008-2027) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // CodEntrega (pos 2028-2042) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // RecebidoNfStatus (pos 2043-2043) - Alfanumérico
        appendAlfa(sb, "", 1);
        
        // ReciboNfTipo (pos 2044-2048) - Alfanumérico
        appendAlfa(sb, "0", 5);
        
        // ReciboNfSituacao (pos 2049-2053) - Alfanumérico
        appendAlfa(sb, "0", 5);
        
        // NroAidf (pos 2054-2083) - Alfanumérico
        appendAlfa(sb, "3684", 30);
        
        // ChaveAcessoNFE (pos 2084-2132) - Alfanumérico
        appendAlfa(sb, "", 49);
        
        return sb.toString();
        
    }

    /**
     * Gera linha tipo I (Item)
     */
    private String gerarLinhaI(NotaDebitoSPC nota, ItemSPC item, int sequencial) {
    	
        StringBuilder sb = new StringBuilder(988);
        
        // Marca (pos 1-1) - Alfanumérico
        appendAlfa(sb, "I", 1);
        
        // CodigoPrd (pos 2-31) - Alfanumérico
        String codigoRmProduto = buscarCodigoRmProduto(item.getDescricaoServico());
        appendAlfa(sb, codigoRmProduto, 30);
        
        // NumeroSequencial (pos 32-36) - Numérico
        String seqStr = String.format("%05d", sequencial);
        appendNumero(sb, seqStr, 5);
        
        // CodTip (pos 37-46) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Quantidade (pos 47-66) - Numérico (com 2 decimais)
        //double qtd = item.getQuantidadeServicos() != null ? item.getQuantidadeServicos().doubleValue() : 1.0;
        //String qtdStr = quantidadeFormat.format(qtd).replace(".", "");
        appendQuantidade(sb, item.getQuantidadeServicos().intValue(), 20);
        
        // PrecoUnitario (pos 67-86) - Numérico (monetário)
        appendValorMonetario(sb, item.getValorUnitario(), 20);
        
        // PrecoTabela (pos 87-106) - Numérico
        BigDecimal PrecoTabela = BigDecimal.valueOf(0);
        appendValorMonetario(sb, PrecoTabela, 20);
        
        // PercentualDesc (pos 107-126) - Numérico
        BigDecimal PercentualDesc = BigDecimal.valueOf(0);
        appendValorMonetario(sb, PercentualDesc, 20);
        
        // ValorDesc (pos 127-146) - Numérico
        BigDecimal ValorDesc = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorDesc, 20);
        
        // PercentualDesp (pos 147-166) - Numérico
        BigDecimal PercentualDesp = BigDecimal.valueOf(0);
        appendValorMonetario(sb, PercentualDesp, 20);
        
        // ValorDesp (pos 167-186) - Numérico
        BigDecimal ValorDesp = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorDesp, 20);
        
        // DataEmissao (pos 187-196) - Alfanumérico
        //appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        appendAlfa(sb, "", 10);
        
        // CodMen (pos 197-201) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // NumeroTributos (pos 202-206) - Numérico
        appendNumero(sb, "0", 5);
        
        // Codtb1fat (pos 207-216) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb2fat (pos 217-226) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb3fat (pos 227-236) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb4fat (pos 237-246) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb5fat (pos 247-256) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // Codtb1op (pos 257-281) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb2op (pos 282-306) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb3op (pos 307-331) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb4op (pos 332-356) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Codtb5op (pos 357-381) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // Campolivre (pos 382-396) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // Codund (pos 397-401) - Alfanumérico
        appendAlfa(sb, "UN", 5);
        
        // Quantidadeareceber (pos 402-421) - Numérico
        BigDecimal Quantidadeareceber = BigDecimal.valueOf(0);
        appendValorMonetario(sb, Quantidadeareceber, 20);
        
        // IdNat (pos 422-426) - Alfanumérico
        appendAlfa(sb, "", 5);
        
        // CodCpg (pos 427-431) - Alfanumérico
        appendAlfa(sb, "99", 5);
        
        // DataEntrega (pos 432-441) - Alfanumérico
        appendAlfa(sb, DATA_EMISSAO_PADRAO, 10);
        
        // Prateleira (pos 442-456) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // IdCnt (pos 457-466) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // NseqItmcnt (pos 467-471) - Numérico
        appendNumero(sb, "0", 5);
        
        // Valortotalitem (pos 472-491) - Numérico (monetário)
        appendValorMonetario(sb, item.getValorTotal(), 20);
        
        // Codccusto (pos 492-516) - Alfanumérico
        appendAlfa(sb, "01.01", 25);
        
        // Codnat (pos 517-536) - Alfanumérico
        appendAlfa(sb, "", 20);
        
        // Codrpr (pos 537-551) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // Idprdcomposto (pos 552-581) - Alfanumérico
        appendAlfa(sb, "", 30);
        
        // CodDepartamento (pos 582-606) - Alfanumérico
        appendAlfa(sb, "", 25);
        
        // TributacaoEcf (pos 607-616) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // ValorEscrituracao (pos 617-636) - Numérico
        BigDecimal ValorEscrituracao = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorEscrituracao, 20);
        
        // ValorCodigoPrd (pos 637-696) - Alfanumérico
        appendAlfa(sb, "", 60);
        
        // TipoCodigoPrd (pos 697-701) - Numérico
        appendNumero(sb, "0", 5);
        
        // Cst (pos 702-704) - Alfanumérico
        appendAlfa(sb, "", 3);
        
        // CodTrf (pos 705-764) - Alfanumérico
        appendAlfa(sb, "", 60);
        
        // CodTboOrcamento (pos 765-774) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // ValorOpfmr1 (pos 775-794) - Numérico
        BigDecimal ValorOpfmr1 = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorOpfmr1, 20);
        
        // ValorOpfmr2 (pos 795-814) - Numérico
        BigDecimal ValorOpfmr2 = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorOpfmr2, 20);
        
        // Cancelado (pos 815-815) - Alfanumérico
        appendAlfa(sb, "", 1);
        
        // ValorUntOrcamento (pos 816-835) - Numérico
        BigDecimal ValorUntOrcamento = BigDecimal.valueOf(0);
        appendNumero(sb, "0", 20);
        
        // DataOrcamento (pos 836-845) - Alfanumérico
        appendAlfa(sb, "", 10);
        
        // ComissaoRepres (pos 846-865) - Numérico
        BigDecimal ComissaoRepres = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ComissaoRepres, 20);
        
        // QtdeUndPedido (pos 866-885) - Numérico
        BigDecimal QtdeUndPedido = BigDecimal.valueOf(0);
        appendValorMonetario(sb, QtdeUndPedido, 20);
        
        // CodFilial (pos 886-890) - Numérico
        appendAlfa(sb, "0", 5);
        
        // CodLoc (pos 891-905) - Alfanumérico
        appendAlfa(sb, "", 15);
        
        // ValorLiquido (pos 906-925) - Numérico
        BigDecimal ValorLiquido = BigDecimal.valueOf(0);
        appendValorMonetario(sb, ValorLiquido, 20);
        
        // CampoNovo (pos 926-961) - Alfanumérico
        appendAlfa(sb, "", 36);
        
        // CampoNovo2 (pos 962-986) - Alfanumérico
        appendAlfa(sb, "04400", 25);
        
        // CampoNovo3 (pos 987-988) - Alfanumérico
        appendAlfa(sb, "CE", 2);
        
        return sb.toString();
    }
    
    /**
     * 🔥 MÉTODO AUXILIAR: Formata quantidade com 4 casas decimais
     * Aceita qualquer tipo numérico (Integer, Double, BigDecimal, etc.)
     */
    private void appendQuantidade(StringBuilder sb, Number quantidade, int tamanhoTotal) {
        BigDecimal qtdDecimal;
        if (quantidade == null) {
            qtdDecimal = BigDecimal.ONE;
        } else if (quantidade instanceof BigDecimal) {
            qtdDecimal = (BigDecimal) quantidade;
        } else {
            qtdDecimal = BigDecimal.valueOf(quantidade.doubleValue());
        }
        
        // Formatar com 4 casas decimais
        DecimalFormat qtdFormat = new DecimalFormat("#0.0000");
        qtdFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
        String qtdStr = qtdFormat.format(qtdDecimal);
        
        // Separar parte inteira e decimal
        String[] partes = qtdStr.split("\\.");
        String parteInteira = partes[0];
        String parteDecimal = partes.length > 1 ? partes[1] : "0000";
        
        // Garantir 4 dígitos decimais
        if (parteDecimal.length() > 4) {
            parteDecimal = parteDecimal.substring(0, 4);
        } else if (parteDecimal.length() < 4) {
            parteDecimal = String.format("%-4s", parteDecimal).replace(' ', '0');
        }
        
        // Calcular tamanho da parte inteira (total - 1 ponto - 4 decimais)
        int tamanhoInteiro = tamanhoTotal - 5;
        
        // Parte inteira com zeros à esquerda
        String inteiroFormatado = String.format("%" + tamanhoInteiro + "s", parteInteira).replace(' ', '0');
        
        // Montar o valor completo: inteiro + ponto + decimal
        String valorFormatado = inteiroFormatado + "." + parteDecimal;
        
        sb.append(valorFormatado);
    }

    /**
     * Formata data do formato DDMMYYYY para DD/MM/YYYY
     */
    private String formatarData(String dataStr) {
        if (dataStr == null || dataStr.length() != 8) return DATA_EMISSAO_PADRAO;
        try {
            return dataStr.substring(0, 2) + "/" + 
                   dataStr.substring(2, 4) + "/" + 
                   dataStr.substring(4, 8);
        } catch (Exception e) {
            return DATA_EMISSAO_PADRAO;
        }
    }
}