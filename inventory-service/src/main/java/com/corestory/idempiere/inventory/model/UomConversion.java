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
 * Conversion factor between two {@link UnitOfMeasure}s.
 * iDempiere parity: {@code C_UOM_Conversion} / {@code MUOMConversion}.
 *
 * <p>Either {@code multiplyRate} or {@code divideRate} is sufficient; legacy
 * stores both for query convenience.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "uom_conversions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"from_uom_id", "to_uom_id"}))
public class UomConversion extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_uom_id", nullable = false)
    private UnitOfMeasure fromUom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_uom_id", nullable = false)
    private UnitOfMeasure toUom;

    @Column(name = "multiply_rate", precision = 19, scale = 8)
    private BigDecimal multiplyRate;

    @Column(name = "divide_rate", precision = 19, scale = 8)
    private BigDecimal divideRate;

    /**
     * Convert a quantity from {@code fromUom} to {@code toUom}.
     * Rule: if {@code multiplyRate} present, multiply. Otherwise if {@code divideRate} present, divide.
     */
    public BigDecimal convert(BigDecimal qty) {
        if (qty == null) {
            return null;
        }
        if (multiplyRate != null) {
            return qty.multiply(multiplyRate);
        }
        if (divideRate != null && divideRate.signum() != 0) {
            return qty.divide(divideRate, toUom.getStdPrecision(), java.math.RoundingMode.HALF_EVEN);
        }
        throw new IllegalStateException("UomConversion " + id + " has neither multiplyRate nor divideRate");
    }
}
