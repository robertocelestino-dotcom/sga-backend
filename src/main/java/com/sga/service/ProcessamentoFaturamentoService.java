package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoProcessamentoDTO;
import com.sga.dto.FaturaItemDTO;
import com.sga.dto.ProcessamentoRequest;
import com.sga.dto.ResultadoProcessamento;
import com.sga.model.Associado;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ReguaFaturamento;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class ProcessamentoFaturamentoService {

	private static final Logger log = LoggerFactory.getLogger(ProcessamentoFaturamentoService.class);

	@Autowired
	private ReguaFaturamentoService reguaService;

	@Autowired
	private AssociadoService associadoService;

	@Autowired
	private NotaDebitoSPCRepository notaDebitoRepository;

	@Autowired
	private FaturaGeracaoService faturaGeracaoService;

	@Transactional
	public ResultadoProcessamento processarFaturamento(ProcessamentoRequest request) {
		log.info("🚀 Processando faturamento: {}", request);

		ResultadoProcessamento resultado = new ResultadoProcessamento();
		resultado.setDataProcessamento(LocalDateTime.now());
		resultado.setErros(new ArrayList<>());
		resultado.setDetalhes(new ArrayList<>());

		if (request.getAssociadosIds() == null || request.getAssociadosIds().isEmpty()) {
			resultado.getErros().add("Nenhum associado selecionado para processamento");
			return resultado;
		}

		resultado.setTotalAssociados((long) request.getAssociadosIds().size());

		ReguaFaturamento regua = null;
		if (request.getReguaId() != null) {
			var reguaOpt = reguaService.buscarPorId(request.getReguaId());
			if (reguaOpt.isPresent()) {
				regua = reguaOpt.get();
			} else {
				resultado.getErros().add("Régua não encontrada: " + request.getReguaId());
				return resultado;
			}
		}

		long processados = 0;
		long erros = 0;
		List<Fatura> faturasGeradas = new ArrayList<>();
		BigDecimal totalDebito = BigDecimal.ZERO;

		boolean isSimulacao = request.getSimular() != null && request.getSimular();

		for (Long associadoId : request.getAssociadosIds()) {
			AssociadoProcessamentoDTO detalhe = new AssociadoProcessamentoDTO();
			detalhe.setAssociadoId(associadoId);
			
			try {
				Associado associado = associadoService.buscarPorIdEntity(associadoId);
				
				detalhe.setAssociadoNome(associado.getNomeRazao());
				detalhe.setCnpjCpf(associado.getCnpjCpf());

				String codigoSpc = associado.getCodigoSpc();
				if (codigoSpc == null || codigoSpc.trim().isEmpty()) {
					log.warn("Associado {} sem código SPC", associadoId);
					detalhe.setMensagemErro("Associado sem código SPC");
					detalhe.setProcessado(false);
					resultado.getDetalhes().add(detalhe);
					resultado.getErros().add("Associado " + associadoId + " sem código SPC");
					erros++;
					continue;
				}

				List<NotaDebitoSPC> notas = notaDebitoRepository.findByCodigoSocio(codigoSpc);

				if (notas == null || notas.isEmpty()) {
					log.warn("Nenhuma nota encontrada para associado: {} - código SPC: {}", associado.getNomeRazao(), codigoSpc);
					detalhe.setMensagemErro("Nenhuma nota de débito encontrada");
					detalhe.setProcessado(false);
					resultado.getDetalhes().add(detalhe);
					erros++;
					continue;
				}

				Fatura fatura = faturaGeracaoService.gerarFatura(
					associado, 
					notas, 
					regua, 
					request.getMes(), 
					request.getAno(), 
					isSimulacao,
					request.getUsuario() != null ? request.getUsuario() : "SISTEMA"
				);

				if (fatura != null) {
					faturasGeradas.add(fatura);
					processados++;
					
					detalhe.setValorNota(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
					detalhe.setValorDebito(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
					detalhe.setProcessado(true);
					detalhe.setGerouNota(true);
					
					// 🔥 Preencher os itens da fatura
					if (fatura.getItens() != null && !fatura.getItens().isEmpty()) {
						for (FaturaItem item : fatura.getItens()) {
							FaturaItemDTO itemDTO = new FaturaItemDTO();
							itemDTO.setCodigoProduto(item.getCodigoProduto());
							itemDTO.setDescricao(item.getDescricao());
							itemDTO.setQuantidade(item.getQuantidade());
							itemDTO.setValorUnitario(item.getValorUnitario());
							itemDTO.setValorTotal(item.getValorTotal());
							itemDTO.setTipoLancamento(item.getTipoLancamento());
							detalhe.addItemFatura(itemDTO);
						}
					}
					
					totalDebito = totalDebito.add(detalhe.getValorDebito());
				} else {
					detalhe.setMensagemErro("Fatura não gerada");
					detalhe.setProcessado(false);
					erros++;
					resultado.getErros().add("Fatura não gerada para associado " + associadoId);
				}
				
				resultado.getDetalhes().add(detalhe);

			} catch (Exception e) {
				log.error("Erro ao processar associado {}: {}", associadoId, e.getMessage());
				detalhe.setAssociadoNome("Associado ID: " + associadoId);
				detalhe.setMensagemErro(e.getMessage());
				detalhe.setProcessado(false);
				resultado.getDetalhes().add(detalhe);
				erros++;
				resultado.getErros().add("Erro no associado " + associadoId + ": " + e.getMessage());
			}
		}

		resultado.setAssociadosProcessados(processados);
		resultado.setAssociadosComErro(erros);
		resultado.setTotalNotasGeradas((long) faturasGeradas.size());
		resultado.setValorTotalDebito(totalDebito);

		log.info("✅ Processamento concluído: {} processados, {} erros, {} faturas geradas, valor total: R$ {}",
				processados, erros, faturasGeradas.size(), totalDebito);
		
		log.info("📊 Detalhes preenchidos: {} registros", resultado.getDetalhes().size());

		return resultado;
	}
}