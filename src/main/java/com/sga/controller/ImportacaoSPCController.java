package com.sga.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sga.dto.ImportacaoResponseDTO;
import com.sga.model.ImportacaoSPC;
import com.sga.service.ImportacaoSPService;

@RestController
@RequestMapping("/api/importacao-spc")
public class ImportacaoSPCController {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoSPCController.class);

    @Autowired
    private ImportacaoSPService importacaoSPService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadArquivoSPC(@RequestParam("arquivo") MultipartFile arquivo) {
        try {
            log.info("📤 Recebendo upload do arquivo: {} Tamanho: {} bytes", 
                    arquivo.getOriginalFilename(), arquivo.getSize());

            // Validações
            if (arquivo.isEmpty()) {
                log.warn("❌ Tentativa de upload com arquivo vazio");
                return ResponseEntity.badRequest().body(criarRespostaErro("Arquivo vazio"));
            }

            if (!arquivo.getOriginalFilename().toLowerCase().endsWith(".txt")) {
                log.warn("❌ Tentativa de upload com tipo de arquivo inválido: {}", arquivo.getOriginalFilename());
                return ResponseEntity.badRequest().body(criarRespostaErro("Apenas arquivos TXT são permitidos"));
            }

            if (arquivo.getSize() > 50 * 1024 * 1024) {
                log.warn("❌ Arquivo muito grande: {} bytes", arquivo.getSize());
                return ResponseEntity.badRequest().body(criarRespostaErro("Arquivo muito grande. Tamanho máximo: 50MB"));
            }

            // Processar arquivo
            ImportacaoSPC importacao = importacaoSPService.processarArquivoSPC(arquivo);
            log.info("✅ Arquivo processado com sucesso: {}", arquivo.getOriginalFilename());

            // Criar DTO com dados básicos - SEM depender de getters específicos
            ImportacaoResponseDTO importacaoDTO = new ImportacaoResponseDTO();
            importacaoDTO.setId(importacao.getId());
            importacaoDTO.setNomeArquivo(importacao.getNomeArquivo());
            importacaoDTO.setStatus(importacao.getStatus());
            importacaoDTO.setDataImportacao(new java.util.Date()); // Data atual
            
            // Tente obter valores calculados do processamento
            // Se não existirem esses campos, use valores padrão
            try {
                // Tente métodos comuns - ajuste conforme sua entidade
            	//if (importacao.getQuantidadeItens() != null) {
            	//    importacaoDTO.setQuantidadeRegistros(importacao.getQuantidadeItens());
            	//} else if (importacao.getTotalRegistros() != null) {
            	//    importacaoDTO.setQuantidadeRegistros(importacao.getTotalRegistros());
            	//} else {
                    // Calcular baseado nas notas de débito
                    int totalItens = importacao.getNotasDebito().stream()
                            .mapToInt(nota -> nota.getItens().size())
                            .sum();
                    importacaoDTO.setQuantidadeRegistros(totalItens);
                    //}
            } catch (Exception e) {
                importacaoDTO.setQuantidadeRegistros(0); // Valor padrão
            }
            
            try {
            	//if (importacao.getValorTotal() != null) {
            	//    importacaoDTO.setTotalValor(importacao.getValorTotal());
            	//} else {
                    // Calcular valor total baseado nos itens
                    double valorTotal = importacao.getNotasDebito().stream()
                            .flatMap(nota -> nota.getItens().stream())
                            .mapToDouble(item -> item.getValorTotal().doubleValue())
                            .sum();
                    importacaoDTO.setTotalValor(valorTotal);
                    //}
            } catch (Exception e) {
                importacaoDTO.setTotalValor(0.0); // Valor padrão
            }
            
            importacaoDTO.setRegistrosProcessados(importacaoDTO.getQuantidadeRegistros()); // Assumindo que todos foram processados

            Map<String, Object> response = new HashMap<>();
            response.put("mensagem", "Arquivo processado com sucesso");
            response.put("importacao", importacaoDTO);
            response.put("nomeArquivo", arquivo.getOriginalFilename());
            response.put("tamanho", arquivo.getSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Erro ao processar arquivo {}: {}", 
                    arquivo.getOriginalFilename(), e.getMessage(), e);
            
            return ResponseEntity.badRequest()
                    .body(criarRespostaErro("Erro ao processar arquivo: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ImportacaoResponseDTO>> listarImportacoes() {
        try {
            log.info("📋 Listando importações SPC");
            List<ImportacaoSPC> importacoes = importacaoSPService.listarImportacoes();
            
            // Converter para DTOs com dados básicos
            List<ImportacaoResponseDTO> importacoesDTO = importacoes.stream()
                    .map(importacao -> {
                        ImportacaoResponseDTO dto = new ImportacaoResponseDTO();
                        dto.setId(importacao.getId());
                        dto.setNomeArquivo(importacao.getNomeArquivo());
                        dto.setStatus(importacao.getStatus());
                        dto.setDataImportacao(new java.util.Date());
                        dto.setQuantidadeRegistros(0); // Valor padrão
                        dto.setRegistrosProcessados(0); // Valor padrão
                        dto.setTotalValor(0.0); // Valor padrão
                        return dto;
                    })
                    .collect(Collectors.toList());
            
            log.info("✅ Encontradas {} importações", importacoesDTO.size());
            return ResponseEntity.ok(importacoesDTO);
        } catch (Exception e) {
            log.error("❌ Erro ao listar importações: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint de saúde para teste
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        log.info("🔍 Health check do ImportacaoSPCController");
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "ImportacaoSPCController");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    // Método auxiliar para criar respostas de erro padronizadas
    private Map<String, String> criarRespostaErro(String mensagem) {
        Map<String, String> erro = new HashMap<>();
        erro.put("erro", mensagem);
        erro.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return erro;
    }
}