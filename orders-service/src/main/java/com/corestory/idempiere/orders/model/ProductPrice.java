package com.corestory.idempiere.orders.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Per-product entry in a {@link PriceListVersion}.
 *
 * <p>iDempiere parity: {@code M_ProductPrice}. Note {@code product_id} is a long
 * pointer rather than an FK — products live in the {@code inventory-service} DB.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product_prices",
       uniqueConstraints = @UniqueConstraint(columnNames = {"price_list_version_id", "product_id"}))
public class ProductPrice extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "price_list_version_id", nullable = false)
    private PriceListVersion priceListVersion;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "list_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal listPrice;

    @Column(name = "std_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal stdPrice;

    @Column(name = "limit_price", precision = 19, scale = 4)
    private BigDecimal limitPrice;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductPrice that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
