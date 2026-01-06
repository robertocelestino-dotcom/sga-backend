// src/main/java/com/sga/service/AssociadoService.java - VERSÃO CORRIGIDA FINAL
package com.sga.service;

import com.sga.dto.AssociadoDTO;
import com.sga.dto.AssociadoResumoDTO;
import com.sga.dto.EmailDTO;
import com.sga.dto.EnderecoDTO;
import com.sga.dto.TelefoneDTO;
import com.sga.model.Associado;
import com.sga.model.Categoria;
import com.sga.model.Email;
import com.sga.model.Endereco;
import com.sga.model.Planos;
import com.sga.model.Telefone;
import com.sga.model.Vendedor;
import com.sga.repository.AssociadoRepository;
import com.sga.repository.CategoriaRepository;
import com.sga.repository.PlanosRepository;
import com.sga.repository.VendedorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AssociadoService {

    private static final Logger logger = LoggerFactory.getLogger(AssociadoService.class);
    
    @Autowired
    private AssociadoRepository associadoRepository;
    
    @Autowired
    private VendedorRepository vendedorRepository;
    
    @Autowired
    private PlanosRepository planosRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;

    // Converter Entity para DTO resumido
    private AssociadoResumoDTO toResumoDTO(Associado associado) {
        if (associado == null) {
            return null;
        }
        
        AssociadoResumoDTO dto = new AssociadoResumoDTO();
        dto.setId(associado.getId());
        dto.setCodigoSpc(associado.getCodigoSpc());
        dto.setCodigoRm(associado.getCodigoRm());
        dto.setCnpjCpf(associado.getCnpjCpf());
        dto.setNomeRazao(associado.getNomeRazao());
        dto.setNomeFantasia(associado.getNomeFantasia());
        dto.setTipoPessoa(associado.getTipoPessoa());
        dto.setStatus(associado.getStatus());
        dto.setFaturamentoMinimo(associado.getFaturamentoMinimo());
        
        // CORREÇÃO: Se AssociadoResumoDTO usa LocalDateTime e Associado usa LocalDate
        if (associado.getDataCadastro() != null) {
            // Converter LocalDate para LocalDateTime (meia-noite)
            //dto.setDataCadastro(associado.getDataCadastro().atStartOfDay());
        	dto.setDataCadastro(LocalDateTime.now());
        }
        
        // Informações básicas dos relacionamentos
        if (associado.getVendedor() != null) {
            dto.setVendedorId(associado.getVendedor().getId());
            dto.setVendedorNome(associado.getVendedor().getNomeRazao());
        }
        
        if (associado.getPlano() != null) {
            dto.setPlanoId(associado.getPlano().getId());
            dto.setPlanoNome(associado.getPlano().getPlano());
        }
        
        if (associado.getCategoria() != null) {
            dto.setCategoriaId(associado.getCategoria().getId());
            dto.setCategoriaNome(associado.getCategoria().getDescricao());
        }
        
        return dto;
    }

    // Converter Entity para DTO completo
    private AssociadoDTO toDTO(Associado associado) {
        if (associado == null) {
            return null;
        }
        
        AssociadoDTO dto = new AssociadoDTO();
        dto.setId(associado.getId());
        dto.setCodigoSpc(associado.getCodigoSpc());
        dto.setCodigoRm(associado.getCodigoRm());
        dto.setCnpjCpf(associado.getCnpjCpf());
        dto.setNomeRazao(associado.getNomeRazao());
        dto.setNomeFantasia(associado.getNomeFantasia());
        dto.setTipoPessoa(associado.getTipoPessoa());
        dto.setStatus(associado.getStatus());
        dto.setFaturamentoMinimo(associado.getFaturamentoMinimo());
        
        // CORREÇÃO: Se AssociadoDTO usa LocalDateTime e Associado usa LocalDate
        if (associado.getDataCadastro() != null) {
            // Converter LocalDate para LocalDateTime (meia-noite)
            //dto.setDataCadastro(associado.getDataCadastro().atStartOfDay());
        	dto.setDataCadastro(LocalDateTime.now());
        }
        
        // IDs dos relacionamentos
        if (associado.getVendedor() != null) {
            dto.setVendedorId(associado.getVendedor().getId());
        }
        
        if (associado.getPlano() != null) {
            dto.setPlanoId(associado.getPlano().getId());
        }
        
        if (associado.getCategoria() != null) {
            dto.setCategoriaId(associado.getCategoria().getId());
        }
        
        // Converter sub-entidades
        if (associado.getEnderecos() != null) {
            dto.setEnderecos(associado.getEnderecos().stream()
                .map(this::toEnderecoDTO)
                .collect(Collectors.toList()));
        }
        
        if (associado.getEmails() != null) {
            dto.setEmails(associado.getEmails().stream()
                .map(this::toEmailDTO)
                .collect(Collectors.toList()));
        }
        
        if (associado.getTelefones() != null) {
            dto.setTelefones(associado.getTelefones().stream()
                .map(this::toTelefoneDTO)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    // Converter sub-entidades
    private EnderecoDTO toEnderecoDTO(Endereco endereco) {
        EnderecoDTO dto = new EnderecoDTO();
        dto.setId(endereco.getId());
        dto.setTipoLogradouro(endereco.getTipoLogradouro());
        dto.setLogradouro(endereco.getLogradouro());
        dto.setNumero(endereco.getNumero());
        dto.setComplemento(endereco.getComplemento());
        dto.setBairro(endereco.getBairro());
        dto.setCidade(endereco.getCidade());
        dto.setCep(endereco.getCep());
        dto.setEstado(endereco.getEstado());
        dto.setTipoEndereco(endereco.getTipoEndereco());
        return dto;
    }

    private EmailDTO toEmailDTO(Email email) {
        EmailDTO dto = new EmailDTO();
        dto.setId(email.getId());
        dto.setEmail(email.getEmail());
        dto.setTipoEmail(email.getTipoEmail());
        dto.setAtivo(email.getAtivo());
        return dto;
    }

    private TelefoneDTO toTelefoneDTO(Telefone telefone) {
        TelefoneDTO dto = new TelefoneDTO();
        dto.setId(telefone.getId());
        dto.setDdd(telefone.getDdd());
        dto.setNumero(telefone.getNumero());
        dto.setTipoTelefone(telefone.getTipoTelefone());
        dto.setWhatsapp(telefone.getWhatsapp());
        dto.setAtivo(telefone.getAtivo());
        return dto;
    }

    // Listar com filtros otimizado
    @Transactional(readOnly = true)
    public Page<AssociadoResumoDTO> listarComFiltros(Pageable pageable, 
                                                    String codigoSpc, 
                                                    String nomeRazao, 
                                                    String cnpjCpf, 
                                                    String status) {
        logger.info("Listando associados com filtros - page: {}, size: {}, sort: {}", 
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        
        // Usar métodos específicos do repository se existirem
        Page<Associado> associadosPage;
        
        // Aplicar filtros de forma otimizada
        if (codigoSpc != null && !codigoSpc.trim().isEmpty()) {
            associadosPage = associadoRepository.findAllByCodigoSpcContainingIgnoreCase(codigoSpc.trim(), pageable);
        } else if (nomeRazao != null && !nomeRazao.trim().isEmpty()) {
            associadosPage = associadoRepository.findAllByNomeRazaoContainingIgnoreCase(nomeRazao.trim(), pageable);
        } else if (cnpjCpf != null && !cnpjCpf.trim().isEmpty()) {
            associadosPage = associadoRepository.findAllByCnpjCpfContaining(cnpjCpf.trim(), pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            associadosPage = associadoRepository.findAllByStatus(status.trim(), pageable);
        } else {
            associadosPage = associadoRepository.findAll(pageable);
        }
        
        // Aplicar filtros adicionais manualmente se necessário
        List<AssociadoResumoDTO> dtos = new ArrayList<>();
        for (Associado associado : associadosPage.getContent()) {
            boolean passaFiltros = true;
            
            // Filtro por código SPC (se já não foi aplicado)
            if (codigoSpc != null && !codigoSpc.trim().isEmpty() && 
                (associado.getCodigoSpc() == null || 
                 !associado.getCodigoSpc().toLowerCase().contains(codigoSpc.toLowerCase().trim()))) {
                passaFiltros = false;
            }
            
            // Filtro por nome/razão (se já não foi aplicado)
            if (passaFiltros && nomeRazao != null && !nomeRazao.trim().isEmpty() &&
                (associado.getNomeRazao() == null || 
                 !associado.getNomeRazao().toLowerCase().contains(nomeRazao.toLowerCase().trim()))) {
                passaFiltros = false;
            }
            
            // Filtro por CNPJ/CPF (se já não foi aplicado)
            if (passaFiltros && cnpjCpf != null && !cnpjCpf.trim().isEmpty() &&
                (associado.getCnpjCpf() == null || 
                 !associado.getCnpjCpf().contains(cnpjCpf.trim()))) {
                passaFiltros = false;
            }
            
            // Filtro por status (se já não foi aplicado)
            if (passaFiltros && status != null && !status.trim().isEmpty() &&
                (associado.getStatus() == null || 
                 !associado.getStatus().equals(status.trim()))) {
                passaFiltros = false;
            }
            
            if (passaFiltros) {
                dtos.add(toResumoDTO(associado));
            }
        }
        
        logger.info("Encontrados {} associados", dtos.size());
        
        // Criar uma página com os resultados filtrados
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtos.size());
        
        if (start > dtos.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        List<AssociadoResumoDTO> paginaDTOs = dtos.subList(start, end);
        return new PageImpl<>(paginaDTOs, pageable, dtos.size());
    }

    // Buscar por ID
    @Transactional(readOnly = true)
    public AssociadoDTO buscarPorId(Long id) {
        logger.info("Buscando associado por ID: {}", id);
        
        Associado associado = associadoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Associado não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Associado não encontrado com ID: " + id);
                });
        
        logger.info("Associado encontrado: {} - {}", associado.getId(), associado.getNomeRazao());
        return toDTO(associado);
    }

    // Criar novo associado
    @Transactional
    public AssociadoDTO criar(AssociadoDTO associadoDTO) {
        logger.info("Criando novo associado: {}", associadoDTO.getNomeRazao());
        
        // Validar dados obrigatórios
        if (associadoDTO.getCnpjCpf() == null || associadoDTO.getCnpjCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CNPJ/CPF é obrigatório");
        }
        
        if (associadoDTO.getNomeRazao() == null || associadoDTO.getNomeRazao().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome/Razão Social é obrigatório");
        }
        
        Associado associado = new Associado();
        updateEntityFromDTO(associado, associadoDTO);
        
        Associado saved = associadoRepository.save(associado);
        logger.info("Associado criado com ID: {}", saved.getId());
        
        return toDTO(saved);
    }

    // Atualizar associado
    @Transactional
    public AssociadoDTO atualizar(Long id, AssociadoDTO associadoDTO) {
        logger.info("Atualizando associado ID: {}", id);
        
        Associado associado = associadoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Associado não encontrado com ID: {}", id);
                    return new EntityNotFoundException("Associado não encontrado com ID: " + id);
                });
        
        updateEntityFromDTO(associado, associadoDTO);
        
        Associado updated = associadoRepository.save(associado);
        logger.info("Associado atualizado: {} - {}", updated.getId(), updated.getNomeRazao());
        
        return toDTO(updated);
    }

    // Excluir associado
    @Transactional
    public void excluir(Long id) {
        logger.info("Excluindo associado ID: {}", id);
        
        if (!associadoRepository.existsById(id)) {
            logger.error("Associado não encontrado para exclusão: {}", id);
            throw new EntityNotFoundException("Associado não encontrado com ID: " + id);
        }
        
        associadoRepository.deleteById(id);
        logger.info("Associado excluído: {}", id);
    }

    // Atualizar entidade a partir do DTO
    private void updateEntityFromDTO(Associado associado, AssociadoDTO dto) {
        logger.info("Atualizando entidade a partir do DTO para: {}", dto.getNomeRazao());
        
        try {
            // 🟢 1. DADOS BÁSICOS DO ASSOCIADO
            // Campos opcionais - garante string vazia se null
            associado.setCodigoSpc(dto.getCodigoSpc() != null ? dto.getCodigoSpc() : "");
            associado.setCodigoRm(dto.getCodigoRm() != null ? dto.getCodigoRm() : "");
            associado.setNomeFantasia(dto.getNomeFantasia() != null ? dto.getNomeFantasia() : "");
            
            // Campos obrigatórios - validação
            if (dto.getCnpjCpf() == null || dto.getCnpjCpf().trim().isEmpty()) {
                throw new IllegalArgumentException("CNPJ/CPF é obrigatório");
            }
            associado.setCnpjCpf(dto.getCnpjCpf().trim());
            
            if (dto.getNomeRazao() == null || dto.getNomeRazao().trim().isEmpty()) {
                throw new IllegalArgumentException("Nome/Razão Social é obrigatório");
            }
            associado.setNomeRazao(dto.getNomeRazao().trim());
            
            if (dto.getTipoPessoa() == null) {
                throw new IllegalArgumentException("Tipo Pessoa é obrigatório");
            }
            associado.setTipoPessoa(dto.getTipoPessoa());
            
            // Status - padrão "A" (Ativo) se não informado
            associado.setStatus(
                (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) 
                    ? dto.getStatus().trim() 
                    : "A"
            );
            
            // 🟢 2. DATA CADASTRO - CONVERSÃO SEGURA (LocalDateTime → LocalDate)
            if (dto.getDataCadastro() != null) {
                // Converte LocalDateTime para LocalDate (pega apenas a data)
                //associado.setDataCadastro(dto.getDataCadastro().toLocalDate());
            	associado.setDataCadastro(LocalDateTime.now());
            } else {
                //associado.setDataCadastro(LocalDate.now());
            	associado.setDataCadastro(LocalDateTime.now());
            }
            
            // 🟢 3. FATURAMENTO MÍNIMO - PODE SER NULL
            // Converte BigDecimal para Double se necessário
            if (dto.getFaturamentoMinimo() != null) {
                associado.setFaturamentoMinimo(dto.getFaturamentoMinimo());
            } else {
                associado.setFaturamentoMinimo(null);
            }
            
            // 🟢 4. RELACIONAMENTOS (vendedor, plano, categoria)
            // Vendedor - só seta se ID > 0
            if (dto.getVendedorId() != null && dto.getVendedorId() > 0) {
                Vendedor vendedor = vendedorRepository.findById(dto.getVendedorId())
                        .orElseThrow(() -> {
                            logger.error("Vendedor não encontrado com ID: {}", dto.getVendedorId());
                            return new EntityNotFoundException("Vendedor não encontrado com ID: " + dto.getVendedorId());
                        });
                associado.setVendedor(vendedor);
            } else {
                associado.setVendedor(null);
            }
            
            // Plano - só seta se ID > 0
            if (dto.getPlanoId() != null && dto.getPlanoId() > 0) {
                Planos plano = planosRepository.findById(dto.getPlanoId())
                        .orElseThrow(() -> {
                            logger.error("Plano não encontrado com ID: {}", dto.getPlanoId());
                            return new EntityNotFoundException("Plano não encontrado com ID: " + dto.getPlanoId());
                        });
                associado.setPlano(plano);
            } else {
                associado.setPlano(null);
            }
            
            // Categoria - só seta se ID > 0
            if (dto.getCategoriaId() != null && dto.getCategoriaId() > 0) {
                Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                        .orElseThrow(() -> {
                            logger.error("Categoria não encontrada com ID: {}", dto.getCategoriaId());
                            return new EntityNotFoundException("Categoria não encontrada com ID: " + dto.getCategoriaId());
                        });
                associado.setCategoria(categoria);
            } else {
                associado.setCategoria(null);
            }
            
            // 🟢 5. INICIALIZAR LISTAS SE NECESSÁRIO
            if (associado.getEnderecos() == null) {
                associado.setEnderecos(new java.util.ArrayList<>());
            }
            if (associado.getEmails() == null) {
                associado.setEmails(new java.util.ArrayList<>());
            }
            if (associado.getTelefones() == null) {
                associado.setTelefones(new java.util.ArrayList<>());
            }
            
            // Limpar listas existentes antes de adicionar novos
            associado.getEnderecos().clear();
            associado.getEmails().clear();
            associado.getTelefones().clear();
            
            // 🟢 6. ENDEREÇOS - TRATAMENTO SEGURO
            if (dto.getEnderecos() != null) {
                for (EnderecoDTO enderecoDTO : dto.getEnderecos()) {
                    if (enderecoDTO == null) continue;
                    
                    Endereco endereco = new Endereco();
                    endereco.setAssociado(associado);
                    
                    // Todos os campos com valores padrão seguros
                    endereco.setTipoLogradouro(
                        enderecoDTO.getTipoLogradouro() != null ? 
                        enderecoDTO.getTipoLogradouro().trim() : "Rua"
                    );
                    
                    endereco.setLogradouro(
                        enderecoDTO.getLogradouro() != null ? 
                        enderecoDTO.getLogradouro().trim() : ""
                    );
                    
                    endereco.setNumero(
                        enderecoDTO.getNumero() != null ? 
                        enderecoDTO.getNumero().trim() : ""
                    );
                    
                    endereco.setComplemento(
                        enderecoDTO.getComplemento() != null ? 
                        enderecoDTO.getComplemento().trim() : ""
                    );
                    
                    endereco.setBairro(
                        enderecoDTO.getBairro() != null ? 
                        enderecoDTO.getBairro().trim() : ""
                    );
                    
                    endereco.setCidade(
                        enderecoDTO.getCidade() != null ? 
                        enderecoDTO.getCidade().trim() : ""
                    );
                    
                    endereco.setCep(
                        enderecoDTO.getCep() != null ? 
                        enderecoDTO.getCep().trim().replaceAll("[^0-9]", "") : ""
                    );
                    
                    endereco.setEstado(
                        enderecoDTO.getEstado() != null ? 
                        enderecoDTO.getEstado().trim() : ""
                    );
                    
                    endereco.setTipoEndereco(
                        enderecoDTO.getTipoEndereco() != null ? 
                        enderecoDTO.getTipoEndereco().trim() : "COMERCIAL"
                    );
                    
                    associado.getEnderecos().add(endereco);
                }
            }
            
            // 🟢 7. EMAILS - TRATAMENTO SEGURO
            if (dto.getEmails() != null) {
                for (EmailDTO emailDTO : dto.getEmails()) {
                    if (emailDTO == null) continue;
                    
                    // Valida email básico
                    if (emailDTO.getEmail() == null || emailDTO.getEmail().trim().isEmpty()) {
                        continue; // Pula emails vazios
                    }
                    
                    Email email = new Email();
                    email.setAssociado(associado);
                    email.setEmail(emailDTO.getEmail().trim().toLowerCase());
                    email.setTipoEmail(
                        emailDTO.getTipoEmail() != null ? 
                        emailDTO.getTipoEmail().trim() : "COMERCIAL"
                    );
                    email.setAtivo(emailDTO.getAtivo() != null ? emailDTO.getAtivo() : true);
                    
                    associado.getEmails().add(email);
                }
            }
            
            // 🟢 8. TELEFONES - TRATAMENTO SEGURO
            if (dto.getTelefones() != null) {
                for (TelefoneDTO telefoneDTO : dto.getTelefones()) {
                    if (telefoneDTO == null) continue;
                    
                    // Valida telefone básico
                    if ((telefoneDTO.getDdd() == null || telefoneDTO.getDdd().trim().isEmpty()) ||
                        (telefoneDTO.getNumero() == null || telefoneDTO.getNumero().trim().isEmpty())) {
                        continue; // Pula telefones inválidos
                    }
                    
                    Telefone telefone = new Telefone();
                    telefone.setAssociado(associado);
                    telefone.setDdd(telefoneDTO.getDdd().trim().replaceAll("[^0-9]", ""));
                    telefone.setNumero(telefoneDTO.getNumero().trim().replaceAll("[^0-9]", ""));
                    telefone.setTipoTelefone(
                        telefoneDTO.getTipoTelefone() != null ? 
                        telefoneDTO.getTipoTelefone().trim() : "CELULAR"
                    );
                    telefone.setWhatsapp(telefoneDTO.getWhatsapp() != null ? telefoneDTO.getWhatsapp() : false);
                    telefone.setAtivo(telefoneDTO.getAtivo() != null ? telefoneDTO.getAtivo() : true);
                    
                    associado.getTelefones().add(telefone);
                }
            }
            
            logger.info("✅ Entidade atualizada com sucesso para associado: {}", associado.getNomeRazao());
            
        } catch (IllegalArgumentException e) {
            logger.error("❌ Erro de validação: {}", e.getMessage());
            throw e;
        } catch (EntityNotFoundException e) {
            logger.error("❌ Recurso não encontrado: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("❌ Erro inesperado ao atualizar entidade: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao processar dados do associado: " + e.getMessage(), e);
        }
    }
    
    // Métodos para estatísticas
    @Transactional(readOnly = true)
    public Long countAssociadosAtivos() {
        Long count = associadoRepository.countAssociadosAtivos();
        logger.info("Total de associados ativos: {}", count);
        return count;
    }

    @Transactional(readOnly = true)
    public Long countTotalAssociados() {
        Long count = associadoRepository.countAllAssociados();
        logger.info("Total de associados: {}", count);
        return count;
    }

    // Buscar por CNPJ/CPF
    @Transactional(readOnly = true)
    public AssociadoDTO buscarPorCnpjCpf(String cnpjCpf) {
        logger.info("Buscando associado por CNPJ/CPF: {}", cnpjCpf);
        
        return associadoRepository.findAllByCnpjCpfContaining(cnpjCpf, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(this::toDTO)
                .orElseThrow(() -> {
                    logger.error("Associado não encontrado com CNPJ/CPF: {}", cnpjCpf);
                    return new EntityNotFoundException("Associado não encontrado com CNPJ/CPF: " + cnpjCpf);
                });
    }

    // Buscar todos os vendedores para dropdown
    @Transactional(readOnly = true)
    public List<Vendedor> buscarTodosVendedores() {
        return vendedorRepository.findAll();
    }

    // Buscar todos os planos para dropdown
    @Transactional(readOnly = true)
    public List<Planos> buscarTodosPlanos() {
        return planosRepository.findAll();
    }

    // Buscar todas as categorias para dropdown
    @Transactional(readOnly = true)
    public List<Categoria> buscarTodasCategorias() {
        return categoriaRepository.findAll();
    }

}