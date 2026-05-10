package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    Optional<ProductPrice> findByPriceListVersionIdAndProductId(Long priceListVersionId, Long productId);

    List<ProductPrice> findByPriceListVersionId(Long priceListVersionId);
}
