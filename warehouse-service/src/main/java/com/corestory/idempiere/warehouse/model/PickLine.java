package com.corestory.idempiere.warehouse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * <b>STUBBED.</b> See {@link Pick}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "pick_lines")
public class PickLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pick_id", nullable = false)
    private Pick pick;

    @Column(name = "order_line_id", nullable = false)
    private Long orderLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "qty_required", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyRequired = BigDecimal.ZERO;

    @Column(name = "qty_picked", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyPicked = BigDecimal.ZERO;

    @Column(name = "locator_id")
    private Long locatorId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PickLine that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
