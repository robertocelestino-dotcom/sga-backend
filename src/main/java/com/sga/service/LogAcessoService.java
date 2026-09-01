package com.sga.service;

import com.sga.dto.LogAcessoDTO;
import com.sga.model.LogAcesso;
import com.sga.model.Usuario;
import com.sga.repository.LogAcessoRepository;
import com.sga.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LogAcessoService {

    @Autowired
    private LogAcessoRepository logAcessoRepository;  // REMOVER 'final'

    @Autowired
    private UsuarioRepository usuarioRepository;  // REMOVER 'final'

    @Transactional
    public void registrarAcesso(Long usuarioId, String username, String ip, 
                                String userAgent, String acao, String status, String mensagem) {
        LogAcesso log = new LogAcesso();
        
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(log::setUsuario);
        }
        
        log.setUsername(username);
        log.setIp(ip);
        log.setUserAgent(userAgent);
        log.setAcao(acao);
        log.setStatus(status);
        log.setMensagem(mensagem);
        log.setDataHora(LocalDateTime.now());
        
        logAcessoRepository.save(log);
    }

    public Page<LogAcessoDTO> listar(Pageable pageable, Long usuarioId, LocalDateTime inicio, LocalDateTime fim) {
        return logAcessoRepository.findByFiltros(usuarioId, inicio, fim, pageable)
            .map(this::toDTO);
    }

    public Page<LogAcessoDTO> listarPorUsuario(Long usuarioId, Pageable pageable) {
        return logAcessoRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId, pageable)
            .map(this::toDTO);
    }

    private LogAcessoDTO toDTO(LogAcesso log) {
        LogAcessoDTO dto = new LogAcessoDTO();
        dto.setId(log.getId());
        dto.setUsuarioId(log.getUsuario() != null ? log.getUsuario().getId() : null);
        dto.setUsername(log.getUsername());
        dto.setDataHora(log.getDataHora());
        dto.setIp(log.getIp());
        dto.setUserAgent(log.getUserAgent());
        dto.setAcao(log.getAcao());
        dto.setStatus(log.getStatus());
        dto.setMensagem(log.getMensagem());
        return dto;
    }
}