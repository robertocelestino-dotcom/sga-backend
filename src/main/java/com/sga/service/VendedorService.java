// src/main/java/com/sga/service/VendedorService.java
package com.sga.service;

import com.sga.dto.VendedorDTO;
import com.sga.dto.VendedorResumoDTO;
import com.sga.model.Vendedor;
import com.sga.model.VendedorTipo;
import com.sga.repository.VendedorRepository;
import com.sga.repository.VendedorTipoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class VendedorService {
    
    private static final Logger logger = LoggerFactory.getLogger(VendedorService.class);
    
    @Autowired
    private VendedorRepository vendedorRepository;
    
    @Autowired
    private VendedorTipoRepository vendedorTipoRepository;
    
    // Converter Entity para DTO
    private VendedorDTO toDTO(Vendedor vendedor) {
        if (vendedor == null) {
            return null;
        }
        
        VendedorDTO dto = new VendedorDTO();
        dto.setId(vendedor.getId());
        dto.setNomeRazao(vendedor.getNomeRazao());
        dto.setNomeFantasia(vendedor.getNomeFantasia());
        dto.setCargoFuncao(vendedor.getCargoFuncao());
        dto.setStatus(vendedor.getStatus());
        dto.setObservacao(vendedor.getObservacao());
        dto.setDataCadastro(vendedor.getDataCadastro());
        
        if (vendedor.getVendedorTipo() != null) {
            dto.setVendedorTipoId(vendedor.getVendedorTipo().getId());
            dto.setVendedorTipoDescricao(vendedor.getVendedorTipo().getDescricao());
        }
        
        return dto;
    }
    
    // Converter Entity para ResumoDTO
    private VendedorResumoDTO toResumoDTO(Vendedor vendedor) {
        if (vendedor == null) {
            return null;
        }
        
        VendedorResumoDTO dto = new VendedorResumoDTO();
        dto.setId(vendedor.getId());
        dto.setNomeRazao(vendedor.getNomeRazao());
        dto.setNomeFantasia(vendedor.getNomeFantasia());
        dto.setCargoFuncao(vendedor.getCargoFuncao());
        dto.setStatus(vendedor.getStatus());
        
        if (vendedor.getVendedorTipo() != null) {
            dto.setVendedorTipoId(vendedor.getVendedorTipo().getId());
            dto.setVendedorTipoDescricao(vendedor.getVendedorTipo().getDescricao());
        }
        
        return dto;
    }
    
    // Listar todos com filtros
    @Transactional(readOnly = true)
    public Page<VendedorResumoDTO> listarComFiltros(Pageable pageable, 
                                                   String nomeRazao, 
                                                   String status,
                                                   Long vendedorTipoId) {
        logger.info("Listando vendedores com filtros - tipoId: {}, status: {}, nome: {}", 
                   vendedorTipoId, status, nomeRazao);
        
        Page<Vendedor> vendedoresPage;
        
        if (vendedorTipoId != null) {
            // Filtrar por tipo específico
            if (status != null && !status.trim().isEmpty()) {
                vendedoresPage = vendedorRepository.findByVendedorTipoIdAndStatus(
                    vendedorTipoId, status.trim(), pageable);
            } else {
                vendedoresPage = vendedorRepository.findByVendedorTipoId(vendedorTipoId, pageable);
            }
        } else if (status != null && !status.trim().isEmpty()) {
            vendedoresPage = vendedorRepository.findByStatus(status.trim(), pageable);
        } else if (nomeRazao != null && !nomeRazao.trim().isEmpty()) {
            vendedoresPage = vendedorRepository.findByNomeRazaoContainingIgnoreCase(
                nomeRazao.trim(), pageable);
        } else {
            vendedoresPage = vendedorRepository.findAll(pageable);
        }
        
        // Retornar página convertida
        return vendedoresPage.map(this::toResumoDTO);
    }
    
    // Buscar vendedores do tipo 1
    @Transactional(readOnly = true)
    public Page<VendedorResumoDTO> buscarVendedoresTipo1(Pageable pageable) {
        logger.info("Buscando vendedores do tipo 1 paginados");
        
        Page<Vendedor> vendedoresPage = vendedorRepository.findVendedoresTipo1(pageable);
        logger.info("Encontrados {} vendedores do tipo 1", vendedoresPage.getTotalElements());
        
        return vendedoresPage.map(this::toResumoDTO);
    }
    
    // Buscar vendedores do tipo 2
    @Transactional(readOnly = true)
    public Page<VendedorResumoDTO> buscarVendedoresTipo2(Pageable pageable) {
        logger.info("Buscando vendedores do tipo 2 paginados");
        
        Page<Vendedor> vendedoresPage = vendedorRepository.findVendedoresTipo2(pageable);
        logger.info("Encontrados {} vendedores do tipo 2", vendedoresPage.getTotalElements());
        
        return vendedoresPage.map(this::toResumoDTO);
    }
    
    // Buscar vendedores do tipo 1 ativos (lista completa)
    @Transactional(readOnly = true)
    public List<VendedorResumoDTO> buscarVendedoresTipo1Ativos() {
        logger.info("Buscando vendedores do tipo 1 ativos");
        
        List<Vendedor> vendedores = vendedorRepository.findVendedoresTipo1Ativos();
        logger.info("Encontrados {} vendedores do tipo 1 ativos", vendedores.size());
        
        return vendedores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    // Buscar vendedores do tipo 2 ativos (lista completa)
    @Transactional(readOnly = true)
    public List<VendedorResumoDTO> buscarVendedoresTipo2Ativos() {
        logger.info("Buscando vendedores do tipo 2 ativos");
        
        List<Vendedor> vendedores = vendedorRepository.findVendedoresTipo2Ativos();
        logger.info("Encontrados {} vendedores do tipo 2 ativos", vendedores.size());
        
        return vendedores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    // Buscar todos vendedores do tipo 1 (lista completa)
    @Transactional(readOnly = true)
    public List<VendedorResumoDTO> buscarTodosVendedoresTipo1() {
        logger.info("Buscando todos vendedores do tipo 1");
        
        List<Vendedor> vendedores = vendedorRepository.findVendedoresTipo1();
        logger.info("Encontrados {} vendedores do tipo 1", vendedores.size());
        
        return vendedores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    // Buscar todos vendedores do tipo 2 (lista completa)
    @Transactional(readOnly = true)
    public List<VendedorResumoDTO> buscarTodosVendedoresTipo2() {
        logger.info("Buscando todos vendedores do tipo 2");
        
        List<Vendedor> vendedores = vendedorRepository.findVendedoresTipo2();
        logger.info("Encontrados {} vendedores do tipo 2", vendedores.size());
        
        return vendedores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    // Buscar por ID
    @Transactional(readOnly = true)
    public VendedorDTO buscarPorId(Long id) {
        logger.info("Buscando vendedor por ID: {}", id);
        
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Vendedor não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Vendedor não encontrado com ID: " + id);
                });
        
        logger.info("Vendedor encontrado: {} - {}", vendedor.getId(), vendedor.getNomeRazao());
        return toDTO(vendedor);
    }
    
    // Criar novo vendedor
    @Transactional
    public VendedorDTO criar(VendedorDTO vendedorDTO) {
        logger.info("Criando novo vendedor: {}", vendedorDTO.getNomeRazao());
        
        // Validar dados
        if (vendedorDTO.getNomeRazao() == null || vendedorDTO.getNomeRazao().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome/Razão Social é obrigatório");
        }
        
        if (vendedorDTO.getVendedorTipoId() == null) {
            throw new IllegalArgumentException("Tipo de vendedor é obrigatório");
        }
        
        // Buscar tipo de vendedor
        VendedorTipo vendedorTipo = vendedorTipoRepository.findById(vendedorDTO.getVendedorTipoId())
                .orElseThrow(() -> {
                    logger.error("Tipo de vendedor não encontrado com ID: {}", vendedorDTO.getVendedorTipoId());
                    return new EntityNotFoundException("Tipo de vendedor não encontrado com ID: " + vendedorDTO.getVendedorTipoId());
                });
        
        // Verificar se já existe vendedor com mesmo nome no mesmo tipo
        if (vendedorRepository.existsByNomeRazaoAndTipoAndNotId(
            vendedorDTO.getNomeRazao().trim(), vendedorDTO.getVendedorTipoId(), 0L)) {
            throw new IllegalArgumentException("Já existe um vendedor com este nome no mesmo tipo");
        }
        
        // Criar entidade
        Vendedor vendedor = new Vendedor();
        vendedor.setNomeRazao(vendedorDTO.getNomeRazao().trim());
        vendedor.setNomeFantasia(vendedorDTO.getNomeFantasia());
        vendedor.setCargoFuncao(vendedorDTO.getCargoFuncao());
        vendedor.setStatus(vendedorDTO.getStatus() != null ? vendedorDTO.getStatus() : "A");
        vendedor.setObservacao(vendedorDTO.getObservacao());
        vendedor.setVendedorTipo(vendedorTipo);
        vendedor.setDataCadastro(LocalDateTime.now());
        
        Vendedor salvo = vendedorRepository.save(vendedor);
        logger.info("Vendedor criado com ID: {}", salvo.getId());
        
        return toDTO(salvo);
    }
    
    // Atualizar vendedor
    @Transactional
    public VendedorDTO atualizar(Long id, VendedorDTO vendedorDTO) {
        logger.info("Atualizando vendedor ID: {}", id);
        
        // Buscar existente
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Vendedor não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Vendedor não encontrado com ID: " + id);
                });
        
        // Validar dados
        if (vendedorDTO.getNomeRazao() == null || vendedorDTO.getNomeRazao().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome/Razão Social é obrigatório");
        }
        
        Long tipoId = vendedorDTO.getVendedorTipoId() != null ? 
                     vendedorDTO.getVendedorTipoId() : 
                     (vendedor.getVendedorTipo() != null ? vendedor.getVendedorTipo().getId() : null);
        
        if (tipoId == null) {
            throw new IllegalArgumentException("Tipo de vendedor é obrigatório");
        }
        
        // Verificar se novo nome já existe (para outro registro no mesmo tipo)
        if (!vendedor.getNomeRazao().equalsIgnoreCase(vendedorDTO.getNomeRazao().trim())) {
            if (vendedorRepository.existsByNomeRazaoAndTipoAndNotId(
                vendedorDTO.getNomeRazao().trim(), tipoId, id)) {
                throw new IllegalArgumentException("Já existe outro vendedor com este nome no mesmo tipo");
            }
        }
        
        // Buscar tipo de vendedor (se alterado)
        VendedorTipo vendedorTipo = null;
        if (vendedorDTO.getVendedorTipoId() != null) {
            vendedorTipo = vendedorTipoRepository.findById(vendedorDTO.getVendedorTipoId())
                    .orElseThrow(() -> {
                        logger.error("Tipo de vendedor não encontrado com ID: {}", vendedorDTO.getVendedorTipoId());
                        return new EntityNotFoundException("Tipo de vendedor não encontrado com ID: " + vendedorDTO.getVendedorTipoId());
                    });
        } else {
            vendedorTipo = vendedor.getVendedorTipo();
        }
        
        // Atualizar campos
        vendedor.setNomeRazao(vendedorDTO.getNomeRazao().trim());
        vendedor.setNomeFantasia(vendedorDTO.getNomeFantasia());
        vendedor.setCargoFuncao(vendedorDTO.getCargoFuncao());
        vendedor.setStatus(vendedorDTO.getStatus() != null ? vendedorDTO.getStatus() : vendedor.getStatus());
        vendedor.setObservacao(vendedorDTO.getObservacao());
        vendedor.setVendedorTipo(vendedorTipo);
        
        Vendedor atualizado = vendedorRepository.save(vendedor);
        logger.info("Vendedor atualizado: {} - {}", atualizado.getId(), atualizado.getNomeRazao());
        
        return toDTO(atualizado);
    }
    
    // Excluir vendedor
    @Transactional
    public void excluir(Long id) {
        logger.info("Excluindo vendedor ID: {}", id);
        
        if (!vendedorRepository.existsById(id)) {
            logger.error("Vendedor não encontrado para exclusão: {}", id);
            throw new EntityNotFoundException("Vendedor não encontrado com ID: " + id);
        }
        
        vendedorRepository.deleteById(id);
        logger.info("Vendedor excluído: {}", id);
    }
    
    // Contar vendedores por tipo
    @Transactional(readOnly = true)
    public Long contarPorTipo(Long tipoId) {
        Long count = vendedorRepository.countByVendedorTipoId(tipoId);
        logger.info("Total de vendedores do tipo {}: {}", tipoId, count);
        return count;
    }
    
    // Contar vendedores ativos por tipo
    @Transactional(readOnly = true)
    public Long contarAtivosPorTipo(Long tipoId) {
        Long count = vendedorRepository.countAtivosByVendedorTipoId(tipoId);
        logger.info("Total de vendedores ativos do tipo {}: {}", tipoId, count);
        return count;
    }
    
    // Buscar todos ativos para dropdown
    @Transactional(readOnly = true)
    public List<VendedorResumoDTO> buscarAtivosParaDropdown() {
        logger.info("Buscando vendedores ativos para dropdown");
        
        List<Vendedor> vendedores = vendedorRepository.findByStatusOrderByNomeRazaoAsc("A");
        return vendedores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    // Buscar ativos por tipo para dropdown
    @Transactional(readOnly = true)
    public List<VendedorResumoDTO> buscarAtivosPorTipoParaDropdown(Long tipoId) {
        logger.info("Buscando vendedores ativos do tipo {} para dropdown", tipoId);
        
        List<Vendedor> vendedores = vendedorRepository.findByVendedorTipoIdAndStatus(tipoId, "A");
        return vendedores.stream()
                .map(this::toResumoDTO)
                .collect(Collectors.toList());
    }
    
    // Ativar/Inativar vendedor
    @Transactional
    public VendedorDTO alterarStatus(Long id, String status) {
        logger.info("Alterando status do vendedor ID: {} para {}", id, status);
        
        if (!"A".equals(status) && !"I".equals(status)) {
            throw new IllegalArgumentException("Status inválido. Use 'A' para Ativo ou 'I' para Inativo");
        }
        
        Vendedor vendedor = vendedorRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Vendedor não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Vendedor não encontrado com ID: " + id);
                });
        
        vendedor.setStatus(status);
        
        Vendedor atualizado = vendedorRepository.save(vendedor);
        logger.info("Status do vendedor {} alterado para {}", id, status);
        
        return toDTO(atualizado);
    }
}