package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.ReceiptLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptLineRepository extends JpaRepository<ReceiptLine, Long> {

    List<ReceiptLine> findByReceiptId(Long receiptId);
}
