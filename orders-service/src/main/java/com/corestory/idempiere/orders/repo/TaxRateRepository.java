package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaxRateRepository extends JpaRepository<TaxRate, Long> {

    List<TaxRate> findByTaxCategoryId(Long taxCategoryId);

    List<TaxRate> findByCountryId(Long countryId);
}
