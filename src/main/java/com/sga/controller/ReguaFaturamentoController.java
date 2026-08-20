// src/main/java/com/sga/controller/ReguaFaturamentoController.java

package com.sga.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.AssociadoReguaDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.HistoricoMigracaoDTO;
import com.sga.dto.MigracaoReguaRequest;
import com.sga.dto.MigracaoReguaResponse;
import com.sga.dto.ReguaFaturamentoDTO;
import com.sga.model.AssociadoRegua;
import com.sga.model.HistoricoMigracaoRegua;
import com.sga.model.ReguaFaturamento;
import com.sga.service.AssociadoService;
import com.sga.service.MigracaoReguaService;
import com.sga.service.ReguaFaturamentoService;

@RestController
@RequestMapping("/api/regua-faturamento")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ReguaFaturamentoController {

    private static final Logger log = LoggerFactory.getLogger(ReguaFaturamentoController.class);

    @Autowired
    private ReguaFaturamentoService reguaService;

    @Autowired
    private AssociadoService associadoService;

    @Autowired
    private MigracaoReguaService migracaoReguaService;

    // ============================================================
    // ENDPOINTS DE RÉGUAS (EXISTENTES)
    // ============================================================

    @GetMapping
    public ResponseEntity<Page<ReguaFaturamento>> listarReguas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        log.info("📋 Listando réguas - page: {}, size: {}, sort: {}, direction: {}", page, size, sort, direction);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<ReguaFaturamento> reguas = reguaService.listarReguas(pageable);
        return ResponseEntity.ok(reguas);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ReguaFaturamento>> listarReguasAtivas() {
        log.info("📋 Listando réguas ativas");
        List<ReguaFaturamento> reguas = reguaService.listarReguasAtivas();
        return ResponseEntity.ok(reguas);
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<ReguaFaturamento>> listarReguasAtivasAlias() {
        log.info("📋 Listando réguas ativas (alias /ativas)");
        List<ReguaFaturamento> reguas = reguaService.listarReguasAtivas();
        return ResponseEntity.ok(reguas);
    }

    @GetMapping("/padrao")
    public ResponseEntity<ReguaFaturamento> buscarReguaPadrao() {
        log.info("🔍 Buscando régua padrão");
        return reguaService.buscarReguaPadrao()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReguaFaturamentoDTO> buscarReguaPorId(@PathVariable Long id) {
        log.info("🔍 Buscando régua ID: {}", id);
        ReguaFaturamentoDTO regua = reguaService.buscarReguaDTOPorId(id);
        if (regua == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(regua);
    }

    @PostMapping
    public ResponseEntity<ReguaFaturamento> criarRegua(
            @Valid @RequestBody ReguaFaturamento regua,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("➕ Criando nova régua: {}", regua.getNome());
        ReguaFaturamento novaRegua = reguaService.criarRegua(regua, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaRegua);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReguaFaturamento> atualizarRegua(
            @PathVariable Long id,
            @Valid @RequestBody ReguaFaturamento regua,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("✏️ Atualizando régua ID: {}", id);
        ReguaFaturamento reguaAtualizada = reguaService.atualizarRegua(id, regua, usuario);
        return ResponseEntity.ok(reguaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirRegua(@PathVariable Long id) {
        log.info("🗑️ Excluindo régua ID: {}", id);
        reguaService.excluirRegua(id);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ENDPOINTS DE ASSOCIADOS NA RÉGUA
    // ============================================================

    @GetMapping("/{id}/associados")
    public ResponseEntity<List<AssociadoReguaDTO>> listarAssociadosPorRegua(@PathVariable Long id) {
        log.info("👥 Listando associados da régua ID: {}", id);
        return ResponseEntity.ok(reguaService.listarAssociadosPorReguaDTO(id));
    }

    @GetMapping("/{id}/associados/paginado")
    public ResponseEntity<Page<AssociadoResumoDTO>> listarAssociadosPorReguaPaginado(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cnpjCpf) {

        log.info("👥 Listando associados da régua ID: {} - página: {}, size: {}, nome: {}, cnpj: {}", 
                id, page, size, nome, cnpjCpf);

        Pageable pageable = PageRequest.of(page, size);
        Page<AssociadoResumoDTO> associados = reguaService.listarAssociadosConsolidadoPaginado(
                id, nome, cnpjCpf, pageable);

        return ResponseEntity.ok(associados);
    }

    @GetMapping("/{reguaId}/associados-consolidado/todos-ids")
    public ResponseEntity<List<Long>> listarTodosIdsAssociadosConsolidado(
            @PathVariable Long reguaId) {

        log.info("📌 Listando TODOS os IDs dos associados CONSOLIDADOS da régua ID: {}", reguaId);

        List<Long> ids = reguaService.listarTodosIdsAssociadosConsolidado(reguaId);
        return ResponseEntity.ok(ids);
    }

    @GetMapping("/{reguaId}/associados-consolidado/paginado")
    public ResponseEntity<Page<AssociadoResumoDTO>> listarAssociadosConsolidadoPaginado(
            @PathVariable Long reguaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cnpjCpf) {

        log.info("📌 Listando associados CONSOLIDADOS da régua ID: {} - página: {}, size: {}, nome: {}, cnpj: {}", 
                reguaId, page, size, nome, cnpjCpf);

        Pageable pageable = PageRequest.of(page, size);
        Page<AssociadoResumoDTO> associados = reguaService.listarAssociadosConsolidadoPaginado(
                reguaId, nome, cnpjCpf, pageable);

        return ResponseEntity.ok(associados);
    }

    @GetMapping("/associado/ativo/{associadoId}")
    public ResponseEntity<?> buscarReguaAtivaDoAssociado(@PathVariable Long associadoId) {
        log.info("🔍 Buscando régua ativa do associado ID: {}", associadoId);
        
        try {
            Optional<AssociadoRegua> associacao = reguaService.buscarAssociadoAtivo(associadoId);
            
            if (associacao.isPresent()) {
                AssociadoRegua ar = associacao.get();
                
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("id", ar.getId());
                response.put("associadoId", ar.getAssociado().getId());
                response.put("associadoNome", ar.getAssociado().getNomeRazao());
                
                Map<String, Object> reguaMap = new LinkedHashMap<>();
                reguaMap.put("id", ar.getRegua().getId());
                reguaMap.put("nome", ar.getRegua().getNome() != null ? ar.getRegua().getNome() : "Sem nome");
                reguaMap.put("descricao", ar.getRegua().getDescricao() != null ? ar.getRegua().getDescricao() : "");
                reguaMap.put("diaEmissao", ar.getRegua().getDiaEmissao() != null ? ar.getRegua().getDiaEmissao() : 0);
                reguaMap.put("diaVencimento", ar.getRegua().getDiaVencimento() != null ? ar.getRegua().getDiaVencimento() : 0);
                reguaMap.put("ehPadrao", ar.getRegua().getEhPadrao() != null ? ar.getRegua().getEhPadrao() : false);
                reguaMap.put("permiteMigracao", ar.getRegua().getPermiteMigracao() != null ? ar.getRegua().getPermiteMigracao() : true);
                reguaMap.put("ativo", ar.getRegua().getAtivo() != null ? ar.getRegua().getAtivo() : true);
                
                response.put("regua", reguaMap);
                response.put("dataInicio", ar.getDataInicio());
                response.put("dataFim", ar.getDataFim());
                response.put("ativo", ar.getAtivo() != null ? ar.getAtivo() : false);
                response.put("motivoMigracao", ar.getMotivoMigracao());
                response.put("observacao", ar.getObservacao());
                
                log.info("✅ Régua encontrada: {} (ID: {})", ar.getRegua().getNome(), ar.getRegua().getId());
                return ResponseEntity.ok(response);
            } else {
                log.info("ℹ️ Nenhuma régua ativa encontrada para o associado {}", associadoId);
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("ativo", false);
                response.put("mensagem", "Nenhuma régua ativa");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar régua ativa: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("erro", e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/{id}/associados/todos")
    public ResponseEntity<List<AssociadoResumoDTO>> listarTodosAssociadosPorRegua(@PathVariable Long id) {
        log.info("👥 Listando TODOS os associados da régua ID: {}", id);
        List<AssociadoResumoDTO> associados = reguaService.listarTodosAssociadosResumo(id);
        return ResponseEntity.ok(associados);
    }

    @GetMapping("/{id}/associados/todos-ids")
    public ResponseEntity<List<Long>> listarTodosIdsAssociadosPorRegua(@PathVariable Long id) {
        log.info("👥 Listando TODOS os IDs dos associados da régua ID: {}", id);
        List<Long> ids = reguaService.listarTodosIdsAssociadosPorRegua(id);
        return ResponseEntity.ok(ids);
    }

    // ============================================================
    // ENDPOINT PARA REMOVER ASSOCIADO DA RÉGUA
    // ============================================================

    @DeleteMapping("/associados/{associadoId}")
    public ResponseEntity<Void> removerAssociadoDaRegua(
            @PathVariable Long associadoId,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

        log.info("🗑️ Removendo associado {} da régua atual - Usuário: {}", associadoId, usuario);

        try {
            reguaService.removerAssociadoDaRegua(associadoId, usuario);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("❌ Erro ao remover associado {} da régua: {}", associadoId, e.getMessage(), e);
            throw e;
        }
    }

    // ============================================================
    // ENDPOINTS DE MIGRAÇÃO DE RÉGUAS
    // ============================================================

    /**
     * 🔥 VERIFICA SE O ASSOCIADO PODE SER MIGRADO
     */
    @GetMapping("/associados/{associadoId}/verificar-migracao/{reguaId}")
    public ResponseEntity<MigracaoReguaResponse> verificarMigracao(
            @PathVariable Long associadoId,
            @PathVariable Long reguaId) {

        log.info("🔍 Verificando migração do associado {} para régua {}", associadoId, reguaId);

        try {
            MigracaoReguaResponse response = migracaoReguaService.verificarMigracao(associadoId, reguaId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erro ao verificar migração: {}", e.getMessage(), e);

            MigracaoReguaResponse errorResponse = new MigracaoReguaResponse();
            errorResponse.setAssociadoId(associadoId);
            errorResponse.setStatus("ERRO");
            errorResponse.setMensagem(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 🔥 VERIFICA SE JÁ EXISTE UMA MIGRAÇÃO COM A DATA INFORMADA
     */
    @GetMapping("/associados/{associadoId}/verificar-data-migracao")
    public ResponseEntity<Map<String, Object>> verificarDataMigracao(
            @PathVariable Long associadoId,
            @RequestParam Long reguaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

        log.info("🔍 Verificando data {} para associado {} e régua {}", data, associadoId, reguaId);

        boolean existe = migracaoReguaService.existeMigracaoComData(associadoId, reguaId, data);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("existe", existe);
        response.put("data", data.toString());
        response.put("associadoId", associadoId);
        response.put("reguaId", reguaId);
        response.put("mensagem", existe ? 
            "Já existe uma migração com esta data" : 
            "Data disponível para migração");

        return ResponseEntity.ok(response);
    }

    /**
     * 🔥 EXECUTA A MIGRAÇÃO DO ASSOCIADO
     */
    @PostMapping("/associados/{associadoId}/migrar")
    public ResponseEntity<MigracaoReguaResponse> migrarAssociado(
            @PathVariable Long associadoId,
            @RequestBody MigracaoReguaRequest request,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

        log.info("🔄 Migrando associado {} para régua {} - Usuário: {}", 
                associadoId, request.getNovaReguaId(), usuario);

        try {
            request.setAssociadoId(associadoId);
            MigracaoReguaResponse response = migracaoReguaService.executarMigracao(request, usuario);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Erro ao migrar associado: {}", e.getMessage(), e);

            MigracaoReguaResponse errorResponse = new MigracaoReguaResponse();
            errorResponse.setAssociadoId(associadoId);
            errorResponse.setStatus("ERRO");
            errorResponse.setMensagem(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 🔥 BUSCA HISTÓRICO DE MIGRAÇÕES DE UM ASSOCIADO
     */
    @GetMapping("/associados/{associadoId}/historico-migracao")
    public ResponseEntity<List<HistoricoMigracaoDTO>> buscarHistoricoMigracao(
            @PathVariable Long associadoId) {

        log.info("📋 Buscando histórico de migrações do associado {}", associadoId);

        try {
            // 🔥 USAR O MÉTODO QUE RETORNA DTO
            List<HistoricoMigracaoDTO> historico = migracaoReguaService.buscarHistoricoMigracaoDTO(associadoId);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar histórico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 🔥 BUSCA HISTÓRICO DE MIGRAÇÕES DE UM ASSOCIADO (LEGADO - RETORNA ENTIDADE)
     * @deprecated Use o método que retorna DTO
     */
    @Deprecated
    @GetMapping("/associados/{associadoId}/historico-migracao-legado")
    public ResponseEntity<List<HistoricoMigracaoRegua>> buscarHistoricoMigracaoLegado(
            @PathVariable Long associadoId) {

        log.info("📋 Buscando histórico de migrações do associado {} (legado)", associadoId);

        try {
            List<HistoricoMigracaoRegua> historico = migracaoReguaService.buscarHistoricoMigracao(associadoId);
            return ResponseEntity.ok(historico);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar histórico: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🔥 LISTA RÉGUAS DISPONÍVEIS PARA MIGRAÇÃO
     */
    @GetMapping("/associados/{associadoId}/reguas-disponiveis")
    public ResponseEntity<List<ReguaFaturamento>> listarReguasDisponiveisParaMigracao(
            @PathVariable Long associadoId) {

        log.info("📋 Listando réguas disponíveis para migração do associado {}", associadoId);

        try {
            List<ReguaFaturamento> reguas = migracaoReguaService.listarReguasDisponiveisParaMigracao(associadoId);
            return ResponseEntity.ok(reguas);
        } catch (Exception e) {
            log.error("❌ Erro ao listar réguas disponíveis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}