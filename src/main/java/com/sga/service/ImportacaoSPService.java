package com.sga.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sga.enums.TipoArquivoFaturamento;
import com.sga.model.HeaderSPC;
import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ParametrosSPC;
import com.sga.model.TraillerSPC;
import com.sga.repository.ImportacaoSPCRepository;
import com.sga.repository.NotaDebitoSPCRepository;

@Service
public class ImportacaoSPService {

	private static final Logger logger = LoggerFactory.getLogger(ImportacaoSPService.class);

	@Autowired
	private ImportacaoSPCRepository importacaoRepository;

	@Autowired
	private NotaDebitoSPCRepository notaDebitoRepository;
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("ddMMyyyy");

	/**
	 * Processa arquivo SPC e identifica o tipo (Consolidado ou Prévia)
	 */
	public ImportacaoSPC processarArquivoSPC(MultipartFile arquivo) {
		logger.info("=== INICIANDO PROCESSAMENTO DO ARQUIVO SPC ===");
		logger.info("Arquivo: {}, Tamanho: {} bytes", arquivo.getOriginalFilename(), arquivo.getSize());

		ImportacaoSPC importacao = new ImportacaoSPC(arquivo.getOriginalFilename());

		// 🔥 IDENTIFICAR TIPO DO ARQUIVO PELO NOME
		TipoArquivoFaturamento tipoArquivo = identificarTipoArquivo(arquivo.getOriginalFilename());
		importacao.setTipoArquivo(tipoArquivo.getCodigo());
		logger.info("📁 Tipo de arquivo identificado: {}", tipoArquivo.getDescricao());

		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(arquivo.getInputStream(), Charset.forName("windows-1252")))) {

			String linha;
			NotaDebitoSPC notaDebitoAtual = null;
			int linhaNumero = 0;
			LocalDate dataFimPeriodo = null;
			String descricaoArquivo = null;

			while ((linha = br.readLine()) != null) {

				linhaNumero++;
				linha = sanitizeLine(linha);

				logger.debug("Linha {}: {}", linhaNumero, linha.substring(0, Math.min(50, linha.length())));

				if (linha.length() >= 1) {
					String tipoRegistro = linha.substring(0, 1);

					switch (tipoRegistro) {
					case "0": // Header
						logger.info("Processando HEADER na linha {}", linhaNumero);
						HeaderSPC header = processarHeader(linha, importacao);
						if (header != null) {
							importacao.getHeaders().add(header);
						}
						break;

					case "1": // Parâmetros
						logger.info("Processando PARÂMETROS na linha {}", linhaNumero);
						ParametrosSPC parametros = processarParametros(linha, importacao);
						if (parametros != null) {
							importacao.getParametros().add(parametros);

							// 🔥 EXTRAIR DATA FIM PERÍODO DO REGISTRO TIPO 1
							String dataFimStr = linha.substring(67, 75).trim(); // pos 68-75
							if (!dataFimStr.isEmpty()) {
								try {
									dataFimPeriodo = LocalDate.parse(dataFimStr, DATE_FORMATTER);
									importacao.setDataFimPeriodo(dataFimPeriodo);
									logger.info("📅 Data fim período: {}", dataFimPeriodo);
								} catch (Exception e) {
									logger.warn("Erro ao parsear data fim período: {}", dataFimStr);
								}
							}

							// 🔥 EXTRAIR DESCRIÇÃO DO ARQUIVO
							descricaoArquivo = linha.substring(39, 59).trim(); // pos 40-59
							importacao.setDescricaoArquivo(descricaoArquivo);
							logger.info("📋 Descrição do arquivo: {}", descricaoArquivo);

							// 🔥 VALIDAR DESCRIÇÃO CONFORME O TIPO
							if (tipoArquivo.isConsolidado() && !descricaoArquivo.contains("ARQUIVO PADRAO CON")) {
								logger.warn(
										"⚠️ Descrição do arquivo consolidado não corresponde. Esperado: ARQUIVO PADRAO CON, Encontrado: {}",
										descricaoArquivo);
							}
							if (tipoArquivo.isPrevia() && !descricaoArquivo.contains("ARQUIVO PADRAO FAT")) {
								logger.warn(
										"⚠️ Descrição do arquivo prévia não corresponde. Esperado: ARQUIVO PADRAO FAT, Encontrado: {}",
										descricaoArquivo);
							}
						}
						break;

					case "3": // Nota Débito
						logger.info("Processando NOTA DÉBITO na linha {}", linhaNumero);
						notaDebitoAtual = processarNotaDebito(linha, importacao);
						if (notaDebitoAtual != null) {
							// 🔥 ADICIONAR DATA FIM PERÍODO E TIPO À NOTA
							notaDebitoAtual.setDataFimPeriodo(dataFimPeriodo);
							notaDebitoAtual.setTipoArquivo(tipoArquivo.getCodigo());
							importacao.getNotasDebito().add(notaDebitoAtual);
						}
						break;

					case "4": // Itens
						logger.info("Processando ITEM na linha {}", linhaNumero);
						if (notaDebitoAtual != null) {
							ItemSPC item = processarItem(linha, notaDebitoAtual, importacao);
							if (item != null) {
								notaDebitoAtual.getItens().add(item);
							}
						} else {
							logger.warn("Item sem nota de débito definida na linha {}", linhaNumero);
						}
						break;

					case "9": // Trailler
						logger.info("Processando TRAILLER na linha {}", linhaNumero);
						TraillerSPC trailler = processarTrailler(linha, importacao);
						if (trailler != null) {
							importacao.getTraillers().add(trailler);
						}
						break;

					default:
						logger.warn("Tipo de registro desconhecido: '{}' na linha {}", tipoRegistro, linhaNumero);
					}
				}
			}

			logger.info("=== LEITURA DO ARQUIVO CONCLUÍDA ===");
			logger.info("Total de linhas processadas: {}", linhaNumero);
			logger.info("Headers: {}, Parâmetros: {}, Notas: {}, Itens totais: {}, Traillers: {}",
					importacao.getHeaders().size(), importacao.getParametros().size(),
					importacao.getNotasDebito().size(),
					importacao.getNotasDebito().stream().mapToInt(n -> n.getItens().size()).sum(),
					importacao.getTraillers().size());

			importacao.setStatus("PROCESSADO");
			ImportacaoSPC resultado = importacaoRepository.save(importacao);

			logger.info("=== IMPORTAÇÃO FINALIZADA COM SUCESSO ===");
			logger.info("ID Importação: {}, Status: {}, Tipo: {}", resultado.getId(), resultado.getStatus(),
					resultado.getTipoArquivo());
			return resultado;

		} catch (Exception e) {
			logger.error("=== ERRO NA IMPORTAÇÃO ===", e);
			importacao.setStatus("ERRO");
			try {
				importacaoRepository.save(importacao);
			} catch (Exception ex) {
				logger.error("Erro ao salvar importação com status ERRO: {}", ex.getMessage());
			}
			throw new RuntimeException("Erro ao processar arquivo SPC: " + e.getMessage(), e);
		}
	}

	/**
	 * 🔥 IDENTIFICA O TIPO DE ARQUIVO PELO NOME
	 */
	private TipoArquivoFaturamento identificarTipoArquivo(String nomeArquivo) {
		if (nomeArquivo == null)
			return TipoArquivoFaturamento.CONSOLIDADO;

		String nomeLower = nomeArquivo.toLowerCase();

		if (nomeLower.contains("5501tmpp.txt")) {
			return TipoArquivoFaturamento.PREVIA_CORRENTE;
		}
		if (nomeLower.contains("5501txtp.txt")) {
			return TipoArquivoFaturamento.CONSOLIDADO;
		}

		return TipoArquivoFaturamento.CONSOLIDADO;
	}

	// ==================== MÉTODOS DE PROCESSAMENTO EXISTENTES ====================

	private HeaderSPC processarHeader(String linha, ImportacaoSPC importacao) {
		// ... manter o código existente ...
		try {
			if (linha.length() < 575) {
				logger.warn("Linha HEADER muito curta: {} caracteres", linha.length());
				return null;
			}

			HeaderSPC header = new HeaderSPC();
			header.setTipoRegistro("0");
			header.setDataGravacao(linha.substring(1, 9).trim());
			header.setNomeArquivo(linha.substring(9, 39).trim());
			header.setDescricaoArquivo(linha.substring(39, 59).trim());
			header.setDataRefFaturamento(linha.substring(59, 67).trim());
			header.setCodigoEntidade(linha.substring(67, 75).trim());
			header.setNomeEntidade(linha.substring(75, 125).trim());
			header.setEnderecoEntidade(linha.substring(125, 175).trim());
			header.setBairroEntidade(linha.substring(175, 205).trim());
			header.setCepEntidade(linha.substring(205, 214).trim());
			header.setCidadeEntidade(linha.substring(214, 244).trim());
			header.setUfEntidade(linha.substring(244, 246).trim());
			header.setTelefoneEntidade(linha.substring(246, 261).trim());
			header.setFaxEntidade(linha.substring(261, 276).trim());
			header.setCnpjEntidade(linha.substring(276, 295).trim());
			header.setInscricaoEstadual(linha.substring(295, 309).trim());
			header.setInscricaoMunicipal(linha.substring(309, 329).trim());
			header.setImportacao(importacao);

			logger.info("Header processado: {}", header.getNomeEntidade());
			return header;

		} catch (Exception e) {
			logger.error("Erro ao processar header: {}", e.getMessage());
			return null;
		}
	}

	private ParametrosSPC processarParametros(String linha, ImportacaoSPC importacao) {
		// ... manter o código existente ...
		try {
			if (linha.length() < 575) {
				logger.warn("Linha PARÂMETROS muito curta: {} caracteres", linha.length());
				return null;
			}

			ParametrosSPC parametros = new ParametrosSPC();
			parametros.setTipoRegistro("1");
			parametros.setDataReferencia(linha.substring(1, 9).trim());

			String valorFatStr = linha.substring(9, 22).trim();
			if (!valorFatStr.isEmpty()) {
				parametros.setValorFatVencto1(new BigDecimal(valorFatStr).divide(BigDecimal.valueOf(100)));
			}

			parametros.setData1oVencimento(linha.substring(22, 30).trim());
			parametros.setData2oVencimento(linha.substring(30, 38).trim());

			String diasAtrasoStr = linha.substring(38, 41).trim();
			if (!diasAtrasoStr.isEmpty()) {
				parametros.setQuantDiasAtraso(Integer.parseInt(diasAtrasoStr));
			}

			String jurosStr = linha.substring(41, 46).trim();
			if (!jurosStr.isEmpty()) {
				parametros.setPercJurosAposVencto(new BigDecimal(jurosStr).divide(BigDecimal.valueOf(100)));
			}

			String multaStr = linha.substring(46, 59).trim();
			if (!multaStr.isEmpty()) {
				parametros.setValorMultaAposVencto(new BigDecimal(multaStr).divide(BigDecimal.valueOf(100)));
			}

			parametros.setDataInicioPeriodoRef(linha.substring(59, 67).trim());
			parametros.setDataFimPeriodoRef(linha.substring(67, 75).trim());

			String fatMinStr = linha.substring(137, 150).trim();
			if (!fatMinStr.isEmpty()) {
				parametros.setValorFaturamentoMinimo(new BigDecimal(fatMinStr).divide(BigDecimal.valueOf(100)));
			}

			parametros.setImportacao(importacao);

			logger.info("Parâmetros processados para data ref: {}", parametros.getDataReferencia());
			return parametros;

		} catch (Exception e) {
			logger.error("Erro ao processar parâmetros: {}", e.getMessage());
			return null;
		}
	}

	private NotaDebitoSPC processarNotaDebito(String linha, ImportacaoSPC importacao) {
		// ... manter o código existente ...
		try {
			if (linha.length() < 575) {
				logger.warn("Linha NOTA DÉBITO muito curta: {} caracteres", linha.length());
				return null;
			}

			NotaDebitoSPC nota = new NotaDebitoSPC();
			nota.setTipoRegistro("3");
			nota.setDataVencimento(linha.substring(1, 9).trim());
			nota.setNumeroFatura(linha.substring(9, 19).trim());
			nota.setNumeroNotaDebito(linha.substring(19, 29).trim());

			String valorNotaStr = linha.substring(29, 42).trim();
			if (!valorNotaStr.isEmpty()) {
				nota.setValorNota(new BigDecimal(valorNotaStr).divide(BigDecimal.valueOf(100)));
			}

			nota.setCodigoSocio(linha.substring(42, 50).trim());
			nota.setNomeAssociado(linha.substring(50, 100).trim());
			nota.setEnderecoCobranca(linha.substring(100, 162).trim());
			nota.setBairroCobranca(linha.substring(162, 192).trim());
			nota.setCepCobranca(linha.substring(192, 201).trim());
			nota.setCidadeCobranca(linha.substring(201, 231).trim());
			nota.setUfCobranca(linha.substring(231, 233).trim());
			nota.setTelefoneCobranca(linha.substring(233, 248).trim());
			nota.setTipoPessoa(linha.substring(263, 264).trim());

			String documentoBruto = linha.substring(264, 283).trim();
			int tamanho = "F".equalsIgnoreCase(nota.getTipoPessoa()) ? 11 : 14;
			String documento = documentoBruto.substring(documentoBruto.length() - tamanho);
			documento = documento.trim();
			nota.setCnpjCic(documento);

			nota.setInscricaoEstadual(linha.substring(283, 297).trim());
			nota.setImportacao(importacao);

			logger.info("Nota débito processada: {} - {}", nota.getCodigoSocio(), nota.getNomeAssociado());
			return nota;

		} catch (Exception e) {
			logger.error("Erro ao processar nota débito: {}", e.getMessage());
			return null;
		}
	}

	public static String sanitizeLine(String line) {
		// ... manter o código existente ...
		if (line == null)
			return null;
		line = line.replace("\uFEFF", "");

		Map<String, String> fixes = new LinkedHashMap<>();
		fixes.put("Ã¡", "á");
		fixes.put("ÃÂ¡", "á");
		fixes.put("Ã©", "é");
		fixes.put("Ãª", "ê");
		fixes.put("Ã³", "ó");
		fixes.put("Ãµ", "õ");
		fixes.put("Ãº", "ú");
		fixes.put("ÃÃ", "Á");
		fixes.put("Ã‰", "É");
		fixes.put("ÃŠ", "Ê");
		fixes.put("Ã§", "ç");
		fixes.put("Ã‡", "Ç");
		fixes.put("Ã", "A");
		fixes.put("Ã¿", "Ç");

		for (Map.Entry<String, String> e : fixes.entrySet()) {
			if (line.contains(e.getKey())) {
				line = line.replace(e.getKey(), e.getValue());
			}
		}

		line = line.replaceAll("\\p{Cc}", " ");
		line = Normalizer.normalize(line, Normalizer.Form.NFD);
		line = line.replaceAll("\\p{M}", "");
		line = line.replaceAll("[^\\p{Graph}\\p{Blank}]", " ");
		line = line.replaceAll("\\p{Cf}", " ");

		if (line.length() > 575) {
			line = line.substring(0, 575);
		} else if (line.length() < 575) {
			line = String.format("%-" + 575 + "s", line);
		}

		return line;
	}

	private ItemSPC processarItem(String linha, NotaDebitoSPC notaDebito, ImportacaoSPC importacao) {
		// ... manter o código existente ...
		try {
			if (linha.length() < 140) {
				logger.warn("Linha ITEM muito curta: {} caracteres", linha.length());
				return null;
			}

			ItemSPC item = new ItemSPC();
			item.setTipoRegistro("4");

			String qtdStr = linha.substring(1, 8).trim();
			if (!qtdStr.isEmpty()) {
				item.setQuantidadeServicos(Integer.parseInt(qtdStr));
			} else {
				item.setQuantidadeServicos(1);
			}

			item.setDescricaoServico(linha.substring(8, 58).trim());

			String valorUnitStr = linha.substring(58, 65).trim();
			if (!valorUnitStr.isEmpty()) {
				item.setValorUnitario(new BigDecimal(valorUnitStr).divide(BigDecimal.valueOf(100)));
			} else {
				item.setValorUnitario(BigDecimal.ZERO);
			}

			String valorTotalStr = linha.substring(65, 76).trim();
			if (!valorTotalStr.isEmpty()) {
				item.setValorTotal(new BigDecimal(valorTotalStr).divide(BigDecimal.valueOf(100)));
			} else {
				item.setValorTotal(item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidadeServicos())));
			}

			item.setCreditoDebito(linha.substring(76, 77).trim());
			item.setTipoProduto(linha.substring(77, 79).trim());
			item.setCodigoProdutoComercial(linha.substring(79, 89).trim());
			item.setCodigoContabil(linha.substring(89, 109).trim());
			item.setNumeroNotaDebito(linha.substring(109, 119).trim());
			item.setSequenciaNotaDebito(linha.substring(119, 124).trim());
			item.setCodigoProduto(linha.substring(124, 128).trim());
			item.setCodigoMeioAcesso(linha.substring(128, 133).trim());
			item.setTipoProdutoDetalhe(linha.substring(133, 134).trim());
			item.setIncideISS(linha.substring(134, 135).trim());

			item.setNotaDebito(notaDebito);
			item.setImportacao(importacao);

			logger.info("Item processado: {} - R$ {}", item.getDescricaoServico(), item.getValorTotal());
			return item;

		} catch (Exception e) {
			logger.error("Erro ao processar item: {}", e.getMessage());
			return null;
		}
	}

	private TraillerSPC processarTrailler(String linha, ImportacaoSPC importacao) {
		// ... manter o código existente ...
		try {
			if (linha.length() < 575) {
				logger.warn("Linha TRAILLER muito curta: {} caracteres", linha.length());
				return null;
			}

			TraillerSPC trailler = new TraillerSPC();
			trailler.setTipoRegistro("9");

			String qtdRegStr = linha.substring(1, 7).trim();
			if (!qtdRegStr.isEmpty()) {
				trailler.setQtdeTotalRegistros(Integer.parseInt(qtdRegStr));
			}

			String qtdBolStr = linha.substring(7, 13).trim();
			if (!qtdBolStr.isEmpty()) {
				trailler.setQtdeTotalBoletos(Long.parseLong(qtdBolStr));
			}

			String valorTotalStr = linha.substring(13, 26).trim();
			if (!valorTotalStr.isEmpty()) {
				trailler.setValorTotalBoletos(new BigDecimal(valorTotalStr).divide(BigDecimal.valueOf(100)));
			}

			trailler.setImportacao(importacao);

			logger.info("Trailler processado: {} registros, R$ {}", trailler.getQtdeTotalRegistros(),
					trailler.getValorTotalBoletos());
			return trailler;

		} catch (Exception e) {
			logger.error("Erro ao processar trailler: {}", e.getMessage());
			return null;
		}
	}

	public List<ImportacaoSPC> listarImportacoes() {
		return importacaoRepository.findAll();
	}

	// Adicione este método para calcular os totais de forma consistente
	public Map<String, Object> calcularTotaisImportacao(Long importacaoId) {
		logger.info("📊 Calculando totais da importação: {}", importacaoId);

		List<NotaDebitoSPC> notas = notaDebitoRepository.findByImportacaoId(importacaoId);

		BigDecimal totalDebitos = BigDecimal.ZERO;
		BigDecimal totalCreditos = BigDecimal.ZERO;
		int totalNotas = notas.size();
		int totalItens = 0;

		for (NotaDebitoSPC nota : notas) {
			BigDecimal debitosNota = BigDecimal.ZERO;
			BigDecimal creditosNota = BigDecimal.ZERO;

			for (ItemSPC item : nota.getItens()) {
				totalItens++;
				if ("D".equals(item.getCreditoDebito())) {
					debitosNota = debitosNota.add(item.getValorTotal());
				} else if ("C".equals(item.getCreditoDebito())) {
					creditosNota = creditosNota.add(item.getValorTotal());
				}
			}

			totalDebitos = totalDebitos.add(debitosNota);
			totalCreditos = totalCreditos.add(creditosNota);

			// Atualizar os totais na nota para consistência
			nota.setTotalDebitos(debitosNota);
			nota.setTotalCreditos(creditosNota);
			nota.setValorCobrado(debitosNota.subtract(creditosNota));
		}

		BigDecimal valorCobrado = totalDebitos.subtract(totalCreditos);

		Map<String, Object> result = new HashMap<>();
		result.put("totalDebitos", totalDebitos);
		result.put("totalCreditos", totalCreditos);
		result.put("valorCobrado", valorCobrado);
		result.put("quantidadeNotas", totalNotas);
		result.put("quantidadeItens", totalItens);

		logger.info("📊 Totais calculados: Débitos={}, Créditos={}, Cobrado={}", totalDebitos, totalCreditos,
				valorCobrado);

		return result;
	}

	/**
	 * Desfaz uma importação e todos os seus dados relacionados
	 * 
	 * @param importacaoId ID da importação a ser removida
	 * @param usuario Nome do usuário que está realizando a ação
	 */
	@Transactional
	public void desfazerImportacao(Long importacaoId, String usuario) {
	    logger.info("🗑️ Desfazendo importação SPC ID: {} por {}", importacaoId, usuario);
	    
	    // Buscar a importação pelo ID
	    ImportacaoSPC importacao = importacaoRepository.findById(importacaoId)
	        .orElseThrow(() -> new RuntimeException("Importação não encontrada: " + importacaoId));
	    
	    // Remover a importação (CascadeType.ALL removerá todas as entidades relacionadas)
	    // As entidades relacionadas são: HeaderSPC, ParametrosSPC, NotaDebitoSPC, ItemSPC, TraillerSPC
	    importacaoRepository.delete(importacao);
	    
	    logger.info("✅ Importação {} removida permanentemente", importacaoId);
	}	
}