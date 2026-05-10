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

/**
 * Bin / shelf / aisle. iDempiere parity: {@code M_Locator} / {@code MLocator}.
 *
 * <p>{@code priorityNo} (legacy {@code PriorityNo}) controls FIFO-by-bin: lower
 * number wins. The reservation algorithm walks locators ascending by priority.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "locators",
       uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "code"}))
public class Locator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "x", length = 8)
    private String x;

    @Column(name = "y", length = 8)
    private String y;

    @Column(name = "z", length = 8)
    private String z;

    @Column(name = "priority_no", nullable = false)
    private Short priorityNo = 50;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
