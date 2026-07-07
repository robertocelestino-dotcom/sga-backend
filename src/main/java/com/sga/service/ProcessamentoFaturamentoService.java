package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import com.sga.model.LoteProcessamento;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ReguaFaturamento;
import com.sga.repository.FaturaRepository;
import com.sga.repository.LoteProcessamentoRepository;
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

	@Autowired
	private FaturaRepository faturaRepository;

	@Autowired
	private LoteProcessamentoRepository loteProcessamentoRepository;

	@Transactional
	public ResultadoProcessamento processarFaturamento(ProcessamentoRequest request) {
		
		log.info("🚀 Processando faturamento: {}", request);

		ResultadoProcessamento resultado = new ResultadoProcessamento();
		resultado.setDataProcessamento(LocalDateTime.now());
		resultado.setErros(new ArrayList<>());
		resultado.setDetalhes(new ArrayList<>());
		resultado.setAssociadosJaFaturados(0L);

		if (request.getAssociadosIds() == null || request.getAssociadosIds().isEmpty()) {
			resultado.getErros().add("Nenhum associado selecionado para processamento");
			return resultado;
		}

		resultado.setTotalAssociados((long) request.getAssociadosIds().size());

		// ========== 1. BUSCAR A RÉGUA ==========
		ReguaFaturamento regua = null;
		if (request.getReguaId() != null) {
			var reguaOpt = reguaService.buscarPorId(request.getReguaId());
			if (reguaOpt.isPresent()) {
				regua = reguaOpt.get();
				log.info("✅ Régua encontrada: {} (ID: {})", regua.getNome(), regua.getId());
			} else {
				resultado.getErros().add("Régua não encontrada: " + request.getReguaId());
				return resultado;
			}
		} else {
			// FALLBACK: Buscar régua padrão
			var reguaPadraoOpt = reguaService.buscarReguaPadrao();
			if (reguaPadraoOpt.isPresent()) {
				regua = reguaPadraoOpt.get();
				log.info("✅ Usando régua padrão: {} (ID: {})", regua.getNome(), regua.getId());
			} else {
				resultado.getErros().add("Nenhuma régua selecionada e nenhuma régua padrão encontrada");
				return resultado;
			}
		}

		// ========== 2. DEFINIR MÊS REFERÊNCIA ==========
		LocalDate mesReferencia;
		if (request.getMes() != null && request.getAno() != null) {
			mesReferencia = LocalDate.of(request.getAno(), request.getMes(), 1);
		} else if (request.getDataEmissao() != null) {
			mesReferencia = request.getDataEmissao().withDayOfMonth(1);
		} else {
			mesReferencia = LocalDate.now().withDayOfMonth(1);
		}
		log.info("📅 Mês referência: {}", mesReferencia);

		// ========== 3. VERIFICAR SE JÁ EXISTE LOTE PARA ESTE PERÍODO E RÉGUA ==========
		LoteProcessamento lote = null;
		
		// 🔥 CORRIGIDO: Buscar por MÊS/ANO (ignorando o dia)
		Integer mes = mesReferencia.getMonthValue();
		Integer ano = mesReferencia.getYear();
		
		log.info("📅 Buscando lote por MÊS/ANO: {}/{} (ignorando dia)", mes, ano);
		
		// Buscar lote existente para este mês/ano e régua
		List<LoteProcessamento> lotesExistentes = loteProcessamentoRepository
			.findByMesAnoAndReguaId(mes, ano, regua.getId());
		
		// Filtrar apenas lotes que não estão em ERRO
		lotesExistentes = lotesExistentes.stream()
			.filter(l -> !"ERRO".equals(l.getStatus()))
			.collect(Collectors.toList());

		if (!lotesExistentes.isEmpty()) {
			// Reutilizar o lote mais recente
			lote = lotesExistentes.get(lotesExistentes.size() - 1);
			log.info("♻️ Reutilizando lote existente ID: {}, Régua: {}, Mês: {}, Status: {}, Faturas: {}", 
				lote.getId(), 
				lote.getRegua().getNome(),
				lote.getMesReferencia(),
				lote.getStatus(),
				lote.getTotalFaturasGeradas());
			
			// Se o lote já está CONCLUIDO, resetar para processar novamente
			if ("CONCLUIDO".equals(lote.getStatus())) {
				log.info("🔄 Lote já concluído, reiniciando processamento...");
				lote.setStatus("EM_PROCESSAMENTO");
				lote.setDataInicioProcessamento(LocalDateTime.now());
				lote.setTotalFaturasGeradas(0);
				lote.setTotalErros(0);
				lote.setTotalCancelamentosAplicados(0);
				loteProcessamentoRepository.save(lote);
			}
		} else {
			// ========== 4. CRIAR NOVO LOTE (APENAS AQUI) ==========
			lote = new LoteProcessamento();
			lote.setDataInicioProcessamento(LocalDateTime.now());
			lote.setUsuarioProcessamento(request.getUsuario() != null ? request.getUsuario() : "SISTEMA");
			lote.setStatus("EM_PROCESSAMENTO");
			lote.setCriadoEm(LocalDateTime.now());
			lote.setTotalNotasProcessadas(0);
			lote.setTotalFaturasGeradas(0);
			lote.setTotalCancelamentosAplicados(0);
			lote.setTotalErros(0);
			lote.setRegua(regua);
			lote.setMesReferencia(mesReferencia); // Já está com dia 1
			
			loteProcessamentoRepository.save(lote);
			log.info("✅ NOVO lote criado com ID: {}, Régua: {} (ID: {}), Mês: {}", 
				lote.getId(), lote.getRegua().getNome(), lote.getRegua().getId(), lote.getMesReferencia());
		}

		// ========== 5. PROCESSAR ASSOCIADOS ==========
		long processados = 0;
		long erros = 0;
		long jaFaturados = 0;
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
				detalhe.setCodigoSpc(associado.getCodigoSpc());
				detalhe.setCodigoRm(associado.getCodigoRm());

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

				List<NotaDebitoSPC> notas = notaDebitoRepository.findByCodigoSocioWithPadding(codigoSpc);

				if (notas == null || notas.isEmpty()) {
					log.warn("Nenhuma nota encontrada para associado: {} - código SPC: {}", 
						associado.getNomeRazao(), codigoSpc);
					detalhe.setMensagemErro("Nenhuma nota de débito encontrada para o código SPC: " + codigoSpc);
					detalhe.setProcessado(false);
					resultado.getDetalhes().add(detalhe);
					erros++;
					continue;
				}

				log.info("📄 Encontradas {} notas para o associado: {}", notas.size(), associado.getNomeRazao());

				Fatura fatura = faturaGeracaoService.gerarFatura(
					associado, 
					notas, 
					regua, 
					request.getMes(), 
					request.getAno(),
					request.getDataEmissao(),
					request.getDataVencimento(),
					isSimulacao,
					request.getUsuario() != null ? request.getUsuario() : "SISTEMA"
				);

				if (fatura != null) {
					// ASSOCIAR A FATURA AO LOTE
					if (!isSimulacao) {
						fatura.setLoteProcessamentoId(lote.getId());
						fatura = faturaRepository.save(fatura);
					}
					
					faturasGeradas.add(fatura);
					processados++;
					
					detalhe.setValorNota(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
					detalhe.setValorDebito(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
					detalhe.setProcessado(true);
					detalhe.setGerouNota(true);
					
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
					log.info("✅ Fatura gerada para {}: R$ {}", associado.getNomeRazao(), detalhe.getValorNota());
				} else {
					detalhe.setMensagemErro("Associado já possui fatura para este período");
					detalhe.setProcessado(false);
					jaFaturados++;
					resultado.getErros().add("Associado " + associadoId + " já possui fatura para o período");
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

		// ========== 6. ATUALIZAR LOTE COM RESULTADOS ==========
		lote.setTotalFaturasGeradas((int) faturasGeradas.size());
		lote.setTotalErros((int) erros);
		lote.setDataFimProcessamento(LocalDateTime.now());
		lote.setStatus("CONCLUIDO");
		loteProcessamentoRepository.save(lote);
		
		log.info("📊 Lote {} finalizado: {} faturas, {} erros", 
			lote.getId(), faturasGeradas.size(), erros);

		// ========== 7. PREENCHER RESULTADO ==========
		resultado.setAssociadosProcessados(processados);
		resultado.setAssociadosComErro(erros);
		resultado.setAssociadosJaFaturados(jaFaturados);
		resultado.setTotalNotasGeradas((long) faturasGeradas.size());
		resultado.setValorTotalDebito(totalDebito);

		log.info("✅ Processamento concluído: {} processados, {} erros, {} já faturados, {} faturas geradas, valor total: R$ {}",
				processados, erros, jaFaturados, faturasGeradas.size(), totalDebito);
		
		log.info("📊 Detalhes preenchidos: {} registros", resultado.getDetalhes().size());

		return resultado;
		
	}
	
	@Transactional
	public ResultadoProcessamento processarFaturamentoComNotificacoes(
	        ProcessamentoRequest request, String usuario) {
	    
	    log.info("🚀 Processando faturamento COM NOTIFICAÇÕES: {}", request);

	    ResultadoProcessamento resultado = new ResultadoProcessamento();
	    resultado.setDataProcessamento(LocalDateTime.now());
	    resultado.setErros(new ArrayList<>());
	    resultado.setDetalhes(new ArrayList<>());
	    resultado.setAssociadosJaFaturados(0L);

	    if (request.getAssociadosIds() == null || request.getAssociadosIds().isEmpty()) {
	        resultado.getErros().add("Nenhum associado selecionado para processamento");
	        return resultado;
	    }

	    resultado.setTotalAssociados((long) request.getAssociadosIds().size());

	    // ========== 1. BUSCAR A RÉGUA ==========
	    ReguaFaturamento regua = null;
	    if (request.getReguaId() != null) {
	        var reguaOpt = reguaService.buscarPorId(request.getReguaId());
	        if (reguaOpt.isPresent()) {
	            regua = reguaOpt.get();
	            log.info("✅ Régua encontrada: {} (ID: {})", regua.getNome(), regua.getId());
	        } else {
	            resultado.getErros().add("Régua não encontrada: " + request.getReguaId());
	            return resultado;
	        }
	    } else {
	        var reguaPadraoOpt = reguaService.buscarReguaPadrao();
	        if (reguaPadraoOpt.isPresent()) {
	            regua = reguaPadraoOpt.get();
	            log.info("✅ Usando régua padrão: {} (ID: {})", regua.getNome(), regua.getId());
	        } else {
	            resultado.getErros().add("Nenhuma régua selecionada e nenhuma régua padrão encontrada");
	            return resultado;
	        }
	    }

	    // ========== 2. DEFINIR MÊS REFERÊNCIA ==========
	    LocalDate mesReferencia;
	    if (request.getMes() != null && request.getAno() != null) {
	        mesReferencia = LocalDate.of(request.getAno(), request.getMes(), 1);
	    } else if (request.getDataEmissao() != null) {
	        mesReferencia = request.getDataEmissao().withDayOfMonth(1);
	    } else {
	        mesReferencia = LocalDate.now().withDayOfMonth(1);
	    }
	    log.info("📅 Mês referência: {}", mesReferencia);

	    Integer mes = mesReferencia.getMonthValue();
	    Integer ano = mesReferencia.getYear();

	    // ========== 3. VERIFICAR SE JÁ EXISTE LOTE ==========
	    LoteProcessamento lote = null;
	    List<LoteProcessamento> lotesExistentes = loteProcessamentoRepository
	        .findByMesAnoAndReguaId(mes, ano, regua.getId());
	    
	    lotesExistentes = lotesExistentes.stream()
	        .filter(l -> !"ERRO".equals(l.getStatus()))
	        .collect(Collectors.toList());

	    if (!lotesExistentes.isEmpty()) {
	        lote = lotesExistentes.get(lotesExistentes.size() - 1);
	        log.info("♻️ Reutilizando lote existente ID: {}", lote.getId());
	        
	        if ("CONCLUIDO".equals(lote.getStatus())) {
	            log.info("🔄 Lote já concluído, reiniciando processamento...");
	            lote.setStatus("EM_PROCESSAMENTO");
	            lote.setDataInicioProcessamento(LocalDateTime.now());
	            lote.setTotalFaturasGeradas(0);
	            lote.setTotalErros(0);
	            lote.setTotalCancelamentosAplicados(0);
	            loteProcessamentoRepository.save(lote);
	        }
	    } else {
	        lote = new LoteProcessamento();
	        lote.setDataInicioProcessamento(LocalDateTime.now());
	        lote.setUsuarioProcessamento(request.getUsuario() != null ? request.getUsuario() : "SISTEMA");
	        lote.setStatus("EM_PROCESSAMENTO");
	        lote.setCriadoEm(LocalDateTime.now());
	        lote.setTotalNotasProcessadas(0);
	        lote.setTotalFaturasGeradas(0);
	        lote.setTotalCancelamentosAplicados(0);
	        lote.setTotalErros(0);
	        lote.setRegua(regua);
	        lote.setMesReferencia(mesReferencia);
	        loteProcessamentoRepository.save(lote);
	        log.info("✅ NOVO lote criado ID: {}", lote.getId());
	    }

	    // ========== 4. PROCESSAR ASSOCIADOS ==========
	    long processados = 0;
	    long erros = 0;
	    long jaFaturados = 0;
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
	            detalhe.setCodigoSpc(associado.getCodigoSpc());
	            detalhe.setCodigoRm(associado.getCodigoRm());

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

	            List<NotaDebitoSPC> notas = notaDebitoRepository.findByCodigoSocioWithPadding(codigoSpc);

	            if (notas == null || notas.isEmpty()) {
	                log.warn("Nenhuma nota encontrada para associado: {}", associado.getNomeRazao());
	                detalhe.setMensagemErro("Nenhuma nota de débito encontrada para o código SPC: " + codigoSpc);
	                detalhe.setProcessado(false);
	                resultado.getDetalhes().add(detalhe);
	                erros++;
	                continue;
	            }

	            log.info("📄 Encontradas {} notas para o associado: {}", notas.size(), associado.getNomeRazao());

	            // 🔥 GERAR FATURA COM NOTIFICAÇÕES
	            Fatura fatura = faturaGeracaoService.gerarFatura(
	                associado, 
	                notas, 
	                regua, 
	                mes, 
	                ano,
	                request.getDataEmissao(),
	                request.getDataVencimento(),
	                isSimulacao,
	                request.getUsuario() != null ? request.getUsuario() : "SISTEMA"
	            );

	            if (fatura != null) {
	                if (!isSimulacao) {
	                    fatura.setLoteProcessamentoId(lote.getId());
	                    fatura = faturaRepository.save(fatura);
	                }
	                
	                faturasGeradas.add(fatura);
	                processados++;
	                
	                detalhe.setValorNota(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
	                detalhe.setValorDebito(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
	                detalhe.setProcessado(true);
	                detalhe.setGerouNota(true);
	                
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
	                log.info("✅ Fatura gerada para {}: R$ {}", associado.getNomeRazao(), detalhe.getValorNota());
	            } else {
	                detalhe.setMensagemErro("Associado já possui fatura para este período");
	                detalhe.setProcessado(false);
	                jaFaturados++;
	                resultado.getErros().add("Associado " + associadoId + " já possui fatura para o período");
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

	    // ========== 5. ATUALIZAR LOTE ==========
	    lote.setTotalFaturasGeradas((int) faturasGeradas.size());
	    lote.setTotalErros((int) erros);
	    lote.setDataFimProcessamento(LocalDateTime.now());
	    lote.setStatus("CONCLUIDO");
	    loteProcessamentoRepository.save(lote);
	    
	    log.info("📊 Lote {} finalizado: {} faturas, {} erros", lote.getId(), faturasGeradas.size(), erros);

	    // ========== 6. PREENCHER RESULTADO ==========
	    resultado.setAssociadosProcessados(processados);
	    resultado.setAssociadosComErro(erros);
	    resultado.setAssociadosJaFaturados(jaFaturados);
	    resultado.setTotalNotasGeradas((long) faturasGeradas.size());
	    resultado.setValorTotalDebito(totalDebito);

	    log.info("✅ Processamento COM NOTIFICAÇÕES concluído: {} processados, {} erros, {} já faturados, {} faturas geradas, valor total: R$ {}",
	            processados, erros, jaFaturados, faturasGeradas.size(), totalDebito);

	    return resultado;
	}
}