// src/main/java/com/sga/controller/ReguaFaturamentoController.java

package com.sga.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
import com.sga.dto.ReguaFaturamentoDTO;
import com.sga.model.Associado;
import com.sga.model.AssociadoRegua;
import com.sga.model.ReguaFaturamento;
import com.sga.service.AssociadoService;
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

    // ==================== RÉGUAS ====================

    @GetMapping
    public ResponseEntity<Page<ReguaFaturamento>> listarReguas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        log.info("📋 Listando réguas - page: {}, size: {}, sort: {}", page, size, sort);
        return ResponseEntity.ok(reguaService.listarReguas(pageable));
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<ReguaFaturamento>> listarReguasAtivas() {
        log.info("📋 Listando réguas ativas");
        return ResponseEntity.ok(reguaService.listarReguasAtivas());
    }

    @GetMapping("/ativas")
    public ResponseEntity<List<ReguaFaturamento>> listarReguasAtivasAlias() {
        log.info("📋 Listando réguas ativas (alias /ativas)");
        return ResponseEntity.ok(reguaService.listarReguasAtivas());
    }

    @GetMapping("/padrao")
    public ResponseEntity<ReguaFaturamento> buscarReguaPadrao() {
        log.info("🔍 Buscando régua padrão");
        return reguaService.buscarReguaPadrao().map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReguaFaturamentoDTO> buscarReguaPorId(@PathVariable Long id) {
        log.info("🔍 Buscando régua ID: {}", id);
        
        // 🔥 USAR O SERVICE PARA BUSCAR COM TIPOS DE ARQUIVO
        ReguaFaturamentoDTO regua = reguaService.buscarReguaDTOPorId(id);
        if (regua == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(regua);
    }

    @PostMapping
    public ResponseEntity<ReguaFaturamento> criarRegua(@Valid @RequestBody ReguaFaturamento regua,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("➕ Criando nova régua: {}", regua.getNome());
        ReguaFaturamento novaRegua = reguaService.criarRegua(regua, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaRegua);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReguaFaturamento> atualizarRegua(@PathVariable Long id,
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

    // ==================== ASSOCIADOS NA RÉGUA ====================

    @GetMapping("/{id}/associados")
    public ResponseEntity<List<AssociadoReguaDTO>> listarAssociadosPorRegua(@PathVariable Long id) {
        log.info("👥 Listando associados da régua ID: {}", id);
        return ResponseEntity.ok(reguaService.listarAssociadosPorReguaDTO(id));
    }

    @GetMapping("/associados/disponiveis")
    public ResponseEntity<Page<AssociadoResumoDTO>> buscarAssociadosDisponiveis(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cnpjCpf,
            @RequestParam(required = false) Long reguaId,
            @RequestParam(required = false) Long planoId,
            @RequestParam(required = false) Long vendedorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("🔍 Buscando associados disponíveis para faturamento");
        Pageable pageable = PageRequest.of(page, size, Sort.by("nomeRazao").ascending());
        Page<AssociadoResumoDTO> associados = associadoService.listarComFiltros(pageable, null, nome, cnpjCpf, "A");
        return ResponseEntity.ok(associados);
    }

    @GetMapping("/{id}/associados/paginado")
    public ResponseEntity<Page<AssociadoResumoDTO>> listarAssociadosPorReguaPaginado(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.info("👥 Listando associados da régua ID: {} - página: {}, size: {}", id, page, size);
        
        // 🔥 IMPORTANTE: Criar Pageable SEM ordenação para evitar problemas
        // A ordenação será feita manualmente ou removida
        Pageable pageable = PageRequest.of(page, size);
        
        Page<AssociadoResumoDTO> associados = reguaService.listarAssociadosPorReguaPaginado(id, pageable);
        return ResponseEntity.ok(associados);
    }

    @GetMapping("/associado/ativo/{associadoId}")
    public ResponseEntity<AssociadoRegua> buscarReguaAtivaDoAssociado(@PathVariable Long associadoId) {
        log.info("🔍 Buscando régua ativa do associado ID: {}", associadoId);
        return reguaService.buscarAssociadoAtivo(associadoId).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{reguaId}/associados/{associadoId}")
    public ResponseEntity<AssociadoReguaDTO> adicionarAssociadoARegua(
            @PathVariable Long reguaId,
            @PathVariable Long associadoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

        log.info("➕ Adicionando associado {} à régua {}", associadoId, reguaId);
        AssociadoReguaDTO associadoRegua = reguaService.adicionarAssociadoAReguaDTO(associadoId, reguaId, dataInicio, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(associadoRegua);
    }

    @PutMapping("/associados/{associadoId}/migrar/{reguaDestinoId}")
    public ResponseEntity<AssociadoRegua> migrarAssociado(@PathVariable Long associadoId,
            @PathVariable Long reguaDestinoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataMigracao,
            @RequestParam(required = false) String motivo,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {

        log.info("🔄 Migrando associado {} para régua {}", associadoId, reguaDestinoId);
        AssociadoRegua novaAssociacao = reguaService.migrarAssociado(associadoId, reguaDestinoId, dataMigracao, motivo, usuario);
        return ResponseEntity.ok(novaAssociacao);
    }

    @DeleteMapping("/associados/{associadoId}")
    public ResponseEntity<Void> removerAssociadoDaRegua(@PathVariable Long associadoId,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        log.info("🗑️ Removendo associado {} da régua atual", associadoId);
        reguaService.removerAssociadoDaRegua(associadoId, usuario);
        return ResponseEntity.noContent().build();
    }
    

}