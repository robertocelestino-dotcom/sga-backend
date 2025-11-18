package com.sga.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sga.model.Faturamento;

@Repository
public interface FaturamentoRepository extends JpaRepository<Faturamento, Long> {
    
    // Use o nome da ENTIDADE (Faturamento), não da tabela (tb_faturamento)
    @Query("SELECT COALESCE(SUM(f.valorTotal), 0) FROM Faturamento f WHERE f.mes = :mes AND f.ano = :ano")
    BigDecimal findValorTotalFaturamentoPorMesAno(@Param("mes") Integer mes, @Param("ano") Integer ano);
    
    // Corrigido: use Faturamento (entidade) em vez de tb_faturamento (tabela)
    @Query("SELECT COALESCE(SUM(f.valorTotal), 0) FROM Faturamento f WHERE f.dataFaturamento = " +
           "(SELECT MAX(f2.dataFaturamento) FROM Faturamento f2)")
    BigDecimal findValorTotalUltimoFaturamento();
    
    Faturamento findTopByOrderByDataFaturamentoDesc();
    
    List<Faturamento> findByMesAndAno(Integer mes, Integer ano);
}