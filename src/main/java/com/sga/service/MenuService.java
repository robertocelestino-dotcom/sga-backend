package com.sga.service;

import com.sga.dto.MenuDTO;
import com.sga.model.Menu;
import com.sga.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;  // REMOVER 'final'

    public List<MenuDTO> listarTodos() {
        return menuRepository.findByAtivoTrueOrderByOrdemAsc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<MenuDTO> listarMenusPorPerfil(Long perfilId) {
        return menuRepository.findMenusByPerfilId(perfilId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<MenuDTO> listarMenusPrincipais() {
        return menuRepository.findByMenuPaiIsNullAndAtivoTrueOrderByOrdemAsc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public MenuDTO buscarPorId(Long id) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu não encontrado"));
        return toDTO(menu);
    }

    @Transactional
    public MenuDTO criar(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setNome(dto.getNome());
        menu.setCaminho(dto.getCaminho());
        menu.setIcone(dto.getIcone());
        menu.setOrdem(dto.getOrdem() != null ? dto.getOrdem() : 0);
        menu.setAtivo(true);
        menu.setDataCriacao(LocalDateTime.now());
        menu.setDataAtualizacao(LocalDateTime.now());

        if (dto.getMenuPaiId() != null) {
            Menu menuPai = menuRepository.findById(dto.getMenuPaiId())
                .orElseThrow(() -> new RuntimeException("Menu pai não encontrado"));
            menu.setMenuPai(menuPai);
        }

        menu = menuRepository.save(menu);
        return toDTO(menu);
    }

    @Transactional
    public MenuDTO atualizar(Long id, MenuDTO dto) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu não encontrado"));

        menu.setNome(dto.getNome());
        menu.setCaminho(dto.getCaminho());
        menu.setIcone(dto.getIcone());
        menu.setOrdem(dto.getOrdem());
        menu.setAtivo(dto.getAtivo());
        menu.setDataAtualizacao(LocalDateTime.now());

        if (dto.getMenuPaiId() != null) {
            Menu menuPai = menuRepository.findById(dto.getMenuPaiId())
                .orElseThrow(() -> new RuntimeException("Menu pai não encontrado"));
            menu.setMenuPai(menuPai);
        } else {
            menu.setMenuPai(null);
        }

        menu = menuRepository.save(menu);
        return toDTO(menu);
    }

    @Transactional
    public void deletar(Long id) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu não encontrado"));
        menuRepository.delete(menu);
    }

    private MenuDTO toDTO(Menu menu) {
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
            dto.setSubMenus(menu.getSubMenus().stream().map(this::toDTO).collect(Collectors.toList()));
        }
        return dto;
    }
}