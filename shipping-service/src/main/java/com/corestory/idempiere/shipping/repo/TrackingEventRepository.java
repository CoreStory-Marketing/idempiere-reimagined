package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByShipmentIdOrderByEventAtDesc(Long shipmentId);
}
