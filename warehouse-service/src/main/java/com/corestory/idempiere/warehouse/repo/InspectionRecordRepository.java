package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, Long> {

    List<InspectionRecord> findByReceiptLineId(Long receiptLineId);
}
