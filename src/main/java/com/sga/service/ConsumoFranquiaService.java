package com.sga.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.ConsumoFranquiaDTO;
import com.sga.model.AssociadoPlano;
import com.sga.model.ConsumoFranquia;
import com.sga.model.PlanoProdutoFranquia;
import com.sga.repository.AssociadoPlanoRepository;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.ConsumoFranquiaRepository;
import com.sga.repository.PlanoProdutoFranquiaRepository;
import com.sga.repository.ProdutoRepository;

@Service
public class ConsumoFranquiaService {

	private final ConsumoFranquiaRepository consumoRepository;
	private final AssociadoRepository associadoRepository;
	private final AssociadoPlanoRepository associadoPlanoRepository;
	private final PlanoProdutoFranquiaRepository planoProdutoFranquiaRepository;
	private final ProdutoRepository produtoRepository;

	public ConsumoFranquiaService(ConsumoFranquiaRepository consumoRepository, AssociadoRepository associadoRepository,
			AssociadoPlanoRepository associadoPlanoRepository,
			PlanoProdutoFranquiaRepository planoProdutoFranquiaRepository, ProdutoRepository produtoRepository) {
		this.consumoRepository = consumoRepository;
		this.associadoRepository = associadoRepository;
		this.associadoPlanoRepository = associadoPlanoRepository;
		this.planoProdutoFranquiaRepository = planoProdutoFranquiaRepository;
		this.produtoRepository = produtoRepository;
	}

	@Transactional
	public ConsumoFranquiaDTO registrarConsumo(Long associadoId, Long produtoId, Integer quantidade, LocalDate data) {

		if (quantidade == null || quantidade <= 0) {
			throw new RuntimeException("Quantidade deve ser maior que zero");
		}

		// Buscar plano ativo do associado
		List<AssociadoPlano> planosAtivos = associadoPlanoRepository.findPlanosAtivosPorAssociado(associadoId);

		if (planosAtivos.isEmpty()) {
			throw new RuntimeException("Associado não possui plano ativo");
		}

		// Para simplificar, pegamos o primeiro plano ativo
		// Em um cenário real, pode ser necessário lógica para escolher o plano correto
		AssociadoPlano associadoPlano = planosAtivos.get(0);

		// Buscar configuração da franquia para este plano e produto
		PlanoProdutoFranquia ppf = planoProdutoFranquiaRepository
				.findByPlanoIdAndProdutoIdAndAtivoTrue(associadoPlano.getPlano().getId(), produtoId).orElseThrow(
						() -> new RuntimeException("Produto não possui franquia configurada no plano do associado"));

		YearMonth yearMonth = data != null ? YearMonth.from(data) : YearMonth.now();
		Integer ano = yearMonth.getYear();
		Integer mes = yearMonth.getMonthValue();

		// Buscar ou criar consumo do período
		ConsumoFranquia consumo = consumoRepository
				.findByAssociadoIdAndProdutoIdAndAnoAndMes(associadoId, produtoId, ano, mes).orElseGet(() -> {
					ConsumoFranquia novo = new ConsumoFranquia();
					novo.setAssociado(associadoRepository.getReferenceById(associadoId));
					novo.setPlano(associadoPlano.getPlano());
					novo.setProduto(produtoRepository.getReferenceById(produtoId));
					novo.setFranquia(ppf.getFranquia());
					novo.setAno(ano);
					novo.setMes(mes);
					novo.setLimiteFranquia(ppf.getLimiteFranquia());
					novo.setValorExcedente(ppf.getValorExcedente());
					novo.setQuantidadeUtilizada(0);
					novo.setQuantidadeExcedente(0);
					return novo;
				});

		// Calcular novo total
		int novoUtilizado = consumo.getQuantidadeUtilizada() + quantidade;
		int excedente = Math.max(0, novoUtilizado - consumo.getLimiteFranquia());

		consumo.setQuantidadeUtilizada(novoUtilizado);
		consumo.setQuantidadeExcedente(excedente);
		consumo.setDataUltimoConsumo(LocalDateTime.now());

		ConsumoFranquia saved = consumoRepository.save(consumo);

		return converterParaDTO(saved);
	}

	@Transactional
	public ConsumoFranquiaDTO registrarConsumoEmLote(Long associadoId, Long produtoId, Integer quantidade,
			LocalDate data) {
		// Versão otimizada para lote usando query de update
		YearMonth yearMonth = data != null ? YearMonth.from(data) : YearMonth.now();
		Integer ano = yearMonth.getYear();
		Integer mes = yearMonth.getMonthValue();

		int updated = consumoRepository.incrementarConsumo(associadoId, produtoId, ano, mes, quantidade);

		if (updated == 0) {
			// Se não existia, cria um novo registro
			return registrarConsumo(associadoId, produtoId, quantidade, data);
		}

		// Buscar o consumo atualizado
		return buscarConsumo(associadoId, produtoId, ano, mes);
	}

	public ConsumoFranquiaDTO buscarConsumo(Long associadoId, Long produtoId, Integer ano, Integer mes) {
		ConsumoFranquia consumo = consumoRepository
				.findByAssociadoIdAndProdutoIdAndAnoAndMes(associadoId, produtoId, ano, mes).orElse(null);

		if (consumo == null) {
			// Retornar consumo zerado
			ConsumoFranquiaDTO dto = new ConsumoFranquiaDTO();
			dto.setAssociadoId(associadoId);
			dto.setProdutoId(produtoId);
			dto.setAno(ano);
			dto.setMes(mes);
			dto.setUtilizado(0);
			dto.setLimite(0);
			dto.setExcedente(0);
			return dto;
		}

		return converterParaDTO(consumo);
	}

	public List<ConsumoFranquiaDTO> listarConsumosDoMes(Long associadoId, Integer ano, Integer mes) {
		return consumoRepository.findByAssociadoIdAndAnoAndMes(associadoId, ano, mes).stream()
				.map(this::converterParaDTO).collect(Collectors.toList());
	}

	public List<ConsumoFranquiaDTO> listarConsumosComExcedente(Long associadoId, Integer ano, Integer mes) {
		return consumoRepository.findConsumosComExcedente(associadoId, ano, mes).stream().map(this::converterParaDTO)
				.collect(Collectors.toList());
	}

	private ConsumoFranquiaDTO converterParaDTO(ConsumoFranquia consumo) {
		ConsumoFranquiaDTO dto = new ConsumoFranquiaDTO();
		dto.setId(consumo.getId());
		dto.setAssociadoId(consumo.getAssociado().getId());
		dto.setAssociadoNome(consumo.getAssociado().getNomeRazao());
		dto.setPlanoId(consumo.getPlano().getId());
		dto.setPlanoNome(consumo.getPlano().getPlano());
		dto.setProdutoId(consumo.getProduto().getId());
		dto.setProdutoNome(consumo.getProduto().getNome());
		dto.setFranquiaId(consumo.getFranquia().getId());
		dto.setFranquiaNome(consumo.getFranquia().getNome());
		dto.setAno(consumo.getAno());
		dto.setMes(consumo.getMes());
		dto.setUtilizado(consumo.getQuantidadeUtilizada());
		dto.setLimite(consumo.getLimiteFranquia());
		dto.setExcedente(consumo.getQuantidadeExcedente());
		dto.setValorExcedente(consumo.getValorExcedente());
		dto.setDataUltimoConsumo(consumo.getDataUltimoConsumo());
		return dto;
	}
}