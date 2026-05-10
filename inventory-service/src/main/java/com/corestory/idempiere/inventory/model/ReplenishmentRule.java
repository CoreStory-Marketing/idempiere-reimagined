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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * iDempiere parity: {@code M_Replenish} / {@code MReplenish}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "replenishment_rules",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
public class ReplenishmentRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "replenish_type", nullable = false, length = 16)
    private ReplenishmentType replenishType = ReplenishmentType.REORDER;

    @Column(name = "min_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal minLevel = BigDecimal.ZERO;

    @Column(name = "max_level", nullable = false, precision = 19, scale = 4)
    private BigDecimal maxLevel = BigDecimal.ZERO;
}
