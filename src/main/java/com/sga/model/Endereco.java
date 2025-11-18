package com.sga.model;

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

import lombok.Data;

@Data
@Entity
@Table(name = "TB_ENDERECO")
public class Endereco {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "IDASSOCIADO")
	private Associado associado;

	@Column(name = "TIPOLOGRADOURO")
	private String tipoLogradouro;

	@Column(name = "LOGRADOURO")
	private String logradouro;

	@Column(name = "NUMERO")
	private String numero;

	@Column(name = "COMPLEMENTO")
	private String complemento;

	@Column(name = "BAIRRO")
	private String bairro;

	@Column(name = "CIDADE")
	private String cidade;

	@Column(name = "CEP")
	private String cep;

	@Column(name = "DDD")
	private String ddd;

	@Column(name = "TELEFONE")
	private String telefone;

	@Column(name = "ESTADO")
	private String estado;

	@Column(name = "TIPOENDERECO")
	private String tipoEndereco;

	public Endereco(Long id) {
		super();
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
		Endereco other = (Endereco) obj;
		return Objects.equals(id, other.id);
	}

}
