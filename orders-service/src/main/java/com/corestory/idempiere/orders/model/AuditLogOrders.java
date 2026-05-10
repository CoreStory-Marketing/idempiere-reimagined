package com.corestory.idempiere.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Lightweight per-service audit log row.
 *
 * <p>Records mutations to orders-service entities as before/after JSON snapshots so that
 * compliance reporting can replay history without depending on Hibernate Envers.
 *
 * <p>iDempiere parity: similar in spirit to {@code AD_ChangeLog}, but stored once per
 * service rather than centrally.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_log_orders",
       indexes = @Index(name = "idx_audit_log_orders_entity", columnList = "entity_type, entity_id"))
public class AuditLogOrders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "action", nullable = false, length = 16)
    private String action;

    @Column(name = "changed_by", nullable = false, length = 64)
    private String changedBy = "system";

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt = OffsetDateTime.now();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before_json", columnDefinition = "jsonb")
    private String beforeJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after_json", columnDefinition = "jsonb")
    private String afterJson;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuditLogOrders that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
