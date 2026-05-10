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
 * iDempiere parity: {@code M_Product} / {@code MProduct}.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
public class Product extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, length = 64, unique = true)
    private String sku;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "product_category_id")
    private ProductCategory productCategory;

    @ManyToOne
    @JoinColumn(name = "uom_id")
    private UnitOfMeasure uom;

    @ManyToOne
    @JoinColumn(name = "attribute_set_id")
    private AttributeSet attributeSet;

    @Column(name = "is_stocked", nullable = false)
    private boolean isStocked = true;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "weight", precision = 19, scale = 4)
    private BigDecimal weight;

    @Column(name = "volume", precision = 19, scale = 4)
    private BigDecimal volume;
}
