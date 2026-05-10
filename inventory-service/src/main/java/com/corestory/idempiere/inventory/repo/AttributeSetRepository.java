package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.AttributeSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeSetRepository extends JpaRepository<AttributeSet, Long> {

    List<AttributeSet> findByIsActiveTrue();
}
