package com.sga.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.sga.dto.ProdutoDTO;
import com.sga.dto.ProdutoResumoDTO;
import com.sga.service.ProdutoService;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProdutoController.class);
    
    private final ProdutoService produtoService;
    
    // CONSTRUTOR para injeção de dependência
    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
        logger.info("ProdutoController inicializado. ProdutoService: {}", 
                    produtoService != null ? "INJETADO" : "NULL");
    }
    
    // ========== CRUD BÁSICO ==========
    
    @PostMapping
    public ResponseEntity<?> criarProduto(@RequestBody ProdutoDTO dto, 
                                         @RequestHeader(value = "X-Usuario", required = false) String usuario) {
        try {
            logger.info("Criando produto: {}", dto.getNome());
            String usuarioLogado = usuario != null ? usuario : "SISTEMA";
            ProdutoDTO produtoCriado = produtoService.criarProduto(dto, usuarioLogado);
            return ResponseEntity.status(HttpStatus.CREATED).body(produtoCriado);
        } catch (RuntimeException e) {
            logger.error("Erro ao criar produto: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("mensagem", "Não foi possível criar o produto");
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarProduto(
            @PathVariable Long id, 
            @RequestBody ProdutoDTO dto,
            @RequestHeader(value = "X-Usuario", required = false) String usuario) {
        try {
            logger.info("Atualizando produto ID: {}", id);
            String usuarioLogado = usuario != null ? usuario : "SISTEMA";
            ProdutoDTO produtoAtualizado = produtoService.atualizarProduto(id, dto, usuarioLogado);
            return ResponseEntity.ok(produtoAtualizado);
        } catch (RuntimeException e) {
            logger.error("Erro ao atualizar produto {}: {}", id, e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("mensagem", "Não foi possível atualizar o produto");
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarProdutoPorId(@PathVariable Long id) {
        try {
            logger.info("Buscando produto ID: {}", id);
            ProdutoDTO produto = produtoService.buscarProdutoPorId(id);
            return ResponseEntity.ok(produto);
        } catch (RuntimeException e) {
            logger.error("Produto não encontrado: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("mensagem", "Produto não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> listarProdutosPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipoProduto,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String modalidade,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean temFranquia) {
        
        try {
            logger.info("Listando produtos - page: {}, size: {}, sort: {}", page, size, sort);
            
            Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
                    ? Sort.Direction.DESC : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
            
            Page<ProdutoResumoDTO> produtos = produtoService.listarProdutosPaginados(
                    codigo, nome, tipoProduto, categoria, modalidade, status, temFranquia, pageable);
            
            logger.info("Encontrados {} produtos", produtos.getTotalElements());
            return ResponseEntity.ok(produtos);
            
        } catch (Exception e) {
            logger.error("Erro ao listar produtos: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro interno ao processar requisição");
            erro.put("mensagem", e.getMessage() != null ? e.getMessage() : "Erro desconhecido");
            erro.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirProduto(@PathVariable Long id) {
        try {
            logger.info("Excluindo produto ID: {}", id);
            produtoService.excluirProduto(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Erro ao excluir produto {}: {}", id, e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("mensagem", "Não foi possível excluir o produto");
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    // ========== ENDPOINTS ESPECÍFICOS ==========
    
    @GetMapping("/franquias/disponiveis")
    public ResponseEntity<?> listarFranquiasDisponiveis() {
        try {
            logger.info("Listando franquias disponíveis");
            List<ProdutoResumoDTO> franquias = produtoService.listarFranquiasDisponiveis();
            logger.info("Retornando {} franquias", franquias.size());
            return ResponseEntity.ok(franquias);
        } catch (Exception e) {
            logger.error("Erro ao listar franquias disponíveis: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar franquias");
            erro.put("mensagem", e.getMessage() != null ? e.getMessage() : "Erro desconhecido");
            erro.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @GetMapping("/franquias")
    public ResponseEntity<?> listarProdutosComFranquia() {
        try {
            logger.info("Listando produtos com franquia");
            List<ProdutoResumoDTO> produtos = produtoService.listarProdutosComFranquia();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao listar produtos com franquia: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar produtos com franquia");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @GetMapping("/{id}/franquias")
    public ResponseEntity<?> getFranquiasDoProduto(@PathVariable Long id) {
        try {
            logger.info("Buscando franquias do produto ID: {}", id);
            List<ProdutoResumoDTO> franquias = produtoService.getFranquiasDoProduto(id);
            return ResponseEntity.ok(franquias);
        } catch (Exception e) {
            logger.error("Erro ao buscar franquias do produto {}: {}", id, e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar franquias do produto");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @PostMapping("/{produtoId}/franquias/{franquiaId}")
    public ResponseEntity<?> adicionarFranquia(
            @PathVariable Long produtoId, 
            @PathVariable Long franquiaId) {
        try {
            logger.info("Adicionando franquia {} ao produto {}", franquiaId, produtoId);
            produtoService.adicionarFranquiaAoProduto(produtoId, franquiaId);
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("mensagem", "Franquia adicionada com sucesso");
            resposta.put("produtoId", produtoId);
            resposta.put("franquiaId", franquiaId);
            return ResponseEntity.ok(resposta);
        } catch (RuntimeException e) {
            logger.error("Erro ao adicionar franquia: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("mensagem", "Não foi possível adicionar a franquia");
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    @DeleteMapping("/{produtoId}/franquias/{franquiaId}")
    public ResponseEntity<?> removerFranquia(
            @PathVariable Long produtoId, 
            @PathVariable Long franquiaId) {
        try {
            logger.info("Removendo franquia {} do produto {}", franquiaId, produtoId);
            produtoService.removerFranquiaDoProduto(produtoId, franquiaId);
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("mensagem", "Franquia removida com sucesso");
            resposta.put("produtoId", produtoId);
            resposta.put("franquiaId", franquiaId);
            return ResponseEntity.ok(resposta);
        } catch (RuntimeException e) {
            logger.error("Erro ao remover franquia: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", e.getMessage());
            erro.put("mensagem", "Não foi possível remover a franquia");
            return ResponseEntity.badRequest().body(erro);
        }
    }
    
    // ========== PRODUTOS SPC/MIX ==========
    
    @GetMapping("/spc")
    public ResponseEntity<?> listarProdutosSPC() {
        try {
            logger.info("Listando produtos SPC");
            List<ProdutoResumoDTO> produtos = produtoService.listarProdutosSPC();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao listar produtos SPC: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar produtos SPC");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @GetMapping("/mix")
    public ResponseEntity<?> listarProdutosMix() {
        try {
            logger.info("Listando produtos MIX");
            List<ProdutoResumoDTO> produtos = produtoService.listarProdutosMix();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao listar produtos MIX: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar produtos MIX");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @GetMapping("/{id}/relacionados")
    public ResponseEntity<?> getProdutosRelacionados(@PathVariable Long id) {
        try {
            logger.info("Buscando produtos relacionados ao produto ID: {}", id);
            List<ProdutoResumoDTO> relacionados = produtoService.getProdutosRelacionados(id);
            return ResponseEntity.ok(relacionados);
        } catch (Exception e) {
            logger.error("Erro ao buscar produtos relacionados {}: {}", id, e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar produtos relacionados");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    // ========== ESTATÍSTICAS E RELATÓRIOS ==========
    
    @GetMapping("/estatisticas")
    public ResponseEntity<?> getEstatisticas() {
        try {
            logger.info("Gerando estatísticas de produtos");
            Map<String, Object> estatisticas = produtoService.getEstatisticasProdutos();
            return ResponseEntity.ok(estatisticas);
        } catch (Exception e) {
            logger.error("Erro ao gerar estatísticas: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao gerar estatísticas");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @GetMapping("/faturamento/disponiveis")
    public ResponseEntity<?> getProdutosParaFaturamento() {
        try {
            logger.info("Buscando produtos para faturamento");
            List<ProdutoResumoDTO> produtos = produtoService.getProdutosParaFaturamento();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao buscar produtos para faturamento: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar produtos para faturamento");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    @GetMapping("/ativos")
    public ResponseEntity<?> listarProdutosAtivos() {
        try {
            logger.info("Listando produtos ativos");
            List<ProdutoResumoDTO> produtos = produtoService.listarProdutosPaginados(
                    null, null, null, null, null, "ATIVO", null, 
                    PageRequest.of(0, 1000, Sort.by("nome"))).getContent();
            return ResponseEntity.ok(produtos);
        } catch (Exception e) {
            logger.error("Erro ao listar produtos ativos: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("erro", "Erro ao buscar produtos ativos");
            erro.put("mensagem", e.getMessage());
            return ResponseEntity.internalServerError().body(erro);
        }
    }
    
    // ========== HEALTH CHECK ==========
    
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            logger.info("Health check dos produtos");
            long total = produtoService.listarProdutosPaginados(
                    null, null, null, null, null, null, null, 
                    PageRequest.of(0, 1)).getTotalElements();
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("totalProdutos", total);
            health.put("timestamp", System.currentTimeMillis());
            health.put("service", produtoService != null ? "OK" : "NULO");
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Erro no health check: {}", e.getMessage(), e);
            Map<String, Object> erro = new HashMap<>();
            erro.put("status", "DOWN");
            erro.put("erro", e.getMessage());
            erro.put("service", produtoService != null ? "PRESENTE" : "AUSENTE");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(erro);
        }
    }
    
    // ========== ENDPOINT DE DEBUG ==========
    @GetMapping("/debug")
    public ResponseEntity<?> debugInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("controller", "ProdutoController");
        info.put("serviceInjetado", produtoService != null ? "SIM" : "NÃO");
        info.put("serviceClass", produtoService != null ? produtoService.getClass().getName() : "null");
        info.put("timestamp", System.currentTimeMillis());
        info.put("status", produtoService != null ? "SERVICE_OK" : "SERVICE_NULL");
        return ResponseEntity.ok(info);
    }
}