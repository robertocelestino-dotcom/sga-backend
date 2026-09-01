// LogAcaoService.java - CORRIGIDO
package com.sga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sga.model.LogAcao;
import com.sga.model.Usuario;
import com.sga.repository.LogAcaoRepository;
import com.sga.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogAcaoService {

    @Autowired
    private LogAcaoRepository logAcaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void registrarAcao(Long usuarioId, String username, String ip,
                              String recurso, String acao, Object detalhes,
                              String status, String mensagem) {
        LogAcao log = new LogAcao();

        // 🔥 CORRIGIDO: Buscar o usuário pelo username se usuarioId for null
        Usuario usuario = null;
        if (usuarioId != null) {
            usuario = usuarioRepository.findById(usuarioId).orElse(null);
        }
        
        // Se não encontrou pelo ID, tentar pelo username
        if (usuario == null && username != null && !username.isEmpty()) {
            usuario = usuarioRepository.findByUsername(username).orElse(null);
        }
        
        // Se ainda não encontrou, criar um usuário "virtual" para o log
        if (usuario == null && username != null) {
            // Não associar a nenhum usuário, apenas usar o username
            log.setUsuario(null);
        } else if (usuario != null) {
            log.setUsuario(usuario);
        } else {
            // Caso extremo: sem usuário e sem username
            log.setUsuario(null);
        }

        log.setUsername(username != null ? username : "SISTEMA");
        log.setIp(ip);
        log.setRecurso(recurso);
        log.setAcao(acao);
        
        // Converter detalhes para String JSON
        String detalhesStr = "{}";
        if (detalhes != null) {
            try {
                if (detalhes instanceof String) {
                    detalhesStr = (String) detalhes;
                } else {
                    detalhesStr = objectMapper.writeValueAsString(detalhes);
                }
            } catch (Exception e) {
                System.err.println("❌ Erro ao converter detalhes: " + e.getMessage());
                detalhesStr = "{\"error\": \"" + e.getMessage() + "\"}";
            }
        }
        log.setDetalhes(detalhesStr);
        
        log.setStatus(status);
        log.setMensagem(mensagem);
        log.setDataHora(LocalDateTime.now());

        logAcaoRepository.save(log);
    }
}