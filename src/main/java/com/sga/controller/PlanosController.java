package com.sga.controller;

import com.sga.dto.PlanoDTO;
import com.sga.dto.PlanoResumoDTO;
import com.sga.service.PlanosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planos")
public class PlanosController {

    @Autowired
    private PlanosService planosService;

    @PostMapping
    public ResponseEntity<PlanoDTO> criar(@RequestBody PlanoDTO dto) {
        try {
            if (dto.getPlano() == null || dto.getPlano().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            PlanoDTO plano = planosService.criar(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(plano);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoDTO> atualizar(@PathVariable Long id, @RequestBody PlanoDTO dto) {
        try {
            PlanoDTO plano = planosService.atualizar(id, dto);
            return ResponseEntity.ok(plano);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanoDTO> buscarPorId(@PathVariable Long id) {
        try {
            PlanoDTO plano = planosService.buscarPorId(id);
            return ResponseEntity.ok(plano);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<PlanoResumoDTO>> listar(
            Pageable pageable,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String status) {
        Page<PlanoResumoDTO> planos = planosService.listar(pageable, nome, status);
        return ResponseEntity.ok(planos);
    }

    // ENDPOINT PARA COMBOBOX - todos os planos (não tem status no modelo)
    @GetMapping("/ativos")
    public ResponseEntity<List<PlanoResumoDTO>> listarAtivos() {
        List<PlanoResumoDTO> planos = planosService.listarTodosAtivos();
        return ResponseEntity.ok(planos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            planosService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        try {
            planosService.inativar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("PlanosController está funcionando!");
    }
}