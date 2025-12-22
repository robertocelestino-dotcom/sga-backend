package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.VendedorDTO;
import com.sga.dto.VendedorResumoDTO;
import com.sga.model.Vendedor;
import com.sga.repository.VendedorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendedorService {

	private VendedorRepository vendedorRepository;

	@Transactional
	public VendedorDTO criar(VendedorDTO dto) {
		Vendedor vendedor = new Vendedor();
		mapDtoToEntity(dto, vendedor);
		vendedor.setDataCadastro(LocalDateTime.now());
		vendedor = vendedorRepository.save(vendedor);
		return mapEntityToDto(vendedor);
	}

	@Transactional
	public VendedorDTO atualizar(Long id, VendedorDTO dto) {
		Vendedor vendedor = vendedorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
		mapDtoToEntity(dto, vendedor);
		vendedor = vendedorRepository.save(vendedor);
		return mapEntityToDto(vendedor);
	}

	public VendedorDTO buscarPorId(Long id) {
		Vendedor vendedor = vendedorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
		return mapEntityToDto(vendedor);
	}

	public Page<VendedorResumoDTO> listar(Pageable pageable, String nome, String status) {
		Page<Vendedor> page;
		if (nome != null && status != null) {
			page = vendedorRepository.findByNomeRazaoContainingIgnoreCaseAndStatus(nome, status, pageable);
		} else if (nome != null) {
			page = vendedorRepository.findByNomeRazaoContainingIgnoreCase(nome, pageable);
		} else if (status != null) {
			page = vendedorRepository.findByStatus(status, pageable);
		} else {
			page = vendedorRepository.findAll(pageable);
		}
		return page.map(this::mapEntityToResumoDto);
	}

	public List<VendedorResumoDTO> listarTodosAtivos() {
		// Modelo usa "A" para ativo (não "ATIVO")
		return vendedorRepository.findByStatus("A").stream()
				.map(this::mapEntityToResumoDto)
				.collect(Collectors.toList());
	}

	@Transactional
	public void excluir(Long id) {
		Vendedor vendedor = vendedorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
		vendedorRepository.delete(vendedor);
	}

	@Transactional
	public void inativar(Long id) {
		Vendedor vendedor = vendedorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
		vendedor.setStatus("I"); // Modelo usa "I" para inativo
		vendedorRepository.save(vendedor);
	}

	private void mapDtoToEntity(VendedorDTO dto, Vendedor entity) {
		entity.setId(dto.getId());
		entity.setNomeRazao(dto.getNomeRazao());
		entity.setNomeFantasia(dto.getNomeFantasia());
		entity.setCargoFuncao(dto.getCargoFuncao());
		entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "A");
		entity.setObservacao(dto.getObservacoes()); // DTO: observacoes, Modelo: observacao
		
		// dataCadastro é definido no criar() ou atualizado se já existir
		if (dto.getDataCadastro() != null) {
			entity.setDataCadastro(dto.getDataCadastro());
		}
	}

	private VendedorDTO mapEntityToDto(Vendedor entity) {
		VendedorDTO dto = new VendedorDTO();
		dto.setId(entity.getId());
		dto.setNomeRazao(entity.getNomeRazao());
		dto.setNomeFantasia(entity.getNomeFantasia());
		dto.setCargoFuncao(entity.getCargoFuncao());
		dto.setStatus(entity.getStatus());
		dto.setObservacoes(entity.getObservacao()); // Modelo: observacao, DTO: observacoes
		dto.setDataCadastro(entity.getDataCadastro());
		return dto;
	}

	private VendedorResumoDTO mapEntityToResumoDto(Vendedor entity) {
		return new VendedorResumoDTO(
				entity.getId(),
				entity.getNomeRazao(),
				entity.getCargoFuncao(),
				entity.getStatus(),
				entity.getDataCadastro()
		);
	}
}