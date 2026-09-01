package com.sga.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.PerfilDTO;
import com.sga.dto.PerfilRequest;
import com.sga.service.LogAcaoService;
import com.sga.service.PerfilService;

@RestController
@RequestMapping("/api/admin/perfis")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @Autowired
    private LogAcaoService logAcaoService;

    // ============================================================
    // LISTAR TODOS OS PERFIS
    // ============================================================
    @GetMapping
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PerfilDTO>> listarTodos() {
        return ResponseEntity.ok(perfilService.listarTodos());
    }

    // ============================================================
    // LISTAR PERFIS ATIVOS (para combos)
    // ============================================================
    @GetMapping("/ativos")
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PerfilDTO>> listarAtivos() {
        return ResponseEntity.ok(perfilService.listarAtivos());
    }

    // ============================================================
    // BUSCAR PERFIL POR ID
    // ============================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PerfilDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.buscarPorId(id));
    }

    // ============================================================
    // CRIAR PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PerfilDTO> criar(
            @Valid @RequestBody PerfilRequest request,
            HttpServletRequest httpRequest) {
        
        PerfilDTO perfil = perfilService.criar(request);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "PERFIL",
            "CREATE",
            request,
            "SUCCESS",
            "Perfil criado: " + request.getNome()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(perfil);
    }

    // ============================================================
    // ATUALIZAR PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PerfilDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PerfilRequest request,
            HttpServletRequest httpRequest) {
        
        PerfilDTO perfil = perfilService.atualizar(id, request);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "PERFIL",
            "UPDATE",
            request,
            "SUCCESS",
            "Perfil atualizado: " + request.getNome()
        );
        
        return ResponseEntity.ok(perfil);
    }

    // ============================================================
    // EXCLUIR PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERFIL_DELETE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        PerfilDTO perfil = perfilService.buscarPorId(id);
        perfilService.deletar(id);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "PERFIL",
            "DELETE",
            null,
            "SUCCESS",
            "Perfil excluído: " + perfil.getNome()
        );
        
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // ASSOCIAR MENU AO PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @PostMapping("/{perfilId}/menus/{menuId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> associarMenu(
            @PathVariable Long perfilId,
            @PathVariable Long menuId) {
        perfilService.associarMenu(perfilId, menuId);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // REMOVER MENU DO PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @DeleteMapping("/{perfilId}/menus/{menuId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> removerMenu(
            @PathVariable Long perfilId,
            @PathVariable Long menuId) {
        perfilService.removerMenu(perfilId, menuId);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // ASSOCIAR PERMISSÃO AO PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @PostMapping("/{perfilId}/permissoes/{permissaoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> associarPermissao(
            @PathVariable Long perfilId,
            @PathVariable Long permissaoId) {
        perfilService.associarPermissao(perfilId, permissaoId);
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // REMOVER PERMISSÃO DO PERFIL (APENAS SUPER_ADMIN)
    // ============================================================
    @DeleteMapping("/{perfilId}/permissoes/{permissaoId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> removerPermissao(
            @PathVariable Long perfilId,
            @PathVariable Long permissaoId) {
        perfilService.removerPermissao(perfilId, permissaoId);
        return ResponseEntity.ok().build();
    }
}