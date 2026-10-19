package com.sga.service;

import com.sga.dto.MenuDTO;
import com.sga.model.Menu;
import com.sga.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    // ============================================================
    // 🔥 MÉTODOS DE LEITURA
    //
    // Todos com @Transactional(readOnly = true) para manter a
    // sessão do Hibernate aberta durante o toDTO(), que acessa
    // menu.getSubMenus() (LAZY) e menu.getMenuPai() (LAZY).
    //
    // Sem essa anotação, o toDTO() fora de transação dispara:
    //   LazyInitializationException: failed to lazily initialize
    //   a collection of role: com.sga.model.Menu.subMenus
    // ============================================================

    @Transactional(readOnly = true)
    public List<MenuDTO> listarTodos() {
        return menuRepository.findByAtivoTrueOrderByOrdemAsc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuDTO> listarMenusPorPerfil(Long perfilId) {
        return menuRepository.findMenusByPerfilId(perfilId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuDTO> listarMenusPrincipais() {
        return menuRepository.findByMenuPaiIsNullAndAtivoTrueOrderByOrdemAsc().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MenuDTO buscarPorId(Long id) {
        Menu menu = menuRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Menu não encontrado"));
        return toDTO(menu);
    }

    // ============================================================
    // 🔥 MÉTODOS DE ESCRITA
    // ============================================================

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

    // ============================================================
    // 🔥 MÉTODO AUXILIAR — construir a árvore de MenuDTO
    //
    // Recebe uma lista plana de Menu (ou a árvore já montada) e
    // devolve os DTOs com subMenus aninhados.
    //
    // Estratégia:
    //  - Constrói um mapa id → DTO em memória (sem recursão no banco).
    //  - Liga os filhos aos pais.
    //  - Retorna apenas as raízes (com a árvore construída).
    //
    // Isso evita N+1 e evita acessar menu.getSubMenus() repetidamente.
    // ============================================================

    /**
     * 🔥 Retorna a lista de DTOs com a árvore completa.
     *
     * ⚠️ IMPORTANTE:
     *    Este método assume que `todosMenus` já tem os `menuPai` e
     *    `subMenus` carregados (via @Transactional no chamador, ou
     *    via JOIN FETCH na query).
     */
    private List<MenuDTO> construirArvoreDTO(List<Menu> todosMenus) {
        if (todosMenus == null || todosMenus.isEmpty()) {
            return new ArrayList<>();
        }

        // 1) Cria DTO flat para cada menu
        Map<Long, MenuDTO> dtoMap = new LinkedHashMap<>();
        for (Menu m : todosMenus) {
            MenuDTO dto = new MenuDTO();
            dto.setId(m.getId());
            dto.setNome(m.getNome());
            dto.setCaminho(m.getCaminho());
            dto.setIcone(m.getIcone());
            dto.setOrdem(m.getOrdem());
            dto.setAtivo(m.getAtivo());
            dto.setSubMenus(new ArrayList<>());

            // menuPai já deve vir carregado (LAZY ok dentro da transação)
            if (m.getMenuPai() != null) {
                dto.setMenuPaiId(m.getMenuPai().getId());
                dto.setMenuPaiNome(m.getMenuPai().getNome());
            }

            dtoMap.put(m.getId(), dto);
        }

        // 2) Liga filhos aos pais
        List<MenuDTO> roots = new ArrayList<>();
        for (Menu m : todosMenus) {
            MenuDTO dto = dtoMap.get(m.getId());
            if (dto == null) continue;

            if (m.getMenuPai() != null && dtoMap.containsKey(m.getMenuPai().getId())) {
                dtoMap.get(m.getMenuPai().getId()).getSubMenus().add(dto);
            } else {
                roots.add(dto);
            }
        }

        // 3) Ordena recursivamente
        ordenarRecursivo(roots);

        return roots;
    }

    private void ordenarRecursivo(List<MenuDTO> arr) {
        if (arr == null || arr.isEmpty()) return;
        arr.sort((a, b) -> {
            int oa = a.getOrdem() != null ? a.getOrdem() : 0;
            int ob = b.getOrdem() != null ? b.getOrdem() : 0;
            return Integer.compare(oa, ob);
        });
        for (MenuDTO m : arr) {
            if (m.getSubMenus() != null && !m.getSubMenus().isEmpty()) {
                ordenarRecursivo(m.getSubMenus());
            }
        }
    }

    // ============================================================
    // toDTO — mantido para compatibilidade com o código existente
    //
    // ⚠️ Este método DEVE ser chamado dentro de uma transação
    //    (por isso todos os métodos de leitura têm @Transactional
    //    readOnly = true).
    //
    //    Ele acessa `menu.getSubMenus()` recursivamente. Dentro de
    //    uma transação, o Hibernate inicializa o lazy automaticamente.
    //    Fora de transação, lança LazyInitializationException.
    // ============================================================

    private MenuDTO toDTO(Menu menu) {
        if (menu == null) return null;

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
            dto.setSubMenus(
                menu.getSubMenus().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }
}