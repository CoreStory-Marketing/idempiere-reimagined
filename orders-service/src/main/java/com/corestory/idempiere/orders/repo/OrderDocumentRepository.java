package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.OrderDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDocumentRepository extends JpaRepository<OrderDocument, Long> {

    List<OrderDocument> findByOrderId(Long orderId);
}
