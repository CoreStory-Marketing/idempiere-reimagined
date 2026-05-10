package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.AuditLogOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogOrdersRepository extends JpaRepository<AuditLogOrders, Long> {

    List<AuditLogOrders> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
