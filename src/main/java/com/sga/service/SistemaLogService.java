package com.sga.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sga.model.SistemaLog;
import com.sga.repository.SistemaLogRepository;

@Service
public class SistemaLogService {

	private static final Logger logger = LoggerFactory.getLogger(SistemaLogService.class);
	private static final ObjectMapper objectMapper = new ObjectMapper();

	// Entidades que NÃO devem ser logadas (prevenção de auto-registro)
	private static final List<String> ENTIDADES_IGNORADAS = Arrays.asList("SistemaLog", "tb_sistema_log", "log", "logs",
			"SistemaLogService", "SistemaLogController", "SistemaLogRepository");

	@PersistenceContext
	private EntityManager entityManager; // ✅ ADICIONE ESTA LINHA

	@Autowired
	private SistemaLogRepository sistemaLogRepository;

	static {
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * VALIDAÇÃO: Prevenir auto-registro de logs
	 */
	private boolean deveIgnorarRegistro(String tabelaAfetada, String modulo, Object dadosAnteriores,
			Object dadosNovos) {
		// Ignorar logs sobre o próprio sistema de logs
		if (ENTIDADES_IGNORADAS.stream().anyMatch(
				ignorado -> tabelaAfetada != null && tabelaAfetada.toLowerCase().contains(ignorado.toLowerCase())
						|| modulo != null && modulo.toLowerCase().contains(ignorado.toLowerCase()))) {
			logger.debug("Ignorando auto-registro para tabela: {}", tabelaAfetada);
			return true;
		}

		// Verificar se dados contêm referência à tabela de logs
		String dadosStr = (dadosNovos != null ? dadosNovos.toString() : "")
				+ (dadosAnteriores != null ? dadosAnteriores.toString() : "");
		return ENTIDADES_IGNORADAS.stream()
				.anyMatch(ignorado -> dadosStr.toLowerCase().contains(ignorado.toLowerCase()));
	}

	/**
	 * LIMITAR TAMANHO: Prevenir OutOfMemoryError
	 */
	private String limitarTamanhoDados(Object dados, int maxLength) {
		if (dados == null)
			return null;

		try {
			String json = objectMapper.writeValueAsString(dados);
			if (json.length() > maxLength) {
				return json.substring(0, maxLength) + "... [TRUNCADO]";
			}
			return json;
		} catch (Exception e) {
			return "Erro ao serializar: " + e.getMessage();
		}
	}

	/**
	 * REGISTRO DE LOGS SIMPLIFICADOS (métodos que faltam)
	 */
	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void logCriacao(String tabelaAfetada, String entidadeId, String descricao) {
		logSimples(tabelaAfetada, entidadeId != null ? Long.parseLong(entidadeId) : null, "CRIAR", "ASSOCIADO",
				descricao);
	}

	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void logAtualizacao(String tabelaAfetada, String entidadeId, String descricao) {
		logSimples(tabelaAfetada, entidadeId != null ? Long.parseLong(entidadeId) : null, "ATUALIZAR", "ASSOCIADO",
				descricao);
	}

	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void logExclusao(String tabelaAfetada, String entidadeId, String descricao) {
		logSimples(tabelaAfetada, entidadeId != null ? Long.parseLong(entidadeId) : null, "EXCLUIR", "ASSOCIADO",
				descricao);
	}

	/**
	 * Registra um log de forma assíncrona
	 */
	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public CompletableFuture<SistemaLog> registrarLogAsync(SistemaLog log) {
		try {
			// VALIDAÇÃO: Prevenir auto-registro
			if (deveIgnorarRegistro(log.getTabelaAfetada(), log.getModulo(), log.getDadosAnteriores(),
					log.getDadosNovos())) {
				return CompletableFuture.completedFuture(null);
			}

			// LIMITAR TAMANHO DOS DADOS
			log.setDadosAnteriores(limitarTamanhoDados(log.getDadosAnteriores(), 5000));
			log.setDadosNovos(limitarTamanhoDados(log.getDadosNovos(), 5000));

			SistemaLog savedLog = sistemaLogRepository.save(log);
			logger.debug("✅ Log registrado: {} - {} - {}", savedLog.getTabelaAfetada(), savedLog.getAcao(),
					savedLog.getIdRegistro());
			return CompletableFuture.completedFuture(savedLog);
		} catch (Exception e) {
			logger.error("❌ Erro ao registrar log: {}", e.getMessage());
			return CompletableFuture.completedFuture(null); // Não falhar a operação principal
		}
	}

	/**
	 * Registra um log de forma síncrona
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public SistemaLog registrarLog(SistemaLog log) {
		try {
			// VALIDAÇÃO: Prevenir auto-registro
			if (deveIgnorarRegistro(log.getTabelaAfetada(), log.getModulo(), log.getDadosAnteriores(),
					log.getDadosNovos())) {
				return null;
			}

			// LIMITAR TAMANHO DOS DADOS
			log.setDadosAnteriores(limitarTamanhoDados(log.getDadosAnteriores(), 5000));
			log.setDadosNovos(limitarTamanhoDados(log.getDadosNovos(), 5000));

			return sistemaLogRepository.save(log);
		} catch (Exception e) {
			logger.error("❌ Erro ao registrar log síncrono: {}", e.getMessage());
			return null;
		}
	}

	/**
	 * Método completo para registrar logs com todas as informações
	 */
	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void registrarAcao(String tabelaAfetada, Long idRegistro, String acao, String modulo, Object dadosAnteriores,
			Object dadosNovos) {

		long startTime = System.currentTimeMillis();

		// VALIDAÇÃO: Prevenir auto-registro
		if (deveIgnorarRegistro(tabelaAfetada, modulo, dadosAnteriores, dadosNovos)) {
			logger.debug("Ignorando auto-registro para: {} - {}", tabelaAfetada, modulo);
			return;
		}

		try {
			SistemaLog log = new SistemaLog();
			log.setTabelaAfetada(tabelaAfetada);
			log.setIdRegistro(idRegistro);
			log.setAcao(acao);
			log.setModulo(modulo);
			log.setDataHora(LocalDateTime.now());

			// Informações do usuário
			try {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication != null && authentication.isAuthenticated()
						&& !"anonymousUser".equals(authentication.getPrincipal())) {
					// Adapte conforme seu sistema de autenticação
					log.setUsuarioId(1L); // Temporário - substitua pelo ID real do usuário
					log.setUsuarioNome(authentication.getName());
				}
			} catch (Exception e) {
				logger.warn("Não foi possível obter informações do usuário: {}", e.getMessage());
			}

			// Informações da requisição HTTP
			try {
				ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
						.getRequestAttributes();
				if (attributes != null) {
					HttpServletRequest request = attributes.getRequest();
					log.setEnderecoIp(obterEnderecoIp(request));
					log.setUserAgent(request.getHeader("User-Agent"));
					log.setOperacao(request.getMethod() + " " + request.getRequestURI());
				}
			} catch (Exception e) {
				logger.warn("Não foi possível obter informações da requisição: {}", e.getMessage());
			}

			// Dados antes/depois (com limitação de tamanho)
			if (dadosAnteriores != null) {
				log.setDadosAnteriores(limitarTamanhoDados(dadosAnteriores, 5000));
			}

			if (dadosNovos != null) {
				log.setDadosNovos(limitarTamanhoDados(dadosNovos, 5000));
			}

			// Calcular diferenças se ambos os dados existirem
			if (dadosAnteriores != null && dadosNovos != null) {
				try {
					log.setDiferencas(calcularDiferencas(dadosAnteriores, dadosNovos));
				} catch (Exception e) {
					log.setDiferencas("Erro ao calcular diferenças: " + e.getMessage());
				}
			}

			log.setSucesso(true);
			log.setTempoExecucaoMs(System.currentTimeMillis() - startTime);

			// Registrar assincronamente
			registrarLogAsync(log);

		} catch (Exception e) {
			logger.error("❌ Erro ao preparar log: {}", e.getMessage());

			// Registrar log de erro (mas sem criar loop)
			if (!"SistemaLog".equalsIgnoreCase(tabelaAfetada)) {
				try {
					SistemaLog erroLog = new SistemaLog();
					erroLog.setTabelaAfetada("SISTEMA");
					erroLog.setAcao("ERRO_LOG");
					erroLog.setModulo("SISTEMA");
					erroLog.setSucesso(false);
					erroLog.setMensagemErro("Erro ao registrar log: " + e.getMessage());
					erroLog.setDataHora(LocalDateTime.now());
					sistemaLogRepository.save(erroLog);
				} catch (Exception ex) {
					logger.error("❌ Erro crítico ao registrar log de erro: {}", ex.getMessage());
				}
			}
		}
	}

	/**
	 * Método simplificado para logs rápidos
	 */
	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void logSimples(String tabela, Long id, String acao, String modulo, String observacao) {
		// VALIDAÇÃO: Prevenir auto-registro
		if (deveIgnorarRegistro(tabela, modulo, null, null)) {
			return;
		}

		try {
			SistemaLog log = new SistemaLog();
			log.setTabelaAfetada(tabela);
			log.setIdRegistro(id);
			log.setAcao(acao);
			log.setModulo(modulo);
			log.setObservacao(observacao);
			log.setDataHora(LocalDateTime.now());
			log.setSucesso(true);

			// Tentar obter informações básicas
			try {
				ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
						.getRequestAttributes();
				if (attributes != null) {
					HttpServletRequest request = attributes.getRequest();
					log.setEnderecoIp(obterEnderecoIp(request));
				}
			} catch (Exception e) {
				// Ignora erros em logs simples
			}

			registrarLogAsync(log);
		} catch (Exception e) {
			logger.error("❌ Erro no log simples: {}", e.getMessage());
		}
	}

	/**
	 * Registrar erro no sistema
	 */
	@Async
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void registrarErro(String modulo, String operacao, Exception erro, Object dados) {
		// Não registrar se for erro do próprio sistema de logs
		if ("SistemaLog".equalsIgnoreCase(modulo) || operacao.contains("registrarAcao")
				|| operacao.contains("registrarLog")) {
			return;
		}

		try {
			SistemaLog log = new SistemaLog();
			log.setModulo(modulo);
			log.setOperacao(operacao);
			log.setAcao("ERRO");
			log.setSucesso(false);
			log.setMensagemErro(erro.getMessage());
			log.setObservacao(erro.getClass().getName());
			log.setDataHora(LocalDateTime.now());

			if (dados != null) {
				log.setDadosAnteriores(limitarTamanhoDados(dados, 5000));
			}

			registrarLogAsync(log);
		} catch (Exception e) {
			logger.error("❌ Erro ao registrar log de erro: {}", e.getMessage());
		}
	}

	/**
	 * Métodos de consulta
	 */
	public Page<SistemaLog> buscarLogsComFiltros(
	    String tabela, 
	    String acao, 
	    String modulo, 
	    Long usuarioId, 
	    Boolean sucesso, 
	    LocalDateTime dataInicio, 
	    LocalDateTime dataFim, 
	    Pageable pageable) {
	    
	    try {
	        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
	        CriteriaQuery<SistemaLog> query = cb.createQuery(SistemaLog.class);
	        Root<SistemaLog> root = query.from(SistemaLog.class);
	        
	        List<Predicate> predicates = new ArrayList<>();
	        
	        if (tabela != null && !tabela.isEmpty()) {
	            predicates.add(cb.equal(root.get("tabelaAfetada"), tabela));
	        }
	        
	        if (acao != null && !acao.isEmpty()) {
	            predicates.add(cb.equal(root.get("acao"), acao));
	        }
	        
	        if (modulo != null && !modulo.isEmpty()) {
	            predicates.add(cb.equal(root.get("modulo"), modulo));
	        }
	        
	        if (usuarioId != null) {
	            predicates.add(cb.equal(root.get("usuarioId"), usuarioId));
	        }
	        
	        if (sucesso != null) {
	            predicates.add(cb.equal(root.get("sucesso"), sucesso));
	        }
	        
	        if (dataInicio != null) {
	            predicates.add(cb.greaterThanOrEqualTo(root.get("dataHora"), dataInicio));
	        }
	        
	        if (dataFim != null) {
	            predicates.add(cb.lessThanOrEqualTo(root.get("dataHora"), dataFim));
	        }
	        
	        query.where(predicates.toArray(new Predicate[0]));
	        query.orderBy(cb.desc(root.get("dataHora")));
	        
	        // Executar a query com paginação
	        TypedQuery<SistemaLog> typedQuery = entityManager.createQuery(query);
	        typedQuery.setFirstResult((int) pageable.getOffset());
	        typedQuery.setMaxResults(pageable.getPageSize());
	        
	        List<SistemaLog> result = typedQuery.getResultList();
	        
	        // Para contar total (necessário para paginação)
	        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
	        Root<SistemaLog> countRoot = countQuery.from(SistemaLog.class);
	        countQuery.select(cb.count(countRoot))
	                  .where(predicates.toArray(new Predicate[0]));
	        
	        Long total = entityManager.createQuery(countQuery).getSingleResult();
	        
	        return new PageImpl<>(result, pageable, total);
	        
	    } catch (Exception e) {
	        logger.error("❌ Erro ao buscar logs com filtros: {}", e.getMessage());
	        // Fallback: retornar página vazia
	        return new PageImpl<>(new ArrayList<>(), pageable, 0);
	    }
	}

	public Page<SistemaLog> filtrarLogs(String modulo, String entidade, LocalDate dataInicio, LocalDate dataFim,
			Pageable pageable) {
		LocalDateTime inicio = dataInicio != null ? dataInicio.atStartOfDay() : null;
		LocalDateTime fim = dataFim != null ? dataFim.plusDays(1).atStartOfDay() : null;

		return sistemaLogRepository.buscarComFiltros(entidade, null, modulo, null, null, inicio, fim, pageable);
	}

	public List<SistemaLog> buscarLogsPorRegistro(String tabela, Long idRegistro) {
		return sistemaLogRepository.findByTabelaAfetadaAndIdRegistroOrderByDataHoraDesc(tabela, idRegistro);
	}

	public List<SistemaLog> buscarLogsPorModulo(String modulo) {
		return sistemaLogRepository.findByModuloOrderByDataHoraDesc(modulo);
	}

	public List<SistemaLog> buscarLogsPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
		return sistemaLogRepository.findByDataHoraBetweenOrderByDataHoraDesc(inicio, fim);
	}

	/**
	 * Obter opções para filtros
	 */
	public List<String> buscarTabelasDistintas() {
		return sistemaLogRepository.findDistinctTabelaAfetada();
	}

	public List<String> buscarAcoesDistintas() {
		return sistemaLogRepository.findDistinctAcao();
	}

	public List<String> buscarModulosDistintos() {
		return sistemaLogRepository.findDistinctModulo();
	}

	/**
	 * Métodos auxiliares privados
	 */
	private String obterEnderecoIp(HttpServletRequest request) {
		try {
			String ip = request.getHeader("X-Forwarded-For");
			if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");
			}
			if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");
			}
			if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();
			}
			return ip;
		} catch (Exception e) {
			return "0.0.0.0";
		}
	}

	private String calcularDiferencas(Object antes, Object depois) {
		try {
			Map<?, ?> mapAntes = objectMapper.convertValue(antes, Map.class);
			Map<?, ?> mapDepois = objectMapper.convertValue(depois, Map.class);

			StringBuilder diferencas = new StringBuilder();

			// Comparar valores
			for (Map.Entry<?, ?> entry : mapAntes.entrySet()) {
				Object key = entry.getKey();
				Object valorAntes = entry.getValue();
				Object valorDepois = mapDepois.get(key);

				if (!Objects.equals(valorAntes, valorDepois)) {
					diferencas.append(String.format("%s: '%s' -> '%s'\n", key,
							valorAntes != null ? valorAntes.toString() : "null",
							valorDepois != null ? valorDepois.toString() : "null"));
				}
			}

			// Verificar novos campos
			for (Map.Entry<?, ?> entry : mapDepois.entrySet()) {
				if (!mapAntes.containsKey(entry.getKey())) {
					diferencas.append(String.format("%s: null -> '%s'\n", entry.getKey(),
							entry.getValue() != null ? entry.getValue().toString() : "null"));
				}
			}

			return diferencas.toString();

		} catch (Exception e) {
			return "Não foi possível calcular diferenças: " + e.getMessage();
		}
	}

	/**
	 * Limpar logs antigos (pode ser agendado)
	 */
	@Transactional
	public int limparLogsAntigos(int diasManter) {
		try {
			LocalDateTime dataLimite = LocalDateTime.now().minusDays(diasManter);
			int quantidade = sistemaLogRepository.deleteByDataHoraBefore(dataLimite);

			if (quantidade > 0) {
				logger.info("🧹 {} logs antigos removidos (anteriores a {})", quantidade, dataLimite);
			}

			return quantidade;
		} catch (Exception e) {
			logger.error("❌ Erro ao limpar logs antigos: {}", e.getMessage());
			return 0;
		}
	}

	/**
	 * Método para Dashboard - estatísticas
	 */
	public Map<String, Object> obterEstatisticas(LocalDateTime inicio, LocalDateTime fim) {
		Map<String, Object> estatisticas = new HashMap<>();

		try {
			estatisticas.put("totalLogs", sistemaLogRepository.countByDataHoraBetween(inicio, fim));
			estatisticas.put("logsSucesso", sistemaLogRepository.countByDataHoraBetweenAndSucesso(inicio, fim, true));
			estatisticas.put("logsErro", sistemaLogRepository.countByDataHoraBetweenAndSucesso(inicio, fim, false));

			// CORREÇÃO: Usando o método corrigido (agora só 2 parâmetros)
			List<Object[]> modulosResult = sistemaLogRepository.findTopModulosByDataHoraBetween(inicio, fim);

			List<Map<String, Object>> modulosList = modulosResult.stream().limit(5) // Limitar manualmente no Service em
																					// vez de no Repository
					.map(obj -> {
						Map<String, Object> moduloMap = new HashMap<>();
						moduloMap.put("modulo", obj[0] != null ? obj[0].toString() : "DESCONHECIDO");
						moduloMap.put("quantidade", obj[1] != null ? obj[1] : 0);
						return moduloMap;
					}).collect(Collectors.toList());

			estatisticas.put("modulosMaisAtivos", modulosList);

		} catch (Exception e) {
			logger.error("Erro ao obter estatísticas: {}", e.getMessage());
			estatisticas.put("totalLogs", 0);
			estatisticas.put("logsSucesso", 0);
			estatisticas.put("logsErro", 0);
			estatisticas.put("modulosMaisAtivos", new ArrayList<>());
		}

		return estatisticas;
	}

}