package com.sga.service;

import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.FaturaItemRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.NotaDebitoSPCRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoSPCRepository;

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private FaturaItemRepository faturaItemRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    /**
     * Gera PDF para uma nota específica usando template HTML
     */
    public byte[] gerarPdfNota(Long notaId) {
        logger.info("📄 Gerando PDF para nota ID: {}", notaId);

        try {
            NotaDebitoSPC nota = notaDebitoSPCRepository.findById(notaId)
                    .orElseThrow(() -> new RuntimeException("Nota não encontrada: " + notaId));

            Context context = new Context();
            
            String dataVencimentoFormatada = formatarData(nota.getDataVencimento());
            
            context.setVariable("nota", nota);
            context.setVariable("itens", nota.getItens());
            context.setVariable("dataVencimentoFormatada", dataVencimentoFormatada);

            String htmlContent = templateEngine.process("nota-pdf", context);
            return convertHtmlToPdf(htmlContent);

        } catch (Exception e) {
            logger.error("❌ Erro ao gerar PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF da nota: " + e.getMessage());
        }
    }

    /**
     * Gera PDF resumo da importação (capa de lote)
     */
    public byte[] gerarPdfResumoImportacao(Long importacaoId, Map<String, Object> resumo) {
        logger.info("📄 Gerando PDF resumo para importação ID: {}", importacaoId);

        try {
            Context context = new Context();
            context.setVariable("importacaoId", importacaoId);
            context.setVariable("resumo", resumo);
            context.setVariable("dataGeracao", java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            String htmlContent = templateEngine.process("resumo-importacao", context);
            return convertHtmlToPdf(htmlContent);

        } catch (Exception e) {
            logger.error("❌ Erro ao gerar PDF resumo: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF resumo: " + e.getMessage());
        }
    }

    // ========== MÉTODOS PARA FATURA ==========

    /**
     * Gera PDF para uma fatura específica
     */
    public byte[] gerarPdfFatura(Long faturaId) {
        logger.info("📄 Gerando PDF para fatura ID: {}", faturaId);
        
        try {
            // Buscar fatura com itens
            Fatura fatura = faturaRepository.findById(faturaId)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + faturaId));
            
            List<FaturaItem> itens = faturaItemRepository.findByFaturaId(faturaId);
            
            // Gerar HTML com os dados
            String html = gerarHtmlFatura(fatura, itens);
            
            // Converter HTML para PDF
            return convertHtmlToPdf(html);
            
        } catch (Exception e) {
            logger.error("❌ Erro ao gerar PDF da fatura: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar PDF da fatura: " + e.getMessage());
        }
    }

    /**
     * Gera HTML para a fatura
     */
    private String gerarHtmlFatura(Fatura fatura, List<FaturaItem> itens) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<title>Fatura ").append(fatura.getNumeroFatura()).append("</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; font-size: 12px; margin: 40px; }");
        html.append(".header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 20px; margin-bottom: 20px; }");
        html.append(".header h1 { font-size: 24px; color: #1a237e; margin: 0; }");
        html.append(".header h2 { font-size: 18px; color: #555; margin: 5px 0; }");
        html.append(".info { margin-bottom: 20px; }");
        html.append(".info table { width: 100%; }");
        html.append(".info td { padding: 5px; }");
        html.append(".info .label { font-weight: bold; color: #555; width: 150px; }");
        html.append("table.items { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("table.items th { background-color: #1a237e; color: white; padding: 10px; text-align: left; }");
        html.append("table.items td { padding: 8px; border-bottom: 1px solid #ddd; }");
        html.append("table.items .total { font-weight: bold; background-color: #f5f5f5; }");
        html.append(".total-geral { text-align: right; font-size: 18px; font-weight: bold; margin-top: 20px; padding-top: 10px; border-top: 2px solid #333; }");
        html.append(".footer { margin-top: 40px; text-align: center; font-size: 10px; color: #999; border-top: 1px solid #ddd; padding-top: 10px; }");
        html.append(".status { display: inline-block; padding: 4px 12px; border-radius: 20px; font-weight: bold; }");
        html.append(".status-PAGA { background-color: #c8e6c9; color: #2e7d32; }");
        html.append(".status-PENDENTE { background-color: #fff9c4; color: #f57f17; }");
        html.append(".status-CANCELADA { background-color: #ffcdd2; color: #c62828; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        
        // Header
        html.append("<div class='header'>");
        html.append("  <h1>FATURA DE SERVIÇOS</h1>");
        html.append("  <h2>Nº ").append(fatura.getNumeroFatura()).append("</h2>");
        html.append("</div>");
        
        // Informações
        html.append("<div class='info'>");
        html.append("  <table>");
        html.append("    <tr><td class='label'>Nº Fatura:</td><td>").append(fatura.getNumeroFatura()).append("</td>");
        html.append("        <td class='label'>Status:</td><td><span class='status status-").append(fatura.getStatus()).append("'>").append(fatura.getStatus()).append("</span></td></tr>");
        html.append("    <tr><td class='label'>Data Emissão:</td><td>").append(formatarDataLocal(fatura.getDataEmissao())).append("</td>");
        html.append("        <td class='label'>Data Vencimento:</td><td>").append(formatarDataLocal(fatura.getDataVencimento())).append("</td></tr>");
        html.append("    <tr><td class='label'>Associado:</td><td colspan='3'>").append(fatura.getAssociado() != null ? fatura.getAssociado().getNomeRazao() : "N/A").append("</td></tr>");
        html.append("    <tr><td class='label'>CNPJ/CPF:</td><td colspan='3'>").append(fatura.getAssociado() != null ? fatura.getAssociado().getCnpjCpf() : "N/A").append("</td></tr>");
        if (fatura.getAssociado() != null && fatura.getAssociado().getCodigoSpc() != null) {
            html.append("    <tr><td class='label'>Código SPC:</td><td colspan='3'>").append(fatura.getAssociado().getCodigoSpc()).append("</td></tr>");
        }
        if (fatura.getNumeroRps() != null) {
            html.append("    <tr><td class='label'>Nº RPS:</td><td colspan='3'>").append(fatura.getNumeroRps()).append("</td></tr>");
        }
        if (fatura.getObservacao() != null && !fatura.getObservacao().isEmpty()) {
            html.append("    <tr><td class='label'>Observação:</td><td colspan='3'>").append(fatura.getObservacao()).append("</td></tr>");
        }
        html.append("  </table>");
        html.append("</div>");
        
        // Itens
        html.append("<table class='items'>");
        html.append("  <thead>");
        html.append("    <tr><th>Código</th><th>Descrição</th><th style='text-align:right'>Qtd</th><th style='text-align:right'>Vl. Unit.</th><th style='text-align:right'>Vl. Total</th></tr>");
        html.append("  </thead>");
        html.append("  <tbody>");
        
        if (itens != null && !itens.isEmpty()) {
            for (FaturaItem item : itens) {
                html.append("    <tr>");
                html.append("      <td>").append(item.getCodigoProduto() != null ? item.getCodigoProduto() : "-").append("</td>");
                html.append("      <td>").append(item.getDescricao() != null ? item.getDescricao() : "-").append("</td>");
                html.append("      <td style='text-align:right'>").append(item.getQuantidade() != null ? item.getQuantidade() : 0).append("</td>");
                html.append("      <td style='text-align:right'>").append(formatarMoeda(item.getValorUnitario())).append("</td>");
                html.append("      <td style='text-align:right'>").append(formatarMoeda(item.getValorTotal())).append("</td>");
                html.append("    </tr>");
            }
        } else {
            html.append("    <tr><td colspan='5' style='text-align:center; color:#999;'>Nenhum item encontrado</td></tr>");
        }
        
        html.append("  </tbody>");
        html.append("</table>");
        
        // Total
        html.append("<div class='total-geral'>");
        html.append("  Valor Total: ").append(formatarMoeda(fatura.getValorTotal()));
        html.append("</div>");
        
        // Footer
        html.append("<div class='footer'>");
        html.append("  Documento gerado em ").append(LocalDate.now().format(DATE_FORMATTER));
        html.append("  | Sistema de Gestão de Associados (SGA)");
        html.append("</div>");
        
        html.append("</body>");
        html.append("</html>");
        
        return html.toString();
    }

    // ========== MÉTODOS AUXILIARES ==========

    /**
     * Converte HTML para PDF usando Flying Saucer
     */
    private byte[] convertHtmlToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            
            // Configurar para suportar fontes padrão
            try {
                renderer.getFontResolver().addFontDirectory("/System/Library/Fonts", true);
            } catch (Exception e) {
                logger.warn("Não foi possível adicionar diretório de fontes: {}", e.getMessage());
            }
            
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
            
            return os.toByteArray();
            
        } catch (Exception e) {
            logger.error("Erro na conversão HTML para PDF: {}", e.getMessage());
            throw new RuntimeException("Falha ao gerar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Formata data do formato DDMMYYYY para DD/MM/YYYY
     */
    private String formatarData(String dataStr) {
        if (dataStr == null || dataStr.length() != 8) return dataStr != null ? dataStr : "-";
        try {
            return dataStr.substring(0, 2) + "/" + 
                   dataStr.substring(2, 4) + "/" + 
                   dataStr.substring(4, 8);
        } catch (Exception e) {
            return dataStr;
        }
    }

    /**
     * Formata uma data LocalDate para DD/MM/YYYY
     */
    private String formatarDataLocal(LocalDate data) {
        if (data == null) return "-";
        return data.format(DATE_FORMATTER);
    }

    /**
     * Formata um valor BigDecimal para moeda (R$)
     */
    private String formatarMoeda(BigDecimal valor) {
        if (valor == null) return "R$ 0,00";
        return CURRENCY_FORMAT.format(valor);
    }
}