// src/main/java/com/sga/controller/CancelamentoImportacaoController.java

package com.sga.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sga.dto.CancelamentoImportacaoDTO;
import com.sga.service.CancelamentoImportacaoService;

@RestController
@RequestMapping("/api/cancelamentos")
public class CancelamentoImportacaoController {

    private static final Logger log = LoggerFactory.getLogger(CancelamentoImportacaoController.class);

    @Autowired
    private CancelamentoImportacaoService cancelamentoService;

    @PostMapping("/importar")
    public ResponseEntity<List<CancelamentoImportacaoDTO>> importarCancelamentos(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("mes") Integer mes,
            @RequestParam("ano") Integer ano,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        
        log.info("📥 Importando cancelamentos - {}/{}", mes, ano);
        
        String nomeArquivo = arquivo.getOriginalFilename();
        Long idImportacao = System.currentTimeMillis();
        
        List<CancelamentoImportacaoDTO> resultado = cancelamentoService.importarCancelamentos(
            arquivo, mes, ano, usuario, idImportacao, nomeArquivo);
        
        return ResponseEntity.ok(resultado);
    }
    
    /**
     * 🔥 Desfaz importação - Remove todos os cancelamentos do período
     */
    @DeleteMapping("/desfazer/{mes}/{ano}")
    public ResponseEntity<Void> desfazerImportacao(
            @PathVariable Integer mes,
            @PathVariable Integer ano,
            @RequestHeader(value = "X-Usuario", defaultValue = "SISTEMA") String usuario) {
        
        log.info("🗑️ Desfazendo importação de cancelamentos - {}/{}", mes, ano);
        
        cancelamentoService.desfazerImportacao(mes, ano, usuario);
        
        return ResponseEntity.ok().build();
    }
    
    
}