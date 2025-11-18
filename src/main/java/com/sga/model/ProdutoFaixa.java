package com.sga.model;

import java.math.BigDecimal;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "TB_PRODUTO_FAIXA")
public class ProdutoFaixa {
	
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    
	    @Column(name = "CODIGOPRODUTORM", nullable = false, length = 20)
	    private String codigoProdutoRm;
	    
	    @Column(name = "NOMERAZAO", nullable = false)
	    private String nomeRazao;
	    
	    @Column(name = "NOMEFANTASIA")
	    private String nomeFantasia;
	    
	    @Column(name = "PRECO1", precision = 15, scale = 4)
	    private BigDecimal preco1;
	    
	    @Column(name = "PRECO2", precision = 15, scale = 4)
	    private BigDecimal preco2;
	    
	    @Column(name = "PRECO3", precision = 15, scale = 4)
	    private BigDecimal preco3;
	    
	    @Column(name = "STATUS", length = 1)
	    private String status = "A";
	    
	    @Lob
	    @Column(name = "OBSERVACAO")
	    private String observacao;

		public ProdutoFaixa(Long id) {

		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getCodigoProdutoRm() {
			return codigoProdutoRm;
		}

		public void setCodigoProdutoRm(String codigoProdutoRm) {
			this.codigoProdutoRm = codigoProdutoRm;
		}

		public String getNomeRazao() {
			return nomeRazao;
		}

		public void setNomeRazao(String nomeRazao) {
			this.nomeRazao = nomeRazao;
		}

		public String getNomeFantasia() {
			return nomeFantasia;
		}

		public void setNomeFantasia(String nomeFantasia) {
			this.nomeFantasia = nomeFantasia;
		}

		public BigDecimal getPreco1() {
			return preco1;
		}

		public void setPreco1(BigDecimal preco1) {
			this.preco1 = preco1;
		}

		public BigDecimal getPreco2() {
			return preco2;
		}

		public void setPreco2(BigDecimal preco2) {
			this.preco2 = preco2;
		}

		public BigDecimal getPreco3() {
			return preco3;
		}

		public void setPreco3(BigDecimal preco3) {
			this.preco3 = preco3;
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
			ProdutoFaixa other = (ProdutoFaixa) obj;
			return Objects.equals(id, other.id);
		}
		
}
