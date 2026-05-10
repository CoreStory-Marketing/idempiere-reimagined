package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByDocumentNo(String documentNo);
}
