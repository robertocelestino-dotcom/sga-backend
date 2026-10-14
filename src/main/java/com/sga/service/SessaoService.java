package com.sga.service;

import com.sga.model.SessaoUsuario;
import com.sga.model.Usuario;
import com.sga.repository.SessaoUsuarioRepository;
import com.sga.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SessaoService {

    private static final Logger logger = LoggerFactory.getLogger(SessaoService.class);

    @Autowired
    private SessaoUsuarioRepository sessaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ============================================================
    // CRIAR SESSÃO — SEM matar as anteriores (múltiplas sessões)
    // ============================================================
    @Transactional
    public SessaoUsuario criarSessao(Long usuarioId, String token, String ip,
                                     String userAgent, LocalDateTime expiracao) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 🔥 NÃO desativamos sessões anteriores — múltiplas sessões permitidas
        SessaoUsuario sessao = new SessaoUsuario();
        sessao.setUsuario(usuario);
        sessao.setToken(token);
        sessao.setIp(ip);
        sessao.setUserAgent(userAgent);
        sessao.setDataCriacao(LocalDateTime.now());
        sessao.setDataExpiracao(expiracao);
        sessao.setAtivo(true);

        SessaoUsuario salva = sessaoRepository.save(sessao);
        logger.info("🔐 Sessão criada: usuário={} | sessãoId={}", usuario.getUsername(), salva.getId());
        return salva;
    }

    // ============================================================
    // VALIDAR SESSÃO — checa se o token está ativo e não expirou
    // ============================================================
    @Transactional(readOnly = true)
    public boolean validarSessao(String token) {
        return sessaoRepository.findByTokenAndAtivoTrue(token)
                .map(sessao -> {
                    if (sessao.getDataExpiracao() == null) return false;
                    return sessao.getDataExpiracao().isAfter(LocalDateTime.now());
                })
                .orElse(false);
    }

    // ============================================================
    // ENCERRAR SESSÃO ESPECÍFICA (logout normal)
    // ============================================================
    @Transactional
    public void encerrarSessao(String token) {
        sessaoRepository.findByTokenAndAtivoTrue(token)
                .ifPresent(sessao -> {
                    sessao.setAtivo(false);
                    sessaoRepository.save(sessao);
                    logger.info("🚪 Sessão encerrada: sessãoId={}", sessao.getId());
                });
    }

    // ============================================================
    // ENCERRAR TODAS AS SESSÕES DO USUÁRIO (logout forçado)
    // ============================================================
    @Transactional
    public void encerrarSessoesUsuario(Long usuarioId) {
        sessaoRepository.desativarSessoesPorUsuario(usuarioId);
        logger.info("🚪 Todas as sessões encerradas: usuárioId={}", usuarioId);
    }

    // ============================================================
    // SCHEDULER — remove sessões expiradas (1x por dia, 3h da manhã)
    // ============================================================
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void removerSessoesExpiradas() {
        try {
            LocalDateTime agora = LocalDateTime.now();
            long totalAntes = sessaoRepository.count();
            sessaoRepository.removerSessoesExpiradas(agora);
            long totalDepois = sessaoRepository.count();
            logger.info("🧹 Limpeza de sessões: removidas {} linhas expiradas", totalAntes - totalDepois);
        } catch (Exception e) {
            logger.error("❌ Erro ao remover sessões expiradas: {}", e.getMessage(), e);
        }
    }

    // ============================================================
    // LISTAR SESSÕES ATIVAS (para eventual uso futuro)
    // ============================================================
    @Transactional(readOnly = true)
    public List<SessaoUsuario> listarSessoesAtivas(Long usuarioId) {
        return sessaoRepository.findByUsuarioIdAndAtivoTrue(usuarioId);
    }

    // ============================================================
    // BUSCAR SESSÃO POR TOKEN
    // ============================================================
    @Transactional(readOnly = true)
    public Optional<SessaoUsuario> buscarPorToken(String token) {
        return sessaoRepository.findByTokenAndAtivoTrue(token);
    }
    
}