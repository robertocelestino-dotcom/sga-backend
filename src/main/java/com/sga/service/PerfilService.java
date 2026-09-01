package com.sga.service;

import com.sga.dto.PerfilDTO;
import com.sga.dto.MenuDTO;
import com.sga.dto.PermissaoDTO;
import com.sga.dto.PerfilRequest;
import com.sga.model.Perfil;
import com.sga.model.Menu;
import com.sga.model.Permissao;
import com.sga.repository.PerfilRepository;
import com.sga.repository.MenuRepository;
import com.sga.repository.PermissaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;  // REMOVER 'final'

    @Autowired
    private MenuRepository menuRepository;  // REMOVER 'final'

    @Autowired
    private PermissaoRepository permissaoRepository;  // REMOVER 'final'

    public List<PerfilDTO> listarTodos() {
        return perfilRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<PerfilDTO> listarAtivos() {
        return perfilRepository.findByAtivoTrue().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public PerfilDTO buscarPorId(Long id) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        return toDTO(perfil);
    }

    // Adicione este método no PerfilService.java

    public Perfil buscarEntidadePorId(Long id) {
        return perfilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
    }   
    
    public List<MenuDTO> listarMenusPorPerfil(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        return perfil.getMenus().stream()
            .map(this::toMenuDTO)
            .collect(Collectors.toList());
    }

    public List<String> listarPermissoesPorPerfil(Long perfilId) {
        return permissaoRepository.findPermissoesByPerfilId(perfilId);
    }

    @Transactional
    public PerfilDTO criar(PerfilRequest request) {
        if (perfilRepository.existsByNome(request.getNome())) {
            throw new RuntimeException("Perfil já existe");
        }

        Perfil perfil = new Perfil();
        perfil.setNome(request.getNome().toUpperCase());
        perfil.setDescricao(request.getDescricao());
        perfil.setAtivo(request.getAtivo());
        perfil.setDataCriacao(LocalDateTime.now());
        perfil.setDataAtualizacao(LocalDateTime.now());

        if (request.getMenusIds() != null && !request.getMenusIds().isEmpty()) {
            List<Menu> menus = menuRepository.findAllById(request.getMenusIds());
            perfil.setMenus(menus.stream().collect(Collectors.toSet()));
        }

        if (request.getPermissoesIds() != null && !request.getPermissoesIds().isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findAllById(request.getPermissoesIds());
            perfil.setPermissoes(permissoes.stream().collect(Collectors.toSet()));
        }

        perfil = perfilRepository.save(perfil);
        return toDTO(perfil);
    }

    @Transactional
    public PerfilDTO atualizar(Long id, PerfilRequest request) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));

        perfil.setNome(request.getNome().toUpperCase());
        perfil.setDescricao(request.getDescricao());
        perfil.setAtivo(request.getAtivo());
        perfil.setDataAtualizacao(LocalDateTime.now());

        if (request.getMenusIds() != null) {
            List<Menu> menus = menuRepository.findAllById(request.getMenusIds());
            perfil.setMenus(menus.stream().collect(Collectors.toSet()));
        }

        if (request.getPermissoesIds() != null) {
            List<Permissao> permissoes = permissaoRepository.findAllById(request.getPermissoesIds());
            perfil.setPermissoes(permissoes.stream().collect(Collectors.toSet()));
        }

        perfil = perfilRepository.save(perfil);
        return toDTO(perfil);
    }

    @Transactional
    public void deletar(Long id) {
        Perfil perfil = perfilRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        perfilRepository.delete(perfil);
    }

    @Transactional
    public void associarMenu(Long perfilId, Long menuId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new RuntimeException("Menu não encontrado"));

        perfil.getMenus().add(menu);
        perfil.setDataAtualizacao(LocalDateTime.now());
        perfilRepository.save(perfil);
    }

    @Transactional
    public void removerMenu(Long perfilId, Long menuId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        Menu menu = menuRepository.findById(menuId)
            .orElseThrow(() -> new RuntimeException("Menu não encontrado"));

        perfil.getMenus().remove(menu);
        perfil.setDataAtualizacao(LocalDateTime.now());
        perfilRepository.save(perfil);
    }

    @Transactional
    public void associarPermissao(Long perfilId, Long permissaoId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        Permissao permissao = permissaoRepository.findById(permissaoId)
            .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        perfil.getPermissoes().add(permissao);
        perfil.setDataAtualizacao(LocalDateTime.now());
        perfilRepository.save(perfil);
    }

    @Transactional
    public void removerPermissao(Long perfilId, Long permissaoId) {
        Perfil perfil = perfilRepository.findById(perfilId)
            .orElseThrow(() -> new RuntimeException("Perfil não encontrado"));
        Permissao permissao = permissaoRepository.findById(permissaoId)
            .orElseThrow(() -> new RuntimeException("Permissão não encontrada"));

        perfil.getPermissoes().remove(permissao);
        perfil.setDataAtualizacao(LocalDateTime.now());
        perfilRepository.save(perfil);
    }

    private PerfilDTO toDTO(Perfil perfil) {
        PerfilDTO dto = new PerfilDTO();
        dto.setId(perfil.getId());
        dto.setNome(perfil.getNome());
        dto.setDescricao(perfil.getDescricao());
        dto.setAtivo(perfil.getAtivo());
        if (perfil.getMenus() != null) {
            dto.setMenus(perfil.getMenus().stream().map(this::toMenuDTO).collect(Collectors.toList()));
        }
        if (perfil.getPermissoes() != null) {
            dto.setPermissoes(perfil.getPermissoes().stream().map(this::toPermissaoDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private MenuDTO toMenuDTO(Menu menu) {
        MenuDTO dto = new MenuDTO();
        dto.setId(menu.getId());
        dto.setNome(menu.getNome());
        dto.setCaminho(menu.getCaminho());
        dto.setIcone(menu.getIcone());
        dto.setOrdem(menu.getOrdem());
        dto.setAtivo(menu.getAtivo());
        if (menu.getMenuPai() != null) {
            dto.setMenuPaiId(menu.getMenuPai().getId());
            dto.setMenuPaiNome(menu.getMenuPai().getNome());
        }
        if (menu.getSubMenus() != null && !menu.getSubMenus().isEmpty()) {
            dto.setSubMenus(menu.getSubMenus().stream().map(this::toMenuDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private PermissaoDTO toPermissaoDTO(Permissao permissao) {
        PermissaoDTO dto = new PermissaoDTO();
        dto.setId(permissao.getId());
        dto.setNome(permissao.getNome());
        dto.setDescricao(permissao.getDescricao());
        dto.setRecurso(permissao.getRecurso());
        dto.setAcao(permissao.getAcao());
        return dto;
    }
}