package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.TransferOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferOrderLineRepository extends JpaRepository<TransferOrderLine, Long> {

    List<TransferOrderLine> findByTransferOrderId(Long transferOrderId);
}
