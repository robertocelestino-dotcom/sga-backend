// src/main/java/com/sga/repository/notificacao/NotificacaoSumarizadaRepository.java

package com.sga.repository.notificacao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // 🔥 ENTITY MANAGER PARA MS-SQL (BANCO DE NOTIFICAÇÕES)
    @PersistenceContext(unitName = "notificacao")
    @Qualifier("notificacaoEntityManager")
    private EntityManager entityManagerMSSQL;

    // 🔥 ENTITY MANAGER PARA POSTGRESQL (BANCO LOCAL)
    @PersistenceContext
    @Qualifier("entityManager")  // OU O NOME DO SEU entityManager PADRÃO
    private EntityManager entityManagerPostgres;

    // ========== BUSCA NO MS-SQL ==========

    /**
     * 🔥 BUSCA NOTIFICAÇÕES AGRUPADAS NO MS-SQL - PARA EXIBIÇÃO NA GRID
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesAgrupadasMSSQL(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        if (dataInicio == null || dataFim == null) {
            log.error("❌ Datas são obrigatórias para busca no MS-SQL");
            return new ArrayList<>();
        }

        log.info("📊 BUSCANDO AGRUPADO NO MS-SQL - Início: {}, Fim: {}, Código: {}", 
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), codigoAssociado);

        try {
            String sql = buildSqlMSSQLAgrupado();

            List<Object[]> results = entityManagerMSSQL.createNativeQuery(sql)
                    .setParameter(1, dataInicio)
                    .setParameter(2, dataFim)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📊 Encontrados {} registros agrupados no MS-SQL", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizadaAgrupado(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações agrupadas do MS-SQL: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🔥 BUSCA NOTIFICAÇÕES NO MS-SQL (DETALHADO)
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesMSSQL(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        if (dataInicio == null || dataFim == null) {
            log.error("❌ Datas são obrigatórias para busca no MS-SQL");
            return new ArrayList<>();
        }

        log.info("🔄 BUSCANDO DETALHADO NO MS-SQL - Início: {}, Fim: {}, Código: {}", 
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), codigoAssociado);

        try {
            String sql = buildSqlMSSQL();

            List<Object[]> results = entityManagerMSSQL.createNativeQuery(sql)
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
            return new ArrayList<>();
        }
    }

    // ========== BUSCA NA TABELA LOCAL (POSTGRESQL) ==========

    /**
     * 🔥 BUSCA NOTIFICAÇÕES NA TABELA LOCAL (POSTGRESQL)
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesLocal(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {

        if (dataInicio == null || dataFim == null) {
            log.error("❌ Datas são obrigatórias para busca");
            return new ArrayList<>();
        }

        log.info("📊 BUSCANDO NA TABELA LOCAL (POSTGRESQL) - Início: {}, Fim: {}, Código: {}", 
                dataInicio.format(DATE_FORMATTER), dataFim.format(DATE_FORMATTER), codigoAssociado);

        try {
            // 🔥 USAR ENTITY MANAGER DO POSTGRESQL
            String sql = buildSqlBuscaLocal();

            List<Object[]> results = entityManagerPostgres.createNativeQuery(sql)
                    .setParameter(1, dataInicio)
                    .setParameter(2, dataFim)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📊 Encontrados {} registros na tabela LOCAL (PostgreSQL)", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizadaLocal(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações da tabela LOCAL (PostgreSQL): {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🔥 BUSCA NOTIFICAÇÕES NA TABELA LOCAL POR MÊS/ANO
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesAgrupadasPorMesAno(
            Integer mes, Integer ano, String codigoAssociado) {

        if (mes == null || ano == null) {
            log.error("❌ Mês e ano são obrigatórios");
            return new ArrayList<>();
        }

        log.info("📊 BUSCANDO NA TABELA LOCAL POR MÊS/ANO - {}/{} - Código: {}", mes, ano, codigoAssociado);

        try {
            // 🔥 USAR ENTITY MANAGER DO POSTGRESQL
            String sql = buildSqlBuscaLocalMesAno();

            List<Object[]> results = entityManagerPostgres.createNativeQuery(sql)
                    .setParameter(1, mes)
                    .setParameter(2, ano)
                    .setParameter(3, codigoAssociado != null ? codigoAssociado : "")
                    .getResultList();

            log.info("📊 Encontrados {} registros na tabela LOCAL (PostgreSQL)", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizadaLocal(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações da tabela LOCAL (PostgreSQL): {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 🔥 BUSCA NOTIFICAÇÕES NA TABELA LOCAL - DETALHADO POR ASSOCIADO
     */
    @SuppressWarnings("unchecked")
    public List<NotificacaoSumarizada> buscarNotificacoesDetalhadasPorAssociado(
            Integer mes, Integer ano, String codigoAssociado) {

        if (mes == null || ano == null) {
            log.error("❌ Mês e ano são obrigatórios");
            return new ArrayList<>();
        }

        if (codigoAssociado == null || codigoAssociado.isEmpty()) {
            log.warn("⚠️ Código do associado é obrigatório");
            return new ArrayList<>();
        }

        log.info("📋 BUSCANDO NA TABELA LOCAL DETALHADO - {}/{} - Código: {}", mes, ano, codigoAssociado);

        try {
            // 🔥 USAR ENTITY MANAGER DO POSTGRESQL
            String sql = buildSqlBuscaLocalDetalhado();

            List<Object[]> results = entityManagerPostgres.createNativeQuery(sql)
                    .setParameter(1, mes)
                    .setParameter(2, ano)
                    .setParameter(3, codigoAssociado)
                    .getResultList();

            log.info("📋 Encontrados {} registros detalhados", results.size());

            List<NotificacaoSumarizada> lista = new ArrayList<>();
            for (Object[] row : results) {
                lista.add(toNotificacaoSumarizadaLocal(row));
            }
            return lista;

        } catch (Exception e) {
            log.error("❌ Erro ao buscar notificações detalhadas: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    // ========== MÉTODOS LEGADO (REDIRECIONAM PARA OS CORRETOS) ==========

    public List<NotificacaoSumarizada> buscarNotificacoesSumarizadas(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {
        return buscarNotificacoesMSSQL(dataInicio, dataFim, codigoAssociado);
    }

    public List<NotificacaoSumarizada> buscarNotificacoesAgrupadasPorPeriodo(
            LocalDate dataInicio, LocalDate dataFim, String codigoAssociado) {
        return buscarNotificacoesLocal(dataInicio, dataFim, codigoAssociado);
    }

    // ========== BUILD SQL PARA MS-SQL ==========

    private String buildSqlMSSQLAgrupado() {
        return "WITH RemessasFiltradas AS ( " +
                "    SELECT DISTINCT RM.ID, RM.DATAHORA, RM.QUANTIDADEREGISTRO, RM.ARQUIVO " +
                "    FROM TNOTIFICACAOREMESSA RM WITH (NOLOCK) " +
                "    WHERE RM.DATAHORA >= CAST(?1 AS DATE) " +
                "      AND RM.DATAHORA < DATEADD(DAY, 1, CAST(?2 AS DATE)) " +
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

    private String buildSqlMSSQL() {
        return "WITH RemessasFiltradas AS ( " +
                "    SELECT RM.ID, RM.DATAHORA, RM.QUANTIDADEREGISTRO, RM.ARQUIVO " +
                "    FROM TNOTIFICACAOREMESSA RM WITH (NOLOCK) " +
                "    WHERE RM.DATAHORA >= CAST(?1 AS DATE) " +
                "      AND RM.DATAHORA < DATEADD(DAY, 1, CAST(?2 AS DATE)) " +
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

    // ========== BUILD SQL PARA POSTGRESQL ==========

    private String buildSqlBuscaLocal() {
        return "SELECT " +
               "   CAST(CONCAT(n.mes_referencia, '/', n.ano_referencia) AS VARCHAR) AS competencia, " +
               "   n.periodo_fim AS data_movimento, " +
               "   CAST(n.codigo_spc AS INTEGER) AS codigo_associado, " +
               "   a.nome_razao AS nome_associado, " +
               "   n.total_registros AS total_registros, " +
               "   n.sms_sem_enriquecimento, " +
               "   n.sms_com_enriquecimento, " +
               "   n.sms_total, " +
               "   n.emails_sem_enriquecimento, " +
               "   n.emails_com_enriquecimento, " +
               "   n.emails_total, " +
               "   n.cartas_total, " +
               "   n.nao_enviadas " +
               "FROM notificacao_associado n " +
               "LEFT JOIN tb_associado a ON n.associado_id = a.id " +
               "WHERE n.periodo_inicio >= ?1 " +
               "  AND n.periodo_fim <= ?2 " +
               "  AND (?3 = '' OR n.codigo_spc = ?3) " +
               "ORDER BY n.periodo_fim DESC, a.nome_razao";
    }

    private String buildSqlBuscaLocalMesAno() {
        return "SELECT " +
               "   CAST(CONCAT(n.mes_referencia, '/', n.ano_referencia) AS VARCHAR) AS competencia, " +
               "   n.periodo_fim AS data_movimento, " +
               "   CAST(n.codigo_spc AS INTEGER) AS codigo_associado, " +
               "   a.nome_razao AS nome_associado, " +
               "   SUM(n.total_registros) AS total_registros, " +
               "   SUM(n.sms_sem_enriquecimento) AS sms_sem_enriquecimento, " +
               "   SUM(n.sms_com_enriquecimento) AS sms_com_enriquecimento, " +
               "   SUM(n.sms_total) AS sms_total, " +
               "   SUM(n.emails_sem_enriquecimento) AS emails_sem_enriquecimento, " +
               "   SUM(n.emails_com_enriquecimento) AS emails_com_enriquecimento, " +
               "   SUM(n.emails_total) AS emails_total, " +
               "   SUM(n.cartas_total) AS cartas_total, " +
               "   SUM(n.nao_enviadas) AS nao_enviadas " +
               "FROM notificacao_associado n " +
               "LEFT JOIN tb_associado a ON n.associado_id = a.id " +
               "WHERE n.mes_referencia = ?1 " +
               "  AND n.ano_referencia = ?2 " +
               "  AND (?3 = '' OR n.codigo_spc = ?3) " +
               "GROUP BY n.mes_referencia, n.ano_referencia, n.periodo_fim, n.codigo_spc, a.nome_razao " +
               "ORDER BY a.nome_razao";
    }

    private String buildSqlBuscaLocalDetalhado() {
        return "SELECT " +
               "   CAST(CONCAT(n.mes_referencia, '/', n.ano_referencia) AS VARCHAR) AS competencia, " +
               "   n.periodo_fim AS data_movimento, " +
               "   CAST(n.codigo_spc AS INTEGER) AS codigo_associado, " +
               "   a.nome_razao AS nome_associado, " +
               "   n.total_registros AS total_registros, " +
               "   n.sms_sem_enriquecimento, " +
               "   n.sms_com_enriquecimento, " +
               "   n.sms_total, " +
               "   n.emails_sem_enriquecimento, " +
               "   n.emails_com_enriquecimento, " +
               "   n.emails_total, " +
               "   n.cartas_total, " +
               "   n.nao_enviadas " +
               "FROM notificacao_associado n " +
               "LEFT JOIN tb_associado a ON n.associado_id = a.id " +
               "WHERE n.mes_referencia = ?1 " +
               "  AND n.ano_referencia = ?2 " +
               "  AND n.codigo_spc = ?3 " +
               "ORDER BY n.periodo_fim DESC";
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

    private NotificacaoSumarizada toNotificacaoSumarizadaAgrupado(Object[] row) {
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

    private NotificacaoSumarizada toNotificacaoSumarizadaLocal(Object[] row) {
        NotificacaoSumarizada dto = new NotificacaoSumarizada();
        int idx = 0;

        dto.setCompetencia((String) row[idx]);
        idx++;
        dto.setDataMovimento((java.time.LocalDate) row[idx]);
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