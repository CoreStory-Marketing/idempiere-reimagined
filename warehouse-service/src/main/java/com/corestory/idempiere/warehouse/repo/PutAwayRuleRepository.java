package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.PutAwayRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PutAwayRuleRepository extends JpaRepository<PutAwayRule, Long> {

    List<PutAwayRule> findByProductIdAndWarehouseIdOrderByPriorityDesc(Long productId, Long warehouseId);

    default Optional<PutAwayRule> findBest(Long productId, Long warehouseId) {
        return findByProductIdAndWarehouseIdOrderByPriorityDesc(productId, warehouseId).stream().findFirst();
    }
}
