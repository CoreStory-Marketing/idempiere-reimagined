package com.corestory.idempiere.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Per-product directed put-away rule: when receipts post, lines with this product are
 * suggested into the indicated locator. Higher-priority rule wins.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "put_away_rules")
public class PutAwayRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "locator_id", nullable = false)
    private Long locatorId;

    @Column(name = "priority", nullable = false)
    private Short priority = 50;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PutAwayRule that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
