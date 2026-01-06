package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.PlanoDTO;
import com.sga.dto.PlanoResumoDTO;
import com.sga.model.Planos;
import com.sga.repository.PlanosRepository;

@Service
public class PlanosService {
    
    @Autowired
    private PlanosRepository planoRepository;

    @Transactional
    public PlanoDTO criar(PlanoDTO dto) {
        // Valida se plano já existe com mesmo nome
        if (planoRepository.findByPlano(dto.getPlano()).isPresent()) {
            throw new RuntimeException("Já existe um plano com este nome");
        }
        
        Planos plano = new Planos();
        mapDtoToEntity(dto, plano);
        plano = planoRepository.save(plano);
        return mapEntityToDto(plano);
    }

    @Transactional
    public PlanoDTO atualizar(Long id, PlanoDTO dto) {
        Planos plano = planoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        
        // Valida se outro plano já tem este nome
        if (dto.getPlano() != null && !dto.getPlano().equals(plano.getPlano())) {
            planoRepository.findByPlano(dto.getPlano())
                    .ifPresent(p -> {
                        if (!p.getId().equals(id)) {
                            throw new RuntimeException("Já existe outro plano com este nome");
                        }
                    });
        }
        
        mapDtoToEntity(dto, plano);
        plano = planoRepository.save(plano);
        return mapEntityToDto(plano);
    }

    public PlanoDTO buscarPorId(Long id) {
        Planos plano = planoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        return mapEntityToDto(plano);
    }

    public Page<PlanoResumoDTO> listar(Pageable pageable, String nome, String status) {
        // Implementação básica de paginação
        List<Planos> todosPlanos = planoRepository.findAll();
        
        // Aplicar filtros
        List<Planos> planosFiltrados = todosPlanos.stream()
                .filter(plano -> nome == null || plano.getPlano().toLowerCase().contains(nome.toLowerCase()))
                .collect(Collectors.toList());
        
        // Paginação manual
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), planosFiltrados.size());
        
        if (start > planosFiltrados.size()) {
            start = planosFiltrados.size();
            end = start;
        }
        
        List<PlanoResumoDTO> conteudoPaginado = planosFiltrados.subList(start, end)
                .stream()
                .map(this::mapEntityToResumoDto)
                .collect(Collectors.toList());
        
        return new PageImpl<>(conteudoPaginado, pageable, planosFiltrados.size());
    }

    public List<PlanoResumoDTO> listarTodosAtivos() {
        // Retorna todos os planos (modelo não tem status)
        return planoRepository.findAllByOrderByPlanoAsc().stream()
                .map(this::mapEntityToResumoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void excluir(Long id) {
        Planos plano = planoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        planoRepository.delete(plano);
    }

    @Transactional
    public void inativar(Long id) {
        // Modelo não tem status, então apenas lança exceção
        throw new RuntimeException("Plano não possui campo status para inativação");
    }

    // ============== MAPEAMENTOS ==============
    
    private void mapDtoToEntity(PlanoDTO dto, Planos entity) {
        entity.setId(dto.getId());
        entity.setPlano(dto.getPlano());
        entity.setValor(dto.getValorMensal());
        entity.setObservacao(dto.getObservacoes());
    }

    private PlanoDTO mapEntityToDto(Planos entity) {
        PlanoDTO dto = new PlanoDTO();
        dto.setId(entity.getId());
        dto.setPlano(entity.getPlano());
        dto.setValorMensal(entity.getValor());
        dto.setObservacoes(entity.getObservacao());
        dto.setCodigo("PL" + entity.getId());
        dto.setDescricao("Plano " + entity.getPlano());
        dto.setStatus("ATIVO");
        dto.setDataCadastro(LocalDateTime.now());
        return dto;
    }

    private PlanoResumoDTO mapEntityToResumoDto(Planos entity) {
        PlanoResumoDTO dto = new PlanoResumoDTO();
        dto.setId(entity.getId());
        dto.setPlano(entity.getPlano());
        dto.setValorMensal(entity.getValor());
        dto.setCodigo("PL" + entity.getId());
        dto.setDescricao("Plano " + entity.getPlano());
        dto.setStatus("ATIVO");
        dto.setDataCadastro(LocalDateTime.now());
        return dto;
    }
}