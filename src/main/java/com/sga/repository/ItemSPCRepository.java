package com.sga.repository;

import com.sga.model.ItemSPC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemSPCRepository extends JpaRepository<ItemSPC, Long> {

	List<ItemSPC> findByImportacaoId(Long importacaoId);

}