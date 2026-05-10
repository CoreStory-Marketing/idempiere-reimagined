package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.AuditLogInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogInventoryRepository extends JpaRepository<AuditLogInventory, Long> {

    List<AuditLogInventory> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
