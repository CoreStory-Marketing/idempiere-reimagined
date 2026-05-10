package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.MovementType;
import com.corestory.idempiere.inventory.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    Page<StockMovement> findByProductIdAndMovementDateBetween(
        Long productId, OffsetDateTime start, OffsetDateTime end, Pageable pageable);

    Page<StockMovement> findByMovementType(MovementType type, Pageable pageable);

    Page<StockMovement> findByMovementDateBetween(OffsetDateTime start, OffsetDateTime end, Pageable pageable);
}
