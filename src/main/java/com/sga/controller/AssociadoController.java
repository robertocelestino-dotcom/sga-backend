package com.sga.controller;

import java.math.BigDecimal;
import org.springframework.http.HttpHeaders;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.AssociadoDTO;
import com.sga.dto.AssociadoDefFaturamentoDTO;
import com.sga.dto.AssociadoDefFaturamentoResumoDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.EmailDTO;
import com.sga.dto.EnderecoDTO;
import com.sga.dto.TelefoneDTO;
import com.sga.model.Categoria;
import com.sga.model.Planos;
import com.sga.model.Vendedor;
import com.sga.service.AssociadoDefFaturamentoService;
import com.sga.service.AssociadoService;

@RestController
@RequestMapping("/api/associados")
public class AssociadoController {

    private static final Logger logger = LoggerFactory.getLogger(AssociadoController.class);

    @Autowired
    private AssociadoService associadoService;

    @Autowired
    private AssociadoDefFaturamentoService associadoDefFaturamentoService;

    // ============================================================
    // LISTAR ASSOCIADOS (VIEW)
    // ============================================================
    @GetMapping
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Page<AssociadoResumoDTO>> listar(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "nomeRazao") String sort,
            @RequestParam(defaultValue = "asc") String direction, @RequestParam(required = false) String codigoSpc,
            @RequestParam(required = false) String codigoRm, @RequestParam(required = false) String cnpjCpf,
            @RequestParam(required = false) String nomeRazao, @RequestParam(required = false) String tipoPessoa,
            @RequestParam(required = false) String status, @RequestParam(required = false) Long vendedorId,
            @RequestParam(required = false) Long planoId, @RequestParam(required = false) Long categoriaId) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<AssociadoResumoDTO> associados = associadoService.listarComFiltros(pageable, codigoSpc, nomeRazao, cnpjCpf,
                status);

        return ResponseEntity.ok(associados);
    }

    // ============================================================
    // BUSCAR ASSOCIADO POR ID (VIEW)
    // ============================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AssociadoDTO> buscarPorId(@PathVariable Long id) {
        AssociadoDTO associado = associadoService.buscarPorId(id);
        return ResponseEntity.ok(associado);
    }

    // ============================================================
    // BUSCAR ASSOCIADO POR CNPJ/CPF (VIEW)
    // ============================================================
    @GetMapping("/cnpj-cpf/{cnpjCpf}")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AssociadoDTO> buscarPorCnpjCpf(@PathVariable String cnpjCpf) {
        AssociadoDTO associado = associadoService.buscarPorCnpjCpf(cnpjCpf);
        return ResponseEntity.ok(associado);
    }

    // ============================================================
    // CRIAR ASSOCIADO (CREATE)
    // ============================================================
    @PostMapping
    @PreAuthorize("hasAuthority('ASSOCIADO_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AssociadoDTO> criar(@Valid @RequestBody AssociadoDTO associadoDTO) {
        AssociadoDTO novoAssociado = associadoService.criar(associadoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAssociado);
    }

    // ============================================================
    // ATUALIZAR ASSOCIADO (EDIT)
    // ============================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSOCIADO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<AssociadoDTO> atualizar(@PathVariable Long id,
            @Valid @RequestBody AssociadoDTO associadoDTO) {
        AssociadoDTO atualizado = associadoService.atualizar(id, associadoDTO);
        return ResponseEntity.ok(atualizado);
    }

    // ============================================================
    // EXCLUIR ASSOCIADO (DELETE)
    // ============================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSOCIADO_DELETE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        associadoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // BUSCAR VENDEDORES (VIEW)
    // ============================================================
    @GetMapping("/vendedores")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Vendedor>> buscarVendedores() {
        List<Vendedor> vendedores = associadoService.buscarTodosVendedores();
        return ResponseEntity.ok(vendedores);
    }

    // ============================================================
    // BUSCAR PLANOS (VIEW)
    // ============================================================
    @GetMapping("/planos")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Planos>> buscarPlanos() {
        List<Planos> planos = associadoService.buscarTodosPlanos();
        return ResponseEntity.ok(planos);
    }

    // ============================================================
    // BUSCAR CATEGORIAS (VIEW)
    // ============================================================
    @GetMapping("/categorias")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Categoria>> buscarCategorias() {
        List<Categoria> categorias = associadoService.buscarTodasCategorias();
        return ResponseEntity.ok(categorias);
    }

    // ============================================================
    // ESTATÍSTICAS (VIEW)
    // ============================================================
    @GetMapping("/estatisticas/total")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Long> getTotalAssociados() {
        Long total = associadoService.countTotalAssociados();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/estatisticas/ativos")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Long> getAssociadosAtivos() {
        Long ativos = associadoService.countAssociadosAtivos();
        return ResponseEntity.ok(ativos);
    }

    // ============================================================
    // HEALTH CHECK (PÚBLICO)
    // ============================================================
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            Long total = associadoService.countTotalAssociados();
            return ResponseEntity.ok().body(new HealthResponse("UP", "Associado Service", total));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new HealthResponse("DOWN", "Associado Service", 0L));
        }
    }

    // ============================================================
    // IMPORTAÇÃO EM LOTE (CREATE)
    // ============================================================
    @PostMapping("/importacao/lote")
    @PreAuthorize("hasAuthority('ASSOCIADO_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AssociadoDTO>> importarAssociadosEmLote(@RequestBody List<AssociadoDTO> associados) {
        logger.info("📥 Importando {} associados em lote (UPSERT)", associados.size());

        List<AssociadoDTO> associadosImportados = new ArrayList<>();
        int criados = 0;
        int atualizados = 0;
        int erros = 0;
        int configuracoesCriadas = 0;
        List<AssociadoDTO> associadosInativados = new ArrayList<>();

        // ===== ETAPA 1: EXTRAIR CNPJs DA LISTA PARA INATIVAÇÃO =====
        List<String> cnpjsImportados = associados.stream()
                .map(AssociadoDTO::getCnpjCpf)
                .filter(cnpj -> cnpj != null && !cnpj.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());
        
        logger.info("📋 Extraídos {} CNPJs/CPFs do arquivo para inativação", cnpjsImportados.size());

        // ===== ETAPA 2: PROCESSAR CADA ASSOCIADO (CRIAR/ATUALIZAR) =====
        for (AssociadoDTO dto : associados) {
            try {
                // VALIDAÇÕES BÁSICAS
                if (dto.getCnpjCpf() == null || dto.getNomeRazao() == null) {
                    logger.warn("⚠️ Associado ignorado: dados obrigatórios faltando");
                    erros++;
                    continue;
                }

                if (dto.getStatus() == null) dto.setStatus("A");
                if (dto.getTipoPessoa() == null) dto.setTipoPessoa("F");
                
                if (dto.getForcarAtualizacao() == null) {
                    dto.setForcarAtualizacao(true);
                }

                // ============================================================
                // 🔥 PROCESSAMENTO DE FATURAMENTO - PRESERVADO
                // ============================================================
                
                // 1. CRIAR CONFIGURAÇÃO DE FATURAMENTO SE NÃO EXISTIR
                if (dto.getDefinicoesFaturamento() == null || dto.getDefinicoesFaturamento().isEmpty()) {
                    AssociadoDefFaturamentoDTO faturamento = new AssociadoDefFaturamentoDTO();
                    Long planoId = dto.getPlanoId() != null ? dto.getPlanoId() : 5L;
                    faturamento.setPlanoId(planoId);
                    faturamento.setDiaEmissao(26);
                    faturamento.setDiaVencimento(10);
                    faturamento.setValorDef(BigDecimal.valueOf(85.00));
                    faturamento.setObservacao("Configuração padrão - Importação em lote");
                    dto.setDefinicoesFaturamento(List.of(faturamento));
                }

                // ============================================================
                // PROCESSAR ASSOCIADO (CRIAR OU ATUALIZAR)
                // ============================================================
                
                AssociadoDTO resultado;
                boolean associadoExistia = false;

                try {
                    // Verificar se o associado já existe
                    AssociadoDTO existente = associadoService.buscarPorCnpjCpf(dto.getCnpjCpf());
                    if (existente != null && existente.getId() != null) {
                        associadoExistia = true;
                        dto.setId(existente.getId());
                        
                        // ATUALIZAR ASSOCIADO
                        var associadoEntity = associadoService.importarAssociado(dto, "IMPORTACAO_LOTE");
                        resultado = associadoService.toDTO(associadoEntity);
                        atualizados++;
                        logger.info("✏️ Associado atualizado: {} (ID: {})", dto.getNomeRazao(), resultado.getId());

                        // ============================================================
                        // 🔥 VERIFICAR CONFIGURAÇÕES DE FATURAMENTO EXISTENTES
                        // ============================================================
                        var configsExistentes = associadoDefFaturamentoService.listarPorAssociado(existente.getId());
                        if (configsExistentes == null || configsExistentes.isEmpty()) {
                            AssociadoDefFaturamentoDTO novaConfig = new AssociadoDefFaturamentoDTO();
                            novaConfig.setAssociadoId(existente.getId());
                            novaConfig.setPlanoId(dto.getPlanoId() != null ? dto.getPlanoId() : 5L);
                            novaConfig.setDiaEmissao(26);
                            novaConfig.setDiaVencimento(10);
                            novaConfig.setValorDef(BigDecimal.valueOf(85.00));
                            novaConfig.setObservacao("Configuração padrão - Criada automaticamente");

                            associadoDefFaturamentoService.criar(novaConfig);
                            configuracoesCriadas++;
                            logger.info("📅 Configuração de faturamento criada para associado existente ID: {}", existente.getId());
                        }
                    } else {
                        throw new Exception("Associado não encontrado");
                    }
                } catch (Exception e) {
                    // ============================================================
                    // CRIAR NOVO ASSOCIADO
                    // ============================================================
                    associadoExistia = false;
                    var associadoEntity = associadoService.importarAssociado(dto, "IMPORTACAO_LOTE");
                    resultado = associadoService.toDTO(associadoEntity);
                    criados++;
                    
                    // ============================================================
                    // 🔥 CONFIGURAÇÃO DE FATURAMENTO PARA NOVO ASSOCIADO
                    // ============================================================
                    configuracoesCriadas++;
                    logger.info("✅ Associado criado com configuração de faturamento: {} (ID: {})", 
                        dto.getNomeRazao(), resultado.getId());
                }

                // ============================================================
                // 🔥 APLICAR STATUS DO CSV (ATIVO/SUSPENSO)
                // ============================================================
                String statusCSV = dto.getStatus();
                if (statusCSV != null && !statusCSV.isEmpty()) {
                    String statusNormalizado = normalizarStatusImportacao(statusCSV);
                    var associadoEntity = associadoService.buscarPorIdEntity(resultado.getId());
                    String statusAtual = associadoEntity.getStatus();

                    if (!statusAtual.equals(statusNormalizado)) {
                        logger.info("🔄 Mudando status de {} para {} para associado {}",
                            statusAtual, statusNormalizado, associadoEntity.getCnpjCpf());
                        
                        associadoEntity.mudarStatus(
                            statusNormalizado,
                            dto.getMotivoInativacao(),
                            dto.getDataFimSuspensao()
                        );
                        associadoService.atualizar(resultado.getId(), 
                            associadoService.toDTO(associadoEntity));
                    }
                }

                associadosImportados.add(resultado);

            } catch (Exception e) {
                logger.error("❌ Erro ao processar associado {}: {}", dto.getNomeRazao(), e.getMessage(), e);
                erros++;
            }
        }

        // ============================================================
        // ETAPA 3: 🔥 INATIVAR ASSOCIADOS ATIVOS NÃO LISTADOS
        // ============================================================
        int totalInativados = 0;

        if (!cnpjsImportados.isEmpty()) {
            logger.info("🔍 Verificando associados ativos para inativação...");

            try {
                // 🔥 BUSCAR APENAS ASSOCIADOS COM STATUS 'A' (ATIVO)
                List<com.sga.model.Associado> paraInativar = associadoService.findAtivosNotInCnpjList(cnpjsImportados);

                if (!paraInativar.isEmpty()) {
                    logger.info("🔄 Inativando {} associados que não estão no arquivo", paraInativar.size());

                    LocalDate dataInativacao = LocalDate.now();
                    String motivo = "INATIVAÇÃO PROCESSAMENTO AUTOMATICO";

                    for (com.sga.model.Associado associado : paraInativar) {
                        // 🔥 USAR MÉTODO EXISTENTE mudarStatus() QUE JÁ TRATA TUDO
                        associado.mudarStatus("I", motivo, null);
                        associado.setDataAtualizacao(LocalDateTime.now());
                        
                        // Converter para DTO para resposta
                        AssociadoDTO dtoInativado = associadoService.toDTO(associado);
                        associadosInativados.add(dtoInativado);
                        
                        logger.debug("🔴 Associado inativado: {} - {}", 
                            associado.getCnpjCpf(), associado.getNomeRazao());
                    }

                    // Salvar todos os inativados em lote
                    associadoService.saveAllAssociados(paraInativar);
                    totalInativados = paraInativar.size();

                    logger.info("✅ {} associados inativados com sucesso", totalInativados);
                } else {
                    logger.info("ℹ️ Nenhum associado ativo precisa ser inativado");
                }

            } catch (Exception e) {
                logger.error("❌ Erro durante inativação: {}", e.getMessage(), e);
            }
        }

        // ============================================================
        // LOG FINAL COM TODAS AS ESTATÍSTICAS
        // ============================================================
        logger.info("📊 Importação concluída: {} criados, {} atualizados, {} erros, {} configurações criadas, {} inativados", 
            criados, atualizados, erros, configuracoesCriadas, totalInativados);

        // ============================================================
        // ETAPA 4: ADICIONAR HEADERS COM ESTATÍSTICAS
        // ============================================================
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Importacao-Criados", String.valueOf(criados));
        headers.add("X-Importacao-Atualizados", String.valueOf(atualizados));
        headers.add("X-Importacao-Erros", String.valueOf(erros));
        headers.add("X-Importacao-Configuracoes", String.valueOf(configuracoesCriadas));
        headers.add("X-Importacao-Inativados", String.valueOf(totalInativados));
        headers.add("X-Importacao-Total", String.valueOf(associados.size()));

        // ============================================================
        // 🔥 INCLUIR LISTA DE INATIVADOS NO HEADER (JSON)
        // ============================================================
        if (!associadosInativados.isEmpty()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<Map<String, Object>> inativadosMap = associadosInativados.stream().map(a -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", a.getId());
                    map.put("cnpjCpf", a.getCnpjCpf());
                    map.put("nomeRazao", a.getNomeRazao());
                    map.put("status", a.getStatus());
                    map.put("dataInativacao", a.getDataInativacao() != null ?
                        a.getDataInativacao().toString() : null);
                    map.put("motivoInativacao", a.getMotivoInativacao());
                    return map;
                }).collect(Collectors.toList());

                String inativadosJson = mapper.writeValueAsString(inativadosMap);
                headers.add("X-Importacao-Inativados-List", inativadosJson);

                logger.info("📋 {} associados inativados incluídos no header", associadosInativados.size());

            } catch (Exception e) {
                logger.warn("⚠️ Erro ao serializar lista de inativados: {}", e.getMessage());
            }
        }

        // ============================================================
        // EXPOSE HEADERS PARA O FRONTEND
        // ============================================================
        headers.add("Access-Control-Expose-Headers", 
            "X-Importacao-Criados, X-Importacao-Atualizados, X-Importacao-Erros, " +
            "X-Importacao-Configuracoes, X-Importacao-Inativados, X-Importacao-Inativados-List");

        return ResponseEntity.ok()
                .headers(headers)
                .body(associadosImportados);
    }
    
    // ========== 🔥 MÉTODO AUXILIAR DE NORMALIZAÇÃO ==========

    /**
     * Normaliza o status vindo do CSV para o formato do sistema
     * "ATIVO" → "A", "SUSPENSO" → "S", "INATIVO" → "I"
     */
    private String normalizarStatusImportacao(String status) {
        if (status == null) return "A";
        String s = status.trim().toUpperCase();
        if ("ATIVO".equals(s) || "A".equals(s)) return "A";
        if ("SUSPENSO".equals(s) || "S".equals(s)) return "S";
        if ("INATIVO".equals(s) || "I".equals(s)) return "I";
        return "A";
    }

    // ============================================================
    // ATUALIZAR ENDEREÇOS (EDIT)
    // ============================================================
    @PutMapping("/{id}/enderecos")
    @PreAuthorize("hasAuthority('ASSOCIADO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EnderecoDTO>> atualizarEnderecos(@PathVariable Long id,
            @Valid @RequestBody List<EnderecoDTO> enderecosDTO) {
        List<EnderecoDTO> enderecosAtualizados = associadoService.atualizarEnderecos(id, enderecosDTO);
        return ResponseEntity.ok(enderecosAtualizados);
    }

    // ============================================================
    // ATUALIZAR TELEFONES (EDIT)
    // ============================================================
    @PutMapping("/{id}/telefones")
    @PreAuthorize("hasAuthority('ASSOCIADO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TelefoneDTO>> atualizarTelefones(@PathVariable Long id,
            @Valid @RequestBody List<TelefoneDTO> telefonesDTO) {
        List<TelefoneDTO> telefonesAtualizados = associadoService.atualizarTelefones(id, telefonesDTO);
        return ResponseEntity.ok(telefonesAtualizados);
    }

    // ============================================================
    // ATUALIZAR EMAILS (EDIT)
    // ============================================================
    @PutMapping("/{id}/emails")
    @PreAuthorize("hasAuthority('ASSOCIADO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EmailDTO>> atualizarEmails(@PathVariable Long id,
            @Valid @RequestBody List<EmailDTO> emailsDTO) {
        List<EmailDTO> emailsAtualizados = associadoService.atualizarEmails(id, emailsDTO);
        return ResponseEntity.ok(emailsAtualizados);
    }

    // ============================================================
    // BUSCAR ENDEREÇOS (VIEW)
    // ============================================================
    @GetMapping("/{id}/enderecos")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EnderecoDTO>> buscarEnderecos(@PathVariable Long id) {
        List<EnderecoDTO> enderecos = associadoService.buscarEnderecosPorAssociadoId(id);
        return ResponseEntity.ok(enderecos);
    }

    // ============================================================
    // BUSCAR TELEFONES (VIEW)
    // ============================================================
    @GetMapping("/{id}/telefones")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<TelefoneDTO>> buscarTelefones(@PathVariable Long id) {
        List<TelefoneDTO> telefones = associadoService.buscarTelefonesPorAssociadoId(id);
        return ResponseEntity.ok(telefones);
    }

    // ============================================================
    // BUSCAR EMAILS (VIEW)
    // ============================================================
    @GetMapping("/{id}/emails")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<EmailDTO>> buscarEmails(@PathVariable Long id) {
        List<EmailDTO> emails = associadoService.buscarEmailsPorAssociadoId(id);
        return ResponseEntity.ok(emails);
    }

    // ============================================================
    // BUSCAR CONFIGURAÇÕES DE FATURAMENTO (VIEW)
    // ============================================================
    @GetMapping("/{id}/configuracoes-faturamento")
    @PreAuthorize("hasAuthority('ASSOCIADO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<AssociadoDefFaturamentoDTO>> buscarConfiguracoesFaturamento(@PathVariable Long id) {
        logger.info("📋 Buscando configurações de faturamento do associado ID: {}", id);
        
        List<AssociadoDefFaturamentoResumoDTO> configs = associadoDefFaturamentoService.listarPorAssociado(id);
        
        List<AssociadoDefFaturamentoDTO> result = configs.stream().map(config -> {
            AssociadoDefFaturamentoDTO dto = new AssociadoDefFaturamentoDTO();
            dto.setId(config.getId());
            dto.setAssociadoId(id);
            dto.setPlanoId(config.getPlanoId());
            dto.setDiaEmissao(config.getDiaEmissao());
            dto.setDiaVencimento(config.getDiaVencimento());
            dto.setValorDef(config.getValorDef());
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    // ============================================================
    // 🔥 MIGRAR ASSOCIADO PARA RÉGUA (MIGRAR_REGUA)
    // ============================================================
    @PostMapping("/{id}/migrar-regua")
    @PreAuthorize("hasAuthority('ASSOCIADO_MIGRAR_REGUA') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> migrarRegua(@PathVariable Long id) {
        logger.info("📏 Migrando associado ID: {} para régua de faturamento", id);
        associadoService.migrarParaRegua(id);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // INNER CLASS
    // ============================================================
    public static class HealthResponse {
        private String status;
        private String service;
        private Long totalAssociados;

        public HealthResponse(String status, String service, Long totalAssociados) {
            this.status = status;
            this.service = service;
            this.totalAssociados = totalAssociados;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public Long getTotalAssociados() {
            return totalAssociados;
        }

        public void setTotalAssociados(Long totalAssociados) {
            this.totalAssociados = totalAssociados;
        }
    }
}