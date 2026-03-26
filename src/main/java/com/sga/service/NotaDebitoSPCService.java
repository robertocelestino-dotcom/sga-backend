package com.sga.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.repository.NotaDebitoSPCRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotaDebitoSPCService {

    private final NotaDebitoSPCRepository repository;
    private final VerificacaoImportacaoService verificacaoService;

    public NotaDebitoSPCService(NotaDebitoSPCRepository repository) {
        this.repository = repository;
        this.verificacaoService = null; // Será injetado pelo setter se necessário
    }
    
    /**
     * Método CORRIGIDO: Usa o método correto do repository
     */
    public Page<NotaDebitoResumoDTO> listarNotas(
            Long importacaoId,
            String filtro,
            Pageable pageable
    ) {

        try {
            Page<Object[]> page;
            
            // 🔥 CORREÇÃO: Usar o método existente no repository
            if (filtro != null && !filtro.trim().isEmpty()) {
                // Método com filtro
                page = repository.listarNotasResumoComFiltro(importacaoId, filtro.trim(), pageable);
            } else {
                // Método sem filtro
                page = repository.listarNotasResumo(importacaoId, pageable);
            }
            
            // Converter Object[] para DTO
            return page.map(row -> {
                NotaDebitoResumoDTO dto = new NotaDebitoResumoDTO();
                
                try {
                    // Mapeamento seguro dos campos
                    if (row.length > 0 && row[0] != null) {
                        dto.setId(((Number) row[0]).longValue());
                    }
                    
                    if (row.length > 1 && row[1] != null) {
                        dto.setNumeroNota(row[1].toString());
                    }
                    
                    if (row.length > 2 && row[2] != null) {
                        dto.setCodigoSocio(row[2].toString());
                    }
                    
                    if (row.length > 3 && row[3] != null) {
                        dto.setNomeAssociado(row[3].toString());
                    }
                    
                    // Valores (tratamento especial para BigDecimal)
                    if (row.length > 4 && row[4] != null) {
                        if (row[4] instanceof BigDecimal) {
                            dto.setTotalDebitos((BigDecimal) row[4]);
                        } else if (row[4] instanceof Number) {
                            dto.setTotalDebitos(BigDecimal.valueOf(((Number) row[4]).doubleValue()));
                        }
                    }
                    
                    if (row.length > 5 && row[5] != null) {
                        if (row[5] instanceof BigDecimal) {
                            dto.setTotalCreditos((BigDecimal) row[5]);
                        } else if (row[5] instanceof Number) {
                            dto.setTotalCreditos(BigDecimal.valueOf(((Number) row[5]).doubleValue()));
                        }
                    }
                    
                    if (row.length > 6 && row[6] != null) {
                        if (row[6] instanceof BigDecimal) {
                            dto.setValorFaturado((BigDecimal) row[6]);
                        } else if (row[6] instanceof Number) {
                            dto.setValorFaturado(BigDecimal.valueOf(((Number) row[6]).doubleValue()));
                        }
                    }
                    
                } catch (Exception e) {
                    //log.error("Erro ao converter linha para DTO: {}", e.getMessage());
                }
                
                return dto;
            });
            
        } catch (Exception e) {
            
            throw new RuntimeException("Erro ao listar notas: " + e.getMessage());
        }
    }

    /**
     * Método alternativo que delega para o VerificacaoImportacaoService
     */
    public Page<NotaDebitoResumoDTO> listarNotasComVerificacao(
            Long importacaoId,
            String filtro,
            Pageable pageable
    ) {
        if (verificacaoService != null) {
            return verificacaoService.listarNotasComFiltro(importacaoId, filtro, pageable);
        }
        // Fallback para o método local
        return listarNotas(importacaoId, filtro, pageable);
    }

    public byte[] gerarPdfNota(Long notaId) {
        
        try {
            // TODO: Implementar geração real de PDF
            // Por enquanto, retorna um PDF simples
            String pdfContent = String.format(
                "PDF da Nota %d\n\n" +
                "Funcionalidade em desenvolvimento.\n" +
                "Em breve você poderá visualizar o PDF completo.", 
                notaId
            );
            
            return pdfContent.getBytes("UTF-8");
            
        } catch (Exception e) {
           
            throw new RuntimeException("Erro ao gerar PDF da nota: " + e.getMessage());
        }
    }
    
    /**
     * Método para contar notas por importação
     */
    public long contarNotasPorImportacao(Long importacaoId) {
        return repository.countByImportacao_Id(importacaoId);
    }
}