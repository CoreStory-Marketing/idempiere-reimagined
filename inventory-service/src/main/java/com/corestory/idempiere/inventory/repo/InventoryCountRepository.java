package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.InventoryCount;
import com.corestory.idempiere.inventory.model.InventoryCountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryCountRepository extends JpaRepository<InventoryCount, Long> {

    Page<InventoryCount> findByStatus(InventoryCountStatus status, Pageable pageable);

    List<InventoryCount> findByWarehouseIdAndStatus(Long warehouseId, InventoryCountStatus status);
}
