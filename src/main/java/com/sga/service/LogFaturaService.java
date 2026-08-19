// src/main/java/com/sga/service/LogFaturaService.java

package com.sga.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sga.dto.LogFaturaDTO;
import com.sga.model.Fatura;
import com.sga.model.LogFatura;
import com.sga.repository.LogFaturaRepository;

@Service
public class LogFaturaService {

    private static final Logger log = LoggerFactory.getLogger(LogFaturaService.class);

    @Autowired
    private LogFaturaRepository logFaturaRepository;

    @Transactional
    public void adicionarLog(Fatura fatura, String nivel, String mensagem, String passo) {
        try {
            if (fatura == null || fatura.getId() == null) {
                log.debug("⏳ Fatura temporária sem ID - Log não persistido: {}", mensagem);
                return;
            }

            LogFatura logEntity = new LogFatura();
            logEntity.setFatura(fatura);
            logEntity.setNivel(nivel);
            logEntity.setMensagem(mensagem);
            logEntity.setPasso(passo);
            logEntity.setDataHora(LocalDateTime.now());

            logFaturaRepository.save(logEntity);
            log.debug("📝 Log adicionado à fatura {}: [{}] {} - {}", 
                fatura.getId(), nivel, passo, mensagem);

        } catch (Exception e) {
            log.error("❌ Erro ao adicionar log da fatura {}: {}", 
                fatura != null ? fatura.getId() : "null", e.getMessage(), e);
        }
    }

    // 🔥 MÉTODO CORRIGIDO - RETORNA DTO
    @Transactional(readOnly = true)
    public List<LogFaturaDTO> buscarLogsPorFaturaDTO(Long faturaId) {
        try {
            log.debug("📋 Buscando logs da fatura: {}", faturaId);
            List<LogFatura> logs = logFaturaRepository.findByFaturaIdOrderByDataHoraAsc(faturaId);
            
            return logs.stream().map(logEntity -> {
                LogFaturaDTO dto = new LogFaturaDTO();
                dto.setId(logEntity.getId());
                dto.setNivel(logEntity.getNivel());
                dto.setMensagem(logEntity.getMensagem());
                dto.setPasso(logEntity.getPasso());
                dto.setDataHora(logEntity.getDataHora());
                
                // 🔥 PEGAR APENAS O ID E NÚMERO DA FATURA
                if (logEntity.getFatura() != null) {
                    dto.setFaturaId(logEntity.getFatura().getId());
                    dto.setNumeroFatura(logEntity.getFatura().getNumeroFatura());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar logs da fatura {}: {}", faturaId, e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<LogFaturaDTO> buscarLogsPorFaturaENivelDTO(Long faturaId, String nivel) {
        try {
            List<LogFatura> logs = logFaturaRepository.findByFaturaIdAndNivelOrderByDataHoraAsc(faturaId, nivel);
            
            return logs.stream().map(logEntity -> {
                LogFaturaDTO dto = new LogFaturaDTO();
                dto.setId(logEntity.getId());
                dto.setNivel(logEntity.getNivel());
                dto.setMensagem(logEntity.getMensagem());
                dto.setPasso(logEntity.getPasso());
                dto.setDataHora(logEntity.getDataHora());
                
                if (logEntity.getFatura() != null) {
                    dto.setFaturaId(logEntity.getFatura().getId());
                    dto.setNumeroFatura(logEntity.getFatura().getNumeroFatura());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar logs da fatura {} com nível {}: {}", faturaId, nivel, e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<LogFaturaDTO> buscarLogsPorFaturaEPassoDTO(Long faturaId, String passo) {
        try {
            List<LogFatura> logs = logFaturaRepository.findByFaturaIdAndPassoOrderByDataHoraAsc(faturaId, passo);
            
            return logs.stream().map(logEntity -> {
                LogFaturaDTO dto = new LogFaturaDTO();
                dto.setId(logEntity.getId());
                dto.setNivel(logEntity.getNivel());
                dto.setMensagem(logEntity.getMensagem());
                dto.setPasso(logEntity.getPasso());
                dto.setDataHora(logEntity.getDataHora());
                
                if (logEntity.getFatura() != null) {
                    dto.setFaturaId(logEntity.getFatura().getId());
                    dto.setNumeroFatura(logEntity.getFatura().getNumeroFatura());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar logs da fatura {} com passo {}: {}", faturaId, passo, e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<LogFaturaDTO> buscarErrosPorFaturaDTO(Long faturaId) {
        try {
            List<LogFatura> logs = logFaturaRepository.findErrosByFaturaId(faturaId);
            
            return logs.stream().map(logEntity -> {
                LogFaturaDTO dto = new LogFaturaDTO();
                dto.setId(logEntity.getId());
                dto.setNivel(logEntity.getNivel());
                dto.setMensagem(logEntity.getMensagem());
                dto.setPasso(logEntity.getPasso());
                dto.setDataHora(logEntity.getDataHora());
                
                if (logEntity.getFatura() != null) {
                    dto.setFaturaId(logEntity.getFatura().getId());
                    dto.setNumeroFatura(logEntity.getFatura().getNumeroFatura());
                }
                
                return dto;
            }).collect(Collectors.toList());
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar logs de erro da fatura {}: {}", faturaId, e.getMessage(), e);
            return List.of();
        }
    }

    // 🔥 MÉTODOS LEGADO - MANTIDOS PARA COMPATIBILIDADE
    @Transactional(readOnly = true)
    public List<LogFatura> buscarLogsPorFatura(Long faturaId) {
        try {
            return logFaturaRepository.findByFaturaIdOrderByDataHoraAsc(faturaId);
        } catch (Exception e) {
            log.error("❌ Erro ao buscar logs da fatura {}: {}", faturaId, e.getMessage(), e);
            return List.of();
        }
    }

    @Transactional
    public void limparLogs(Long faturaId) {
        try {
            log.info("🗑️ Removendo logs da fatura: {}", faturaId);
            logFaturaRepository.deleteByFaturaId(faturaId);
            log.info("✅ Logs da fatura {} removidos com sucesso", faturaId);
        } catch (Exception e) {
            log.error("❌ Erro ao remover logs da fatura {}: {}", faturaId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public long contarLogsPorFatura(Long faturaId) {
        try {
            return logFaturaRepository.countByFaturaId(faturaId);
        } catch (Exception e) {
            log.error("❌ Erro ao contar logs da fatura {}: {}", faturaId, e.getMessage(), e);
            return 0;
        }
    }
}