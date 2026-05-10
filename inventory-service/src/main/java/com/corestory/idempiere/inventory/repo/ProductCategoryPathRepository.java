package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.ProductCategoryPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryPathRepository extends JpaRepository<ProductCategoryPath, Long> {

    List<ProductCategoryPath> findByCategoryId(Long categoryId);

    List<ProductCategoryPath> findByPathStartingWith(String prefix);
}
