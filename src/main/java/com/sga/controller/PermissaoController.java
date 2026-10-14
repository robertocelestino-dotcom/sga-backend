package com.sga.controller;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sga.dto.PermissaoDTO;
import com.sga.model.Permissao;
import com.sga.repository.PermissaoRepository;

@RestController
@RequestMapping("/api/admin/permissoes")
public class PermissaoController {

    @Autowired
    private PermissaoRepository permissaoRepository;

    // ============================================================
    // LISTAR TODAS AS PERMISSÕES
    // Usado pelo PerfilForm para montar a lista real do banco.
    // Substitui a lista hardcoded que estava corrompendo perfis.
    // ============================================================
    @GetMapping
    @PreAuthorize("hasAuthority('PERFIL_VIEW') or hasAuthority('PERFIL_EDIT') or hasAuthority('PERFIL_CREATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<PermissaoDTO>> listarTodas() {
        List<PermissaoDTO> lista = permissaoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .sorted(Comparator
                        .comparing((PermissaoDTO p) -> p.getRecurso() != null ? p.getRecurso() : "ZZZ")
                        .thenComparing(p -> p.getNome() != null ? p.getNome() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    private PermissaoDTO toDTO(Permissao p) {
        PermissaoDTO dto = new PermissaoDTO();
        dto.setId(p.getId());
        dto.setNome(p.getNome());
        dto.setDescricao(p.getDescricao());
        dto.setRecurso(p.getRecurso());
        dto.setAcao(p.getAcao());
        return dto;
    }
}