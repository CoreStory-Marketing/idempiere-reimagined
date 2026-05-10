package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.UomConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UomConversionRepository extends JpaRepository<UomConversion, Long> {

    Optional<UomConversion> findByFromUomIdAndToUomId(Long fromUomId, Long toUomId);

    List<UomConversion> findByFromUomId(Long fromUomId);
}
