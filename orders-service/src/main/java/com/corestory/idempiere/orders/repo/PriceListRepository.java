package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.PriceList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    Optional<PriceList> findFirstByDefaultListTrue();

    List<PriceList> findByCurrency(String currency);
}
