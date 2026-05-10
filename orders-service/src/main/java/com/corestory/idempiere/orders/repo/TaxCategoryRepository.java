package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.TaxCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaxCategoryRepository extends JpaRepository<TaxCategory, Long> {

    Optional<TaxCategory> findByCode(String code);
}
