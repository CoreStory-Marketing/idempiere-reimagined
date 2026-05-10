package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

    List<Attribute> findByAttributeSetId(Long attributeSetId);
}
