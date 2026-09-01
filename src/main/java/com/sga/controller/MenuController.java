package com.sga.controller;

import com.sga.dto.MenuDTO;
import com.sga.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    // ============================================================
    // LISTAR TODOS OS MENUS
    // ============================================================
    @GetMapping
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MenuDTO>> listarTodos() {
        return ResponseEntity.ok(menuService.listarTodos());
    }

    // ============================================================
    // LISTAR MENUS POR PERFIL
    // ============================================================
    @GetMapping("/perfil/{perfilId}")
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MenuDTO>> listarPorPerfil(@PathVariable Long perfilId) {
        return ResponseEntity.ok(menuService.listarMenusPorPerfil(perfilId));
    }

    // ============================================================
    // LISTAR MENUS PRINCIPAIS (sem pai)
    // ============================================================
    @GetMapping("/principais")
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<MenuDTO>> listarPrincipais() {
        return ResponseEntity.ok(menuService.listarMenusPrincipais());
    }

    // ============================================================
    // BUSCAR MENU POR ID
    // ============================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<MenuDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(menuService.buscarPorId(id));
    }
}