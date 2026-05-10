package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.Receipt;
import com.corestory.idempiere.warehouse.model.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByDocumentNo(String documentNo);

    Page<Receipt> findByStatus(ReceiptStatus status, Pageable pageable);

    Page<Receipt> findByVendorId(Long vendorId, Pageable pageable);
}
