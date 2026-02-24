package com.sga.controller;

import com.sga.dto.TipoEnvioDTO;
import com.sga.dto.TipoEnvioResumoDTO;
import com.sga.service.TipoEnvioService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tipos-envio")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TipoEnvioController {

    private static final Logger logger = LoggerFactory.getLogger(TipoEnvioController.class);
    
    
    @Autowired
    private TipoEnvioService tipoEnvioService;

    @GetMapping
    public ResponseEntity<Page<TipoEnvioResumoDTO>> listarComFiltros(
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) Boolean ativo,
            @PageableDefault(page = 0, size = 10, sort = "codigo", direction = Sort.Direction.ASC) Pageable pageable) {

        logger.info("Listando tipos de envio com filtros - descricao: {}, ativo: {}", descricao, ativo);
        Page<TipoEnvioResumoDTO> result = tipoEnvioService.buscarComFiltros(descricao, ativo, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<TipoEnvioResumoDTO>> listarAtivos() {
        logger.info("Listando tipos de envio ativos");
        List<TipoEnvioResumoDTO> result = tipoEnvioService.listarAtivos();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEnvioDTO> buscarPorId(@PathVariable Long id) {
        logger.info("Buscando tipo de envio por ID: {}", id);
        TipoEnvioDTO dto = tipoEnvioService.buscarPorId(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<TipoEnvioDTO> criar(@Valid @RequestBody TipoEnvioDTO dto) {
        logger.info("Criando novo tipo de envio: {}", dto.getDescricao());
        TipoEnvioDTO created = tipoEnvioService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEnvioDTO> atualizar(@PathVariable Long id, @Valid @RequestBody TipoEnvioDTO dto) {
        logger.info("Atualizando tipo de envio ID: {}", id);
        TipoEnvioDTO updated = tipoEnvioService.atualizar(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        logger.info("Excluindo tipo de envio ID: {}", id);
        tipoEnvioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TipoEnvioDTO> alterarStatus(@PathVariable Long id, @RequestParam Boolean ativo) {
        logger.info("Alterando status do tipo de envio ID: {} para ativo: {}", id, ativo);
        TipoEnvioDTO updated = tipoEnvioService.alterarStatus(id, ativo);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("TipoEnvioController está funcionando");
    }
}