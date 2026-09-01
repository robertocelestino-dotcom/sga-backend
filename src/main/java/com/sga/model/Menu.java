package com.sga.model;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TB_MENU")
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String caminho;

    @Column(length = 50)
    private String icone;

    @Column(nullable = false)
    private Integer ordem = 0;

    @Column(nullable = false)
    private Boolean ativo = true;

    @ManyToOne
    @JoinColumn(name = "MENU_PAI_ID")
    private Menu menuPai;

    @OneToMany(mappedBy = "menuPai", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderBy("ordem ASC")
    private List<Menu> subMenus = new ArrayList<>();

    @Column(name = "DATA_CRIACAO")
    private LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "DATA_ATUALIZACAO")
    private LocalDateTime dataAtualizacao = LocalDateTime.now();

    // Construtores
    public Menu() {
        super();
    }

    public Menu(String nome, String caminho, String icone, Integer ordem) {
        this.nome = nome;
        this.caminho = caminho;
        this.icone = icone;
        this.ordem = ordem;
        this.ativo = true;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCaminho() {
        return caminho;
    }

    public void setCaminho(String caminho) {
        this.caminho = caminho;
    }

    public String getIcone() {
        return icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Menu getMenuPai() {
        return menuPai;
    }

    public void setMenuPai(Menu menuPai) {
        this.menuPai = menuPai;
    }

    public List<Menu> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(List<Menu> subMenus) {
        this.subMenus = subMenus;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Menu other = (Menu) obj;
        return Objects.equals(id, other.id);
    }
}