// src/main/java/com/sga/controller/VendedorTipoController.java
package com.sga.controller;

import com.sga.dto.VendedorTipoDTO;
import com.sga.service.VendedorTipoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vendedor-tipos")
@CrossOrigin(origins = "*")
public class VendedorTipoController {
    
    private static final Logger logger = LoggerFactory.getLogger(VendedorTipoController.class);
    
    @Autowired
    private VendedorTipoService vendedorTipoService;
    
    // GET - Listar todos
    @GetMapping
    public ResponseEntity<List<VendedorTipoDTO>> listarTodos() {
        logger.info("Recebida requisição para listar todos os tipos de vendedor");
        
        try {
            List<VendedorTipoDTO> tipos = vendedorTipoService.listarTodos();
            return ResponseEntity.ok(tipos);
        } catch (Exception e) {
            logger.error("Erro ao listar tipos de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<VendedorTipoDTO> buscarPorId(@PathVariable Long id) {
        logger.info("Recebida requisição para buscar tipo de vendedor por ID: {}", id);
        
        try {
            VendedorTipoDTO tipo = vendedorTipoService.buscarPorId(id);
            return ResponseEntity.ok(tipo);
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Tipo de vendedor não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar tipo de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar por descrição
    @GetMapping("/buscar")
    public ResponseEntity<List<VendedorTipoDTO>> buscarPorDescricao(@RequestParam String descricao) {
        logger.info("Recebida requisição para buscar tipos de vendedor por descrição: {}", descricao);
        
        try {
            List<VendedorTipoDTO> tipos = vendedorTipoService.buscarPorDescricao(descricao);
            return ResponseEntity.ok(tipos);
        } catch (Exception e) {
            logger.error("Erro ao buscar tipos de vendedor por descrição: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // POST - Criar novo
    @PostMapping
    public ResponseEntity<VendedorTipoDTO> criar(@Valid @RequestBody VendedorTipoDTO vendedorTipoDTO) {
        logger.info("Recebida requisição para criar tipo de vendedor: {}", vendedorTipoDTO.getDescricao());
        
        try {
            VendedorTipoDTO criado = vendedorTipoService.criar(vendedorTipoDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao criar tipo de vendedor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao criar tipo de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // PUT - Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<VendedorTipoDTO> atualizar(@PathVariable Long id, 
                                                    @Valid @RequestBody VendedorTipoDTO vendedorTipoDTO) {
        logger.info("Recebida requisição para atualizar tipo de vendedor ID: {}", id);
        
        try {
            VendedorTipoDTO atualizado = vendedorTipoService.atualizar(id, vendedorTipoDTO);
            return ResponseEntity.ok(atualizado);
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Tipo de vendedor não encontrado para atualização: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar tipo de vendedor: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao atualizar tipo de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DELETE - Excluir
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        logger.info("Recebida requisição para excluir tipo de vendedor ID: {}", id);
        
        try {
            vendedorTipoService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Tipo de vendedor não encontrado para exclusão: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao excluir tipo de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Contar total
    @GetMapping("/count")
    public ResponseEntity<Long> contarTotal() {
        logger.info("Recebida requisição para contar tipos de vendedor");
        
        try {
            Long total = vendedorTipoService.contarTotal();
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            logger.error("Erro ao contar tipos de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Verificar se existe por descrição
    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorDescricao(@RequestParam String descricao) {
        logger.info("Recebida requisição para verificar se existe tipo de vendedor com descrição: {}", descricao);
        
        try {
            boolean existe = vendedorTipoService.existePorDescricao(descricao);
            return ResponseEntity.ok(existe);
        } catch (Exception e) {
            logger.error("Erro ao verificar existência de tipo de vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check para VendedorTipoController");
        return ResponseEntity.ok("VendedorTipoController está funcionando");
    }
}