package com.sga.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.PlanoProdutoFranquiaDTO;
import com.sga.service.PlanoProdutoFranquiaService;

@RestController
@RequestMapping("/api/planos-produtos-franquia")
@CrossOrigin(origins = "*")
public class PlanoProdutoFranquiaController {

    private final PlanoProdutoFranquiaService service;

    public PlanoProdutoFranquiaController(PlanoProdutoFranquiaService service) {
        this.service = service;
    }

    @PostMapping("/associar")
    public ResponseEntity<PlanoProdutoFranquiaDTO> associar(@RequestParam Long planoId, @RequestParam Long produtoId,
            @RequestParam Long franquiaId, @RequestParam Integer limiteFranquia,
            @RequestParam(required = false) BigDecimal valorExcedente,
            @RequestParam(required = false, defaultValue = "MENSAL") String periodoFranquia) {

        PlanoProdutoFranquiaDTO dto = service.associar(planoId, produtoId, franquiaId, limiteFranquia, valorExcedente,
                periodoFranquia);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanoProdutoFranquiaDTO> atualizar(@PathVariable Long id,
            @RequestParam(required = false) Integer limiteFranquia,
            @RequestParam(required = false) BigDecimal valorExcedente, @RequestParam(required = false) Boolean ativo) {

        PlanoProdutoFranquiaDTO dto = service.atualizar(id, limiteFranquia, valorExcedente, ativo);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/plano/{planoId}")
    public ResponseEntity<List<PlanoProdutoFranquiaDTO>> listarPorPlano(@PathVariable Long planoId) {
        List<PlanoProdutoFranquiaDTO> dtos = service.listarPorPlano(planoId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/plano/{planoId}/produto/{produtoId}")
    public ResponseEntity<PlanoProdutoFranquiaDTO> buscarPorPlanoEProduto(@PathVariable Long planoId,
            @PathVariable Long produtoId) {

        PlanoProdutoFranquiaDTO dto = service.buscarPorPlanoEProduto(planoId, produtoId);
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
    
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<?> listarPorProduto(@PathVariable Long produtoId) {
        try {
            System.out.println("📥 [Controller] Recebida requisição para produto ID: " + produtoId);
            
            List<PlanoProdutoFranquiaDTO> dtos = service.listarPorProduto(produtoId);
            
            System.out.println("📤 [Controller] Retornando " + dtos.size() + " associações");
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            System.err.println("❌ [Controller] Erro ao listar associações por produto: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erro interno ao processar requisição");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
    
}