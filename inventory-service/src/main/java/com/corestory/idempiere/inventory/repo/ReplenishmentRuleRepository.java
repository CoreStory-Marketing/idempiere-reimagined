package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.ReplenishmentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReplenishmentRuleRepository extends JpaRepository<ReplenishmentRule, Long> {

    Optional<ReplenishmentRule> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
