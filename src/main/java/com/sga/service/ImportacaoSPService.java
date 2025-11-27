package com.sga.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.sga.model.HeaderSPC;
import com.sga.model.ImportacaoSPC;
import com.sga.model.ItemSPC;
import com.sga.model.NotaDebitoSPC;
import com.sga.model.ParametrosSPC;
import com.sga.model.TraillerSPC;
import com.sga.repository.ImportacaoSPCRepository;

@Service
public class ImportacaoSPService {

	private static final Logger logger = LoggerFactory.getLogger(ImportacaoSPService.class);

	@Autowired
	private ImportacaoSPCRepository importacaoRepository;

	public ImportacaoSPC processarArquivoSPC(MultipartFile arquivo) {
		logger.info("=== INICIANDO PROCESSAMENTO DO ARQUIVO SPC ===");
		logger.info("Arquivo: {}, Tamanho: {} bytes", arquivo.getOriginalFilename(), arquivo.getSize());

		ImportacaoSPC importacao = new ImportacaoSPC(arquivo.getOriginalFilename());

		try (BufferedReader br = new BufferedReader(new InputStreamReader(arquivo.getInputStream()))) {

			String linha;
			NotaDebitoSPC notaDebitoAtual = null;
			int linhaNumero = 0;

			while ((linha = br.readLine()) != null) {
				linhaNumero++;
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
						}
						break;

					case "3": // Nota Débito
						logger.info("Processando NOTA DÉBITO na linha {}", linhaNumero);
						notaDebitoAtual = processarNotaDebito(linha, importacao);
						if (notaDebitoAtual != null) {
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

			// Salvar apenas a importação (CASCADE salvará tudo)
			logger.info("Salvando importação com todos os registros via CASCADE...");
			importacao.setStatus("PROCESSADO");
			ImportacaoSPC resultado = importacaoRepository.save(importacao);

			logger.info("=== IMPORTAÇÃO FINALIZADA COM SUCESSO ===");
			logger.info("ID Importação: {}, Status: {}", resultado.getId(), resultado.getStatus());
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

	private HeaderSPC processarHeader(String linha, ImportacaoSPC importacao) {
		try {
			if (linha.length() < 575) {
				logger.warn("Linha HEADER muito curta: {} caracteres", linha.length());
				return null;
			}

			HeaderSPC header = new HeaderSPC();
			header.setTipoRegistro("0");
			header.setDataGravacao(linha.substring(1, 9).trim()); // pos 2-9
			header.setNomeArquivo(linha.substring(9, 39).trim()); // pos 10-39
			header.setDescricaoArquivo(linha.substring(39, 59).trim()); // pos 40-59
			header.setDataRefFaturamento(linha.substring(59, 67).trim()); // pos 60-67
			header.setCodigoEntidade(linha.substring(67, 75).trim()); // pos 68-75
			header.setNomeEntidade(linha.substring(75, 125).trim()); // pos 76-125
			header.setEnderecoEntidade(linha.substring(125, 175).trim()); // pos 126-175
			header.setBairroEntidade(linha.substring(175, 205).trim()); // pos 176-205
			header.setCepEntidade(linha.substring(205, 214).trim()); // pos 206-214
			header.setCidadeEntidade(linha.substring(214, 244).trim()); // pos 215-244
			header.setUfEntidade(linha.substring(244, 246).trim()); // pos 245-246
			header.setTelefoneEntidade(linha.substring(246, 261).trim()); // pos 247-261
			header.setFaxEntidade(linha.substring(261, 276).trim()); // pos 262-276
			header.setCnpjEntidade(linha.substring(276, 295).trim()); // pos 277-295
			header.setInscricaoEstadual(linha.substring(295, 309).trim()); // pos 296-309
			header.setInscricaoMunicipal(linha.substring(309, 329).trim()); // pos 310-329
			header.setImportacao(importacao);

			logger.info("Header processado: {}", header.getNomeEntidade());
			return header;

		} catch (Exception e) {
			logger.error("Erro ao processar header: {}", e.getMessage());
			return null;
		}
	}

	private ParametrosSPC processarParametros(String linha, ImportacaoSPC importacao) {
		try {
			if (linha.length() < 575) {
				logger.warn("Linha PARÂMETROS muito curta: {} caracteres", linha.length());
				return null;
			}

			ParametrosSPC parametros = new ParametrosSPC();
			parametros.setTipoRegistro("1");
			parametros.setDataReferencia(linha.substring(1, 9).trim()); // pos 2-9

			// Valor Fat. Vencto 1 (pos 10-22)
			String valorFatStr = linha.substring(9, 22).trim();
			if (!valorFatStr.isEmpty()) {
				parametros.setValorFatVencto1(new BigDecimal(valorFatStr).divide(BigDecimal.valueOf(100)));
			}

			parametros.setData1oVencimento(linha.substring(22, 30).trim()); // pos 23-30
			parametros.setData2oVencimento(linha.substring(30, 38).trim()); // pos 31-38

			// Quant. dias atraso (pos 39-41)
			String diasAtrasoStr = linha.substring(38, 41).trim();
			if (!diasAtrasoStr.isEmpty()) {
				parametros.setQuantDiasAtraso(Integer.parseInt(diasAtrasoStr));
			}

			// % juros (pos 42-46)
			String jurosStr = linha.substring(41, 46).trim();
			if (!jurosStr.isEmpty()) {
				parametros.setPercJurosAposVencto(new BigDecimal(jurosStr).divide(BigDecimal.valueOf(100)));
			}

			// Valor multa (pos 47-59)
			String multaStr = linha.substring(46, 59).trim();
			if (!multaStr.isEmpty()) {
				parametros.setValorMultaAposVencto(new BigDecimal(multaStr).divide(BigDecimal.valueOf(100)));
			}

			parametros.setDataInicioPeriodoRef(linha.substring(59, 67).trim()); // pos 60-67
			parametros.setDataFimPeriodoRef(linha.substring(67, 75).trim()); // pos 68-75

			// Valor faturamento mínimo (pos 138-150)
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
		try {
			if (linha.length() < 575) {
				logger.warn("Linha NOTA DÉBITO muito curta: {} caracteres", linha.length());
				return null;
			}

			NotaDebitoSPC nota = new NotaDebitoSPC();
			nota.setTipoRegistro("3");
			nota.setDataVencimento(linha.substring(1, 9).trim()); // pos 2-9
			nota.setNumeroFatura(linha.substring(9, 19).trim()); // pos 10-19
			nota.setNumeroNotaDebito(linha.substring(19, 29).trim()); // pos 20-29

			// Valor da nota (pos 30-42)
			String valorNotaStr = linha.substring(29, 42).trim();
			if (!valorNotaStr.isEmpty()) {
				nota.setValorNota(new BigDecimal(valorNotaStr).divide(BigDecimal.valueOf(100)));
			}

			nota.setCodigoSocio(linha.substring(42, 50).trim()); // pos 43-50
			nota.setNomeAssociado(linha.substring(50, 100).trim()); // pos 51-100
			nota.setEnderecoCobranca(linha.substring(100, 162).trim()); // pos 101-162
			nota.setBairroCobranca(linha.substring(162, 192).trim()); // pos 163-192
			nota.setCepCobranca(linha.substring(192, 201).trim()); // pos 193-201
			nota.setCidadeCobranca(linha.substring(201, 231).trim()); // pos 202-231
			nota.setUfCobranca(linha.substring(231, 233).trim()); // pos 232-233
			nota.setTelefoneCobranca(linha.substring(233, 248).trim()); // pos 234-248
			
			// Tipo Pessoa: F ou J 
			nota.setTipoPessoa(linha.substring(263, 264).trim()); // pos 264-264
			
			// CNPJ/CPF bruto (pos 250–268)
			String documentoBruto = linha.substring(264, 283).trim();
			
			// Define tamanho correto conforme tipo
			int tamanho = "F".equalsIgnoreCase(nota.getTipoPessoa()) ? 11 : 14;
			
			// Mantém apenas os últimos caracteres necessários (CPF=11 / CNPJ=14)
			String documento = documentoBruto.substring(documentoBruto.length() - tamanho);
			
			// Remove apenas espaços, nunca zeros
			documento = documento.trim();
			nota.setCnpjCic(documento); // pos 250-268
			
			nota.setInscricaoEstadual(linha.substring(283, 297).trim()); // pos 269-282
			nota.setImportacao(importacao);

			logger.info("Nota débito processada: {} - {}", nota.getCodigoSocio(), nota.getNomeAssociado());
			return nota;

		} catch (Exception e) {
			logger.error("Erro ao processar nota débito: {}", e.getMessage());
			return null;
		}
	}

	private ItemSPC processarItem(String linha, NotaDebitoSPC notaDebito, ImportacaoSPC importacao) {
		try {
			if (linha.length() < 140) {
				logger.warn("Linha ITEM muito curta: {} caracteres", linha.length());
				return null;
			}

			ItemSPC item = new ItemSPC();
			item.setTipoRegistro("4");

			// Quantidade (pos 2-8)
			String qtdStr = linha.substring(1, 8).trim();
			if (!qtdStr.isEmpty()) {
				item.setQuantidadeServicos(Integer.parseInt(qtdStr));
			} else {
				item.setQuantidadeServicos(1);
			}

			item.setDescricaoServico(linha.substring(8, 58).trim()); // pos 9-58

			// Valor unitário (pos 59-65)
			String valorUnitStr = linha.substring(58, 65).trim();
			if (!valorUnitStr.isEmpty()) {
				item.setValorUnitario(new BigDecimal(valorUnitStr).divide(BigDecimal.valueOf(100)));
			} else {
				item.setValorUnitario(BigDecimal.ZERO);
			}

			// Valor total (pos 66-76)
			String valorTotalStr = linha.substring(65, 76).trim();
			if (!valorTotalStr.isEmpty()) {
				item.setValorTotal(new BigDecimal(valorTotalStr).divide(BigDecimal.valueOf(100)));
			} else {
				// Calcular se não informado
				item.setValorTotal(item.getValorUnitario().multiply(BigDecimal.valueOf(item.getQuantidadeServicos())));
			}

			item.setCreditoDebito(linha.substring(76, 77).trim()); // pos 77-77
			item.setTipoProduto(linha.substring(77, 79).trim()); // pos 78-79
			item.setCodigoProdutoComercial(linha.substring(79, 89).trim()); // pos 80-89
			item.setCodigoContabil(linha.substring(89, 109).trim()); // pos 90-109
			item.setNumeroNotaDebito(linha.substring(109, 119).trim()); // pos 110-119
			item.setSequenciaNotaDebito(linha.substring(119, 124).trim()); // pos 120-124
			item.setCodigoProduto(linha.substring(124, 128).trim()); // pos 125-128
			item.setCodigoMeioAcesso(linha.substring(128, 133).trim()); // pos 129-133
			item.setTipoProdutoDetalhe(linha.substring(133, 134).trim()); // pos 134-134
			item.setIncideISS(linha.substring(134, 135).trim()); // pos 135-135

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
		try {
			if (linha.length() < 575) {
				logger.warn("Linha TRAILLER muito curta: {} caracteres", linha.length());
				return null;
			}

			TraillerSPC trailler = new TraillerSPC();
			trailler.setTipoRegistro("9");

			// Qtde total registros (pos 2-7)
			String qtdRegStr = linha.substring(1, 7).trim();
			if (!qtdRegStr.isEmpty()) {
				trailler.setQtdeTotalRegistros(Integer.parseInt(qtdRegStr));
			}

			// Qtde total boletos (pos 8-13)
			String qtdBolStr = linha.substring(7, 13).trim();
			if (!qtdBolStr.isEmpty()) {
				//trailler.setQtdeTotalBoletos(Integer.parseInt(qtdBolStr));
				trailler.setQtdeTotalBoletos(Long.parseLong(qtdBolStr));
			}

			// Valor total boletos (pos 14-26)
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
}