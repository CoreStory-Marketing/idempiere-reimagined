package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.ShipmentLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShipmentLineRepository extends JpaRepository<ShipmentLine, Long> {

    List<ShipmentLine> findByShipmentId(Long shipmentId);
}
