package com.sga.model;

import java.util.Objects;

import javax.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "TB_USUARIO")
public class Usuario {

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

	public Usuario() {
		super();
	}

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
