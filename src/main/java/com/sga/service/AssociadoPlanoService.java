package com.sga.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoPlanoDTO;
import com.sga.dto.PlanoProdutoFranquiaDTO;
import com.sga.model.Associado;
import com.sga.model.AssociadoPlano;
import com.sga.model.Planos;
import com.sga.repository.AssociadoPlanoRepository;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.PlanosRepository;

@Service
public class AssociadoPlanoService {

	private final AssociadoPlanoRepository repository;
	private final AssociadoRepository associadoRepository;
	private final PlanosRepository planosRepository;
	private final PlanoProdutoFranquiaService planoProdutoFranquiaService;

	public AssociadoPlanoService(AssociadoPlanoRepository repository, AssociadoRepository associadoRepository,
			PlanosRepository planosRepository, PlanoProdutoFranquiaService planoProdutoFranquiaService) {
		this.repository = repository;
		this.associadoRepository = associadoRepository;
		this.planosRepository = planosRepository;
		this.planoProdutoFranquiaService = planoProdutoFranquiaService;
	}

	@Transactional
	public AssociadoPlanoDTO associarPlano(Long associadoId, Long planoId, LocalDate dataAdesao, String observacao) {

		// Verificar se associado já possui este plano ativo
		if (repository.associadoPossuiPlanoAtivo(associadoId, planoId)) {
			throw new RuntimeException("Associado já possui este plano ativo");
		}

		Associado associado = associadoRepository.findById(associadoId)
				.orElseThrow(() -> new RuntimeException("Associado não encontrado com ID: " + associadoId));

		Planos plano = planosRepository.findById(planoId)
				.orElseThrow(() -> new RuntimeException("Plano não encontrado com ID: " + planoId));

		AssociadoPlano associadoPlano = new AssociadoPlano();
		associadoPlano.setAssociado(associado);
		associadoPlano.setPlano(plano);
		associadoPlano.setDataAdesao(dataAdesao != null ? dataAdesao : LocalDate.now());
		associadoPlano.setStatus("ATIVO");
		associadoPlano.setObservacao(observacao);

		AssociadoPlano saved = repository.save(associadoPlano);
		return converterParaDTO(saved);
	}

	@Transactional
	public AssociadoPlanoDTO cancelarPlano(Long id, LocalDate dataCancelamento, String motivo) {
		AssociadoPlano associadoPlano = repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Associação plano não encontrada com ID: " + id));

		associadoPlano.setStatus("CANCELADO");
		associadoPlano.setDataCancelamento(dataCancelamento != null ? dataCancelamento : LocalDate.now());
		associadoPlano.setObservacao(motivo);

		AssociadoPlano updated = repository.save(associadoPlano);
		return converterParaDTO(updated);
	}

	public List<AssociadoPlanoDTO> listarPlanosDoAssociado(Long associadoId) {
		return repository.findByAssociadoIdAndStatus(associadoId, "ATIVO").stream().map(this::converterParaDTO)
				.collect(Collectors.toList());
	}

	public List<AssociadoPlanoDTO> listarPlanosAtivos(Long associadoId) {
		return repository.findPlanosAtivosPorAssociado(associadoId).stream().map(this::converterParaDTO)
				.collect(Collectors.toList());
	}

	private AssociadoPlanoDTO converterParaDTO(AssociadoPlano associadoPlano) {
		AssociadoPlanoDTO dto = new AssociadoPlanoDTO();
		dto.setId(associadoPlano.getId());
		dto.setAssociadoId(associadoPlano.getAssociado().getId());
		dto.setAssociadoNome(associadoPlano.getAssociado().getNomeRazao());
		dto.setPlanoId(associadoPlano.getPlano().getId());
		dto.setPlanoNome(associadoPlano.getPlano().getPlano());
		dto.setPlanoValor(associadoPlano.getPlano().getValor());
		dto.setDataAdesao(associadoPlano.getDataAdesao());
		dto.setDataCancelamento(associadoPlano.getDataCancelamento());
		dto.setStatus(associadoPlano.getStatus());
		dto.setObservacao(associadoPlano.getObservacao());

		// Buscar produtos do plano com suas franquias
		List<PlanoProdutoFranquiaDTO> produtos = planoProdutoFranquiaService
				.listarPorPlano(associadoPlano.getPlano().getId());
		dto.setProdutosDoPlano(produtos);

		return dto;
	}
}
