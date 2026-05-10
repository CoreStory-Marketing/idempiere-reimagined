package com.corestory.idempiere.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * The on-hand / reserved / on-order working set for a (product, warehouse, locator) triple.
 *
 * <p>iDempiere parity: {@code M_StorageOnHand} (qty_on_hand half) and {@code M_StorageReservation}
 * (legacy splits these into two tables; we mirror the two columns into one row, plus the
 * {@code reservations} table for the reservation document trail).
 *
 * <p>The {@code @Version} column on {@link AuditableEntity} provides optimistic locking that
 * powers the concurrent-reservation correctness guarantee: two transactions racing for the
 * last unit will produce one OptimisticLockException, the loser retries (or fails) cleanly.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_levels",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id", "locator_id"}))
public class StockLevel extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne
    @JoinColumn(name = "locator_id")
    private Locator locator;

    @Column(name = "qty_on_hand", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyOnHand = BigDecimal.ZERO;

    @Column(name = "qty_reserved", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyReserved = BigDecimal.ZERO;

    @Column(name = "qty_ordered", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyOrdered = BigDecimal.ZERO;

    /**
     * Quantity currently available for reservation = on_hand - reserved.
     */
    public BigDecimal getQtyAvailable() {
        BigDecimal onHand = qtyOnHand == null ? BigDecimal.ZERO : qtyOnHand;
        BigDecimal reserved = qtyReserved == null ? BigDecimal.ZERO : qtyReserved;
        return onHand.subtract(reserved);
    }
}
