package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.CarrierService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarrierServiceRepository extends JpaRepository<CarrierService, Long> {

    List<CarrierService> findByCarrierId(Long carrierId);
}
