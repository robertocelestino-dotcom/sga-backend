// LogProcessamentoController.java
package com.sga.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class LogProcessamentoController {

    private static final Logger log = LoggerFactory.getLogger(LogProcessamentoController.class);

    @PostMapping("/processamento")
    public ResponseEntity<?> registrarLog(@RequestBody Map<String, Object> logData) {
        log.info("📝 Log de processamento: {}", logData);
        // Aqui você pode salvar no banco se quiser
        return ResponseEntity.ok().build();
    }
}