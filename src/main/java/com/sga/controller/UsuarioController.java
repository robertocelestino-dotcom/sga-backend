package com.sga.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.SenhaRequest;
import com.sga.dto.UsuarioDTO;
import com.sga.dto.UsuarioRequest;
import com.sga.dto.UsuarioUpdateRequest;
import com.sga.model.Usuario;
import com.sga.service.LogAcaoService;
import com.sga.service.UsuarioService;

@RestController
@RequestMapping("/api/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private LogAcaoService logAcaoService;

    // ============================================================
    // LISTAR TODOS OS USUÁRIOS
    // ============================================================
    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<UsuarioDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodosDTO());
    }

    // ============================================================
    // BUSCAR USUÁRIO POR ID
    // ============================================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorIdDTO(id));
    }

    // ============================================================
    // CRIAR USUÁRIO
    // ============================================================
    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioDTO> criar(
            @Valid @RequestBody UsuarioRequest request,
            HttpServletRequest httpRequest) {
        
        // 🔥 O UsuarioService precisa ter o método criar(UsuarioRequest)
        UsuarioDTO usuario = usuarioService.criar(request);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "CREATE",
            request,
            "SUCCESS",
            "Usuário criado: " + request.getUsername()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    // ============================================================
    // ATUALIZAR USUÁRIO
    // ============================================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<UsuarioDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request,
            HttpServletRequest httpRequest) {
        
        // 🔥 O UsuarioService precisa ter o método atualizar(Long, UsuarioUpdateRequest)
        UsuarioDTO usuario = usuarioService.atualizar(id, request);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "UPDATE",
            request,
            "SUCCESS",
            "Usuário atualizado: " + usuario.getUsername()
        );
        
        return ResponseEntity.ok(usuario);
    }

    // ============================================================
    // ALTERAR SENHA
    // ============================================================
    @PatchMapping("/{id}/alterar-senha")
    @PreAuthorize("hasAuthority('USUARIO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> alterarSenha(
            @PathVariable Long id,
            @Valid @RequestBody SenhaRequest request,
            HttpServletRequest httpRequest) {
        
        usuarioService.alterarSenha(id, request.getSenhaAtual(), request.getNovaSenha());
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "UPDATE",
            "Alteração de senha",
            "SUCCESS",
            "Senha alterada para usuário ID: " + id
        );
        
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // BLOQUEAR USUÁRIO
    // ============================================================
    @PatchMapping("/{id}/bloquear")
    @PreAuthorize("hasAuthority('USUARIO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> bloquear(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        usuarioService.bloquear(id);
        Usuario usuario = usuarioService.buscarPorId(id);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "UPDATE",
            "Bloqueio",
            "SUCCESS",
            "Usuário bloqueado: " + usuario.getUsername()
        );
        
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // DESBLOQUEAR USUÁRIO
    // ============================================================
    @PatchMapping("/{id}/desbloquear")
    @PreAuthorize("hasAuthority('USUARIO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> desbloquear(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        usuarioService.desbloquear(id);
        Usuario usuario = usuarioService.buscarPorId(id);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "UPDATE",
            "Desbloqueio",
            "SUCCESS",
            "Usuário desbloqueado: " + usuario.getUsername()
        );
        
        return ResponseEntity.ok().build();
    }

    // ============================================================
    // EXCLUIR USUÁRIO
    // ============================================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_DELETE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        Usuario usuario = usuarioService.buscarPorId(id);
        usuarioService.deletar(id);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "DELETE",
            null,
            "SUCCESS",
            "Usuário excluído: " + usuario.getUsername()
        );
        
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/resetar-senha")
    @PreAuthorize("hasAuthority('USUARIO_EDIT') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> resetarSenha(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String novaSenha = request.get("novaSenha");
        usuarioService.resetarSenha(id, novaSenha);
        
        logAcaoService.registrarAcao(
            null,
            "SISTEMA",
            httpRequest.getRemoteAddr(),
            "USUARIO",
            "UPDATE",
            "Reset de senha",
            "SUCCESS",
            "Senha resetada para usuário ID: " + id
        );
        
        return ResponseEntity.ok().build();
    }    
}