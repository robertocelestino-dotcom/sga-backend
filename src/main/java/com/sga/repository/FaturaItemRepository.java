package com.sga.repository;

import com.sga.model.FaturaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FaturaItemRepository extends JpaRepository<FaturaItem, Long> {

    List<FaturaItem> findByFaturaId(Long faturaId);

    List<FaturaItem> findByItemSpcId(Long itemSpcId);

    @Query("SELECT fi FROM FaturaItem fi WHERE fi.aplicouRegraFranquia = true")
    List<FaturaItem> findItensComRegraFranquiaAplicada();

    @Query("SELECT fi FROM FaturaItem fi WHERE fi.aplicouRegraMinimo = true")
    List<FaturaItem> findItensComRegraMinimoAplicada();

    @Query("SELECT SUM(fi.valorTotal) FROM FaturaItem fi WHERE fi.fatura.id = :faturaId AND fi.tipoLancamento = 'D'")
    BigDecimal sumDebitosByFaturaId(@Param("faturaId") Long faturaId);

    @Query("SELECT SUM(fi.valorTotal) FROM FaturaItem fi WHERE fi.fatura.id = :faturaId AND fi.tipoLancamento = 'C'")
    BigDecimal sumCreditosByFaturaId(@Param("faturaId") Long faturaId);
}