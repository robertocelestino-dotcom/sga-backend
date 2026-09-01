package com.sga.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAuthResponse {
    private String token;
    private String tokenType = "Bearer";
    private UsuarioDTO usuario;
    private List<MenuDTO> menus;
    private List<String> permissoes;
}
