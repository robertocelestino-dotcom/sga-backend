// src/main/java/com/sga/controller/RmApiController.java

package com.sga.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sga.dto.RmApiConfigDTO;
import com.sga.dto.RmApiIntegracaoRequest;
import com.sga.dto.RmApiIntegracaoResponse;
import com.sga.model.Fatura;
import com.sga.model.NotaDebitoSPC;
import com.sga.repository.FaturaRepository;
import com.sga.repository.NotaDebitoSPCRepository;
import com.sga.service.RmApiConfigService;
import com.sga.service.RmApiIntegracaoService;

@RestController
@RequestMapping("/api/rm-api")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RmApiController {

    private static final Logger log = LoggerFactory.getLogger(RmApiController.class);

    @Autowired
    private RmApiConfigService configService;

    @Autowired
    private RmApiIntegracaoService integracaoService;

    @Autowired
    private NotaDebitoSPCRepository notaDebitoRepository;

    @Autowired
    private FaturaRepository faturaRepository;

    // ==================== CONFIGURAÇÕES ====================

    @GetMapping("/configuracoes")
    public ResponseEntity<List<RmApiConfigDTO>> listarConfiguracoes() {
        log.info("📋 Listando configurações RM API");
        return ResponseEntity.ok(configService.listarConfiguracoes());
    }

    @GetMapping("/configuracoes/ativas")
    public ResponseEntity<List<RmApiConfigDTO>> listarConfiguracoesAtivas() {
        log.info("📋 Listando configurações RM API ativas");
        return ResponseEntity.ok(configService.listarConfiguracoesAtivas());
    }

    @GetMapping("/configuracoes/ativa")
    public ResponseEntity<RmApiConfigDTO> buscarConfiguracaoAtiva() {
        log.info("🔍 Buscando configuração RM API ativa");
        return ResponseEntity.ok(configService.buscarConfiguracaoAtiva());
    }

    @GetMapping("/configuracoes/{id}")
    public ResponseEntity<RmApiConfigDTO> buscarConfiguracaoPorId(@PathVariable Long id) {
        log.info("🔍 Buscando configuração RM API por ID: {}", id);
        return ResponseEntity.ok(configService.buscarPorId(id));
    }

    @PostMapping("/configuracoes")
    public ResponseEntity<RmApiConfigDTO> criarConfiguracao(
            @RequestBody RmApiConfigDTO dto,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("➕ Criando configuração RM API: {}", dto.getNome());
        return ResponseEntity.ok(configService.criarConfiguracao(dto, usuario));
    }

    @PutMapping("/configuracoes/{id}")
    public ResponseEntity<RmApiConfigDTO> atualizarConfiguracao(
            @PathVariable Long id,
            @RequestBody RmApiConfigDTO dto,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("✏️ Atualizando configuração RM API ID: {}", id);
        return ResponseEntity.ok(configService.atualizarConfiguracao(id, dto, usuario));
    }

    @DeleteMapping("/configuracoes/{id}")
    public ResponseEntity<Void> excluirConfiguracao(@PathVariable Long id) {
        log.info("🗑️ Excluindo configuração RM API ID: {}", id);
        configService.excluirConfiguracao(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/configuracoes/{id}/ativar")
    public ResponseEntity<Void> ativarConfiguracao(@PathVariable Long id) {
        log.info("🔛 Ativando configuração RM API ID: {}", id);
        configService.ativarConfiguracao(id);
        return ResponseEntity.ok().build();
    }

    // ==================== INTEGRAÇÃO ====================

    @PostMapping("/integrar")
    public ResponseEntity<RmApiIntegracaoResponse> integrar(
            @RequestBody RmApiIntegracaoRequest request,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("📤 Integrando {} notas via RM API", 
            request.getNotaIds() != null ? request.getNotaIds().size() : 0);

        RmApiIntegracaoResponse response = integracaoService.integrarNotas(
            request.getNotaIds(),
            request.getConfiguracaoId(),
            usuario
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/integrar/automatico")
    public ResponseEntity<RmApiIntegracaoResponse> integrarAutomatico(
            @RequestBody RmApiIntegracaoRequest request,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("🤖 Integrando automaticamente via RM API");

        RmApiConfigDTO config = configService.buscarConfiguracaoAtiva();

        RmApiIntegracaoResponse response = integracaoService.integrarNotas(
            request.getNotaIds(),
            config.getId(),
            usuario
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/testar-conexao")
    public ResponseEntity<Boolean> testarConexao(@RequestBody RmApiConfigDTO config) {
        log.info("🧪 Testando conexão com WebService: {}", config.getWsUrl());
        boolean resultado = integracaoService.testarConexao(config);
        return ResponseEntity.ok(resultado);
    }

    // ============================================================
    // 🔥 PRÉ-VISUALIZAR XML
    // ============================================================

    @PostMapping("/pre-visualizar-xml")
    public ResponseEntity<Map<String, Object>> preVisualizarXml(
            @RequestBody RmApiIntegracaoRequest request,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

        log.info("🔍 Gerando pré-visualização XML para {} notas", 
            request.getNotaIds() != null ? request.getNotaIds().size() : 0);

        Map<String, Object> response = new HashMap<>();

        try {
            RmApiConfigDTO config = request.getConfiguracaoId() != null
                ? configService.buscarPorId(request.getConfiguracaoId())
                : configService.buscarConfiguracaoAtiva();

            if (config == null) {
                response.put("sucesso", false);
                response.put("mensagem", "Nenhuma configuração RM API ativa encontrada");
                return ResponseEntity.ok(response);
            }

            List<NotaDebitoSPC> notas = notaDebitoRepository.findAllById(request.getNotaIds());
            List<Fatura> faturas = faturaRepository.findByNotaDebitoIdIn(request.getNotaIds());

            Map<Long, Fatura> faturaMap = new HashMap<>();
            for (Fatura fatura : faturas) {
                if (fatura.getNotaDebitoId() != null) {
                    faturaMap.put(fatura.getNotaDebitoId(), fatura);
                }
            }

            List<Map<String, Object>> detalhes = new ArrayList<>();

            for (NotaDebitoSPC nota : notas) {
                Map<String, Object> item = new HashMap<>();
                item.put("notaId", nota.getId());

                Fatura fatura = faturaMap.get(nota.getId());

                if (fatura == null) {
                    java.util.Optional<Fatura> faturaOpt = faturaRepository.findByNotaDebitoId(nota.getId());
                    if (faturaOpt.isPresent()) {
                        fatura = faturaOpt.get();
                    }
                }

                if (fatura == null) {
                    item.put("faturaId", null);
                    item.put("erro", "Fatura não encontrada para nota " + nota.getId());
                    detalhes.add(item);
                    continue;
                }

                item.put("faturaId", fatura.getId());
                item.put("numeroFatura", fatura.getNumeroFatura());

                // 🔥 GERAR XML
                String xml = integracaoService.gerarXmlMovimento(fatura, config);
                
                // 🔥 VALIDAR DADOS
                Map<String, Object> validacao = integracaoService.validarDadosParaVisualizacao(fatura);
                
                item.put("xml", xml);
                item.put("validacao", validacao);
                item.put("temErros", validacao.get("erros") != null && !((List<?>) validacao.get("erros")).isEmpty());

                detalhes.add(item);
            }

            response.put("sucesso", true);
            response.put("total", detalhes.size());
            response.put("detalhes", detalhes);
            response.put("configuracao", config);

        } catch (Exception e) {
            log.error("❌ Erro ao gerar pré-visualização: {}", e.getMessage(), e);
            response.put("sucesso", false);
            response.put("mensagem", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}