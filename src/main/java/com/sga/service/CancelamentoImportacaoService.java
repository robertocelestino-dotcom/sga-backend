// src/main/java/com/sga/service/CancelamentoImportacaoService.java

package com.sga.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sga.dto.CancelamentoImportacaoDTO;
import com.sga.model.CancelamentoImportacao;
import com.sga.repository.CancelamentoImportacaoRepository;

@Service
public class CancelamentoImportacaoService {

    private static final Logger log = LoggerFactory.getLogger(CancelamentoImportacaoService.class);

    @Autowired
    private CancelamentoImportacaoRepository cancelamentoRepository;

    @Transactional
    public List<CancelamentoImportacaoDTO> importarCancelamentos(
            MultipartFile file, Integer mes, Integer ano, String usuario, Long importacaoId, String nomeArquivo) {
        
        log.info("📥 Importando cancelamentos - {}/{} - Arquivo: {}", mes, ano, nomeArquivo);
        
        List<CancelamentoImportacaoDTO> resultados = new ArrayList<>();
        
        try (InputStream inputStream = file.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String linha;
            int linhaNumero = 0;
            boolean primeiraLinha = true;
            
            while ((linha = reader.readLine()) != null) {
                linhaNumero++;
                
                if (linha.trim().isEmpty()) continue;
                
                // Pular cabeçalho
                if (primeiraLinha && (linha.toUpperCase().contains("CODIGO") || linha.toUpperCase().contains("COD"))) {
                    primeiraLinha = false;
                    log.info("📋 Cabeçalho ignorado: {}", linha);
                    continue;
                }
                primeiraLinha = false;
                
                CancelamentoImportacaoDTO dto = new CancelamentoImportacaoDTO();
                dto.setLinha(linhaNumero);
                
                try {
                    String[] colunas = linha.split(";");
                    if (colunas.length < 2) {
                        throw new IllegalArgumentException("Formato inválido. Esperado: CODIGO;SERVICO1;SERVICO2;...");
                    }
                    
                    String codigoAssociado = colunas[0].trim();
                    if (codigoAssociado.isEmpty()) {
                        throw new IllegalArgumentException("Código do associado é obrigatório");
                    }
                    
                    // 🔥 Coletar TODOS os serviços das colunas 1 em diante
                    List<String> servicos = new ArrayList<>();
                    for (int i = 1; i < colunas.length; i++) {
                        String servico = colunas[i] != null ? colunas[i].trim() : "";
                        if (!servico.isEmpty()) {
                            servicos.add(servico);
                        }
                    }
                    
                    if (servicos.isEmpty()) {
                        throw new IllegalArgumentException("Pelo menos um serviço deve ser informado");
                    }
                    
                    log.info("📝 Linha {}: Associado={}, Serviços={}", linhaNumero, codigoAssociado, servicos);
                    
                    // 🔥 Criar um registro de cancelamento para CADA serviço
                    List<String> servicosImportados = new ArrayList<>();
                    for (String servico : servicos) {
                        CancelamentoImportacao cancelamento = new CancelamentoImportacao();
                        cancelamento.setCodigoAssociado(codigoAssociado);
                        cancelamento.setDescricaoProduto("Cancelamento de serviço: " + servico);
                        cancelamento.setProdutoPersonalizado(servico);
                        cancelamento.setValorCancelamento(BigDecimal.ZERO);
                        cancelamento.setMesReferencia(mes);
                        cancelamento.setAnoReferencia(ano);
                        cancelamento.setDataImportacao(LocalDateTime.now());
                        cancelamento.setUsuarioImportacao(usuario);
                        cancelamento.setLinhaArquivo(linhaNumero);
                        cancelamento.setImportacaoId(importacaoId);
                        cancelamento.setNomeArquivo(nomeArquivo);
                        cancelamento.setStatus("PENDENTE");
                        cancelamento.setCriadoEm(LocalDateTime.now());
                        
                        cancelamentoRepository.save(cancelamento);
                        servicosImportados.add(servico);
                        
                        log.debug("  ✅ Serviço '{}' importado para associado {}", servico, codigoAssociado);
                    }
                    
                    // Preencher DTO com a lista de serviços
                    dto.setCodigoAssociado(codigoAssociado);
                    dto.setDescricaoProduto("Cancelamento de " + servicosImportados.size() + " serviço(s)");
                    dto.setServicos(servicosImportados);
                    dto.setQuantidadeServicos(servicosImportados.size());
                    dto.setValorCancelamento(BigDecimal.ZERO);
                    
                    log.info("✅ Linha {} importada: {} serviços para associado {}", 
                        linhaNumero, servicosImportados.size(), codigoAssociado);
                    
                } catch (Exception e) {
                    log.error("❌ Erro na linha {}: {}", linhaNumero, e.getMessage());
                    dto.setErro(e.getMessage());
                }
                
                resultados.add(dto);
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar arquivo de cancelamentos", e);
            throw new RuntimeException("Erro ao processar arquivo: " + e.getMessage());
        }
        
        long sucessos = resultados.stream().filter(r -> r.getErro() == null).count();
        long erros = resultados.stream().filter(r -> r.getErro() != null).count();
        log.info("📊 Importação concluída: {} sucessos, {} erros", sucessos, erros);
        
        return resultados;
    }
    
    @Transactional
    public void desfazerImportacao(Integer mes, Integer ano, String usuario) {
        log.info("🗑️ Desfazendo importação de cancelamentos - {}/{} por {}", mes, ano, usuario);
        List<CancelamentoImportacao> cancelamentos = cancelamentoRepository.findByMesReferenciaAndAnoReferencia(mes, ano);
        if (!cancelamentos.isEmpty()) {
            cancelamentoRepository.deleteAll(cancelamentos);
            log.info("✅ {} cancelamentos removidos", cancelamentos.size());
        }
    }
}