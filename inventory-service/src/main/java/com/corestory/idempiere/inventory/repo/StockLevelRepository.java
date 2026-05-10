package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.StockLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    /**
     * Aggregate lookup across all warehouses/locators for a given product.
     */
    List<StockLevel> findByProductId(Long productId);

    List<StockLevel> findByProductIdAndWarehouseId(Long productId, Long warehouseId);

    Optional<StockLevel> findByProductIdAndWarehouseIdAndLocatorId(
        Long productId, Long warehouseId, Long locatorId);

    /**
     * Locator-priority-ordered list of stock-level rows for a product (ascending priority_no).
     * The reservation algorithm walks this list and takes from the first row with
     * sufficient available qty.
     */
    @Query("""
        SELECT s FROM StockLevel s
        JOIN s.locator l
        WHERE s.product.id = :productId
        ORDER BY l.priorityNo ASC, s.id ASC
        """)
    List<StockLevel> findByProductIdOrderByLocatorPriority(@Param("productId") Long productId);

    /**
     * Optimistic-locked load — used inside ReservationService.reserve() to detect concurrent
     * decrement on the same row. {@code @Version} on AuditableEntity does the actual check.
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT s FROM StockLevel s WHERE s.id = :id")
    Optional<StockLevel> findByIdWithOptimisticLock(@Param("id") Long id);
}
