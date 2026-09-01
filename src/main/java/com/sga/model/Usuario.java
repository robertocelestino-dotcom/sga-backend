package com.sga.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_USUARIO")
public class Usuario implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 50)
	private String username;

	@Column(nullable = false)
	private String password;

	private String email;

	@Column(nullable = false, length = 100)
	private String nome;

	@Column(name = "IDVENDEDOR")
	private Long idVendedor;

	@Column(length = 20)
	private String role = "USER"; // ADMIN, VENDEDOR, FINANCEIRO

	private boolean ativo = true;

	@Column(name = "DATA_CRIACAO")
	private java.time.LocalDateTime dataCriacao = java.time.LocalDateTime.now();

	// ============================================================
	// NOVOS CAMPOS (ADICIONADOS SEM REMOVER OS EXISTENTES)
	// ============================================================

	@Column(name = "PERFIL_ID")
	private Long perfilId;

	@Column(name = "TENTATIVAS_LOGIN")
	private Integer tentativasLogin = 0;

	@Column(name = "ULTIMO_IP", length = 45)
	private String ultimoIp;

	@Column(name = "ULTIMO_LOGIN")
	private java.time.LocalDateTime ultimoLogin;

	@Column(name = "DATA_EXPIRACAO_SENHA")
	private java.time.LocalDateTime dataExpiracaoSenha;

	@Column(name = "BLOQUEADO")
	private Boolean bloqueado = false;

	@Column(name = "DATA_ATUALIZACAO")
	private java.time.LocalDateTime dataAtualizacao = java.time.LocalDateTime.now();

	// ============================================================
	// RELACIONAMENTO COM PERFIL (NOVO - OPCIONAL)
	// ============================================================

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PERFIL_ID", insertable = false, updatable = false)
	private Perfil perfil;

	// ============================================================
	// CONSTRUTORES
	// ============================================================

	public Usuario() {
		super();
	}

	// ============================================================
	// GETTERS E SETTERS EXISTENTES (MANTIDOS)
	// ============================================================

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Long getIdVendedor() {
		return idVendedor;
	}

	public void setIdVendedor(Long idVendedor) {
		this.idVendedor = idVendedor;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public java.time.LocalDateTime getDataCriacao() {
		return dataCriacao;
	}

	public void setDataCriacao(java.time.LocalDateTime dataCriacao) {
		this.dataCriacao = dataCriacao;
	}

	// ============================================================
	// GETTERS E SETTERS DOS NOVOS CAMPOS
	// ============================================================

	public Long getPerfilId() {
		return perfilId;
	}

	public void setPerfilId(Long perfilId) {
		this.perfilId = perfilId;
	}

	public Integer getTentativasLogin() {
		return tentativasLogin;
	}

	public void setTentativasLogin(Integer tentativasLogin) {
		this.tentativasLogin = tentativasLogin;
	}

	public String getUltimoIp() {
		return ultimoIp;
	}

	public void setUltimoIp(String ultimoIp) {
		this.ultimoIp = ultimoIp;
	}

	public java.time.LocalDateTime getUltimoLogin() {
		return ultimoLogin;
	}

	public void setUltimoLogin(java.time.LocalDateTime ultimoLogin) {
		this.ultimoLogin = ultimoLogin;
	}

	public java.time.LocalDateTime getDataExpiracaoSenha() {
		return dataExpiracaoSenha;
	}

	public void setDataExpiracaoSenha(java.time.LocalDateTime dataExpiracaoSenha) {
		this.dataExpiracaoSenha = dataExpiracaoSenha;
	}

	public Boolean getBloqueado() {
		return bloqueado;
	}

	public void setBloqueado(Boolean bloqueado) {
		this.bloqueado = bloqueado;
	}

	public java.time.LocalDateTime getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(java.time.LocalDateTime dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
		if (perfil != null) {
			this.perfilId = perfil.getId();
		}
	}

	// ============================================================
	// MÉTODOS AUXILIARES PARA COMPATIBILIDADE
	// ============================================================

	public String getNomeCompleto() {
		return nome;
	}

	public void setNomeCompleto(String nomeCompleto) {
		this.nome = nomeCompleto;
	}

	public String getSenha() {
		return password;
	}

	public void setSenha(String senha) {
		this.password = senha;
	}

	// ============================================================
	// MÉTODOS DO UserDetails
	// ============================================================

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
	    // Se tiver perfil com permissões, usa elas
	    if (perfil != null && perfil.getPermissoes() != null && !perfil.getPermissoes().isEmpty()) {
	        List<GrantedAuthority> authorities = new ArrayList<>();
	        for (Permissao p : perfil.getPermissoes()) {
	            authorities.add(new SimpleGrantedAuthority(p.getNome()));
	        }
	        return authorities;
	    }
	    
	    // Fallback: usa o role existente
	    if (role != null && !role.isEmpty()) {
	        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
	    }
	    
	    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return bloqueado == null || !bloqueado;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		if (dataExpiracaoSenha == null) {
			return true;
		}
		return java.time.LocalDateTime.now().isBefore(dataExpiracaoSenha);
	}

	@Override
	public boolean isEnabled() {
		return ativo;
	}

	// ============================================================
	// hashCode e equals (MANTIDOS)
	// ============================================================

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
		Usuario other = (Usuario) obj;
		return Objects.equals(id, other.id);
	}
}