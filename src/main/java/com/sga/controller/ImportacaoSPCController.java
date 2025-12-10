package com.sga.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sga.dto.ImportacaoResponseDTO;
import com.sga.dto.VerificacaoAssociadosDTO;
import com.sga.model.ImportacaoSPC;
import com.sga.service.ImportacaoSPService;
import com.sga.service.VerificacaoImportacaoService;

@RestController
@RequestMapping("/api/importacao-spc")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ImportacaoSPCController {

	private static final Logger log = LoggerFactory.getLogger(ImportacaoSPCController.class);

	@Autowired
	private ImportacaoSPService importacaoSPService;

	@Autowired
	private VerificacaoImportacaoService verificacaoImportacaoService;

	@PostMapping("/upload")
	public ResponseEntity<?> uploadArquivo(@RequestParam("arquivo") MultipartFile arquivo) {
		try {
			log.info("📤 Recebido arquivo: {}", arquivo.getOriginalFilename());

			if (arquivo.isEmpty()) {
				return erro("Arquivo vazio");
			}

			if (!arquivo.getOriginalFilename().toLowerCase().endsWith(".txt")) {
				return erro("Apenas arquivos .txt são permitidos");
			}

			if (arquivo.getSize() > 100 * 1024 * 1024) {
				return erro("Arquivo muito grande (máx: 100MB)");
			}

			ImportacaoSPC importacao = importacaoSPService.processarArquivoSPC(arquivo);

			ImportacaoResponseDTO dto = ImportacaoResponseDTO.from(importacao);

			Map<String, Object> resposta = new HashMap<>();
			resposta.put("mensagem", "Arquivo processado com sucesso");
			resposta.put("importacao", dto);
			resposta.put("nomeArquivo", arquivo.getOriginalFilename());
			resposta.put("tamanho", arquivo.getSize());

			return ResponseEntity.ok(resposta);

		} catch (Exception e) {
			log.error("❌ Erro upload: {}", e.getMessage());
			return erro("Erro ao processar arquivo: " + e.getMessage());
		}
	}

	@GetMapping
	public ResponseEntity<?> listar() {
		try {
			List<ImportacaoResponseDTO> lista = importacaoSPService.listarImportacoes().stream()
					.map(ImportacaoResponseDTO::from).collect(Collectors.toList());

			return ResponseEntity.ok(lista);
		} catch (Exception e) {
			return erro("Erro ao listar importações");
		}
	}

	@GetMapping("/health")
	public ResponseEntity<Map<String, String>> health() {
		Map<String, String> map = new HashMap<>();
		map.put("status", "UP");
		map.put("service", "ImportacaoSPCController");
		map.put("timestamp", String.valueOf(System.currentTimeMillis()));
		return ResponseEntity.ok(map);
	}

	@GetMapping("/{id}/verificar-associados")
	public ResponseEntity<?> verificarAssociados(@PathVariable Long id) {
		try {
			VerificacaoAssociadosDTO resultado = verificacaoImportacaoService.verificarAssociados(id);
			return ResponseEntity.ok(resultado);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
		}
	}

	private ResponseEntity<Map<String, String>> erro(String msg) {
		Map<String, String> map = new HashMap<>();
		map.put("erro", msg);
		map.put("timestamp", String.valueOf(System.currentTimeMillis()));
		return ResponseEntity.badRequest().body(map);
	}

	/*
	 * ============================================================ NOVO ENDPOINT —
	 * RESUMO GERAL DA VERIFICAÇÃO Usado pelo FRONTEND em /{id}/verificacao
	 * ============================================================
	 */
	@GetMapping("/{id}/verificacao")
	public ResponseEntity<?> verificarImportacao(@PathVariable Long id) {
		try {
			Map<String, Object> resultado = verificacaoImportacaoService.verificarImportacao(id);
			return ResponseEntity.ok(resultado);
		} catch (Exception e) {
			log.error("Erro na verificação: {}", e.getMessage());
			return ResponseEntity.status(500).body(Map.of("erro", e.getMessage()));
		}
	}

	/*
	 * ============================================================ NOVO ENDPOINT —
	 * VERIFICAÇÃO COMPLETA (associados + divergências) Usado futuramente no
	 * frontend ============================================================
	 */
	@GetMapping("/{id}/verificacao/completo")
	public ResponseEntity<?> verificarAssociadosCompleto(@PathVariable Long id) {
		try {
			Object dto = verificacaoImportacaoService.verificarAssociadosCompleto(id);
			return ResponseEntity.ok(dto);
		} catch (RuntimeException e) {
			return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
		}
	}

	/*
	 * ============================================================ NOVO ENDPOINT —
	 * DETALHES DAS DIVERGÊNCIAS
	 * ============================================================
	 */
	@GetMapping("/{id}/verificacao/detalhes")
	public ResponseEntity<?> verificarDivergencias(@PathVariable Long id) {
		try {
			Map<String, Object> dados = verificacaoImportacaoService.verificarDivergenciasDetalhadas(id);
			return ResponseEntity.ok(dados);
		} catch (RuntimeException e) {
			return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
		}
	}

}
