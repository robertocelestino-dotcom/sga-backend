package com.sga.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Utilitário para conversão segura de valores vindos de queries nativas/JPA
 * que retornam Object[] ou valores primitivos empacotados.
 */
public final class CastUtils {

    private static final Logger log = LoggerFactory.getLogger(CastUtils.class);

    private CastUtils() { /* classe utilitária */ }

    /**
     * Converte Object para BigDecimal com segurança.
     * Suporta: BigDecimal, Number (Integer, Long, Double, BigInteger), Object[] (desempacota), String, null.
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO;

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }

        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                log.warn("⚠️ Não foi possível converter String para BigDecimal: {}", value);
                return BigDecimal.ZERO;
            }
        }

        // Caso o driver tenha empacotado em Object[]
        if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            if (arr.length > 0) {
                return toBigDecimal(arr[0]);   // recursão
            }
            return BigDecimal.ZERO;
        }

        log.warn("⚠️ Tipo inesperado para BigDecimal: {} ({})",
                 value.getClass().getName(), value);
        return BigDecimal.ZERO;
    }

    /**
     * Converte Object para String com segurança.
     */
    public static String toStringSafe(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            return arr.length > 0 ? toStringSafe(arr[0]) : null;
        }
        return value.toString();
    }

    /**
     * Converte Object para Integer com segurança.
     */
    public static Integer toInteger(Object value) {
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            return arr.length > 0 ? toInteger(arr[0]) : 0;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        log.warn("⚠️ Tipo inesperado para Integer: {}", value.getClass().getName());
        return 0;
    }

    /**
     * Converte Object para Long com segurança.
     */
    public static Long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof Object[]) {
            Object[] arr = (Object[]) value;
            return arr.length > 0 ? toLong(arr[0]) : 0L;
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        log.warn("⚠️ Tipo inesperado para Long: {}", value.getClass().getName());
        return 0L;
    }
}