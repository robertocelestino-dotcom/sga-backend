package com.sga.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // ✅ Import correto
import org.springframework.stereotype.Service;

import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.repository.NotaDebitoSPCRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotaDebitoSPCService {

    // ✅ CORRETO: Spring injetará automaticamente via construtor
    private final NotaDebitoSPCRepository repository;

    
    public NotaDebitoSPCService(NotaDebitoSPCRepository repository) {
        this.repository = repository;
    }
    
    public Page<NotaDebitoResumoDTO> listarNotas(
            Long importacaoId,
            String filtro,
            Pageable pageable // ✅ Sem qualificação completa
    ) {
        // ✅ CORRETO: Sem cast, passando diretamente
        return repository.listarResumoNotas(importacaoId, filtro, pageable);
    }

    public byte[] gerarPdfNota(Long notaId) {
        // Implementação temporária - retorna array vazio
        return new byte[0];
        
        // Ou lança exceção mais descritiva:
        // throw new RuntimeException("Funcionalidade de PDF ainda não implementada");
    }
}