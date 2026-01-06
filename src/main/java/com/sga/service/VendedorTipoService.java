// src/main/java/com/sga/service/VendedorTipoService.java
package com.sga.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.VendedorTipoDTO;
import com.sga.model.VendedorTipo;
import com.sga.repository.VendedorTipoRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class VendedorTipoService {
    
    private static final Logger logger = LoggerFactory.getLogger(VendedorTipoService.class);
    
    @Autowired
    private VendedorTipoRepository vendedorTipoRepository;
    
    // Converter Entity para DTO
    private VendedorTipoDTO toDTO(VendedorTipo vendedorTipo) {
        if (vendedorTipo == null) {
            return null;
        }
        
        VendedorTipoDTO dto = new VendedorTipoDTO();
        dto.setId(vendedorTipo.getId());
        dto.setDescricao(vendedorTipo.getDescricao());
        
        return dto;
    }
    
    // Converter DTO para Entity
    private VendedorTipo toEntity(VendedorTipoDTO dto) {
        if (dto == null) {
            return null;
        }
        
        VendedorTipo vendedorTipo = new VendedorTipo();
        vendedorTipo.setId(dto.getId());
        vendedorTipo.setDescricao(dto.getDescricao());
        
        return vendedorTipo;
    }
    
    // Listar todos os tipos de vendedor
    @Transactional(readOnly = true)
    public List<VendedorTipoDTO> listarTodos() {
        logger.info("Listando todos os tipos de vendedor");
        
        List<VendedorTipo> tipos = vendedorTipoRepository.findAllByOrderByDescricaoAsc();
        logger.info("Encontrados {} tipos de vendedor", tipos.size());
        
        return tipos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Buscar por ID
    @Transactional(readOnly = true)
    public VendedorTipoDTO buscarPorId(Long id) {
        logger.info("Buscando tipo de vendedor por ID: {}", id);
        
        VendedorTipo vendedorTipo = vendedorTipoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tipo de vendedor não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Tipo de vendedor não encontrado com ID: " + id);
                });
        
        logger.info("Tipo de vendedor encontrado: {} - {}", vendedorTipo.getId(), vendedorTipo.getDescricao());
        return toDTO(vendedorTipo);
    }
    
    // Buscar por descrição
    @Transactional(readOnly = true)
    public List<VendedorTipoDTO> buscarPorDescricao(String descricao) {
        logger.info("Buscando tipos de vendedor por descrição: {}", descricao);
        
        List<VendedorTipo> tipos = vendedorTipoRepository.findByDescricaoContainingIgnoreCase(descricao);
        logger.info("Encontrados {} tipos de vendedor para descrição: {}", tipos.size(), descricao);
        
        return tipos.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    // Criar novo tipo de vendedor
    @Transactional
    public VendedorTipoDTO criar(VendedorTipoDTO vendedorTipoDTO) {
        logger.info("Criando novo tipo de vendedor: {}", vendedorTipoDTO.getDescricao());
        
        // Validar dados
        if (vendedorTipoDTO.getDescricao() == null || vendedorTipoDTO.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        
        // Verificar se já existe
        if (vendedorTipoRepository.existsByDescricao(vendedorTipoDTO.getDescricao().trim())) {
            throw new IllegalArgumentException("Já existe um tipo de vendedor com esta descrição");
        }
        
        VendedorTipo vendedorTipo = new VendedorTipo();
        vendedorTipo.setDescricao(vendedorTipoDTO.getDescricao().trim());
        
        VendedorTipo salvo = vendedorTipoRepository.save(vendedorTipo);
        logger.info("Tipo de vendedor criado com ID: {}", salvo.getId());
        
        return toDTO(salvo);
    }
    
    // Atualizar tipo de vendedor
    @Transactional
    public VendedorTipoDTO atualizar(Long id, VendedorTipoDTO vendedorTipoDTO) {
        logger.info("Atualizando tipo de vendedor ID: {}", id);
        
        // Buscar existente
        VendedorTipo vendedorTipo = vendedorTipoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Tipo de vendedor não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Tipo de vendedor não encontrado com ID: " + id);
                });
        
        // Validar dados
        if (vendedorTipoDTO.getDescricao() == null || vendedorTipoDTO.getDescricao().trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        
        // Verificar se nova descrição já existe (para outro registro)
        if (!vendedorTipo.getDescricao().equalsIgnoreCase(vendedorTipoDTO.getDescricao().trim())) {
            if (vendedorTipoRepository.existsByDescricao(vendedorTipoDTO.getDescricao().trim())) {
                throw new IllegalArgumentException("Já existe outro tipo de vendedor com esta descrição");
            }
        }
        
        // Atualizar
        vendedorTipo.setDescricao(vendedorTipoDTO.getDescricao().trim());
        
        VendedorTipo atualizado = vendedorTipoRepository.save(vendedorTipo);
        logger.info("Tipo de vendedor atualizado: {} - {}", atualizado.getId(), atualizado.getDescricao());
        
        return toDTO(atualizado);
    }
    
    // Excluir tipo de vendedor
    @Transactional
    public void excluir(Long id) {
        logger.info("Excluindo tipo de vendedor ID: {}", id);
        
        // Verificar se existe
        if (!vendedorTipoRepository.existsById(id)) {
            logger.error("Tipo de vendedor não encontrado para exclusão: {}", id);
            throw new EntityNotFoundException("Tipo de vendedor não encontrado com ID: " + id);
        }
        
        // TODO: Verificar se existem vendedores vinculados a este tipo
        // Pode-se adicionar validação aqui
        
        vendedorTipoRepository.deleteById(id);
        logger.info("Tipo de vendedor excluído: {}", id);
    }
    
    // Contar total de tipos de vendedor
    @Transactional(readOnly = true)
    public Long contarTotal() {
        Long count = vendedorTipoRepository.countTotal();
        logger.info("Total de tipos de vendedor: {}", count);
        return count;
    }
    
    // Verificar se existe por descrição
    @Transactional(readOnly = true)
    public boolean existePorDescricao(String descricao) {
        boolean existe = vendedorTipoRepository.existsByDescricao(descricao);
        logger.info("Tipo de vendedor com descrição '{}' existe: {}", descricao, existe);
        return existe;
    }
}