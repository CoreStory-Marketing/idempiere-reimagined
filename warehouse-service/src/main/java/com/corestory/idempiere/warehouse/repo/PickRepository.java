package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.Pick;
import com.corestory.idempiere.warehouse.model.PickStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * <b>STUBBED.</b> Repository declared but no service-layer caller wires picks together yet.
 */
@Repository
public interface PickRepository extends JpaRepository<Pick, Long> {

    Optional<Pick> findByDocumentNo(String documentNo);

    List<Pick> findByOrderId(Long orderId);

    List<Pick> findByStatus(PickStatus status);
}
