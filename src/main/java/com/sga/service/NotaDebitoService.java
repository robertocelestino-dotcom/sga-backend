package com.sga.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.sga.dto.NotaDebitoDetalheDTO;
import com.sga.dto.NotaDebitoResumoDTO;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.ItemSPCRepository;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class NotaDebitoService {

	@Autowired
	private NotaDebitoSPCRepository notaRepo;

	@Autowired
	private ItemSPCRepository itemRepo;

	private BigDecimal safeZero(BigDecimal b) {
		return b == null ? BigDecimal.ZERO : b;
	}

	public Page<NotaDebitoResumoDTO> listarNotasPaginado(Long importacaoId, int page, int size, String filtro) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

		Page<NotaDebitoSPC> notas;
		if (filtro == null || filtro.trim().isEmpty()) {
			notas = notaRepo.findByImportacao_Id(importacaoId, pageable);
		} else {
			notas = notaRepo.findByImportacaoIdAndFiltro(importacaoId, filtro.trim(), pageable);
		}

		return notas.map(n -> {
			// calcular débitos/creditos usando itemRepo para garantir consistência com o banco
			BigDecimal deb = itemRepo.sumDebitosByNotaId(n.getId());
			BigDecimal cred = itemRepo.sumCreditosByNotaId(n.getId());
			BigDecimal faturado = safeZero(deb).subtract(safeZero(cred));
			return new NotaDebitoResumoDTO(n.getId(), n.getNumeroNotaDebito(), n.getCodigoSocio(), n.getNomeAssociado(),
					safeZero(deb), safeZero(cred), faturado);
		});
	}

	public NotaDebitoDetalheDTO buscarDetalhes(Long notaId) {
		NotaDebitoSPC n = notaRepo.findById(notaId)
				.orElseThrow(() -> new RuntimeException("Nota não encontrada: " + notaId));
		NotaDebitoDetalheDTO dto = new NotaDebitoDetalheDTO();
		dto.id = n.getId();
		dto.numeroNota = n.getNumeroNotaDebito();
		dto.codigoSocio = n.getCodigoSocio();
		dto.nomeAssociado = n.getNomeAssociado();
		List<ItemSPC> itens = n.getItens();
		dto.itens = itens;
		BigDecimal deb = itens.stream().filter(i -> "D".equalsIgnoreCase(i.getCreditoDebito()))
				.map(ItemSPC::getValorTotal).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal cred = itens.stream().filter(i -> "C".equalsIgnoreCase(i.getCreditoDebito()))
				.map(ItemSPC::getValorTotal).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
		dto.totalDebitos = deb;
		dto.totalCreditos = cred;
		dto.valorFaturado = deb.subtract(cred);
		return dto;
	}
}
