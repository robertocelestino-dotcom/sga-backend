package com.sga.service;

import com.sga.model.SessaoUsuario;
import com.sga.model.Usuario;
import com.sga.repository.SessaoUsuarioRepository;
import com.sga.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SessaoService {

    @Autowired
    private SessaoUsuarioRepository sessaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public void criarSessao(Long usuarioId, String token, String ip, String userAgent, LocalDateTime expiracao) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // Desativar sessões antigas do mesmo usuário
        sessaoRepository.desativarSessoesPorUsuario(usuarioId);

        SessaoUsuario sessao = new SessaoUsuario();
        sessao.setUsuario(usuario);
        sessao.setToken(token);
        sessao.setIp(ip);
        sessao.setUserAgent(userAgent);
        sessao.setDataExpiracao(expiracao);
        sessao.setAtivo(true);

        sessaoRepository.save(sessao);
    }

    public boolean validarSessao(String token) {
        return sessaoRepository.findByTokenAndAtivoTrue(token)
            .map(sessao -> sessao.getDataExpiracao().isAfter(LocalDateTime.now()))
            .orElse(false);
    }

    @Transactional
    public void encerrarSessao(String token) {
        sessaoRepository.findByTokenAndAtivoTrue(token)
            .ifPresent(sessao -> {
                sessao.setAtivo(false);
                sessaoRepository.save(sessao);
            });
    }

    @Transactional
    public void encerrarSessoesUsuario(Long usuarioId) {
        sessaoRepository.desativarSessoesPorUsuario(usuarioId);
    }

    @Scheduled(cron = "0 0 * * * *") // Executa a cada hora
    @Transactional
    public void removerSessoesExpiradas() {
        sessaoRepository.removerSessoesExpiradas(LocalDateTime.now());
    }

    public List<SessaoUsuario> listarSessoesAtivas(Long usuarioId) {
        return sessaoRepository.findByUsuarioIdAndAtivoTrue(usuarioId);
    }
}