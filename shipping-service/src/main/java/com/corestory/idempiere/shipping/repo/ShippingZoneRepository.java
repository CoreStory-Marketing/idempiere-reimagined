package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.ShippingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingZoneRepository extends JpaRepository<ShippingZone, Long> {
}
