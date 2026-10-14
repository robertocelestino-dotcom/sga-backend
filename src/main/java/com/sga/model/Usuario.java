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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_USUARIO")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
	private String role = "USER";

	private boolean ativo = true;

	@Column(name = "DATA_CRIACAO")
	private java.time.LocalDateTime dataCriacao = java.time.LocalDateTime.now();

	// ============================================================
	// CAMPOS ADICIONADOS
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

	@Column(name = "data_bloqueio")
	private java.time.LocalDateTime dataBloqueio;

	@Column(name = "motivo_bloqueio", length = 255)
	private String motivoBloqueio;

	// ============================================================
	// 🔥 FOTO DO USUÁRIO (avatar em base64)
	//
	// - @JsonIgnore: ESSENCIAL. Sem isso, o SistemaLogAspect serializa
	//   1-2MB de base64 em CADA operação que toca Usuario, gerando
	//   inserts gigantes no tb_sistema_log.
	//
	// ⚠️ O AuthController retorna a foto via DTO próprio (UsuarioDTO),
	//    portanto o @JsonIgnore não afeta a resposta do login.
	// ============================================================
	@JsonIgnore
	@Column(name = "foto_base64", columnDefinition = "TEXT")
	private String fotoBase64;

	// ============================================================
	// RELACIONAMENTO COM PERFIL
	//
	// - @JsonIgnore: impede que o Perfil (com menus recursivos)
	//   seja serializado junto com o Usuario em logs.
	// - Mantido EAGER porque o getAuthorities() do UserDetails acessa
	//   perfil.getPermissoes() e precisa estar carregado para
	//   autenticação/autorização funcionar.
	// ============================================================
	@JsonIgnore
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
	// GETTERS E SETTERS
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

	public boolean estaBloqueado() {
		return bloqueado != null && bloqueado;
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
	// MÉTODOS AUXILIARES
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

	public java.time.LocalDateTime getDataBloqueio() {
		return dataBloqueio;
	}

	public void setDataBloqueio(java.time.LocalDateTime dataBloqueio) {
		this.dataBloqueio = dataBloqueio;
	}

	public String getMotivoBloqueio() {
		return motivoBloqueio;
	}

	public void setMotivoBloqueio(String motivoBloqueio) {
		this.motivoBloqueio = motivoBloqueio;
	}

	public String getFotoBase64() {
		return fotoBase64;
	}

	public void setFotoBase64(String fotoBase64) {
		this.fotoBase64 = fotoBase64;
	}

	// ============================================================
	// UserDetails
	// ============================================================

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();

		if (role != null && !role.trim().isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
		}

		if (perfil != null && perfil.getPermissoes() != null) {
			for (Permissao p : perfil.getPermissoes()) {
				if (p.getNome() != null && !p.getNome().trim().isEmpty()) {
					authorities.add(new SimpleGrantedAuthority(p.getNome()));
				}
			}
		}

		if (authorities.isEmpty()) {
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		}

		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
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
	// hashCode e equals
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