package com.sga.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.ProdutoDTO;
import com.sga.dto.ProdutoResumoDTO;
import com.sga.model.Produto;
import com.sga.repository.ProdutoRepository;

@Service
@Transactional
public class ProdutoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProdutoService.class);
    
    private final ProdutoRepository produtoRepository;
    
    // CONSTRUTOR para injeção
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
        logger.info("ProdutoService inicializado. Repository: {}", 
                    produtoRepository != null ? "INJETADO" : "NULL");
    }
    
    // ========== CRUD BÁSICO ==========
    
    @Transactional
    public ProdutoDTO criarProduto(ProdutoDTO dto, String usuario) {
        logger.info("Criando produto: {} - {}", dto.getCodigo(), dto.getNome());
        
        if (produtoRepository == null) {
            throw new RuntimeException("Repositório não inicializado");
        }
        
        // Validar código único
        validarCodigoUnico(dto.getCodigo(), null);
        
        // Validar código RM único (se informado)
        if (dto.getCodigoRm() != null && !dto.getCodigoRm().isEmpty()) {
            validarCodigoRmUnico(dto.getCodigoRm(), null);
        }
        
        // Criar produto
        Produto produto = new Produto();
        mapearDtoParaEntidade(dto, produto);
        produto.setUsuarioCriacao(usuario);
        produto.setUsuarioAtualizacao(usuario);
        produto.setCriadoEm(LocalDateTime.now());
        produto.setAtualizadoEm(LocalDateTime.now());
        
        Produto produtoSalvo = produtoRepository.save(produto);
        logger.info("Produto criado com ID: {}", produtoSalvo.getId());
        
        return mapearEntidadeParaDto(produtoSalvo);
    }
    
    @Transactional
    public ProdutoDTO atualizarProduto(Long id, ProdutoDTO dto, String usuario) {
        logger.info("Atualizando produto ID: {}", id);
        
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
        
        // Validar código único
        validarCodigoUnico(dto.getCodigo(), id);
        
        // Validar código RM único (se informado)
        if (dto.getCodigoRm() != null && !dto.getCodigoRm().isEmpty()) {
            validarCodigoRmUnico(dto.getCodigoRm(), id);
        }
        
        // Atualizar dados básicos
        mapearDtoParaEntidade(dto, produto);
        produto.setUsuarioAtualizacao(usuario);
        produto.setAtualizadoEm(LocalDateTime.now());
        
        Produto produtoAtualizado = produtoRepository.save(produto);
        logger.info("Produto atualizado: {}", produtoAtualizado.getId());
        
        return mapearEntidadeParaDto(produtoAtualizado);
    }
    
    @Transactional(readOnly = true)
    public ProdutoDTO buscarProdutoPorId(Long id) {
        logger.info("Buscando produto por ID: {}", id);
        
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Produto não encontrado: {}", id);
                    return new RuntimeException("Produto não encontrado: " + id);
                });
        return mapearEntidadeParaDto(produto);
    }
    
    @Transactional(readOnly = true)
    public Page<ProdutoResumoDTO> listarProdutosPaginados(
            String codigo, String nome, String tipoProduto, 
            String categoria, String modalidade, String status, 
            Boolean temFranquia, Pageable pageable) {
        
        logger.info("Listando produtos com filtros: page={}, size={}, sort={}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        
        try {
            if (produtoRepository == null) {
                logger.error("ERRO CRÍTICO: produtoRepository é NULL!");
                throw new RuntimeException("Repositório não inicializado");
            }
            
            Page<Produto> page = produtoRepository.filtrarProdutos(
                    codigo, nome, tipoProduto, categoria, modalidade, status, temFranquia, pageable);
            
            logger.info("Encontrados {} produtos", page.getTotalElements());
            
            return page.map(this::mapearParaResumoDTO);
        } catch (Exception e) {
            logger.error("ERRO ao listar produtos paginados: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar produtos: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public void excluirProduto(Long id) {
        logger.info("Excluindo produto ID: {}", id);
        
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
        
        produtoRepository.delete(produto);
        logger.info("Produto excluído: {}", id);
    }
    
    // ========== MÉTODOS DE FRANQUIAS ==========
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> listarFranquiasDisponiveis() {
        logger.info("Listando franquias disponíveis");
        try {
            if (produtoRepository == null) {
                logger.error("ERRO: produtoRepository é NULL em listarFranquiasDisponiveis");
                throw new RuntimeException("Repositório não inicializado");
            }
            
            List<Produto> franquias = produtoRepository.findFranquiasDisponiveis();
            logger.info("Encontradas {} franquias", franquias.size());
            
            return franquias.stream()
                    .filter(produto -> produto != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("ERRO ao listar franquias disponíveis: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar franquias disponíveis: " + e.getMessage(), e);
        }
    }
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> listarProdutosComFranquia() {
        logger.info("Listando produtos com franquia");
        try {
            List<Produto> produtos = produtoRepository.findProdutosComFranquia();
            return produtos.stream()
                    .filter(p -> p != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar produtos com franquia: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar produtos com franquia: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> getFranquiasDoProduto(Long produtoId) {
        logger.info("Buscando franquias do produto ID: {}", produtoId);
        try {
            List<Produto> franquias = produtoRepository.findFranquiasByProdutoId(produtoId);
            return franquias.stream()
                    .filter(f -> f != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao buscar franquias do produto {}: {}", produtoId, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar franquias do produto: " + e.getMessage());
        }
    }
    
    // **MÉTODO FALTANTE - ADICIONE ESTE**
    @Transactional
    public void adicionarFranquiaAoProduto(Long produtoId, Long franquiaId) {
        logger.info("Adicionando franquia {} ao produto {}", franquiaId, produtoId);
        
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));
        
        Produto franquia = produtoRepository.findById(franquiaId)
                .orElseThrow(() -> new RuntimeException("Franquia não encontrada: " + franquiaId));
        
        // Verificar se não é a mesma entidade
        if (produtoId.equals(franquiaId)) {
            throw new RuntimeException("Produto não pode ser franquia de si mesmo");
        }
        
        // Verificar se já é franquia
        if (produto.getFranquias() != null && produto.getFranquias().contains(franquia)) {
            throw new RuntimeException("Franquia já adicionada ao produto");
        }
        
        // Adicionar franquia
        if (produto.getFranquias() == null) {
            // Inicializar a coleção se for null
            // Supondo que Produto tem um método para inicializar franquias
            // Se não tiver, você pode precisar ajustar a entidade Produto
        }
        
        // Adicionar lógica para adicionar franquia
        // Depende de como sua entidade Produto está estruturada
        // Exemplo genérico:
        produto.getFranquias().add(franquia);
        produtoRepository.save(produto);
        
        logger.info("Franquia adicionada com sucesso");
    }
    
    // **MÉTODO FALTANTE - ADICIONE ESTE**
    @Transactional
    public void removerFranquiaDoProduto(Long produtoId, Long franquiaId) {
        logger.info("Removendo franquia {} do produto {}", franquiaId, produtoId);
        
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + produtoId));
        
        Produto franquia = produtoRepository.findById(franquiaId)
                .orElseThrow(() -> new RuntimeException("Franquia não encontrada: " + franquiaId));
        
        if (produto.getFranquias() == null || !produto.getFranquias().contains(franquia)) {
            throw new RuntimeException("Franquia não encontrada no produto");
        }
        
        produto.getFranquias().remove(franquia);
        produtoRepository.save(produto);
        
        logger.info("Franquia removida com sucesso");
    }
    
    // ========== OUTROS MÉTODOS DO CONTROLLER ==========
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> listarProdutosSPC() {
        logger.info("Listando produtos SPC");
        try {
            List<Produto> produtos = produtoRepository.findProdutosSPC();
            return produtos.stream()
                    .filter(p -> p != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar produtos SPC: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar produtos SPC: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> listarProdutosMix() {
        logger.info("Listando produtos MIX");
        try {
            List<Produto> produtos = produtoRepository.findProdutosMix();
            return produtos.stream()
                    .filter(p -> p != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar produtos MIX: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao listar produtos MIX: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> getProdutosRelacionados(Long produtoId) {
        logger.info("Buscando produtos relacionados ao produto ID: {}", produtoId);
        try {
            List<Produto> relacionados = produtoRepository.findProdutosRelacionados(produtoId);
            return relacionados.stream()
                    .filter(p -> p != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao buscar produtos relacionados {}: {}", produtoId, e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar produtos relacionados: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public List<ProdutoResumoDTO> getProdutosParaFaturamento() {
        logger.info("Buscando produtos para faturamento");
        try {
            List<Produto> produtos = produtoRepository.findProdutosParaFaturamento();
            return produtos.stream()
                    .filter(p -> p != null)
                    .map(this::mapearParaResumoDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao buscar produtos para faturamento: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar produtos para faturamento: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getEstatisticasProdutos() {
        logger.info("Gerando estatísticas de produtos");
        
        try {
            Map<String, Object> estatisticas = new HashMap<>();
            
            // Contagem total
            long total = produtoRepository.count();
            estatisticas.put("total", total);
            
            if (total == 0) {
                estatisticas.put("ativos", 0);
                estatisticas.put("inativos", 0);
                estatisticas.put("comFranquia", 0);
                estatisticas.put("valorTotalAtivos", BigDecimal.ZERO);
                return estatisticas;
            }
            
            // Contagem por status
            List<Produto> todos = produtoRepository.findAll();
            long ativos = todos.stream().filter(p -> "ATIVO".equals(p.getStatus())).count();
            long inativos = total - ativos;
            
            // Produtos com franquia
            long comFranquia = todos.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getTemFranquia()))
                    .count();
            
            estatisticas.put("ativos", ativos);
            estatisticas.put("inativos", inativos);
            estatisticas.put("comFranquia", comFranquia);
            estatisticas.put("valorTotalAtivos", calcularValorTotalProdutosAtivos());
            
            return estatisticas;
        } catch (Exception e) {
            logger.error("Erro ao gerar estatísticas: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao gerar estatísticas: " + e.getMessage());
        }
    }
    
    @Transactional(readOnly = true)
    public BigDecimal calcularValorTotalProdutosAtivos() {
        try {
            BigDecimal total = produtoRepository.calcularValorTotalAtivos();
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            logger.error("Erro ao calcular valor total: {}", e.getMessage(), e);
            return BigDecimal.ZERO;
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private void validarCodigoUnico(String codigo, Long idExcluir) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new RuntimeException("Código do produto é obrigatório");
        }
        
        Optional<Produto> existente = produtoRepository.findByCodigo(codigo);
        if (existente.isPresent() && !existente.get().getId().equals(idExcluir)) {
            throw new RuntimeException("Já existe um produto com o código: " + codigo);
        }
    }
    
    private void validarCodigoRmUnico(String codigoRm, Long idExcluir) {
        if (codigoRm == null || codigoRm.trim().isEmpty()) {
            return;
        }
        
        Optional<Produto> existente = produtoRepository.findByCodigoRm(codigoRm);
        if (existente.isPresent() && !existente.get().getId().equals(idExcluir)) {
            throw new RuntimeException("Já existe um produto com o código RM: " + codigoRm);
        }
    }
    
    private void mapearDtoParaEntidade(ProdutoDTO dto, Produto produto) {
        produto.setCodigo(dto.getCodigo());
        produto.setCodigoRm(dto.getCodigoRm());
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setValorUnitario(dto.getValorUnitario() != null ? dto.getValorUnitario() : BigDecimal.ZERO);
        produto.setStatus(dto.getStatus() != null ? dto.getStatus() : "ATIVO");
        produto.setTipoProduto(dto.getTipoProduto() != null ? dto.getTipoProduto() : "SERVICO");
        produto.setUnidadeMedida(dto.getUnidadeMedida());
        produto.setCategoria(dto.getCategoria());
        produto.setModalidade(dto.getModalidade());
        produto.setLimiteFranquia(dto.getLimiteFranquia());
        produto.setPeriodoFranquia(dto.getPeriodoFranquia());
        produto.setGeraCobrancaAutomatica(dto.getGeraCobrancaAutomatica() != null ? dto.getGeraCobrancaAutomatica() : true);
        produto.setCobrancaPeriodica(dto.getCobrancaPeriodica() != null ? dto.getCobrancaPeriodica() : false);
        produto.setPeriodicidadeCobranca(dto.getPeriodicidadeCobranca());
        produto.setDiaCobranca(dto.getDiaCobranca());
        produto.setPermiteDesconto(dto.getPermiteDesconto() != null ? dto.getPermiteDesconto() : true);
        produto.setDescontoMaximo(dto.getDescontoMaximo() != null ? dto.getDescontoMaximo() : BigDecimal.ZERO);
        produto.setExigeAutorizacao(dto.getExigeAutorizacao() != null ? dto.getExigeAutorizacao() : false);
        produto.setNivelAutorizacao(dto.getNivelAutorizacao());
        produto.setTemFranquia(dto.getTemFranquia() != null ? dto.getTemFranquia() : false);
    }
    
    private ProdutoDTO mapearEntidadeParaDto(Produto produto) {
        ProdutoDTO dto = new ProdutoDTO();
        dto.setId(produto.getId());
        dto.setCodigo(produto.getCodigo());
        dto.setCodigoRm(produto.getCodigoRm());
        dto.setNome(produto.getNome());
        dto.setDescricao(produto.getDescricao());
        dto.setValorUnitario(produto.getValorUnitario());
        dto.setStatus(produto.getStatus());
        dto.setTipoProduto(produto.getTipoProduto());
        dto.setUnidadeMedida(produto.getUnidadeMedida());
        dto.setCategoria(produto.getCategoria());
        dto.setModalidade(produto.getModalidade());
        dto.setTemFranquia(produto.getTemFranquia());
        dto.setLimiteFranquia(produto.getLimiteFranquia());
        dto.setPeriodoFranquia(produto.getPeriodoFranquia());
        dto.setGeraCobrancaAutomatica(produto.getGeraCobrancaAutomatica());
        dto.setCobrancaPeriodica(produto.getCobrancaPeriodica());
        dto.setPeriodicidadeCobranca(produto.getPeriodicidadeCobranca());
        dto.setDiaCobranca(produto.getDiaCobranca());
        dto.setPermiteDesconto(produto.getPermiteDesconto());
        dto.setDescontoMaximo(produto.getDescontoMaximo());
        dto.setExigeAutorizacao(produto.getExigeAutorizacao());
        dto.setNivelAutorizacao(produto.getNivelAutorizacao());
        dto.setCriadoEm(produto.getCriadoEm());
        dto.setAtualizadoEm(produto.getAtualizadoEm());
        dto.setUsuarioCriacao(produto.getUsuarioCriacao());
        dto.setUsuarioAtualizacao(produto.getUsuarioAtualizacao());
        
        return dto;
    }
    
    private ProdutoResumoDTO mapearParaResumoDTO(Produto produto) {
        if (produto == null) {
            return null;
        }
        
        ProdutoResumoDTO dto = new ProdutoResumoDTO();
        dto.setId(produto.getId());
        dto.setCodigo(produto.getCodigo());
        dto.setNome(produto.getNome());
        dto.setValorUnitario(produto.getValorUnitario() != null ? produto.getValorUnitario() : BigDecimal.ZERO);
        dto.setStatus(produto.getStatus() != null ? produto.getStatus() : "ATIVO");
        dto.setTipoProduto(produto.getTipoProduto() != null ? produto.getTipoProduto() : "SERVICO");
        dto.setCategoria(produto.getCategoria());
        dto.setModalidade(produto.getModalidade());
        dto.setTemFranquia(produto.getTemFranquia() != null ? produto.getTemFranquia() : false);
        
        if (produto.getFranquias() != null) {
            dto.setTotalFranquias(produto.getFranquias().size());
        } else {
            dto.setTotalFranquias(0);
        }
        
        dto.setNomeCompleto(produto.getNome());
        
        return dto;
    }
    
    // ========== MÉTODO DE TESTE ==========
    public String testService() {
        return "ProdutoService funcionando! Repository: " + 
               (produtoRepository != null ? "OK" : "NULL");
    }
    
}