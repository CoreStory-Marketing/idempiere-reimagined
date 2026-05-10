package com.corestory.idempiere.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Append-only audit row written on every {@link Order} status transition.
 *
 * <p>Unlike the auditable entities this is intentionally NOT extending
 * {@code AuditableEntity} — the schema for {@code order_status_history} only carries
 * {@code changed_at} / {@code changed_by} columns (see {@code V1__init.sql}).
 *
 * <p>iDempiere parity: comparable to the {@code DocStatus} change rows in the
 * {@code AD_ChangeLog} infrastructure scoped to a single document column.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "order_status_history",
       indexes = @Index(name = "idx_order_status_history_order", columnList = "order_id"))
public class OrderStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 32)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 32)
    private OrderStatus toStatus;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt = OffsetDateTime.now();

    @Column(name = "changed_by", nullable = false, length = 64)
    private String changedBy = "system";

    @Column(name = "reason", length = 512)
    private String reason;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderStatusHistory that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
