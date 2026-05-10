package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.PriceListVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceListVersionRepository extends JpaRepository<PriceListVersion, Long> {

    List<PriceListVersion> findByPriceListId(Long priceListId);
}
