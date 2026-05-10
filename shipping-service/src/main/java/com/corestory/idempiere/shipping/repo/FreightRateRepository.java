package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.FreightRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FreightRateRepository extends JpaRepository<FreightRate, Long> {

    List<FreightRate> findByCarrierId(Long carrierId);
}
