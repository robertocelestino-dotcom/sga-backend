package com.sga.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "tb_produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", unique = true, length = 50)
    private String codigo;

    @Column(name = "codigo_rm", unique = true, length = 50)
    private String codigoRm;

    @Column(name = "nome", length = 200, nullable = false)
    private String nome;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "valor_unitario", precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    @Column(name = "status", length = 20)
    private String status = "ATIVO"; // ATIVO, INATIVO

    // ========== NOVOS CAMPOS PARA FRANQUIAS ==========
    @Column(name = "tipo_produto", length = 30)
    private String tipoProduto = "SERVICO"; // SERVICO, FRANQUIA, PRODUTO, ASSINATURA
    
    @Column(name = "unidade_medida", length = 20)
    private String unidadeMedida = "UNIDADE"; // UNIDADE, CONSULTA, HORA, MES
    
    @Column(name = "categoria", length = 50)
    private String categoria; // SPC, CHEQUE, INTERNET, WEBSERVICE, MIX
    
    @Column(name = "modalidade", length = 50)
    private String modalidade; // POSITIVO, NEGATIVO, FOR, INTERNET
    
    @Column(name = "tem_franquia")
    private Boolean temFranquia = false;
    
    @Column(name = "limite_franquia")
    private Integer limiteFranquia;
    
    @Column(name = "periodo_franquia", length = 20)
    private String periodoFranquia; // MENSAL, ANUAL, DIARIO
    
    // ========== RELACIONAMENTOS ==========
    
    // Produtos que são franquias DESTE produto (ex: SPC MIX tem franquia de consultas SPC)
    @ManyToMany
    @JoinTable(
        name = "tb_produto_franquia",
        joinColumns = @JoinColumn(name = "produto_id"),
        inverseJoinColumns = @JoinColumn(name = "franquia_id")
    )
    private Set<Produto> franquias = new HashSet<>();
    
    // Produtos que têm ESTE produto como franquia (relacionamento inverso)
    @ManyToMany(mappedBy = "franquias")
    private Set<Produto> produtosQueUsamEstaFranquia = new HashSet<>();
    
    // Produtos relacionados (ex: SPC MIX relacionado com SPC e CHEQUE)
    @ManyToMany
    @JoinTable(
        name = "tb_produto_relacionado",
        joinColumns = @JoinColumn(name = "produto_id"),
        inverseJoinColumns = @JoinColumn(name = "relacionado_id")
    )
    private Set<Produto> produtosRelacionados = new HashSet<>();
    
    // ========== CAMPOS PARA REGRAS DE FATURAMENTO ==========
    @Column(name = "gera_cobranca_automatica")
    private Boolean geraCobrancaAutomatica = true;
    
    @Column(name = "cobranca_periodica")
    private Boolean cobrancaPeriodica = false; // Para assinaturas
    
    @Column(name = "periodicidade_cobranca", length = 20)
    private String periodicidadeCobranca; // MENSAL, ANUAL
    
    @Column(name = "dia_cobranca")
    private Integer diaCobranca; // Dia do mês para cobrança
    
    @Column(name = "permite_desconto")
    private Boolean permiteDesconto = true;
    
    @Column(name = "desconto_maximo", precision = 5, scale = 2)
    private BigDecimal descontoMaximo = BigDecimal.ZERO;
    
    @Column(name = "exige_autorizacao")
    private Boolean exigeAutorizacao = false;
    
    @Column(name = "nivel_autorizacao")
    private Integer nivelAutorizacao; // 1, 2, 3 (níveis hierárquicos)
    
    // ========== CAMPOS AUDITORIA ==========
    @Column(name = "criado_em")
    private java.time.LocalDateTime criadoEm;
    
    @Column(name = "atualizado_em")
    private java.time.LocalDateTime atualizadoEm;
    
    @Column(name = "usuario_criacao", length = 100)
    private String usuarioCriacao;
    
    @Column(name = "usuario_atualizacao", length = 100)
    private String usuarioAtualizacao;

    // ========== CONSTRUTORES ==========
    public Produto() {
        this.criadoEm = java.time.LocalDateTime.now();
        this.atualizadoEm = java.time.LocalDateTime.now();
    }

    public Produto(String codigo, String nome) {
        this();
        this.codigo = codigo;
        this.nome = nome;
    }
    
    public Produto(String codigo, String nome, String tipoProduto, String categoria) {
        this();
        this.codigo = codigo;
        this.nome = nome;
        this.tipoProduto = tipoProduto;
        this.categoria = categoria;
    }

    // ========== GETTERS E SETTERS ==========
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoRm() {
        return codigoRm;
    }

    public void setCodigoRm(String codigoRm) {
        this.codigoRm = codigoRm;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Novos getters e setters
    public String getTipoProduto() {
        return tipoProduto;
    }

    public void setTipoProduto(String tipoProduto) {
        this.tipoProduto = tipoProduto;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public void setUnidadeMedida(String unidadeMedida) {
        this.unidadeMedida = unidadeMedida;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getModalidade() {
        return modalidade;
    }

    public void setModalidade(String modalidade) {
        this.modalidade = modalidade;
    }

    public Boolean getTemFranquia() {
        return temFranquia;
    }

    public void setTemFranquia(Boolean temFranquia) {
        this.temFranquia = temFranquia;
    }

    public Integer getLimiteFranquia() {
        return limiteFranquia;
    }

    public void setLimiteFranquia(Integer limiteFranquia) {
        this.limiteFranquia = limiteFranquia;
        this.temFranquia = limiteFranquia != null && limiteFranquia > 0;
    }

    public String getPeriodoFranquia() {
        return periodoFranquia;
    }

    public void setPeriodoFranquia(String periodoFranquia) {
        this.periodoFranquia = periodoFranquia;
    }

    public Set<Produto> getFranquias() {
        return franquias;
    }

    public void setFranquias(Set<Produto> franquias) {
        this.franquias = franquias;
    }
    
    public void addFranquia(Produto franquia) {
        this.franquias.add(franquia);
        this.temFranquia = true;
    }
    
    public void removeFranquia(Produto franquia) {
        this.franquias.remove(franquia);
        this.temFranquia = !this.franquias.isEmpty();
    }

    public Set<Produto> getProdutosQueUsamEstaFranquia() {
        return produtosQueUsamEstaFranquia;
    }

    public void setProdutosQueUsamEstaFranquia(Set<Produto> produtosQueUsamEstaFranquia) {
        this.produtosQueUsamEstaFranquia = produtosQueUsamEstaFranquia;
    }

    public Set<Produto> getProdutosRelacionados() {
        return produtosRelacionados;
    }

    public void setProdutosRelacionados(Set<Produto> produtosRelacionados) {
        this.produtosRelacionados = produtosRelacionados;
    }
    
    public void addProdutoRelacionado(Produto produto) {
        this.produtosRelacionados.add(produto);
    }

    // Getters e setters para regras de faturamento
    public Boolean getGeraCobrancaAutomatica() {
        return geraCobrancaAutomatica;
    }

    public void setGeraCobrancaAutomatica(Boolean geraCobrancaAutomatica) {
        this.geraCobrancaAutomatica = geraCobrancaAutomatica;
    }

    public Boolean getCobrancaPeriodica() {
        return cobrancaPeriodica;
    }

    public void setCobrancaPeriodica(Boolean cobrancaPeriodica) {
        this.cobrancaPeriodica = cobrancaPeriodica;
    }

    public String getPeriodicidadeCobranca() {
        return periodicidadeCobranca;
    }

    public void setPeriodicidadeCobranca(String periodicidadeCobranca) {
        this.periodicidadeCobranca = periodicidadeCobranca;
    }

    public Integer getDiaCobranca() {
        return diaCobranca;
    }

    public void setDiaCobranca(Integer diaCobranca) {
        this.diaCobranca = diaCobranca;
    }

    public Boolean getPermiteDesconto() {
        return permiteDesconto;
    }

    public void setPermiteDesconto(Boolean permiteDesconto) {
        this.permiteDesconto = permiteDesconto;
    }

    public BigDecimal getDescontoMaximo() {
        return descontoMaximo;
    }

    public void setDescontoMaximo(BigDecimal descontoMaximo) {
        this.descontoMaximo = descontoMaximo;
    }

    public Boolean getExigeAutorizacao() {
        return exigeAutorizacao;
    }

    public void setExigeAutorizacao(Boolean exigeAutorizacao) {
        this.exigeAutorizacao = exigeAutorizacao;
    }

    public Integer getNivelAutorizacao() {
        return nivelAutorizacao;
    }

    public void setNivelAutorizacao(Integer nivelAutorizacao) {
        this.nivelAutorizacao = nivelAutorizacao;
    }

    // Getters e setters para auditoria
    public java.time.LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(java.time.LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public java.time.LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(java.time.LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public String getUsuarioCriacao() {
        return usuarioCriacao;
    }

    public void setUsuarioCriacao(String usuarioCriacao) {
        this.usuarioCriacao = usuarioCriacao;
    }

    public String getUsuarioAtualizacao() {
        return usuarioAtualizacao;
    }

    public void setUsuarioAtualizacao(String usuarioAtualizacao) {
        this.usuarioAtualizacao = usuarioAtualizacao;
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    /**
     * Retorna o nome completo do produto com categoria e modalidade
     */
    public String getNomeCompleto() {
        StringBuilder nomeCompleto = new StringBuilder(nome);
        if (categoria != null && !categoria.isEmpty()) {
            nomeCompleto.append(" - ").append(categoria);
        }
        if (modalidade != null && !modalidade.isEmpty()) {
            nomeCompleto.append(" ").append(modalidade);
        }
        return nomeCompleto.toString();
    }
    
    /**
     * Verifica se o produto é uma franquia
     */
    public boolean isFranquia() {
        return "FRANQUIA".equalsIgnoreCase(tipoProduto) || 
               (temFranquia != null && temFranquia);
    }
    
    /**
     * Verifica se o produto é um serviço SPC
     */
    public boolean isServicoSPC() {
        return "SPC".equalsIgnoreCase(categoria) || 
               (nome != null && nome.toUpperCase().contains("SPC"));
    }
    
    /**
     * Verifica se o produto é do tipo MIX (combinação de serviços)
     */
    public boolean isMix() {
        return nome != null && nome.toUpperCase().contains("MIX");
    }
    
    /**
     * Retorna as franquias como lista de IDs (útil para DTOs)
     */
    public java.util.List<Long> getFranquiasIds() {
        return franquias.stream()
                .map(Produto::getId)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Atualiza a data de modificação
     */
    @PreUpdate
    public void preUpdate() {
        this.atualizadoEm = java.time.LocalDateTime.now();
    }
}