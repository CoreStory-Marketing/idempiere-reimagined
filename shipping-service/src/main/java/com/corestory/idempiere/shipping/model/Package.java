package com.corestory.idempiere.shipping.model;

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
 * Physical package within a shipment. iDempiere parity: {@code M_Package}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "packages",
    uniqueConstraints = @UniqueConstraint(columnNames = {"shipment_id", "package_no"})
)
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "package_no", nullable = false, length = 64)
    private String packageNo;

    @Column(name = "weight", precision = 19, scale = 4)
    private BigDecimal weight;

    @Column(name = "length", precision = 19, scale = 4)
    private BigDecimal length;

    @Column(name = "width", precision = 19, scale = 4)
    private BigDecimal width;

    @Column(name = "height", precision = 19, scale = 4)
    private BigDecimal height;

    @Column(name = "package_type", length = 32)
    private String packageType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Package that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
