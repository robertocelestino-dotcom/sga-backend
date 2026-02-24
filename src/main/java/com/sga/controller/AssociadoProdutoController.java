package com.sga.controller;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.AssociadoProdutoDTO;
import com.sga.dto.AssociadoProdutoResumoDTO;
import com.sga.service.AssociadoProdutoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/associados-produtos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssociadoProdutoController {

    private static final Logger logger = LoggerFactory.getLogger(AssociadoProdutoController.class);
    
    @Autowired
    private AssociadoProdutoService associadoProdutoService;

    @GetMapping("/associado/{associadoId}")
    public ResponseEntity<List<AssociadoProdutoResumoDTO>> listarPorAssociado(@PathVariable Long associadoId) {
        logger.info("Listando produtos do associado ID: {}", associadoId);
        List<AssociadoProdutoResumoDTO> result = associadoProdutoService.listarPorAssociado(associadoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/associado/{associadoId}/paginado")
    public ResponseEntity<Page<AssociadoProdutoResumoDTO>> listarPorAssociadoPaginado(
            @PathVariable Long associadoId,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        logger.info("Listando produtos do associado ID: {} - página: {}", associadoId, pageable.getPageNumber());
        Page<AssociadoProdutoResumoDTO> result = associadoProdutoService.listarPorAssociadoPaginado(associadoId, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/associado/{associadoId}/ativos")
    public ResponseEntity<List<AssociadoProdutoResumoDTO>> listarAtivosPorAssociado(@PathVariable Long associadoId) {
        logger.info("Listando produtos ativos do associado ID: {}", associadoId);
        List<AssociadoProdutoResumoDTO> result = associadoProdutoService.listarAtivosPorAssociado(associadoId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssociadoProdutoDTO> buscarPorId(@PathVariable Long id) {
        logger.info("Buscando produto do associado por ID: {}", id);
        AssociadoProdutoDTO dto = associadoProdutoService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<AssociadoProdutoDTO> criar(
            @Valid @RequestBody AssociadoProdutoDTO dto,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        logger.info("Criando produto para associado ID: {}", dto.getAssociadoId());
        dto.setUsuario(usuario != null ? usuario : "SISTEMA");
        AssociadoProdutoDTO created = associadoProdutoService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/lote")
    public ResponseEntity<List<AssociadoProdutoDTO>> criarEmLote(
            @Valid @RequestBody List<AssociadoProdutoDTO> dtos,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        logger.info("Criando {} produtos em lote", dtos.size());
        dtos.forEach(dto -> dto.setUsuario(usuario != null ? usuario : "SISTEMA"));
        List<AssociadoProdutoDTO> created = associadoProdutoService.criarEmLote(dtos);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssociadoProdutoDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AssociadoProdutoDTO dto,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        logger.info("Atualizando produto do associado ID: {}", id);
        dto.setUsuario(usuario != null ? usuario : "SISTEMA");
        AssociadoProdutoDTO updated = associadoProdutoService.atualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        logger.info("Excluindo produto do associado ID: {}", id);
        associadoProdutoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/associado/{associadoId}")
    public ResponseEntity<Void> excluirPorAssociado(@PathVariable Long associadoId) {
        logger.info("Excluindo todos os produtos do associado ID: {}", associadoId);
        associadoProdutoService.excluirPorAssociado(associadoId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AssociadoProdutoDTO> alterarStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {

        logger.info("Alterando status do produto ID: {} para {}", id, status);
        AssociadoProdutoDTO updated = associadoProdutoService.alterarStatus(id, status,
                usuario != null ? usuario : "SISTEMA");
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AssociadoProdutoController está funcionando");
    }
    
}