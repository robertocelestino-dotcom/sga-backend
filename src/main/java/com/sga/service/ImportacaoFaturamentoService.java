package com.sga.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImportacaoFaturamentoService {

    private static final Logger log = LoggerFactory.getLogger(ImportacaoFaturamentoService.class);

    @Autowired
    private ReguaFaturamentoService reguaService;

    /**
     * Importa notas de débito de acordo com a régua
     */
    public void importarPorRegua(Long reguaId, Integer mes, Integer ano, String usuario) {
        log.info("📥 Importando faturamento para régua: {} - {}/{}", reguaId, mes, ano);
        
        var reguaOpt = reguaService.buscarPorId(reguaId);
        
        if (reguaOpt.isEmpty()) {
            log.error("Régua não encontrada: {}", reguaId);
            return;
        }
        
        var regua = reguaOpt.get();
        var sequencia = regua.getSequenciaArquivosList();
        
        for (String tipoArquivo : sequencia) {
            switch (tipoArquivo) {
                case "CONSOLIDADO":
                    importarConsolidado(mes, ano, usuario);
                    break;
                case "PREVIA_ANTERIOR":
                    importarPreviaAnterior(mes, ano, usuario);
                    break;
                case "PREVIA_CORRENTE":
                    importarPreviaCorrente(mes, ano, usuario);
                    break;
                default:
                    log.warn("Tipo de arquivo desconhecido: {}", tipoArquivo);
            }
        }
    }

    private void importarConsolidado(Integer mes, Integer ano, String usuario) {
        log.info("📄 Importando arquivo CONSOLIDADO - {}/{}", mes, ano);
        // TODO: Implementar lógica de importação
    }

    private void importarPreviaAnterior(Integer mes, Integer ano, String usuario) {
        log.info("📄 Importando arquivo PRÉVIA ANTERIOR - {}/{}", mes, ano);
        // TODO: Implementar lógica de importação
    }

    private void importarPreviaCorrente(Integer mes, Integer ano, String usuario) {
        log.info("📄 Importando arquivo PRÉVIA CORRENTE - {}/{}", mes, ano);
        // TODO: Implementar lógica de importação
    }
}