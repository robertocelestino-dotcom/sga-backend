// src/main/java/com/sga/model/HistoricoMigracaoRegua.java

package com.sga.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tb_historico_migracao_regua")
public class HistoricoMigracaoRegua {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "associado_id", nullable = false)
    private Associado associado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regua_origem_id")
    private ReguaFaturamento reguaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regua_destino_id", nullable = false)
    private ReguaFaturamento reguaDestino;

    @Column(name = "data_migracao", nullable = false)
    private LocalDateTime dataMigracao;

    @Column(name = "usuario", nullable = false, length = 100)
    private String usuario;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // SUCESSO, ERRO, CANCELADO

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "faturas_pendentes")
    private Integer faturasPendentes;

    @Column(name = "migracao_forcada")
    private Boolean migracaoForcada = false;

    // ========== CONSTRUTORES ==========

    public HistoricoMigracaoRegua() {}

    public HistoricoMigracaoRegua(Associado associado, ReguaFaturamento reguaOrigem,
                                   ReguaFaturamento reguaDestino, String usuario,
                                   String motivo, String status) {
        this.associado = associado;
        this.reguaOrigem = reguaOrigem;
        this.reguaDestino = reguaDestino;
        this.dataMigracao = LocalDateTime.now();
        this.usuario = usuario;
        this.motivo = motivo;
        this.status = status;
    }

    // ========== GETTERS E SETTERS ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Associado getAssociado() {
        return associado;
    }

    public void setAssociado(Associado associado) {
        this.associado = associado;
    }

    public ReguaFaturamento getReguaOrigem() {
        return reguaOrigem;
    }

    public void setReguaOrigem(ReguaFaturamento reguaOrigem) {
        this.reguaOrigem = reguaOrigem;
    }

    public ReguaFaturamento getReguaDestino() {
        return reguaDestino;
    }

    public void setReguaDestino(ReguaFaturamento reguaDestino) {
        this.reguaDestino = reguaDestino;
    }

    public LocalDateTime getDataMigracao() {
        return dataMigracao;
    }

    public void setDataMigracao(LocalDateTime dataMigracao) {
        this.dataMigracao = dataMigracao;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Integer getFaturasPendentes() {
        return faturasPendentes;
    }

    public void setFaturasPendentes(Integer faturasPendentes) {
        this.faturasPendentes = faturasPendentes;
    }

    public Boolean getMigracaoForcada() {
        return migracaoForcada != null ? migracaoForcada : false;
    }

    public void setMigracaoForcada(Boolean migracaoForcada) {
        this.migracaoForcada = migracaoForcada;
    }

    @Override
    public String toString() {
        return "HistoricoMigracaoRegua{" +
                "id=" + id +
                ", associadoId=" + (associado != null ? associado.getId() : null) +
                ", reguaOrigemId=" + (reguaOrigem != null ? reguaOrigem.getId() : null) +
                ", reguaDestinoId=" + (reguaDestino != null ? reguaDestino.getId() : null) +
                ", dataMigracao=" + dataMigracao +
                ", usuario='" + usuario + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
