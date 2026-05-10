package com.corestory.idempiere.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * iDempiere parity: {@code M_InventoryLine}. {@code variance = qtyCount - qtyBook}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "inventory_count_lines")
public class InventoryCountLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "inventory_count_id", nullable = false)
    private InventoryCount inventoryCount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "locator_id")
    private Locator locator;

    @Column(name = "qty_book", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyBook = BigDecimal.ZERO;

    @Column(name = "qty_count", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyCount = BigDecimal.ZERO;

    @Column(name = "variance", nullable = false, precision = 19, scale = 4)
    private BigDecimal variance = BigDecimal.ZERO;
}
