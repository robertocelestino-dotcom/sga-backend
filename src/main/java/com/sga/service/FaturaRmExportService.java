// src/main/java/com/sga/service/FaturaRmExportService.java

package com.sga.service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.model.ConfiguracaoRm;
import com.sga.model.Fatura;
import com.sga.model.FaturaItem;
import com.sga.model.LoteProcessamento;
import com.sga.model.ReguaFaturamento;
import com.sga.repository.FaturaItemRepository;
import com.sga.repository.FaturaRepository;
import com.sga.repository.LoteProcessamentoRepository;
import com.sga.repository.ReguaFaturamentoRepository;

@Service
public class FaturaRmExportService {

	private static final Logger logger = LoggerFactory.getLogger(FaturaRmExportService.class);

	@Autowired
	private FaturaRepository faturaRepository;

	@Autowired
	private FaturaItemRepository faturaItemRepository;

	@Autowired
	private LoteProcessamentoRepository loteProcessamentoRepository;

	@Autowired
	private ConfiguracaoRmService configuracaoRmService;
	
	@Autowired
	private ReguaFaturamentoRepository reguaFaturamentoRepository;

	private final DecimalFormat valorFormat = new DecimalFormat("#0.00", new DecimalFormatSymbols(Locale.US));
	private final NumberFormat quantidadeFormat = NumberFormat.getInstance(Locale.US);

	private int ultimoNumeroRPS = 0;

	private static final String DATA_EMISSAO_PADRAO = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

	{
		quantidadeFormat.setMinimumFractionDigits(2);
		quantidadeFormat.setMaximumFractionDigits(2);
		quantidadeFormat.setGroupingUsed(false);
	}

	private static final Long CONFIGURACAO_RM_PADRAO_ID = 6L;

	private ConfiguracaoRm buscarConfiguracaoAtiva() {
		Optional<ConfiguracaoRm> configPorId = configuracaoRmService.buscarPorId(CONFIGURACAO_RM_PADRAO_ID);

		if (configPorId.isPresent() && Boolean.TRUE.equals(configPorId.get().getAtivo())) {
			logger.info("✅ Usando configuração RM padrão ID {}: {}", CONFIGURACAO_RM_PADRAO_ID,
					configPorId.get().getDescricao());
			return configPorId.get();
		}

		List<ConfiguracaoRm> configsAtivas = configuracaoRmService.listarAtivos();
		if (configsAtivas != null && !configsAtivas.isEmpty()) {
			logger.info("✅ Usando configuração RM ativa ID {}: {}", configsAtivas.get(0).getId(),
					configsAtivas.get(0).getDescricao());
			return configsAtivas.get(0);
		}

		logger.warn("⚠️ Nenhuma configuração RM ativa encontrada. Usando valores padrão.");
		return null;
	}

	private String getValorConfiguracao(ConfiguracaoRm config, String campo) {
		if (config == null)
			return "";

		switch (campo) {
		case "serie":
			return config.getSerie() != null ? config.getSerie() : "RPS";
		case "tipoMovimento":
			return config.getTipoMovimento() != null ? config.getTipoMovimento() : "2.1.05";
		case "condicaoPagamento":
			return config.getCondicaoPagamento() != null ? config.getCondicaoPagamento() : "99";
		case "centroCusto":
			return config.getCentroCusto() != null ? config.getCentroCusto() : "01.01";
		case "contaCaixa":
			return config.getContaCaixa() != null ? config.getContaCaixa() : "78";
		case "codigoServico":
			return config.getCodigoServico() != null ? config.getCodigoServico() : "5.949.01";
		case "municipioServico":
			return config.getMunicipioServico() != null ? config.getMunicipioServico() : "04400";
		case "ufServico":
			return config.getUfServico() != null ? config.getUfServico() : "CE";
		default:
			return "";
		}
	}

	// ========== 🔥 MÉTODO CRIADO PARA RESOLVER O ERRO DA LINHA 173 ==========
	/**
	 * Cria um novo lote de processamento para exportação RM
	 */
	@Transactional
	public LoteProcessamento criarLoteProcessamento(String usuario, Long reguaId, LocalDate mesReferencia) {
		logger.info("📝 Criando lote de processamento para exportação RM - Usuário: {}, Régua: {}, Mês: {}", 
				usuario, reguaId, mesReferencia);

		LoteProcessamento lote = new LoteProcessamento();
		lote.setUsuarioProcessamento(usuario);
		lote.setMesReferencia(mesReferencia);
		lote.setDataInicioProcessamento(LocalDateTime.now());
		lote.setStatus("PROCESSANDO");
		// 🔥 CORREÇÃO: Removido setTipo() - usar setObservacao ou outro campo existente
		// lote.setTipo("EXPORTACAO_RM"); // Este método não existe
		lote.setObservacao("EXPORTACAO_RM"); // Usando campo observacao para armazenar o tipo
		lote.setTotalFaturasGeradas(0);
		lote.setTotalErros(0);

		// Buscar e associar a régua se fornecida
		if (reguaId != null) {
			Optional<ReguaFaturamento> reguaOpt = reguaFaturamentoRepository.findById(reguaId);
			if (reguaOpt.isPresent()) {
				lote.setRegua(reguaOpt.get());
				logger.info("📋 Régua associada: {}", reguaOpt.get().getNome());
			} else {
				logger.warn("⚠️ Régua ID {} não encontrada", reguaId);
			}
		}

		LoteProcessamento loteSalvo = loteProcessamentoRepository.save(lote);
		logger.info("✅ Lote criado com sucesso! ID: {}", loteSalvo.getId());

		return loteSalvo;
	}

	@Transactional(readOnly = true)
	public byte[] exportarRmFatura(Long faturaId, Integer numeroRps, LoteProcessamento lote) {
		logger.info("📤 Exportando RM para fatura ID: {}", faturaId);
		ultimoNumeroRPS = numeroRps;

		try {
			Fatura fatura = faturaRepository.findById(faturaId)
					.orElseThrow(() -> new RuntimeException("Fatura não encontrada: " + faturaId));

			List<FaturaItem> itens = faturaItemRepository.findByFaturaId(faturaId);
			logger.info("📊 Encontrados {} itens para a fatura {}", itens.size(), faturaId);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(baos);

			writer.println(gerarLinhaM(fatura));

			int itemSeq = 1;
			for (FaturaItem item : itens) {
				writer.println(gerarLinhaI(item, fatura, itemSeq++));
			}

			writer.flush();
			byte[] resultado = baos.toByteArray();
			logger.info("✅ Arquivo RM gerado com sucesso! Tamanho: {} bytes", resultado.length);
			return resultado;

		} catch (Exception e) {
			logger.error("❌ Erro ao gerar arquivo RM: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao gerar arquivo RM: " + e.getMessage());
		}
	}

	@Transactional
	public byte[] exportarRmLote(List<Long> faturaIds, Integer ultimoNumeroRps, String usuario, Long reguaId, LocalDate mesReferencia) {
		
		logger.info("📤 Exportando RM lote para {} faturas: {}", faturaIds.size(), faturaIds);
	    logger.info("📤 reguaId recebido: {}", reguaId);
	    
	    if (faturaIds == null || faturaIds.isEmpty()) {
	    	logger.warn("⚠️ Nenhuma fatura para exportar!");
	        return new byte[0];
	    }
	    
	    // 🔥 CORREÇÃO: Variável final para uso em lambda
	    final Long[] loteIdHolder = {null};
	    
	    // 1. TENTAR ENCONTRAR UM LOTE EXISTENTE
	    for (Long id : faturaIds) {
	        Optional<Fatura> faturaOpt = faturaRepository.findById(id);
	        if (faturaOpt.isPresent() && faturaOpt.get().getLoteProcessamentoId() != null) {
	            loteIdHolder[0] = faturaOpt.get().getLoteProcessamentoId();
	            logger.info("🔍 Encontrado lote ID {} na fatura {}", loteIdHolder[0], id);
	            break;
	        }
	    }
	    
	    LoteProcessamento lote;
	    if (loteIdHolder[0] != null) {
	    	final Long loteId = loteIdHolder[0]; // 🔥 CORREÇÃO: Variável final para uso em lambda
	        lote = loteProcessamentoRepository.findById(loteId)
	            .orElseThrow(() -> new RuntimeException("Lote não encontrado: " + loteId));
	        logger.info("♻️ Reutilizando lote existente ID: {}, Régua: {}", 
	            lote.getId(), lote.getRegua() != null ? lote.getRegua().getNome() : "null");
	    } else {
	        // 🔥 LINHA 173 - AGORA O MÉTODO EXISTE!
	        lote = criarLoteProcessamento(usuario, reguaId, mesReferencia);
	        loteProcessamentoRepository.save(lote);
	        logger.info("✅ Novo lote criado ID: {}", lote.getId());
	    }
	    
	    int numeroRpsAtual = ultimoNumeroRps;
	    int faturasProcessadas = 0;
	    int erros = 0;
	    
	    // 2. ATUALIZAR FATURAS
	    logger.info("🔄 Atualizando faturas com lote ID: {}", lote.getId());
	    int rpsAtual = numeroRpsAtual + 1;
	    
	    for (Long id : faturaIds) {
	        try {
	            Optional<Fatura> faturaOpt = faturaRepository.findById(id);
	            if (faturaOpt.isPresent()) {
	                Fatura fatura = faturaOpt.get();
	                
	                fatura.setProcessadoRm(true);
	                fatura.setLoteProcessamentoId(lote.getId());
	                fatura.setDataProcessamento(LocalDateTime.now());
	                fatura.setStatus("PAGA");
	                fatura.setNumeroRps(rpsAtual);
	                
	                faturaRepository.saveAndFlush(fatura);
	                
	                faturasProcessadas++;
	                rpsAtual++;
	            } else {
	            	logger.warn("⚠️ Fatura {} não encontrada!", id);
	                erros++;
	            }
	        } catch (Exception e) {
	        	logger.error("❌ Erro ao processar fatura {}: {}", id, e.getMessage(), e);
	            erros++;
	        }
	    }
	    
	    // 3. ATUALIZAR LOTE
	    lote.setTotalFaturasGeradas(faturasProcessadas);
	    lote.setTotalErros(erros);
	    lote.setDataFimProcessamento(LocalDateTime.now());
	    lote.setStatus("CONCLUIDO");
	    loteProcessamentoRepository.saveAndFlush(lote);
	    
	    logger.info("📊 Faturas atualizadas: {} processadas, {} erros", faturasProcessadas, erros);
	    
	    // 4. GERAR ARQUIVO RM - 🔥 CORRIGIDO (REMOVENDO AUTO-REFERÊNCIA)
	    try {
	    	logger.info("📤 Gerando arquivo RM...");
	    	
	    	// 🔥 LINHA 224 - CORREÇÃO: Usar this em vez de faturaRmExportService
	    	// Agora chamamos o método diretamente, pois estamos na própria classe
	    	byte[] arquivo = this.gerarArquivoRmLote(faturaIds, ultimoNumeroRps, usuario, reguaId, mesReferencia);
	    	
	        logger.info("✅ Arquivo RM gerado, tamanho: {} bytes", arquivo.length);
	        
	        return arquivo;
	    } catch (Exception e) {
	        logger.error("❌ Erro na geração do arquivo RM: {}", e.getMessage(), e);
	        
	        lote.setTotalFaturasGeradas(faturasProcessadas);
	        lote.setTotalErros(erros);
	        lote.setDataFimProcessamento(LocalDateTime.now());
	        lote.setStatus("ERRO");
	        lote.setObservacao("Erro na geração do arquivo RM: " + e.getMessage());
	        loteProcessamentoRepository.saveAndFlush(lote);
	        
	        throw new RuntimeException("Erro na geração do arquivo RM lote: " + e.getMessage());
	    }
	}

	// ========== 🔥 NOVO MÉTODO AUXILIAR PARA GERAR ARQUIVO ==========
	/**
	 * Método auxiliar para gerar o arquivo RM sem auto-referência
	 */
	private byte[] gerarArquivoRmLote(List<Long> faturaIds, Integer ultimoNumeroRps, String usuario, 
			Long reguaId, LocalDate mesReferencia) {
		
		logger.info("📄 Gerando conteúdo do arquivo RM para {} faturas", faturaIds.size());
		ultimoNumeroRPS = ultimoNumeroRps;
		
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(baos);
			
			int faturasProcessadas = 0;
			int erros = 0;
			BigDecimal valorTotal = BigDecimal.ZERO;
			
			for (Long faturaId : faturaIds) {
				try {
					Optional<Fatura> faturaOpt = faturaRepository.findById(faturaId);
					
					if (faturaOpt.isEmpty()) {
						logger.warn("⚠️ Fatura {} não encontrada!", faturaId);
						erros++;
						continue;
					}
					
					Fatura fatura = faturaOpt.get();
					
					// Verificar se a fatura tem valor
					if (fatura.getValorTotal() == null || fatura.getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
						logger.warn("⚠️ Fatura {} com valor ZERO - ignorando", faturaId);
						erros++;
						continue;
					}
					
					// Verificar se a fatura tem itens
					List<FaturaItem> itens = faturaItemRepository.findByFaturaId(faturaId);
					if (itens == null || itens.isEmpty()) {
						logger.warn("⚠️ Fatura {} sem itens - ignorando", faturaId);
						erros++;
						continue;
					}
					
					// Gerar linha M
					writer.println(gerarLinhaM(fatura));
					
					// Gerar linhas I
					int itemSeq = 1;
					for (FaturaItem item : itens) {
						writer.println(gerarLinhaI(item, fatura, itemSeq++));
					}
					
					valorTotal = valorTotal.add(fatura.getValorTotal());
					faturasProcessadas++;
					ultimoNumeroRPS++;
					
				} catch (Exception e) {
					logger.error("❌ Erro ao processar fatura {}: {}", faturaId, e.getMessage());
					erros++;
				}
			}
			
			writer.flush();
			byte[] resultado = baos.toByteArray();
			
			logger.info("✅ Arquivo gerado: {} faturas processadas, {} erros, valor total: R$ {}", 
					faturasProcessadas, erros, valorTotal);
			
			return resultado;
			
		} catch (Exception e) {
			logger.error("❌ Erro ao gerar arquivo RM: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao gerar arquivo RM: " + e.getMessage());
		}
	}

	// 🔥 NOVO MÉTODO: Exporta RM com metadados para listagens
	@Transactional(readOnly = true)
	public ExportacaoRmResultado exportarRmLoteComMetadados(List<Long> faturaIds, Integer numeroRpsInicial,
			LoteProcessamento lote) {
		logger.info("📤 Exportando RM lote para {} faturas com metadados", faturaIds.size());
		logger.info("📋 IDs recebidos: {}", faturaIds);

		ultimoNumeroRPS = numeroRpsInicial;

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(baos);

			List<ExportacaoRmResultado.FaturaExportada> faturasExportadas = new ArrayList<>();
			List<Long> faturasIgnoradasList = new ArrayList<>();
			int totalItens = 0;
			BigDecimal valorTotal = BigDecimal.ZERO;
			BigDecimal valorTotalIgnoradas = BigDecimal.ZERO;
			int rpsInicial = ultimoNumeroRPS + 1;
			int faturasIgnoradas = 0;

			for (Long faturaId : faturaIds) {
				logger.info("🔍 Processando fatura ID: {}", faturaId);

				Optional<Fatura> faturaOpt = faturaRepository.findById(faturaId);

				if (faturaOpt.isEmpty()) {
					logger.warn("⚠️ Fatura {} não encontrada!", faturaId);
					faturasIgnoradas++;
					faturasIgnoradasList.add(faturaId);
					continue;
				}

				Fatura fatura = faturaOpt.get();
				logger.info("  - Número: {}, Associado: {}, Valor: {}, Status: {}", fatura.getNumeroFatura(),
						fatura.getAssociado().getNomeRazao(), fatura.getValorTotal(), fatura.getStatus());

				// 🔥 VERIFICAR SE A FATURA TEM VALOR
				if (fatura.getValorTotal() == null || fatura.getValorTotal().compareTo(BigDecimal.ZERO) == 0) {
					logger.warn("⚠️ Fatura {} com valor ZERO - ignorando", faturaId);
					faturasIgnoradas++;
					faturasIgnoradasList.add(faturaId);
					valorTotalIgnoradas = valorTotalIgnoradas.add(BigDecimal.ZERO);
					continue;
				}

				// 🔥 VERIFICAR SE A FATURA TEM ITENS
				List<FaturaItem> itens = faturaItemRepository.findByFaturaId(faturaId);
				if (itens == null || itens.isEmpty()) {
					logger.warn("⚠️ Fatura {} sem itens - ignorando", faturaId);
					faturasIgnoradas++;
					faturasIgnoradasList.add(faturaId);
					valorTotalIgnoradas = valorTotalIgnoradas
							.add(fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO);
					continue;
				}

				logger.info("✅ Fatura {} será exportada - Valor: {}, Itens: {}", faturaId, fatura.getValorTotal(),
						itens.size());

				// Gerar linha M
				writer.println(gerarLinhaM(fatura));

				List<ExportacaoRmResultado.ItemFatura> itensDTO = new ArrayList<>();
				int itemSeq = 1;

				for (FaturaItem item : itens) {
					writer.println(gerarLinhaI(item, fatura, itemSeq++));
					totalItens++;

					itensDTO.add(new ExportacaoRmResultado.ItemFatura(item.getCodigoProduto(), item.getDescricao(),
							item.getQuantidade(), item.getValorUnitario(), item.getValorTotal()));
				}

				faturasExportadas.add(new ExportacaoRmResultado.FaturaExportada(fatura.getId(),
						fatura.getNumeroFatura(), ultimoNumeroRPS, "SUCESSO", null,
						fatura.getAssociado().getNomeRazao(), fatura.getAssociado().getCodigoRm(),
						fatura.getAssociado().getCodigoSpc(), fatura.getAssociado().getCnpjCpf(),
						fatura.getValorTotal(),
						fatura.getDataEmissao() != null ? fatura.getDataEmissao().toString() : null,
						fatura.getDataVencimento() != null ? fatura.getDataVencimento().toString() : null, itensDTO));

				valorTotal = valorTotal.add(fatura.getValorTotal());
				ultimoNumeroRPS++;
			}

			writer.flush();
			byte[] arquivo = baos.toByteArray();

			logger.info("✅ Exportação concluída!");
			logger.info("  - Faturas solicitadas: {}", faturaIds.size());
			logger.info("  - Faturas exportadas: {}", faturasExportadas.size());
			logger.info("  - Faturas ignoradas: {} - IDs: {}", faturasIgnoradas, faturasIgnoradasList);
			logger.info("  - Valor total exportado: R$ {}", valorTotal);
			logger.info("  - Valor total ignorado: R$ {}", valorTotalIgnoradas);
			logger.info("  - Total de itens: {}", totalItens);
			logger.info("  - Tamanho do arquivo: {} bytes", arquivo.length);

			return new ExportacaoRmResultado(lote.getId(), arquivo, faturaIds.size(), faturasExportadas.size(),
					faturasIgnoradas, rpsInicial, ultimoNumeroRPS - 1, LocalDateTime.now().toString(), valorTotal,
					faturasExportadas, faturasIgnoradasList, valorTotalIgnoradas);

		} catch (Exception e) {
			logger.error("❌ Erro ao gerar arquivo RM lote com metadados: {}", e.getMessage(), e);
			throw new RuntimeException("Erro ao gerar arquivo RM lote: " + e.getMessage());
		}
	}

	// ========== CLASSES DE RESULTADO ==========

	public static class ExportacaoRmResultado {
		private Long loteId;
		private byte[] arquivo;
		private int totalFaturas;
		private int faturasProcessadas;
		private int faturasComErro;
		private int primeiroNumeroRps;
		private int ultimoNumeroRps;
		private String dataProcessamento;
		private BigDecimal valorTotal;
		private List<FaturaExportada> faturas;

		// 🔥 NOVOS CAMPOS
		private List<Long> faturasIgnoradasIds;
		private BigDecimal valorTotalIgnorado;

		public ExportacaoRmResultado(Long loteId, byte[] arquivo, int totalFaturas, int faturasProcessadas,
				int faturasComErro, int primeiroNumeroRps, int ultimoNumeroRps, String dataProcessamento,
				BigDecimal valorTotal, List<FaturaExportada> faturas, List<Long> faturasIgnoradasIds,
				BigDecimal valorTotalIgnorado) {
			this.loteId = loteId;
			this.arquivo = arquivo;
			this.totalFaturas = totalFaturas;
			this.faturasProcessadas = faturasProcessadas;
			this.faturasComErro = faturasComErro;
			this.primeiroNumeroRps = primeiroNumeroRps;
			this.ultimoNumeroRps = ultimoNumeroRps;
			this.dataProcessamento = dataProcessamento;
			this.valorTotal = valorTotal;
			this.faturas = faturas;
			this.faturasIgnoradasIds = faturasIgnoradasIds;
			this.valorTotalIgnorado = valorTotalIgnorado;
		}

		public Long getLoteId() {
			return loteId;
		}

		public byte[] getArquivo() {
			return arquivo;
		}

		public int getTotalFaturas() {
			return totalFaturas;
		}

		public int getFaturasProcessadas() {
			return faturasProcessadas;
		}

		public int getFaturasComErro() {
			return faturasComErro;
		}

		public int getPrimeiroNumeroRps() {
			return primeiroNumeroRps;
		}

		public int getUltimoNumeroRps() {
			return ultimoNumeroRps;
		}

		public String getDataProcessamento() {
			return dataProcessamento;
		}

		public BigDecimal getValorTotal() {
			return valorTotal;
		}

		public List<FaturaExportada> getFaturas() {
			return faturas;
		}
		
		// 🔥 GETTERS ADICIONADOS
	    public List<Long> getFaturasIgnoradasIds() { 
	        return faturasIgnoradasIds; 
	    }
	    
	    public BigDecimal getValorTotalIgnorado() { 
	        return valorTotalIgnorado; 
	    }
	    
	    /**
	     * Retorna a quantidade de faturas ignoradas
	     */
	    public int getFaturasIgnoradas() { 
	        return faturasIgnoradasIds != null ? faturasIgnoradasIds.size() : 0; 
	    }		

		public static class FaturaExportada {
			private Long faturaId;
			private String numeroFatura;
			private int numeroRps;
			private String status;
			private String mensagem;
			private String associadoNome;
			private String codigoRm;
			private String codigoSpc;
			private String cnpjCpf;
			private BigDecimal valorTotal;
			private String dataEmissao;
			private String dataVencimento;
			private List<ItemFatura> itens;

			public FaturaExportada(Long faturaId, String numeroFatura, int numeroRps, String status, String mensagem,
					String associadoNome, String codigoRm, String codigoSpc, String cnpjCpf, BigDecimal valorTotal,
					String dataEmissao, String dataVencimento, List<ItemFatura> itens) {
				this.faturaId = faturaId;
				this.numeroFatura = numeroFatura;
				this.numeroRps = numeroRps;
				this.status = status;
				this.mensagem = mensagem;
				this.associadoNome = associadoNome;
				this.codigoRm = codigoRm;
				this.codigoSpc = codigoSpc;
				this.cnpjCpf = cnpjCpf;
				this.valorTotal = valorTotal;
				this.dataEmissao = dataEmissao;
				this.dataVencimento = dataVencimento;
				this.itens = itens;
			}

			public Long getFaturaId() {
				return faturaId;
			}

			public String getNumeroFatura() {
				return numeroFatura;
			}

			public int getNumeroRps() {
				return numeroRps;
			}

			public String getStatus() {
				return status;
			}

			public String getMensagem() {
				return mensagem;
			}

			public String getAssociadoNome() {
				return associadoNome;
			}

			public String getCodigoRm() {
				return codigoRm;
			}

			public String getCodigoSpc() {
				return codigoSpc;
			}

			public String getCnpjCpf() {
				return cnpjCpf;
			}

			public BigDecimal getValorTotal() {
				return valorTotal;
			}

			public String getDataEmissao() {
				return dataEmissao;
			}

			public String getDataVencimento() {
				return dataVencimento;
			}

			public List<ItemFatura> getItens() {
				return itens;
			}
		}

		public static class ItemFatura {
			private String codigoProduto;
			private String descricao;
			private BigDecimal quantidade;
			private BigDecimal valorUnitario;
			private BigDecimal valorTotal;

			public ItemFatura(String codigoProduto, String descricao, BigDecimal quantidade, BigDecimal valorUnitario,
					BigDecimal valorTotal) {
				this.codigoProduto = codigoProduto;
				this.descricao = descricao;
				this.quantidade = quantidade;
				this.valorUnitario = valorUnitario;
				this.valorTotal = valorTotal;
			}

			public String getCodigoProduto() {
				return codigoProduto;
			}

			public String getDescricao() {
				return descricao;
			}

			public BigDecimal getQuantidade() {
				return quantidade;
			}

			public BigDecimal getValorUnitario() {
				return valorUnitario;
			}

			public BigDecimal getValorTotal() {
				return valorTotal;
			}
		}
	}

	// ========== MÉTODOS AUXILIARES ==========

	private String obterProximoNumeroRPS() {
		ultimoNumeroRPS++;
		return String.format("%06d", ultimoNumeroRPS);
	}

	private void appendAlfa(StringBuilder sb, String valor, int tamanho) {
		if (valor == null)
			valor = "";
		if (valor.length() > tamanho)
			valor = valor.substring(0, tamanho);
		sb.append(valor);
		for (int i = valor.length(); i < tamanho; i++)
			sb.append(' ');
	}

	private void appendNumero(StringBuilder sb, String valor, int tamanho) {
		if (valor == null)
			valor = "0";
		String numeros = valor.replaceAll("[^0-9]", "");
		if (numeros.length() > tamanho)
			numeros = numeros.substring(numeros.length() - tamanho);
		for (int i = numeros.length(); i < tamanho; i++)
			sb.append('0');
		sb.append(numeros);
	}

	private void appendNumeroZero(StringBuilder sb, String valor, int tamanho) {
		if (valor == null)
			valor = "0";
		String numeros = valor.replaceAll("[^0-9]", "");
		if (numeros.isEmpty())
			numeros = "0";
		if (numeros.length() > tamanho)
			numeros = numeros.substring(numeros.length() - tamanho);
		for (int i = numeros.length(); i < tamanho; i++)
			sb.append('0');
		sb.append(numeros);
	}

	private void appendValorMonetario(StringBuilder sb, BigDecimal valor, int tamanhoTotal) {
		if (valor == null)
			valor = BigDecimal.ZERO;
		String valorStr = String.format("%.2f", valor).replace(',', '.');
		String[] partes = valorStr.split("\\.");
		String parteInteira = partes[0];
		String parteDecimal = partes.length > 1 ? partes[1] : "00";
		if (parteDecimal.length() > 2)
			parteDecimal = parteDecimal.substring(0, 2);
		else if (parteDecimal.length() < 2)
			parteDecimal = String.format("%-2s", parteDecimal).replace(' ', '0');
		int tamanhoInteiro = tamanhoTotal - 3;
		String inteiroFormatado = String.format("%" + tamanhoInteiro + "s", parteInteira).replace(' ', '0');
		sb.append(inteiroFormatado + "." + parteDecimal);
	}

	private void appendQuantidade(StringBuilder sb, Number quantidade, int tamanhoTotal) {
		BigDecimal qtdDecimal = (quantidade == null) ? BigDecimal.ONE : BigDecimal.valueOf(quantidade.doubleValue());
		DecimalFormat qtdFormat = new DecimalFormat("#0.0000", new DecimalFormatSymbols(Locale.US));
		String qtdStr = qtdFormat.format(qtdDecimal);
		String[] partes = qtdStr.split("\\.");
		String parteInteira = partes[0];
		String parteDecimal = partes.length > 1 ? partes[1] : "0000";
		if (parteDecimal.length() > 4)
			parteDecimal = parteDecimal.substring(0, 4);
		else if (parteDecimal.length() < 4)
			parteDecimal = String.format("%-4s", parteDecimal).replace(' ', '0');
		int tamanhoInteiro = tamanhoTotal - 5;
		String inteiroFormatado = String.format("%" + tamanhoInteiro + "s", parteInteira).replace(' ', '0');
		sb.append(inteiroFormatado + "." + parteDecimal);
	}

	// ========== LINHA M (CABEÇALHO) ==========

	private String gerarLinhaM(Fatura fatura) {
		StringBuilder sb = new StringBuilder(2134);
		ConfiguracaoRm config = buscarConfiguracaoAtiva();

		String serie = getValorConfiguracao(config, "serie");
		String tipoMovimento = getValorConfiguracao(config, "tipoMovimento");
		String condicaoPagamento = getValorConfiguracao(config, "condicaoPagamento");
		String centroCusto = getValorConfiguracao(config, "centroCusto");
		String contaCaixa = getValorConfiguracao(config, "contaCaixa");
		String codigoServico = getValorConfiguracao(config, "codigoServico");
		String municipioServico = getValorConfiguracao(config, "municipioServico");
		String ufServico = getValorConfiguracao(config, "ufServico");

		String dataEmissao = fatura.getDataEmissao() != null
				? fatura.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
				: DATA_EMISSAO_PADRAO;
		String numeroRPS = obterProximoNumeroRPS();
		String codigoRmAssociado = fatura.getAssociado().getCodigoRm() != null ? fatura.getAssociado().getCodigoRm()
				: "";

		appendAlfa(sb, "M", 1);
		appendNumero(sb, "1", 5);
		appendAlfa(sb, "1", 30);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, codigoRmAssociado, 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, numeroRPS, 35);
		appendAlfa(sb, serie, 8);
		appendAlfa(sb, tipoMovimento, 10);
		appendAlfa(sb, "S", 1);
		appendAlfa(sb, "A", 1);
		appendNumero(sb, "0", 1);
		appendNumero(sb, "0", 1);
		appendNumero(sb, "0", 1);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "0.00", 20);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, condicaoPagamento, 5);
		appendNumero(sb, "0", 5);

		BigDecimal valorTotal = fatura.getValorTotal() != null ? fatura.getValorTotal() : BigDecimal.ZERO;
		appendValorMonetario(sb, valorTotal, 20);
		appendValorMonetario(sb, valorTotal, 20);
		appendValorMonetario(sb, valorTotal, 20);
		appendAlfa(sb, "", 60);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 16);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 2);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "0", 10);
		appendAlfa(sb, "0", 10);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "0001", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 100);
		appendAlfa(sb, "", 100);
		appendAlfa(sb, "", 100);
		appendAlfa(sb, "R$", 10);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, dataEmissao, 10);
		appendNumero(sb, "1", 5);
		appendNumero(sb, "0", 5);
		appendNumero(sb, "1", 5);
		appendAlfa(sb, "", 16);
		appendAlfa(sb, "", 20);
		appendNumero(sb, "0", 5);
		appendAlfa(sb, "", 1);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "0", 5);
		appendNumero(sb, "0", 5);
		appendNumero(sb, "9", 5);
		appendNumero(sb, "0", 5);
		appendNumero(sb, "0", 5);
		appendAlfa(sb, "", 16);
		appendAlfa(sb, "", 16);
		appendNumero(sb, "0", 20);
		appendAlfa(sb, "", 5);
		appendNumero(sb, "0", 20);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "", 5);
		appendNumero(sb, "0", 5);
		appendNumero(sb, "0", 5);
		appendAlfa(sb, "", 1);
		appendAlfa(sb, "", 10);
		appendNumeroZero(sb, "0", 20);
		appendAlfa(sb, centroCusto, 25);
		appendAlfa(sb, "1", 5);
		appendAlfa(sb, contaCaixa, 10);
		appendAlfa(sb, "1", 5);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "1", 5);
		appendAlfa(sb, "1", 5);
		appendAlfa(sb, "0", 10);
		appendAlfa(sb, dataEmissao, 10);
		appendNumero(sb, "0", 10);
		appendNumero(sb, "0", 10);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, codigoServico, 20);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "17.21/829110001", 15);
		appendAlfa(sb, municipioServico, 20);
		appendAlfa(sb, ufServico, 2);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "39", 10);
		appendAlfa(sb, "", 10);
		appendNumero(sb, "0", 20);
		appendNumero(sb, "0", 20);
		appendNumero(sb, "0", 20);
		appendAlfa(sb, "", 24);
		appendNumero(sb, "0", 5);
		appendAlfa(sb, "", 8);
		appendAlfa(sb, "", 10);
		appendNumero(sb, "0", 20);
		appendAlfa(sb, "0", 5);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, "0", 10);
		appendAlfa(sb, "0", 10);
		appendNumero(sb, "0", 10);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 1);
		appendAlfa(sb, "0", 5);
		appendAlfa(sb, "0", 5);
		appendAlfa(sb, "3684", 30);
		appendAlfa(sb, "", 49);

		return sb.toString();
	}

	// ========== LINHA I (ITEM) ==========

	private String gerarLinhaI(FaturaItem item, Fatura fatura, int sequencial) {
		StringBuilder sb = new StringBuilder(988);
		String dataEmissao = fatura.getDataEmissao() != null
				? fatura.getDataEmissao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
				: DATA_EMISSAO_PADRAO;

		ConfiguracaoRm config = buscarConfiguracaoAtiva();
		String centroCusto = getValorConfiguracao(config, "centroCusto");
		String municipioServico = getValorConfiguracao(config, "municipioServico");
		String ufServico = getValorConfiguracao(config, "ufServico");

		appendAlfa(sb, "I", 1);
		String codigoProduto = item.getCodigoProduto() != null ? item.getCodigoProduto() : "";
		appendAlfa(sb, codigoProduto, 30);
		String seqStr = String.format("%05d", sequencial);
		appendNumero(sb, seqStr, 5);
		appendAlfa(sb, "", 10);

		BigDecimal quantidade = item.getQuantidade() != null ? item.getQuantidade() : BigDecimal.ONE;
		appendQuantidade(sb, quantidade, 20);
		appendValorMonetario(sb, item.getValorUnitario(), 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 5);
		appendNumero(sb, "0", 5);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 10);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "UN", 5);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 5);
		appendAlfa(sb, "99", 5);
		appendAlfa(sb, dataEmissao, 10);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 10);
		appendNumero(sb, "0", 5);
		appendValorMonetario(sb, item.getValorTotal(), 20);
		appendAlfa(sb, centroCusto, 25);
		appendAlfa(sb, "", 20);
		appendAlfa(sb, "", 15);
		appendAlfa(sb, "", 30);
		appendAlfa(sb, "", 25);
		appendAlfa(sb, "", 10);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 60);
		appendNumero(sb, "0", 5);
		appendAlfa(sb, "", 3);
		appendAlfa(sb, "", 60);
		appendAlfa(sb, "", 10);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 1);
		appendNumero(sb, "0", 20);
		appendAlfa(sb, "", 10);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "0", 5);
		appendAlfa(sb, "", 15);
		appendValorMonetario(sb, BigDecimal.ZERO, 20);
		appendAlfa(sb, "", 36);
		appendAlfa(sb, municipioServico, 25);
		appendAlfa(sb, ufServico, 2);

		return sb.toString();
	}
	
	/**
     * Retorna o último número RPS utilizado
     */
    public int getUltimoNumeroRPS() {
        return ultimoNumeroRPS;
    }
    
}