// src/main/java/com/sga/dto/ReguaFaturamentoProcessamentoDTO.java

package com.sga.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReguaFaturamentoProcessamentoDTO {

    // ========== CONFIGURAÇÃO DO PROCESSAMENTO ==========
    
    private Long reguaId;
    private List<Long> associadosIds = new ArrayList<>();
    private Integer mesReferencia;
    private Integer anoReferencia;
    private Boolean simular = false;
    private Boolean gerarNotas = true;
    private Boolean integrarRM = false;
    
    // ========== NOVOS CAMPOS DE CONFIGURAÇÃO ==========
    
    private Boolean processarCancelamentos = true;      // Se deve processar cancelamentos
    private Boolean aplicarRegraFranquia = true;       // Se deve aplicar regra de franquia
    private Boolean aplicarRegraFaturamentoMinimo = false; // Se deve aplicar regra de faturamento mínimo
    private Boolean extemporaneo = false;              // Se é faturamento extemporâneo
    private Integer diaVencimentoPersonalizado;        // Dia de vencimento personalizado (opcional)
    private String observacao;                         // Observação do processamento
    
    // ========== RESULTADOS DO PROCESSAMENTO ==========
    
    private Long totalAssociados;
    private Long associadosProcessados;
    private Long associadosComErro;
    private Long totalNotasGeradas;
    private Long totalCancelamentosProcessados;
    private Long totalItensRemovidos;
    
    private BigDecimal valorTotalFaturamento;
    private BigDecimal valorTotalFranquia;
    private BigDecimal valorTotalConsumo;
    private BigDecimal valorTotalDebito;
    private BigDecimal valorTotalCreditos;
    private BigDecimal valorTotalCancelamentos;
    
    private LocalDateTime dataProcessamento;
    private LocalDateTime dataInicioProcessamento;
    private LocalDateTime dataFimProcessamento;
    
    private List<AssociadoProcessamentoDTO> detalhes = new ArrayList<>();
    private List<String> erros = new ArrayList<>();
    private List<CancelamentoProcessadoDTO> cancelamentosProcessados = new ArrayList<>();
    
    // ========== INFORMAÇÕES ADICIONAIS ==========
    
    private String reguaNome;
    private Integer reguaDiaEmissao;
    private Integer reguaDiaVencimento;
    private String periodoDescricao;
    private Long tempoExecucaoMs;
    
    // ========== GETTERS E SETTERS (EXISTENTES) ==========
    
    public Long getReguaId() {
        return reguaId;
    }

    public void setReguaId(Long reguaId) {
        this.reguaId = reguaId;
    }

    public List<Long> getAssociadosIds() {
        return associadosIds;
    }

    public void setAssociadosIds(List<Long> associadosIds) {
        this.associadosIds = associadosIds;
    }

    public Integer getMesReferencia() {
        return mesReferencia;
    }

    public void setMesReferencia(Integer mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public Integer getAnoReferencia() {
        return anoReferencia;
    }

    public void setAnoReferencia(Integer anoReferencia) {
        this.anoReferencia = anoReferencia;
    }

    public Boolean getSimular() {
        return simular;
    }

    public void setSimular(Boolean simular) {
        this.simular = simular;
    }

    public Boolean getGerarNotas() {
        return gerarNotas;
    }

    public void setGerarNotas(Boolean gerarNotas) {
        this.gerarNotas = gerarNotas;
    }

    public Boolean getIntegrarRM() {
        return integrarRM;
    }

    public void setIntegrarRM(Boolean integrarRM) {
        this.integrarRM = integrarRM;
    }

    // ========== NOVOS GETTERS E SETTERS ==========
    
    public Boolean getProcessarCancelamentos() {
        return processarCancelamentos != null ? processarCancelamentos : true;
    }

    public void setProcessarCancelamentos(Boolean processarCancelamentos) {
        this.processarCancelamentos = processarCancelamentos;
    }

    public Boolean getAplicarRegraFranquia() {
        return aplicarRegraFranquia != null ? aplicarRegraFranquia : true;
    }

    public void setAplicarRegraFranquia(Boolean aplicarRegraFranquia) {
        this.aplicarRegraFranquia = aplicarRegraFranquia;
    }

    public Boolean getAplicarRegraFaturamentoMinimo() {
        return aplicarRegraFaturamentoMinimo != null ? aplicarRegraFaturamentoMinimo : false;
    }

    public void setAplicarRegraFaturamentoMinimo(Boolean aplicarRegraFaturamentoMinimo) {
        this.aplicarRegraFaturamentoMinimo = aplicarRegraFaturamentoMinimo;
    }

    public Boolean getExtemporaneo() {
        return extemporaneo != null ? extemporaneo : false;
    }

    public void setExtemporaneo(Boolean extemporaneo) {
        this.extemporaneo = extemporaneo;
    }

    public Integer getDiaVencimentoPersonalizado() {
        return diaVencimentoPersonalizado;
    }

    public void setDiaVencimentoPersonalizado(Integer diaVencimentoPersonalizado) {
        this.diaVencimentoPersonalizado = diaVencimentoPersonalizado;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Long getTotalAssociados() {
        return totalAssociados;
    }

    public void setTotalAssociados(Long totalAssociados) {
        this.totalAssociados = totalAssociados;
    }

    public Long getAssociadosProcessados() {
        return associadosProcessados;
    }

    public void setAssociadosProcessados(Long associadosProcessados) {
        this.associadosProcessados = associadosProcessados;
    }

    public Long getAssociadosComErro() {
        return associadosComErro;
    }

    public void setAssociadosComErro(Long associadosComErro) {
        this.associadosComErro = associadosComErro;
    }

    public Long getTotalNotasGeradas() {
        return totalNotasGeradas;
    }

    public void setTotalNotasGeradas(Long totalNotasGeradas) {
        this.totalNotasGeradas = totalNotasGeradas;
    }

    public Long getTotalCancelamentosProcessados() {
        return totalCancelamentosProcessados;
    }

    public void setTotalCancelamentosProcessados(Long totalCancelamentosProcessados) {
        this.totalCancelamentosProcessados = totalCancelamentosProcessados;
    }

    public Long getTotalItensRemovidos() {
        return totalItensRemovidos;
    }

    public void setTotalItensRemovidos(Long totalItensRemovidos) {
        this.totalItensRemovidos = totalItensRemovidos;
    }

    public BigDecimal getValorTotalFaturamento() {
        return valorTotalFaturamento;
    }

    public void setValorTotalFaturamento(BigDecimal valorTotalFaturamento) {
        this.valorTotalFaturamento = valorTotalFaturamento;
    }

    public BigDecimal getValorTotalFranquia() {
        return valorTotalFranquia;
    }

    public void setValorTotalFranquia(BigDecimal valorTotalFranquia) {
        this.valorTotalFranquia = valorTotalFranquia;
    }

    public BigDecimal getValorTotalConsumo() {
        return valorTotalConsumo;
    }

    public void setValorTotalConsumo(BigDecimal valorTotalConsumo) {
        this.valorTotalConsumo = valorTotalConsumo;
    }

    public BigDecimal getValorTotalDebito() {
        return valorTotalDebito;
    }

    public void setValorTotalDebito(BigDecimal valorTotalDebito) {
        this.valorTotalDebito = valorTotalDebito;
    }

    public BigDecimal getValorTotalCreditos() {
        return valorTotalCreditos;
    }

    public void setValorTotalCreditos(BigDecimal valorTotalCreditos) {
        this.valorTotalCreditos = valorTotalCreditos;
    }

    public BigDecimal getValorTotalCancelamentos() {
        return valorTotalCancelamentos;
    }

    public void setValorTotalCancelamentos(BigDecimal valorTotalCancelamentos) {
        this.valorTotalCancelamentos = valorTotalCancelamentos;
    }

    public LocalDateTime getDataProcessamento() {
        return dataProcessamento;
    }

    public void setDataProcessamento(LocalDateTime dataProcessamento) {
        this.dataProcessamento = dataProcessamento;
    }

    public LocalDateTime getDataInicioProcessamento() {
        return dataInicioProcessamento;
    }

    public void setDataInicioProcessamento(LocalDateTime dataInicioProcessamento) {
        this.dataInicioProcessamento = dataInicioProcessamento;
    }

    public LocalDateTime getDataFimProcessamento() {
        return dataFimProcessamento;
    }

    public void setDataFimProcessamento(LocalDateTime dataFimProcessamento) {
        this.dataFimProcessamento = dataFimProcessamento;
    }

    public List<AssociadoProcessamentoDTO> getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(List<AssociadoProcessamentoDTO> detalhes) {
        this.detalhes = detalhes;
    }

    public List<String> getErros() {
        return erros;
    }

    public void setErros(List<String> erros) {
        this.erros = erros;
    }

    public List<CancelamentoProcessadoDTO> getCancelamentosProcessados() {
        return cancelamentosProcessados;
    }

    public void setCancelamentosProcessados(List<CancelamentoProcessadoDTO> cancelamentosProcessados) {
        this.cancelamentosProcessados = cancelamentosProcessados;
    }

    public String getReguaNome() {
        return reguaNome;
    }

    public void setReguaNome(String reguaNome) {
        this.reguaNome = reguaNome;
    }

    public Integer getReguaDiaEmissao() {
        return reguaDiaEmissao;
    }

    public void setReguaDiaEmissao(Integer reguaDiaEmissao) {
        this.reguaDiaEmissao = reguaDiaEmissao;
    }

    public Integer getReguaDiaVencimento() {
        return reguaDiaVencimento;
    }

    public void setReguaDiaVencimento(Integer reguaDiaVencimento) {
        this.reguaDiaVencimento = reguaDiaVencimento;
    }

    public String getPeriodoDescricao() {
        return periodoDescricao;
    }

    public void setPeriodoDescricao(String periodoDescricao) {
        this.periodoDescricao = periodoDescricao;
    }

    public Long getTempoExecucaoMs() {
        return tempoExecucaoMs;
    }

    public void setTempoExecucaoMs(Long tempoExecucaoMs) {
        this.tempoExecucaoMs = tempoExecucaoMs;
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Calcula o tempo de execução
     */
    public void calcularTempoExecucao() {
        if (dataInicioProcessamento != null && dataFimProcessamento != null) {
            this.tempoExecucaoMs = java.time.Duration.between(dataInicioProcessamento, dataFimProcessamento).toMillis();
        }
    }
    
    /**
     * Adiciona um erro ao processamento
     */
    public void addErro(String erro) {
        if (this.erros == null) {
            this.erros = new ArrayList<>();
        }
        this.erros.add(erro);
    }
    
    /**
     * Adiciona um cancelamento processado
     */
    public void addCancelamentoProcessado(CancelamentoProcessadoDTO cancelamento) {
        if (this.cancelamentosProcessados == null) {
            this.cancelamentosProcessados = new ArrayList<>();
        }
        this.cancelamentosProcessados.add(cancelamento);
    }
    
    /**
     * Soma valores ao total de débito
     */
    public void addValorDebito(BigDecimal valor) {
        if (this.valorTotalDebito == null) {
            this.valorTotalDebito = BigDecimal.ZERO;
        }
        this.valorTotalDebito = this.valorTotalDebito.add(valor);
    }
    
    /**
     * Soma valores ao total de créditos
     */
    public void addValorCredito(BigDecimal valor) {
        if (this.valorTotalCreditos == null) {
            this.valorTotalCreditos = BigDecimal.ZERO;
        }
        this.valorTotalCreditos = this.valorTotalCreditos.add(valor);
    }
    
    /**
     * Retorna resumo do processamento em texto
     */
    public String getResumoTexto() {
        StringBuilder sb = new StringBuilder();
        sb.append("Processamento de Faturamento\n");
        sb.append("Período: ").append(mesReferencia).append("/").append(anoReferencia).append("\n");
        sb.append("Régua: ").append(reguaNome != null ? reguaNome : "Selecionada").append("\n");
        sb.append("Associados: ").append(associadosProcessados != null ? associadosProcessados : 0)
          .append(" processados, ").append(associadosComErro != null ? associadosComErro : 0).append(" com erro\n");
        sb.append("Notas geradas: ").append(totalNotasGeradas != null ? totalNotasGeradas : 0).append("\n");
        sb.append("Valor total: ").append(valorTotalDebito != null ? valorTotalDebito : BigDecimal.ZERO).append("\n");
        if (tempoExecucaoMs != null) {
            sb.append("Tempo de execução: ").append(tempoExecucaoMs).append("ms");
        }
        return sb.toString();
    }
}