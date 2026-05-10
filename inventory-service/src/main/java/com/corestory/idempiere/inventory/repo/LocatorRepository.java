package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.Locator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocatorRepository extends JpaRepository<Locator, Long> {

    List<Locator> findByWarehouseIdOrderByPriorityNoAsc(Long warehouseId);

    List<Locator> findByWarehouseIdAndIsActiveTrueOrderByPriorityNoAsc(Long warehouseId);

    Optional<Locator> findByWarehouseIdAndCode(Long warehouseId, String code);

    Optional<Locator> findByWarehouseIdAndIsDefaultTrue(Long warehouseId);
}
