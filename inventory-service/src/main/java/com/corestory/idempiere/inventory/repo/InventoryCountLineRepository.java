package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.InventoryCountLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryCountLineRepository extends JpaRepository<InventoryCountLine, Long> {

    List<InventoryCountLine> findByInventoryCountId(Long inventoryCountId);
}
