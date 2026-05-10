package com.corestory.idempiere.shipping.repo;

import com.corestory.idempiere.shipping.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    List<Package> findByShipmentId(Long shipmentId);
}
