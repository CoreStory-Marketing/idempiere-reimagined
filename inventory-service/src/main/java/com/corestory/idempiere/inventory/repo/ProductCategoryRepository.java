package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long> {

    List<ProductCategory> findByParentId(Long parentId);

    List<ProductCategory> findByIsActiveTrue();
}
