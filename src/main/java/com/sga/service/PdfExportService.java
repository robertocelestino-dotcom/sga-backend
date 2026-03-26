package com.sga.service;

import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.NotaDebitoSPCRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfExportService.class);

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoSPCRepository;

    /**
     * Gera PDF para uma nota específica usando template HTML
     */
    public byte[] gerarPdfNota(Long notaId) {
        logger.info("📄 Gerando PDF para nota ID: {}", notaId);

        try {
            // Buscar a nota e seus itens
            NotaDebitoSPC nota = notaDebitoSPCRepository.findById(notaId)
                    .orElseThrow(() -> new RuntimeException("Nota não encontrada: " + notaId));

            // Preparar o contexto Thymeleaf
            Context context = new Context();
            
            // Formatar data de vencimento (assumindo formato DDMMYYYY)
            String dataVencimentoFormatada = formatarData(nota.getDataVencimento());
            
            // Adicionar variáveis ao contexto
            context.setVariable("nota", nota);
            context.setVariable("itens", nota.getItens());
            context.setVariable("dataVencimentoFormatada", dataVencimentoFormatada);

            // Processar o template HTML
            String htmlContent = templateEngine.process("nota-pdf", context);
            
            // Converter HTML para PDF
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
}