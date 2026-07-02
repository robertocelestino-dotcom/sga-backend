// src/main/java/com/sga/dto/NotificacaoSincronizacaoRequest.java
package com.sga.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class NotificacaoSincronizacaoRequest {
    private Integer mes;
    private Integer ano;
    private String codigoAssociado;
    private LocalDate dataInicio;
    private LocalDate dataFim;
}