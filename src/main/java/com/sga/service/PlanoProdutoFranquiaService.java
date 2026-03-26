package com.sga.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.PlanoProdutoFranquiaDTO;
import com.sga.model.PlanoProdutoFranquia;
import com.sga.model.Planos;
import com.sga.model.Produto;
import com.sga.repository.PlanoProdutoFranquiaRepository;
import com.sga.repository.PlanosRepository;
import com.sga.repository.ProdutoRepository;

@Service
public class PlanoProdutoFranquiaService {

    private final PlanoProdutoFranquiaRepository repository;
    private final PlanosRepository planosRepository;
    private final ProdutoRepository produtoRepository;

    public PlanoProdutoFranquiaService(
            PlanoProdutoFranquiaRepository repository,
            PlanosRepository planosRepository,
            ProdutoRepository produtoRepository) {
        this.repository = repository;
        this.planosRepository = planosRepository;
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public PlanoProdutoFranquiaDTO associar(Long planoId, Long produtoId, Long franquiaId,
                                            Integer limiteFranquia, BigDecimal valorExcedente,
                                            String periodoFranquia) {

        // Verificar se já existe associação
        if (repository.existsByPlanoIdAndProdutoIdAndAtivoTrue(planoId, produtoId)) {
            throw new RuntimeException("Este produto já possui uma franquia associada neste plano");
        }

        Planos plano = planosRepository.findById(planoId)
            .orElseThrow(() -> new RuntimeException("Plano não encontrado"));

        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        Produto franquia = produtoRepository.findById(franquiaId)
            .orElseThrow(() -> new RuntimeException("Franquia não encontrada"));

        // Verificar se a franquia é do tipo FRANQUIA
        if (!"FRANQUIA".equals(franquia.getTipoProduto())) {
            throw new RuntimeException("O produto selecionado não é uma franquia válida");
        }

        PlanoProdutoFranquia ppf = new PlanoProdutoFranquia();
        ppf.setPlano(plano);
        ppf.setProduto(produto);
        ppf.setFranquia(franquia);
        ppf.setLimiteFranquia(limiteFranquia);
        ppf.setValorExcedente(valorExcedente);
        ppf.setPermiteExcedente(valorExcedente != null && valorExcedente.compareTo(BigDecimal.ZERO) > 0);
        ppf.setPeriodoFranquia(periodoFranquia != null ? periodoFranquia : "MENSAL");
        ppf.setAtivo(true);

        PlanoProdutoFranquia saved = repository.save(ppf);
        return converterParaDTO(saved);
    }

    @Transactional
    public PlanoProdutoFranquiaDTO atualizar(Long id, Integer limiteFranquia, 
                                            BigDecimal valorExcedente, Boolean ativo) {
        PlanoProdutoFranquia ppf = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Associação não encontrada"));

        if (limiteFranquia != null) {
            ppf.setLimiteFranquia(limiteFranquia);
        }

        if (valorExcedente != null) {
            ppf.setValorExcedente(valorExcedente);
            ppf.setPermiteExcedente(valorExcedente.compareTo(BigDecimal.ZERO) > 0);
        }

        if (ativo != null) {
            ppf.setAtivo(ativo);
        }

        PlanoProdutoFranquia updated = repository.save(ppf);
        return converterParaDTO(updated);
    }

    public List<PlanoProdutoFranquiaDTO> listarPorPlano(Long planoId) {
        return repository.findByPlanoIdAndAtivoTrue(planoId)
            .stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }

    public PlanoProdutoFranquiaDTO buscarPorPlanoEProduto(Long planoId, Long produtoId) {
        return repository.findByPlanoIdAndProdutoIdAndAtivoTrue(planoId, produtoId)
            .map(this::converterParaDTO)
            .orElse(null);
    }

    // 🔥 CORRIGIDO: Adicionado mais logs e verificação
    @Transactional(readOnly = true)
    public List<PlanoProdutoFranquiaDTO> listarPorProduto(Long produtoId) {
        System.out.println("\n========== [Service] listarPorProduto ==========");
        System.out.println("📥 Recebido produtoId: " + produtoId);
        
        try {
            // Buscar as associações (o JPA vai carregar tudo dentro da transação)
            List<PlanoProdutoFranquia> associacoes = repository.findByProdutoId(produtoId);
            System.out.println("📊 Associações encontradas: " + associacoes.size());
            
            if (associacoes.isEmpty()) {
                System.out.println("ℹ️ Nenhuma associação encontrada");
                return new ArrayList<>();
            }
            
            List<PlanoProdutoFranquiaDTO> dtos = new ArrayList<>();
            
            for (PlanoProdutoFranquia assoc : associacoes) {
                try {
                    // Forçar o carregamento dos dados dentro da transação
                    Planos plano = assoc.getPlano();
                    Produto produto = assoc.getProduto();
                    Produto franquia = assoc.getFranquia();
                    
                    // Log para debug
                    System.out.println("   → Associação " + assoc.getId() + 
                        ": Plano=" + plano.getPlano() + 
                        " (ID=" + plano.getId() + ")" +
                        ", Produto=" + produto.getNome() + 
                        " (ID=" + produto.getId() + ")" +
                        ", Franquia=" + franquia.getNome() + 
                        " (ID=" + franquia.getId() + ")");
                    
                    PlanoProdutoFranquiaDTO dto = new PlanoProdutoFranquiaDTO();
                    dto.setId(assoc.getId());
                    dto.setPlanoId(plano.getId());
                    dto.setPlanoNome(plano.getPlano());
                    dto.setProdutoId(produto.getId());
                    dto.setProdutoNome(produto.getNome());
                    dto.setProdutoCodigo(produto.getCodigo());
                    dto.setFranquiaId(franquia.getId());
                    dto.setFranquiaNome(franquia.getNome());
                    dto.setFranquiaCodigo(franquia.getCodigo());
                    dto.setLimiteFranquia(assoc.getLimiteFranquia());
                    dto.setPeriodoFranquia(assoc.getPeriodoFranquia());
                    dto.setValorExcedente(assoc.getValorExcedente());
                    dto.setPermiteExcedente(assoc.getPermiteExcedente());
                    dto.setAtivo(assoc.getAtivo());
                    
                    dtos.add(dto);
                    System.out.println("   ✅ Associação " + assoc.getId() + " convertida com sucesso");
                    
                } catch (Exception e) {
                    System.err.println("   ❌ Erro na associação " + assoc.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("✅ Total de DTOs gerados: " + dtos.size());
            return dtos;
            
        } catch (Exception e) {
            System.err.println("❌ Erro geral: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar associações do produto", e);
        }
    }
    
    public List<PlanoProdutoFranquiaDTO> listarPorFranquia(Long franquiaId) {
        return repository.findByFranquiaId(franquiaId)
            .stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }

    public List<PlanoProdutoFranquiaDTO> listarFranquiasPorAssociado(Long associadoId) {
        return repository.findFranquiasPorAssociado(associadoId)
            .stream()
            .map(this::converterParaDTO)
            .collect(Collectors.toList());
    }

    private PlanoProdutoFranquiaDTO converterParaDTO(PlanoProdutoFranquia ppf) {
        PlanoProdutoFranquiaDTO dto = new PlanoProdutoFranquiaDTO();
        
        // Dados básicos
        dto.setId(ppf.getId());
        dto.setLimiteFranquia(ppf.getLimiteFranquia());
        dto.setPeriodoFranquia(ppf.getPeriodoFranquia());
        dto.setValorExcedente(ppf.getValorExcedente());
        dto.setPermiteExcedente(ppf.getPermiteExcedente());
        dto.setAtivo(ppf.getAtivo());
        
        // Dados do plano (com verificação de null)
        if (ppf.getPlano() != null) {
            dto.setPlanoId(ppf.getPlano().getId());
            dto.setPlanoNome(ppf.getPlano().getPlano());
        } else {
            System.out.println("⚠️ [converterParaDTO] Plano é null para associação ID: " + ppf.getId());
        }
        
        // Dados do produto (com verificação de null)
        if (ppf.getProduto() != null) {
            dto.setProdutoId(ppf.getProduto().getId());
            dto.setProdutoNome(ppf.getProduto().getNome());
            dto.setProdutoCodigo(ppf.getProduto().getCodigo());
        } else {
            System.out.println("⚠️ [converterParaDTO] Produto é null para associação ID: " + ppf.getId());
        }
        
        // Dados da franquia (com verificação de null)
        if (ppf.getFranquia() != null) {
            dto.setFranquiaId(ppf.getFranquia().getId());
            dto.setFranquiaNome(ppf.getFranquia().getNome());
            dto.setFranquiaCodigo(ppf.getFranquia().getCodigo());
        } else {
            System.out.println("⚠️ [converterParaDTO] Franquia é null para associação ID: " + ppf.getId());
        }
        
        return dto;
    }
}