// src/main/java/com/sga/repository/notificacao/NotificacaoSumarizadaRepository.java
package com.sga.repository.notificacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.sga.model.notificacao.NotificacaoSumarizada;

@Repository
public class NotificacaoSumarizadaRepository {

    private static final Logger log = LoggerFactory.getLogger(NotificacaoSumarizadaRepository.class);

    @PersistenceContext(unitName = "notificacao")
    @Qualifier("notificacaoEntityManager")
    private EntityManager entityManager;

    // ========== MÉTODOS DE BUSCA ==========

    /**
     * 🔥 BUSCA NOTIFICAÇÕES SUMARIZADAS POR PERÍODO
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesSumarizadas(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        log.info("🔍 Buscando notificações sumarizadas - DataInicio: {}, DataFim: {}, Codigo: {}", 
                dataInicio, dataFim, codigoAssociado);

        try {
            String sql = buildSqlSumarizado();

            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, dataInicio)
                    .setParameter(2, dataFim)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📊 Encontrados {} registros no MS-SQL", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizada(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações do MS-SQL: {}", e.getMessage(), e);
            log.warn("⚠️ Retornando lista vazia - MS-SQL indisponível ou erro na consulta");
            return new ArrayList<>();
        }
    }

    /**
     * 🔥 BUSCA NOTIFICAÇÕES AGRUPADAS POR MÊS/ANO
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesAgrupadasPorMesAno(
            Integer mes, Integer ano, String codigoAssociado) {

        log.info("📊 Buscando notificações agrupadas por MÊS/ANO - {}/{} - Código: {}", mes, ano, codigoAssociado);

        try {
            String sql = buildSqlAgrupadoMesAno();

            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, mes)
                    .setParameter(2, ano)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📊 Encontrados {} registros agrupados", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizadaAgrupada(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações agrupadas: {}", e.getMessage(), e);
            log.warn("⚠️ Retornando lista vazia - MS-SQL indisponível ou erro na consulta");
            return new ArrayList<>();
        }
    }

    /**
     * 🔥 BUSCA NOTIFICAÇÕES AGRUPADAS POR PERÍODO (LOCALDATE)
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesAgrupadasPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        log.info("📊 Buscando notificações agrupadas por PERÍODO - Início: {}, Fim: {}, Código: {}", 
                dataInicio, dataFim, codigoAssociado);

        try {
            String sql = buildSqlAgrupadoPeriodo();

            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, dataInicio)
                    .setParameter(2, dataFim)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📊 Encontrados {} registros agrupados por período", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizadaAgrupada(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações agrupadas por período: {}", e.getMessage(), e);
            log.warn("⚠️ Retornando lista vazia - MS-SQL indisponível ou erro na consulta");
            return new ArrayList<>();
        }
    }

    /**
     * 🔥 BUSCA NOTIFICAÇÕES DETALHADAS POR ASSOCIADO
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesDetalhadasPorAssociado(
            Integer mes, Integer ano, String codigoAssociado) {

        log.info("📋 Buscando notificações detalhadas - {}/{} - Código: {}", mes, ano, codigoAssociado);

        try {
            String sql = buildSqlDetalhado();

            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, mes)
                    .setParameter(2, ano)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📋 Encontrados {} registros detalhados", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizada(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações detalhadas: {}", e.getMessage(), e);
            log.warn("⚠️ Retornando lista vazia - MS-SQL indisponível ou erro na consulta");
            return new ArrayList<>();
        }
    }

    // ========== BUILD SQL ==========

    /**
     * 🔥 SQL SUMARIZADO POR PERÍODO
     */
    private String buildSqlSumarizado() {
        return "WITH RemessasFiltradas AS ( " +
                "    SELECT RM.ID, RM.DATAHORA, RM.QUANTIDADEREGISTRO, RM.ARQUIVO " +
                "    FROM TNOTIFICACAOREMESSA RM WITH (NOLOCK) " +
                "    WHERE RM.DATAHORA >= ?1 AND RM.DATAHORA < DATEADD(DAY, 1, ?2) " +
                "      AND RM.ARQUIVO NOT LIKE 'CC%' " +
                "      AND (?3 = '' OR RM.ARQUIVO LIKE '%' + ?3 + '%' OR RM.ARQUIVO LIKE '%000' + ?3 + '%' OR RM.ARQUIVO LIKE '%00' + ?3 + '%') " +
                "), NotificacoesDetalhadas AS ( " +
                "    SELECT RF.ID AS IDREMESSA, RF.DATAHORA, RF.QUANTIDADEREGISTRO, " +
                "           RE.CODIGODOASSOCIADO, RE.NOMEDEFANTASIAASSOCIADO, " +
                "           CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND ISNULL(NR.SMSENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END AS SMS_SEM_ENR, " +
                "           CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND NR.SMSENRIQUECIDO = 'S' THEN 1 ELSE 0 END AS SMS_COM_ENR, " +
                "           CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND ISNULL(NR.EMAILENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END AS EMAIL_SEM_ENR, " +
                "           CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND NR.EMAILENRIQUECIDO = 'S' THEN 1 ELSE 0 END AS EMAIL_COM_ENR, " +
                "           CASE WHEN NR.STATUSEMAIL = 'E' AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END AS CARTA_ENV, " +
                "           CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NULL AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END AS NAO_ENVIADA " +
                "    FROM RemessasFiltradas RF " +
                "    INNER JOIN TNOTIFICACAOREMESSA_ENVIO RE WITH (NOLOCK) ON RE.IDREMESSA = RF.ID " +
                "    INNER JOIN TNOTIFICACAOREMESSA_RETORNO NR WITH (NOLOCK) ON NR.IDREMESSA = RE.IDREMESSA AND NR.IDENTIFICADORNOTIFICACAO = RE.IDENTIFICADORREGISTRO " +
                "    WHERE (?3 = '' OR CONVERT(INTEGER, RE.CODIGODOASSOCIADO) = CONVERT(INTEGER, ?3)) " +
                ") " +
                "SELECT FORMAT(ND.DATAHORA, 'MM/yyyy') AS COMPETENCIA, " +
                "       CONVERT(VARCHAR(10), ND.DATAHORA, 103) AS DATA_MOVIMENTO, " +
                "       CONVERT(INTEGER, ND.CODIGODOASSOCIADO) AS CODIGO_ASSOCIADO, " +
                "       ND.NOMEDEFANTASIAASSOCIADO, " +
                "       ND.QUANTIDADEREGISTRO AS TOTAL_REGISTROS_DIGITAL, " +
                "       SUM(ND.SMS_SEM_ENR) AS SMS_SEM_ENR, " +
                "       SUM(ND.SMS_COM_ENR) AS SMS_COM_ENR, " +
                "       SUM(ND.SMS_SEM_ENR + ND.SMS_COM_ENR) AS TOTAL_SMS, " +
                "       SUM(ND.EMAIL_SEM_ENR) AS EMAIL_SEM_ENR, " +
                "       SUM(ND.EMAIL_COM_ENR) AS EMAIL_COM_ENR, " +
                "       SUM(ND.EMAIL_SEM_ENR + ND.EMAIL_COM_ENR) AS TOTAL_EMAIL, " +
                "       SUM(ND.CARTA_ENV) AS CARTAS_ENVIADAS, " +
                "       SUM(ND.NAO_ENVIADA) AS NAO_ENVIADA " +
                "FROM NotificacoesDetalhadas ND " +
                "GROUP BY ND.DATAHORA, ND.CODIGODOASSOCIADO, ND.NOMEDEFANTASIAASSOCIADO, ND.QUANTIDADEREGISTRO " +
                "ORDER BY ND.NOMEDEFANTASIAASSOCIADO, ND.DATAHORA";
    }

    /**
     * 🔥 SQL AGRUPADO POR MÊS/ANO
     */
    private String buildSqlAgrupadoMesAno() {
        return "WITH RemessasFiltradas AS ( " +
                "    SELECT DISTINCT RM.ID, RM.DATAHORA, RM.QUANTIDADEREGISTRO, RM.ARQUIVO " +
                "    FROM TNOTIFICACAOREMESSA RM WITH (NOLOCK) " +
                "    WHERE MONTH(RM.DATAHORA) = ?1 " +
                "      AND YEAR(RM.DATAHORA) = ?2 " +
                "      AND RM.ARQUIVO NOT LIKE 'CC%' " +
                "      AND (?3 = '' OR RM.ARQUIVO LIKE '%' + ?3 + '%' OR RM.ARQUIVO LIKE '%000' + ?3 + '%' OR RM.ARQUIVO LIKE '%00' + ?3 + '%') " +
                "), NotificacoesDetalhadas AS ( " +
                "    SELECT RF.ID AS IDREMESSA, RF.DATAHORA, RF.QUANTIDADEREGISTRO, " +
                "           RE.CODIGODOASSOCIADO, RE.NOMEDEFANTASIAASSOCIADO, " +
                "           SUM(CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND ISNULL(NR.SMSENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END) AS SMS_SEM_ENR, " +
                "           SUM(CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND NR.SMSENRIQUECIDO = 'S' THEN 1 ELSE 0 END) AS SMS_COM_ENR, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND ISNULL(NR.EMAILENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END) AS EMAIL_SEM_ENR, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND NR.EMAILENRIQUECIDO = 'S' THEN 1 ELSE 0 END) AS EMAIL_COM_ENR, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'E' AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END) AS CARTA_ENV, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NULL AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END) AS NAO_ENVIADA " +
                "    FROM RemessasFiltradas RF " +
                "    INNER JOIN TNOTIFICACAOREMESSA_ENVIO RE WITH (NOLOCK) ON RE.IDREMESSA = RF.ID " +
                "    LEFT JOIN TNOTIFICACAOREMESSA_RETORNO NR WITH (NOLOCK) ON NR.IDREMESSA = RE.IDREMESSA AND NR.IDENTIFICADORNOTIFICACAO = RE.IDENTIFICADORREGISTRO " +
                "    WHERE (?3 = '' OR CONVERT(INTEGER, RE.CODIGODOASSOCIADO) = CONVERT(INTEGER, ?3)) " +
                "    GROUP BY RF.ID, RF.DATAHORA, RF.QUANTIDADEREGISTRO, RE.CODIGODOASSOCIADO, RE.NOMEDEFANTASIAASSOCIADO " +
                ") " +
                "SELECT CAST(?1 AS VARCHAR) + '/' + CAST(?2 AS VARCHAR) AS COMPETENCIA, " +
                "       '' AS DATA_MOVIMENTO, " +
                "       CONVERT(INTEGER, ND.CODIGODOASSOCIADO) AS CODIGO_ASSOCIADO, " +
                "       ND.NOMEDEFANTASIAASSOCIADO, " +
                "       SUM(ND.QUANTIDADEREGISTRO) AS TOTAL_REGISTROS_DIGITAL, " +
                "       SUM(ND.SMS_SEM_ENR) AS SMS_SEM_ENR, " +
                "       SUM(ND.SMS_COM_ENR) AS SMS_COM_ENR, " +
                "       SUM(ND.SMS_SEM_ENR + ND.SMS_COM_ENR) AS TOTAL_SMS, " +
                "       SUM(ND.EMAIL_SEM_ENR) AS EMAIL_SEM_ENR, " +
                "       SUM(ND.EMAIL_COM_ENR) AS EMAIL_COM_ENR, " +
                "       SUM(ND.EMAIL_SEM_ENR + ND.EMAIL_COM_ENR) AS TOTAL_EMAIL, " +
                "       SUM(ND.CARTA_ENV) AS CARTAS_ENVIADAS, " +
                "       SUM(ND.NAO_ENVIADA) AS NAO_ENVIADA " +
                "FROM NotificacoesDetalhadas ND " +
                "GROUP BY ND.CODIGODOASSOCIADO, ND.NOMEDEFANTASIAASSOCIADO " +
                "ORDER BY ND.NOMEDEFANTASIAASSOCIADO";
    }

    /**
     * 🔥 SQL AGRUPADO POR PERÍODO
     */
    private String buildSqlAgrupadoPeriodo() {
        return "WITH RemessasFiltradas AS ( " +
                "    SELECT DISTINCT RM.ID, RM.DATAHORA, RM.QUANTIDADEREGISTRO, RM.ARQUIVO " +
                "    FROM TNOTIFICACAOREMESSA RM WITH (NOLOCK) " +
                "    WHERE RM.DATAHORA >= ?1 AND RM.DATAHORA < DATEADD(DAY, 1, ?2) " +
                "      AND RM.ARQUIVO NOT LIKE 'CC%' " +
                "      AND (?3 = '' OR RM.ARQUIVO LIKE '%' + ?3 + '%' OR RM.ARQUIVO LIKE '%000' + ?3 + '%' OR RM.ARQUIVO LIKE '%00' + ?3 + '%') " +
                "), NotificacoesDetalhadas AS ( " +
                "    SELECT RF.ID AS IDREMESSA, RF.DATAHORA, RF.QUANTIDADEREGISTRO, " +
                "           RE.CODIGODOASSOCIADO, RE.NOMEDEFANTASIAASSOCIADO, " +
                "           SUM(CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND ISNULL(NR.SMSENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END) AS SMS_SEM_ENR, " +
                "           SUM(CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND NR.SMSENRIQUECIDO = 'S' THEN 1 ELSE 0 END) AS SMS_COM_ENR, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND ISNULL(NR.EMAILENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END) AS EMAIL_SEM_ENR, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND NR.EMAILENRIQUECIDO = 'S' THEN 1 ELSE 0 END) AS EMAIL_COM_ENR, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'E' AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END) AS CARTA_ENV, " +
                "           SUM(CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NULL AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END) AS NAO_ENVIADA " +
                "    FROM RemessasFiltradas RF " +
                "    INNER JOIN TNOTIFICACAOREMESSA_ENVIO RE WITH (NOLOCK) ON RE.IDREMESSA = RF.ID " +
                "    LEFT JOIN TNOTIFICACAOREMESSA_RETORNO NR WITH (NOLOCK) ON NR.IDREMESSA = RE.IDREMESSA AND NR.IDENTIFICADORNOTIFICACAO = RE.IDENTIFICADORREGISTRO " +
                "    WHERE (?3 = '' OR CONVERT(INTEGER, RE.CODIGODOASSOCIADO) = CONVERT(INTEGER, ?3)) " +
                "    GROUP BY RF.ID, RF.DATAHORA, RF.QUANTIDADEREGISTRO, RE.CODIGODOASSOCIADO, RE.NOMEDEFANTASIAASSOCIADO " +
                ") " +
                "SELECT FORMAT(ND.DATAHORA, 'MM/yyyy') AS COMPETENCIA, " +
                "       CONVERT(VARCHAR(10), ND.DATAHORA, 103) AS DATA_MOVIMENTO, " +
                "       CONVERT(INTEGER, ND.CODIGODOASSOCIADO) AS CODIGO_ASSOCIADO, " +
                "       ND.NOMEDEFANTASIAASSOCIADO, " +
                "       ND.QUANTIDADEREGISTRO AS TOTAL_REGISTROS_DIGITAL, " +
                "       ND.SMS_SEM_ENR AS SMS_SEM_ENR, " +
                "       ND.SMS_COM_ENR AS SMS_COM_ENR, " +
                "       ND.SMS_SEM_ENR + ND.SMS_COM_ENR AS TOTAL_SMS, " +
                "       ND.EMAIL_SEM_ENR AS EMAIL_SEM_ENR, " +
                "       ND.EMAIL_COM_ENR AS EMAIL_COM_ENR, " +
                "       ND.EMAIL_SEM_ENR + ND.EMAIL_COM_ENR AS TOTAL_EMAIL, " +
                "       ND.CARTA_ENV AS CARTAS_ENVIADAS, " +
                "       ND.NAO_ENVIADA AS NAO_ENVIADA " +
                "FROM NotificacoesDetalhadas ND " +
                "ORDER BY ND.DATAHORA";
    }

    /**
     * 🔥 SQL DETALHADO POR ASSOCIADO
     */
    private String buildSqlDetalhado() {
        return "WITH RemessasFiltradas AS ( " +
                "    SELECT RM.ID, RM.DATAHORA, RM.QUANTIDADEREGISTRO, RM.ARQUIVO " +
                "    FROM TNOTIFICACAOREMESSA RM WITH (NOLOCK) " +
                "    WHERE MONTH(RM.DATAHORA) = ?1 " +
                "      AND YEAR(RM.DATAHORA) = ?2 " +
                "      AND RM.ARQUIVO NOT LIKE 'CC%' " +
                "      AND (?3 = '' OR RM.ARQUIVO LIKE '%' + ?3 + '%' OR RM.ARQUIVO LIKE '%000' + ?3 + '%' OR RM.ARQUIVO LIKE '%00' + ?3 + '%') " +
                "), NotificacoesDetalhadas AS ( " +
                "    SELECT RF.ID AS IDREMESSA, RF.DATAHORA, RF.QUANTIDADEREGISTRO, " +
                "           RE.CODIGODOASSOCIADO, RE.NOMEDEFANTASIAASSOCIADO, " +
                "           CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND ISNULL(NR.SMSENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END AS SMS_SEM_ENR, " +
                "           CASE WHEN NR.SMSENVIADO = 'S' AND NR.STATUSSMS = 'N' AND NR.SMSENRIQUECIDO = 'S' THEN 1 ELSE 0 END AS SMS_COM_ENR, " +
                "           CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND ISNULL(NR.EMAILENRIQUECIDO, 'N') = 'N' THEN 1 ELSE 0 END AS EMAIL_SEM_ENR, " +
                "           CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NOT NULL AND NR.EMAILENRIQUECIDO = 'S' THEN 1 ELSE 0 END AS EMAIL_COM_ENR, " +
                "           CASE WHEN NR.STATUSEMAIL = 'E' AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END AS CARTA_ENV, " +
                "           CASE WHEN NR.STATUSEMAIL = 'N' AND NR.DATAENVIOEMAIL IS NULL AND NR.STATUSSMS = 'E' THEN 1 ELSE 0 END AS NAO_ENVIADA " +
                "    FROM RemessasFiltradas RF " +
                "    INNER JOIN TNOTIFICACAOREMESSA_ENVIO RE WITH (NOLOCK) ON RE.IDREMESSA = RF.ID " +
                "    INNER JOIN TNOTIFICACAOREMESSA_RETORNO NR WITH (NOLOCK) ON NR.IDREMESSA = RE.IDREMESSA AND NR.IDENTIFICADORNOTIFICACAO = RE.IDENTIFICADORREGISTRO " +
                "    WHERE (?3 = '' OR CONVERT(INTEGER, RE.CODIGODOASSOCIADO) = CONVERT(INTEGER, ?3)) " +
                ") " +
                "SELECT FORMAT(ND.DATAHORA, 'MM/yyyy') AS COMPETENCIA, " +
                "       CONVERT(VARCHAR(10), ND.DATAHORA, 103) AS DATA_MOVIMENTO, " +
                "       CONVERT(INTEGER, ND.CODIGODOASSOCIADO) AS CODIGO_ASSOCIADO, " +
                "       ND.NOMEDEFANTASIAASSOCIADO, " +
                "       ND.QUANTIDADEREGISTRO AS TOTAL_REGISTROS_DIGITAL, " +
                "       ND.SMS_SEM_ENR AS SMS_SEM_ENR, " +
                "       ND.SMS_COM_ENR AS SMS_COM_ENR, " +
                "       ND.SMS_SEM_ENR + ND.SMS_COM_ENR AS TOTAL_SMS, " +
                "       ND.EMAIL_SEM_ENR AS EMAIL_SEM_ENR, " +
                "       ND.EMAIL_COM_ENR AS EMAIL_COM_ENR, " +
                "       ND.EMAIL_SEM_ENR + ND.EMAIL_COM_ENR AS TOTAL_EMAIL, " +
                "       ND.CARTA_ENV AS CARTAS_ENVIADAS, " +
                "       ND.NAO_ENVIADA AS NAO_ENVIADA " +
                "FROM NotificacoesDetalhadas ND " +
                "ORDER BY ND.DATAHORA";
    }

    // ========== CONVERSÃO ==========

    private NotificacaoSumarizada toNotificacaoSumarizada(Object[] row) {
        NotificacaoSumarizada dto = new NotificacaoSumarizada();
        int idx = 0;

        dto.setCompetencia((String) row[idx]);
        idx++;

        String dataStr = (String) row[idx];
        if (dataStr != null && !dataStr.isEmpty()) {
            try {
                java.time.format.DateTimeFormatter formatter = 
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
                dto.setDataMovimento(java.time.LocalDate.parse(dataStr, formatter));
            } catch (Exception e) {
                log.warn("⚠️ Não foi possível parsear a data: {}", dataStr);
                dto.setDataMovimento(null);
            }
        } else {
            dto.setDataMovimento(null);
        }
        idx++;

        dto.setCodigoAssociado(row[idx] != null ? ((Number) row[idx]).intValue() : null);
        idx++;
        dto.setNomeAssociado((String) row[idx]);
        idx++;
        dto.setTotalRegistrosDigital(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setSmsSemEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setSmsComEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setTotalSms(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setEmailsSemEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setEmailsComEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setTotalEmail(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setCartasEnviadas(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setNaoEnviada(row[idx] != null ? ((Number) row[idx]).intValue() : 0);

        return dto;
    }

    private NotificacaoSumarizada toNotificacaoSumarizadaAgrupada(Object[] row) {
        NotificacaoSumarizada dto = new NotificacaoSumarizada();
        int idx = 0;

        dto.setCompetencia((String) row[idx]);
        idx++;
        dto.setDataMovimento(null);
        idx++;
        dto.setCodigoAssociado(row[idx] != null ? ((Number) row[idx]).intValue() : null);
        idx++;
        dto.setNomeAssociado((String) row[idx]);
        idx++;
        dto.setTotalRegistrosDigital(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setSmsSemEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setSmsComEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setTotalSms(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setEmailsSemEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setEmailsComEnriquecimento(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setTotalEmail(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setCartasEnviadas(row[idx] != null ? ((Number) row[idx]).intValue() : 0);
        idx++;
        dto.setNaoEnviada(row[idx] != null ? ((Number) row[idx]).intValue() : 0);

        return dto;
    }
}