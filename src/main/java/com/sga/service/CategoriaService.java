package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.CategoriaDTO;
import com.sga.dto.CategoriaResumoDTO;
import com.sga.model.Categoria;
import com.sga.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

	private CategoriaRepository categoriaRepository;

	@Transactional
	public CategoriaDTO criar(CategoriaDTO dto) {
		// Valida se já existe categoria com mesma descrição
		// (adicione validação por código se necessário)
		
		Categoria categoria = new Categoria();
		mapDtoToEntity(dto, categoria);
		// Note: Seu modelo não tem dataCadastro, mas o DTO tem
		// Se precisar, adicione o campo ao modelo
		categoria = categoriaRepository.save(categoria);
		return mapEntityToDto(categoria);
	}

	@Transactional
	public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
		mapDtoToEntity(dto, categoria);
		categoria = categoriaRepository.save(categoria);
		return mapEntityToDto(categoria);
	}

	public CategoriaDTO buscarPorId(Long id) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
		return mapEntityToDto(categoria);
	}

	public Page<CategoriaResumoDTO> listar(Pageable pageable, String descricao, String tipo, String status) {
		Page<Categoria> page;
		
		if (descricao != null && status != null) {
			page = categoriaRepository.findByDescricaoContainingIgnoreCaseAndStatus(descricao, status, pageable);
		} else if (descricao != null) {
			page = categoriaRepository.findByDescricaoContainingIgnoreCase(descricao, pageable);
		} else if (status != null) {
			page = categoriaRepository.findByStatus(status, pageable);
		} else if (tipo != null) {
			page = categoriaRepository.findByTipo(tipo, pageable);
		} else {
			page = categoriaRepository.findAll(pageable);
		}
		
		return page.map(this::mapEntityToResumoDto);
	}

	public List<CategoriaResumoDTO> listarTodasAtivas() {
		// Como o modelo não tem status, retornamos todas
		// Se precisar de status, adicione ao modelo
		return categoriaRepository.findAll().stream()
				.map(this::mapEntityToResumoDto)
				.collect(Collectors.toList());
	}

	public List<CategoriaResumoDTO> listarPorTipo(String tipo) {
		// Como o modelo não tem tipo, retornamos todas
		// Se precisar filtrar por tipo, adicione campo ao modelo
		return categoriaRepository.findAll().stream()
				.map(this::mapEntityToResumoDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public void excluir(Long id) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
		categoriaRepository.delete(categoria);
	}

	@Transactional
	public void inativar(Long id) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
		// Modelo não tem status, não é possível inativar
		// Se precisar, adicione campo status ao modelo
		throw new RuntimeException("Funcionalidade não disponível - modelo não possui campo status");
	}

	// Mapeamento DTO -> Entity (modelo simplificado)
	private void mapDtoToEntity(CategoriaDTO dto, Categoria entity) {
		entity.setId(dto.getId());
		entity.setDescricao(dto.getDescricao());
		
		// Campos do DTO que não existem no modelo:
		// codigo, tipo, status, nivel, subcategoriaDe, corHex, icone, observacoes, dataCadastro
		// Se precisar desses campos, adicione-os ao modelo Categoria
	}

	// Mapeamento Entity -> DTO
	private CategoriaDTO mapEntityToDto(Categoria entity) {
		CategoriaDTO dto = new CategoriaDTO();
		dto.setId(entity.getId());
		dto.setDescricao(entity.getDescricao());
		
		// Valores padrão para campos que não existem no modelo
		dto.setCodigo("CAT" + entity.getId()); // Exemplo: gera código baseado no ID
		dto.setTipo("GERAL"); // Valor padrão
		dto.setStatus("ATIVO"); // Valor padrão
		dto.setNivel(1); // Valor padrão
		dto.setDataCadastro(LocalDateTime.now()); // Data atual
		
		return dto;
	}

	// Mapeamento Entity -> ResumoDTO
	private CategoriaResumoDTO mapEntityToResumoDto(Categoria entity) {
		CategoriaResumoDTO dto = new CategoriaResumoDTO();
		dto.setId(entity.getId());
		dto.setDescricao(entity.getDescricao());
		dto.setCodigo("CAT" + entity.getId()); // Valor padrão
		dto.setTipo("GERAL"); // Valor padrão
		dto.setStatus("ATIVO"); // Valor padrão
		dto.setDataCadastro(LocalDateTime.now()); // Data atual
		return dto;
	}
}