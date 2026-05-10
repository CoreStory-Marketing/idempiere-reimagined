package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.TransferOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferOrderRepository extends JpaRepository<TransferOrder, Long> {

    Optional<TransferOrder> findByDocumentNo(String documentNo);

    List<TransferOrder> findByStatus(String status);

    List<TransferOrder> findByFromWarehouseIdOrToWarehouseId(Long fromWarehouseId, Long toWarehouseId);
}
