package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.Shipment;
import com.corestory.idempiere.shipping.model.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Shipment query surface. Service layer is intentionally absent for the demo — the
 * brownfield-feature-implementation skill (SHIP-101) wires up
 * {@code ShipmentService} and the controller's POST handlers against this repo.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByOrderId(Long orderId);

    Optional<Shipment> findByDocumentNo(String documentNo);

    Page<Shipment> findByStatus(ShipmentStatus status, Pageable pageable);
}
