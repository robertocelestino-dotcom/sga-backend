package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sga.dto.PlanoDTO;
import com.sga.dto.PlanoResumoDTO;
import com.sga.model.Planos;
import com.sga.repository.PlanosRepository;

public class PlanosService {
	
	
	
	private PlanosRepository planoRepository;

    @Transactional
    public PlanoDTO criar(PlanoDTO dto) {
        // Valida se código já existe
        if (planoRepository.findByCodigo(dto.getCodigo()).isPresent()) {
            throw new RuntimeException("Já existe um plano com este código");
        }
        
        Planos plano = new Planos();
        mapDtoToEntity(dto, plano);
        //plano.setDataCadastro(LocalDateTime.now());
        plano = planoRepository.save(plano);
        return mapEntityToDto(plano);
    }

    @Transactional
    public PlanoDTO atualizar(Long id, PlanoDTO dto) {
    	Planos plano = planoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        
        // Valida se código já existe (exceto para o próprio plano)
        planoRepository.findByCodigo(dto.getCodigo())
                .ifPresent(p -> {
                    if (!p.getId().equals(id)) {
                        throw new RuntimeException("Já existe outro plano com este código");
                    }
                });
        
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
        Page<Planos> page;
        if (nome != null && status != null) {
            page = planoRepository.findByNomeContainingIgnoreCaseAndStatus(nome, status, pageable);
        } else if (nome != null) {
            page = planoRepository.findByNomeContainingIgnoreCase(nome, pageable);
        //} else if (status != null) {
        //    page = planoRepository.findAll(pageable).filter(p -> p.getStatus().equals(status));
        } else {
            page = planoRepository.findAll(pageable);
        }
        return page.map(this::mapEntityToResumoDto);
    }

    public List<PlanoResumoDTO> listarTodosAtivos() {
        return planoRepository.findByStatusOrderByNomeAsc("A").stream()
                .map(this::mapEntityToResumoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void excluir(Long id) {
    	Planos plano = planoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        
        // Verifica se há associados vinculados
        Long count = planoRepository.countAssociadosByPlanoId(id);
        if (count > 0) {
            throw new RuntimeException("Não é possível excluir o plano pois existem " + count + " associado(s) vinculado(s)");
        }
        
        planoRepository.delete(plano);
    }

    @Transactional
    public void inativar(Long id) {
    	Planos plano = planoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plano não encontrado"));
        //plano.setStatus("I");
        planoRepository.save(plano);
    }

    private void mapDtoToEntity(PlanoDTO dto, Planos entity) {
        entity.setId(dto.getId());
        //entity.setCodigo(dto.getCodigo());
        entity.setPlano(dto.getPlano());
        //entity.setDescricao(dto.getDescricao());
        entity.setValor(dto.getValorMensal());
        //entity.setValorAnual(dto.getValorAnual());
        //entity.setPeriodoCobranca(dto.getPeriodoCobranca());
        //entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "A");
        //entity.setLimiteAssociados(dto.getLimiteAssociados());
        entity.setObservacao(dto.getObservacoes());
    }

    private PlanoDTO mapEntityToDto(Planos entity) {
        PlanoDTO dto = new PlanoDTO();
        dto.setId(entity.getId());
        //dto.setCodigo(entity.getCodigo());
        dto.setPlano(entity.getPlano());
        //dto.setDescricao(entity.getDescricao());
        dto.setValorMensal(entity.getValor());
        //dto.setValorAnual(entity.getValorAnual());
        //dto.setPeriodoCobranca(entity.getPeriodoCobranca());
        //dto.setStatus(entity.getStatus());
        //dto.setLimiteAssociados(entity.getLimiteAssociados());
        dto.setObservacoes(entity.getObservacao());
        //dto.setDataCadastro(entity.getDataCadastro());
        return dto;
    }

    private PlanoResumoDTO mapEntityToResumoDto(Planos entity) {
        return new PlanoResumoDTO(
                entity.getId(),
                //entity.getCodigo(),
                entity.getPlano(),
                entity.getValor()
                //entity.getStatus()
        );
    }

}
