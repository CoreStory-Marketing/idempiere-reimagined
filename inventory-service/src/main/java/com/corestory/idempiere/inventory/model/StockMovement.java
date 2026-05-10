package com.corestory.idempiere.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Append-only inventory ledger. iDempiere parity: {@code M_Transaction} / {@code MTransaction}.
 *
 * <p>Sign convention: positive {@code qty} = stock added (RECEIPT, ADJUSTMENT-in,
 * TRANSFER-in); negative = stock removed (SHIPMENT / reservation hold,
 * ADJUSTMENT-out, TRANSFER-out).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movement_date", nullable = false)
    private OffsetDateTime movementDate = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 16)
    private MovementType movementType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "qty", nullable = false, precision = 19, scale = 4)
    private BigDecimal qty;

    @ManyToOne
    @JoinColumn(name = "from_locator_id")
    private Locator fromLocator;

    @ManyToOne
    @JoinColumn(name = "to_locator_id")
    private Locator toLocator;

    @Column(name = "reference_doc_id")
    private Long referenceDocId;

    @Column(name = "reference_doc_type", length = 32)
    private String referenceDocType;
}
