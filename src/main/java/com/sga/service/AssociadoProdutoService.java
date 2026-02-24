package com.sga.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.AssociadoProdutoDTO;
import com.sga.dto.AssociadoProdutoResumoDTO;
import com.sga.exception.ResourceNotFoundException;
import com.sga.model.Associado;
import com.sga.model.AssociadoProduto;
import com.sga.model.Produto;
import com.sga.model.TipoEnvio;
import com.sga.repository.AssociadoProdutoRepository;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.ProdutoRepository;
import com.sga.repository.TipoEnvioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssociadoProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(AssociadoProdutoService.class);

    @Autowired
    private AssociadoProdutoRepository associadoProdutoRepository;
    
    @Autowired
    private AssociadoRepository associadoRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private TipoEnvioRepository tipoEnvioRepository;

    @Transactional
    public AssociadoProdutoDTO criar(AssociadoProdutoDTO dto) {
        logger.info("Criando produto para associado ID: {}, produto ID: {}", dto.getAssociadoId(), dto.getProdutoId());

        // Verificar se já existe
        if (associadoProdutoRepository.existsByAssociadoAndProduto(dto.getAssociadoId(), dto.getProdutoId())) {
            throw new IllegalArgumentException("Este produto já está habilitado para este associado");
        }

        AssociadoProduto associadoProduto = convertToEntity(dto);
        associadoProduto.setUsuarioCriacao(dto.getUsuario());

        AssociadoProduto saved = associadoProdutoRepository.save(associadoProduto);
        logger.info("Produto habilitado com sucesso. ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    @Transactional
    public List<AssociadoProdutoDTO> criarEmLote(List<AssociadoProdutoDTO> dtos) {
        logger.info("Criando {} produtos para associado", dtos.size());

        List<AssociadoProduto> entities = dtos.stream()
                .map(this::convertToEntity)
                .peek(ap -> ap.setUsuarioCriacao(dtos.get(0).getUsuario()))
                .collect(Collectors.toList());

        List<AssociadoProduto> saved = associadoProdutoRepository.saveAll(entities);
        logger.info("{} produtos habilitados com sucesso", saved.size());

        return saved.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public AssociadoProdutoDTO atualizar(Long id, AssociadoProdutoDTO dto) {
        logger.info("Atualizando produto do associado ID: {}", id);

        AssociadoProduto associadoProduto = associadoProdutoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + id));

        updateEntity(associadoProduto, dto);
        associadoProduto.setUsuarioAlteracao(dto.getUsuario());

        AssociadoProduto updated = associadoProdutoRepository.save(associadoProduto);

        return convertToDTO(updated);
    }

    @Transactional(readOnly = true)
    public AssociadoProdutoDTO buscarPorId(Long id) {
        logger.info("Buscando produto do associado por ID: {}", id);

        AssociadoProduto associadoProduto = associadoProdutoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + id));

        return convertToDTO(associadoProduto);
    }

    @Transactional(readOnly = true)
    public List<AssociadoProdutoResumoDTO> listarPorAssociado(Long associadoId) {
        logger.info("Listando produtos do associado ID: {}", associadoId);

        return associadoProdutoRepository.findByAssociadoId(associadoId)
                .stream()
                .map(this::convertToResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AssociadoProdutoResumoDTO> listarPorAssociadoPaginado(Long associadoId, Pageable pageable) {
        logger.info("Listando produtos do associado ID: {} - página: {}", associadoId, pageable.getPageNumber());

        return associadoProdutoRepository.findByAssociadoId(associadoId, pageable)
                .map(this::convertToResumoDTO);
    }

    @Transactional(readOnly = true)
    public List<AssociadoProdutoResumoDTO> listarAtivosPorAssociado(Long associadoId) {
        logger.info("Listando produtos ativos do associado ID: {}", associadoId);

        return associadoProdutoRepository.findAtivosPorAssociado(associadoId)
                .stream()
                .map(this::convertToResumoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void excluir(Long id) {
        logger.info("Excluindo produto do associado ID: {}", id);

        AssociadoProduto associadoProduto = associadoProdutoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + id));

        associadoProdutoRepository.delete(associadoProduto);
    }

    @Transactional
    public void excluirPorAssociado(Long associadoId) {
        logger.info("Excluindo todos os produtos do associado ID: {}", associadoId);
        associadoProdutoRepository.deleteByAssociadoId(associadoId);
    }

    @Transactional
    public AssociadoProdutoDTO alterarStatus(Long id, String status, String usuario) {
        logger.info("Alterando status do produto ID: {} para {}", id, status);

        AssociadoProduto associadoProduto = associadoProdutoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + id));

        associadoProduto.setStatusNoProcesso(status);
        associadoProduto.setUsuarioAlteracao(usuario);

        AssociadoProduto updated = associadoProdutoRepository.save(associadoProduto);

        return convertToDTO(updated);
    }

    private AssociadoProduto convertToEntity(AssociadoProdutoDTO dto) {
        AssociadoProduto entity = new AssociadoProduto();

        entity.setId(dto.getId());

        // Buscar relacionamentos
        Associado associado = associadoRepository.findById(dto.getAssociadoId())
                .orElseThrow(() -> new ResourceNotFoundException("Associado não encontrado: " + dto.getAssociadoId()));
        entity.setAssociado(associado);

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado: " + dto.getProdutoId()));
        entity.setProduto(produto);

        if (dto.getTipoEnvioId() != null) {
            TipoEnvio tipoEnvio = tipoEnvioRepository.findById(dto.getTipoEnvioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de envio não encontrado: " + dto.getTipoEnvioId()));
            entity.setTipoEnvio(tipoEnvio);
        }

        entity.setValorDefinido(dto.getValorDefinido());
        entity.setDataAdesao(dto.getDataAdesao());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataFim(dto.getDataFim());
        entity.setDataReinicio(dto.getDataReinicio());

        entity.setEnvioPadrao(dto.getEnvioPadrao() != null && dto.getEnvioPadrao() ? "S" : "N");
        entity.setUtilizaEnriquecimento(dto.getUtilizaEnriquecimento() != null && dto.getUtilizaEnriquecimento() ? "S" : "N");
        entity.setDeduzirDoPlano(dto.getDeduzirDoPlano() != null && dto.getDeduzirDoPlano() ? "S" : "N");

        entity.setStatusNoProcesso(dto.getStatusNoProcesso() != null ? dto.getStatusNoProcesso() : "A");
        entity.setTipoProduto(dto.getTipoProduto());
        entity.setObservacao(dto.getObservacao());

        return entity;
    }

    private void updateEntity(AssociadoProduto entity, AssociadoProdutoDTO dto) {
        // Não atualiza associado e produto (chaves)
        if (dto.getTipoEnvioId() != null) {
            TipoEnvio tipoEnvio = tipoEnvioRepository.findById(dto.getTipoEnvioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de envio não encontrado: " + dto.getTipoEnvioId()));
            entity.setTipoEnvio(tipoEnvio);
        } else {
            entity.setTipoEnvio(null);
        }

        entity.setValorDefinido(dto.getValorDefinido());
        entity.setDataAdesao(dto.getDataAdesao());
        entity.setDataInicio(dto.getDataInicio());
        entity.setDataFim(dto.getDataFim());
        entity.setDataReinicio(dto.getDataReinicio());

        entity.setEnvioPadrao(dto.getEnvioPadrao() != null && dto.getEnvioPadrao() ? "S" : "N");
        entity.setUtilizaEnriquecimento(dto.getUtilizaEnriquecimento() != null && dto.getUtilizaEnriquecimento() ? "S" : "N");
        entity.setDeduzirDoPlano(dto.getDeduzirDoPlano() != null && dto.getDeduzirDoPlano() ? "S" : "N");

        entity.setStatusNoProcesso(dto.getStatusNoProcesso());
        entity.setTipoProduto(dto.getTipoProduto());
        entity.setObservacao(dto.getObservacao());
    }

    private AssociadoProdutoDTO convertToDTO(AssociadoProduto entity) {
        AssociadoProdutoDTO dto = new AssociadoProdutoDTO();

        dto.setId(entity.getId());
        dto.setAssociadoId(entity.getAssociado().getId());

        if (entity.getProduto() != null) {
            dto.setProdutoId(entity.getProduto().getId());
            dto.setTipoProduto(entity.getTipoProduto() != null ?
                    entity.getTipoProduto() : entity.getProduto().getTipoProduto());
        }

        if (entity.getTipoEnvio() != null) {
            dto.setTipoEnvioId(entity.getTipoEnvio().getId());
        }

        dto.setValorDefinido(entity.getValorDefinido());
        dto.setDataAdesao(entity.getDataAdesao());
        dto.setDataInicio(entity.getDataInicio());
        dto.setDataFim(entity.getDataFim());
        dto.setDataReinicio(entity.getDataReinicio());

        dto.setEnvioPadrao(entity.isEnvioPadrao());
        dto.setUtilizaEnriquecimento(entity.isUtilizaEnriquecimento());
        dto.setDeduzirDoPlano(entity.isDeduzirDoPlano());

        dto.setStatusNoProcesso(entity.getStatusNoProcesso());
        dto.setObservacao(entity.getObservacao());

        return dto;
    }

    private AssociadoProdutoResumoDTO convertToResumoDTO(AssociadoProduto entity) {
        AssociadoProdutoResumoDTO dto = new AssociadoProdutoResumoDTO();

        dto.setId(entity.getId());
        dto.setAssociadoId(entity.getAssociado().getId());
        dto.setAssociadoNome(entity.getAssociado().getNomeRazao());

        if (entity.getProduto() != null) {
            dto.setProdutoId(entity.getProduto().getId());
            dto.setProdutoCodigo(entity.getProduto().getCodigo());
            dto.setProdutoNome(entity.getProduto().getNome());
            dto.setTipoProduto(entity.getTipoProduto() != null ?
                    entity.getTipoProduto() : entity.getProduto().getTipoProduto());
        }

        dto.setValorDefinido(entity.getValorDefinido());
        dto.setValorEfetivo(entity.getValorEfetivo());

        dto.setStatusNoProcesso(entity.getStatusNoProcesso());
        dto.setAtivo(entity.isAtivo());

        if (entity.getTipoEnvio() != null) {
            dto.setTipoEnvioDescricao(entity.getTipoEnvio().getDescricao());
        }

        return dto;
    }
}