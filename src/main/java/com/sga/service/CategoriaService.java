package com.sga.service;

import com.sga.dto.CategoriaDTO;
import com.sga.dto.CategoriaResumoDTO;
import com.sga.model.Categoria;
import com.sga.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaDTO criar(CategoriaDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setDescricao(dto.getDescricao());
        categoria = categoriaRepository.save(categoria);
        return mapEntityToDto(categoria);
    }

    @Transactional
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        categoria.setDescricao(dto.getDescricao());
        categoria = categoriaRepository.save(categoria);
        return mapEntityToDto(categoria);
    }

    public CategoriaDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada com ID: " + id));
        return mapEntityToDto(categoria);
    }

    public Page<CategoriaResumoDTO> listar(Pageable pageable, String descricao, String tipo, String status) {
        // Como o modelo não tem tipo ou status, filtrar apenas por descrição
        Page<Categoria> page;
        
        if (descricao != null) {
            page = categoriaRepository.findByDescricaoContainingIgnoreCase(descricao, pageable);
        } else {
            page = categoriaRepository.findAll(pageable);
        }
        
        return page.map(this::mapEntityToResumoDto);
    }

    // MÉTODO PARA COMBOBOX - todas as categorias (não tem status)
    public List<CategoriaResumoDTO> listarTodasAtivas() {
        List<Categoria> categorias = categoriaRepository.findAllByOrderByDescricaoAsc();
        return categorias.stream()
                .map(this::mapEntityToResumoDto)
                .collect(Collectors.toList());
    }

    public List<CategoriaResumoDTO> listarPorTipo(String tipo) {
        // Modelo não tem tipo, retorna todas
        return listarTodasAtivas();
    }

    @Transactional
    public void excluir(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoria não encontrada com ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }

    @Transactional
    public void inativar(Long id) {
        // Modelo não tem status, não pode inativar
        throw new RuntimeException("Categoria não possui campo status para inativação");
    }

    // ============== MAPEAMENTOS SIMPLIFICADOS ==============
    
    private CategoriaDTO mapEntityToDto(Categoria entity) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setId(entity.getId());
        dto.setDescricao(entity.getDescricao());
        dto.setTipo("GERAL"); // Valor padrão
        dto.setStatus("ATIVO"); // Valor padrão
        dto.setDataCadastro(LocalDateTime.now()); // Data atual
        return dto;
    }

    private CategoriaResumoDTO mapEntityToResumoDto(Categoria entity) {
        CategoriaResumoDTO dto = new CategoriaResumoDTO();
        dto.setId(entity.getId());
        dto.setDescricao(entity.getDescricao());
        dto.setTipo("GERAL"); // Valor padrão
        dto.setStatus("ATIVO"); // Valor padrão
        dto.setDataCadastro(LocalDateTime.now()); // Data atual
        return dto;
    }
}