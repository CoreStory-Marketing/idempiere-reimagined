package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.ReplenishmentOrder;
import com.corestory.idempiere.inventory.model.ReplenishmentOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReplenishmentOrderRepository extends JpaRepository<ReplenishmentOrder, Long> {

    List<ReplenishmentOrder> findByStatus(ReplenishmentOrderStatus status);

    List<ReplenishmentOrder> findByProductIdAndWarehouseId(Long productId, Long warehouseId);
}
