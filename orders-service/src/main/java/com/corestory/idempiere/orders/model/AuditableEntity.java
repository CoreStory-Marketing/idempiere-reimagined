package com.corestory.idempiere.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

/**
 * Base superclass for all auditable JPA entities.
 *
 * <p>Maps the canonical multi-tenant + audit columns shared across every table in
 * {@code V1__init.sql}: {@code tenant_id}, {@code org_id}, {@code created_at},
 * {@code updated_at}, {@code created_by}, {@code updated_by}, {@code version}.
 *
 * <p>iDempiere parity: legacy uses {@code AD_Client_ID}, {@code AD_Org_ID},
 * {@code Created}, {@code CreatedBy}, {@code Updated}, {@code UpdatedBy}.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId = 1L;

    @Column(name = "org_id", nullable = false)
    private Long orgId = 1L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 64)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by", nullable = false, length = 64)
    private String updatedBy;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
