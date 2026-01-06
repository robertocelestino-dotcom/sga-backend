// src/main/java/com/sga/controller/VendedorController.java
package com.sga.controller;

import com.sga.dto.VendedorDTO;
import com.sga.dto.VendedorResumoDTO;
import com.sga.service.VendedorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vendedores")
@CrossOrigin(origins = "*")
public class VendedorController {
    
    private static final Logger logger = LoggerFactory.getLogger(VendedorController.class);
    
    @Autowired
    private VendedorService vendedorService;
    
    // GET - Listar com filtros (paginado)
    @GetMapping
    public ResponseEntity<Page<VendedorResumoDTO>> listarComFiltros(
            Pageable pageable,
            @RequestParam(required = false) String nomeRazao,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long vendedorTipoId) {
        
        logger.info("Recebida requisição para listar vendedores com filtros");
        
        try {
            Page<VendedorResumoDTO> vendedores = vendedorService.listarComFiltros(
                pageable, nomeRazao, status, vendedorTipoId);
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao listar vendedores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar vendedores do tipo 1 (paginado)
    @GetMapping("/tipo-1/paginado")
    public ResponseEntity<Page<VendedorResumoDTO>> buscarVendedoresTipo1Paginado(Pageable pageable) {
        logger.info("Recebida requisição para buscar vendedores do tipo 1 (paginado)");
        
        try {
            Page<VendedorResumoDTO> vendedores = vendedorService.buscarVendedoresTipo1(pageable);
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores do tipo 1: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar vendedores do tipo 2 (paginado)
    @GetMapping("/tipo-2/paginado")
    public ResponseEntity<Page<VendedorResumoDTO>> buscarVendedoresTipo2Paginado(Pageable pageable) {
        logger.info("Recebida requisição para buscar vendedores do tipo 2 (paginado)");
        
        try {
            Page<VendedorResumoDTO> vendedores = vendedorService.buscarVendedoresTipo2(pageable);
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores do tipo 2: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar vendedores do tipo 1 (lista completa)
    @GetMapping("/tipo-1")
    public ResponseEntity<List<VendedorResumoDTO>> buscarTodosVendedoresTipo1() {
        logger.info("Recebida requisição para buscar todos vendedores do tipo 1");
        
        try {
            List<VendedorResumoDTO> vendedores = vendedorService.buscarTodosVendedoresTipo1();
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores do tipo 1: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar vendedores do tipo 2 (lista completa)
    @GetMapping("/tipo-2")
    public ResponseEntity<List<VendedorResumoDTO>> buscarTodosVendedoresTipo2() {
        logger.info("Recebida requisição para buscar todos vendedores do tipo 2");
        
        try {
            List<VendedorResumoDTO> vendedores = vendedorService.buscarTodosVendedoresTipo2();
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores do tipo 2: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar vendedores do tipo 1 ativos (lista completa)
    @GetMapping("/tipo-1/ativos")
    public ResponseEntity<List<VendedorResumoDTO>> buscarVendedoresTipo1Ativos() {
        logger.info("Recebida requisição para buscar vendedores do tipo 1 ativos");
        
        try {
            List<VendedorResumoDTO> vendedores = vendedorService.buscarVendedoresTipo1Ativos();
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores do tipo 1 ativos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar vendedores do tipo 2 ativos (lista completa)
    @GetMapping("/tipo-2/ativos")
    public ResponseEntity<List<VendedorResumoDTO>> buscarVendedoresTipo2Ativos() {
        logger.info("Recebida requisição para buscar vendedores do tipo 2 ativos");
        
        try {
            List<VendedorResumoDTO> vendedores = vendedorService.buscarVendedoresTipo2Ativos();
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores do tipo 2 ativos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<VendedorDTO> buscarPorId(@PathVariable Long id) {
        logger.info("Recebida requisição para buscar vendedor por ID: {}", id);
        
        try {
            VendedorDTO vendedor = vendedorService.buscarPorId(id);
            return ResponseEntity.ok(vendedor);
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Vendedor não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // POST - Criar novo
    @PostMapping
    public ResponseEntity<VendedorDTO> criar(@Valid @RequestBody VendedorDTO vendedorDTO) {
        logger.info("Recebida requisição para criar vendedor: {}", vendedorDTO.getNomeRazao());
        
        try {
            VendedorDTO criado = vendedorService.criar(vendedorDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao criar vendedor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro ao criar vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // PUT - Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<VendedorDTO> atualizar(@PathVariable Long id, 
                                                 @Valid @RequestBody VendedorDTO vendedorDTO) {
        logger.info("Recebida requisição para atualizar vendedor ID: {}", id);
        
        try {
            VendedorDTO atualizado = vendedorService.atualizar(id, vendedorDTO);
            return ResponseEntity.ok(atualizado);
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Vendedor não encontrado para atualização: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao atualizar vendedor: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro ao atualizar vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DELETE - Excluir
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        logger.info("Recebida requisição para excluir vendedor ID: {}", id);
        
        try {
            vendedorService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Vendedor não encontrado para exclusão: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao excluir vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Contar por tipo
    @GetMapping("/count/tipo/{tipoId}")
    public ResponseEntity<Long> contarPorTipo(@PathVariable Long tipoId) {
        logger.info("Recebida requisição para contar vendedores do tipo: {}", tipoId);
        
        try {
            Long total = vendedorService.contarPorTipo(tipoId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            logger.error("Erro ao contar vendedores por tipo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Contar ativos por tipo
    @GetMapping("/count/tipo/{tipoId}/ativos")
    public ResponseEntity<Long> contarAtivosPorTipo(@PathVariable Long tipoId) {
        logger.info("Recebida requisição para contar vendedores ativos do tipo: {}", tipoId);
        
        try {
            Long total = vendedorService.contarAtivosPorTipo(tipoId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            logger.error("Erro ao contar vendedores ativos por tipo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar ativos para dropdown
    @GetMapping("/dropdown/ativos")
    public ResponseEntity<List<VendedorResumoDTO>> buscarAtivosParaDropdown() {
        logger.info("Recebida requisição para buscar vendedores ativos para dropdown");
        
        try {
            List<VendedorResumoDTO> vendedores = vendedorService.buscarAtivosParaDropdown();
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores ativos para dropdown: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // GET - Buscar ativos por tipo para dropdown
    @GetMapping("/dropdown/tipo/{tipoId}/ativos")
    public ResponseEntity<List<VendedorResumoDTO>> buscarAtivosPorTipoParaDropdown(@PathVariable Long tipoId) {
        logger.info("Recebida requisição para buscar vendedores ativos do tipo {} para dropdown", tipoId);
        
        try {
            List<VendedorResumoDTO> vendedores = vendedorService.buscarAtivosPorTipoParaDropdown(tipoId);
            return ResponseEntity.ok(vendedores);
        } catch (Exception e) {
            logger.error("Erro ao buscar vendedores ativos por tipo para dropdown: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // PUT - Alterar status (Ativar/Inativar)
    @PutMapping("/{id}/status")
    public ResponseEntity<VendedorDTO> alterarStatus(@PathVariable Long id, 
                                                    @RequestParam String status) {
        logger.info("Recebida requisição para alterar status do vendedor ID: {} para {}", id, status);
        
        try {
            VendedorDTO vendedor = vendedorService.alterarStatus(id, status);
            return ResponseEntity.ok(vendedor);
        } catch (javax.persistence.EntityNotFoundException e) {
            logger.error("Vendedor não encontrado: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao alterar status: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Erro ao alterar status do vendedor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        logger.info("Health check para VendedorController");
        return ResponseEntity.ok("VendedorController está funcionando");
    }
}