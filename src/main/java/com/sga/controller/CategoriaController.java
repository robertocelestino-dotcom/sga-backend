package com.sga.controller;

import com.sga.dto.CategoriaDTO;
import com.sga.dto.CategoriaResumoDTO;
import com.sga.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaDTO> criar(@RequestBody CategoriaDTO dto) {
        if (dto.getDescricao() == null || dto.getDescricao().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        CategoriaDTO categoria = categoriaService.criar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoria);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> atualizar(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
        CategoriaDTO categoria = categoriaService.atualizar(id, dto);
        return ResponseEntity.ok(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaDTO> buscarPorId(@PathVariable Long id) {
        CategoriaDTO categoria = categoriaService.buscarPorId(id);
        return ResponseEntity.ok(categoria);
    }

    @GetMapping
    public ResponseEntity<Page<CategoriaResumoDTO>> listar(
            Pageable pageable,
            @RequestParam(required = false) String descricao,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String status) {
        Page<CategoriaResumoDTO> categorias = categoriaService.listar(pageable, descricao, tipo, status);
        return ResponseEntity.ok(categorias);
    }

    // ENDPOINT PARA COMBOBOX - todas as categorias
    @GetMapping("/ativas")
    public ResponseEntity<List<CategoriaResumoDTO>> listarAtivas() {
        List<CategoriaResumoDTO> categorias = categoriaService.listarTodasAtivas();
        return ResponseEntity.ok(categorias);
    }

    @GetMapping("/por-tipo/{tipo}")
    public ResponseEntity<List<CategoriaResumoDTO>> listarPorTipo(@PathVariable String tipo) {
        List<CategoriaResumoDTO> categorias = categoriaService.listarPorTipo(tipo);
        return ResponseEntity.ok(categorias);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        try {
            categoriaService.inativar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("CategoriaController está funcionando!");
    }
}