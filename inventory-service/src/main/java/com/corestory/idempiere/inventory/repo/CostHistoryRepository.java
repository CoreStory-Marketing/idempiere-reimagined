package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.CostHistory;
import com.corestory.idempiere.inventory.model.CostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CostHistoryRepository extends JpaRepository<CostHistory, Long> {

    List<CostHistory> findByProductIdOrderByValidFromDesc(Long productId);

    List<CostHistory> findByProductIdAndCostTypeOrderByValidFromDesc(Long productId, CostType costType);
}
