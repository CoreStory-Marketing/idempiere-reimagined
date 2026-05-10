package com.corestory.idempiere.warehouse.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "receipt_lines",
    uniqueConstraints = @UniqueConstraint(columnNames = {"receipt_id", "line_no"})
)
public class ReceiptLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "qty_received", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyReceived = BigDecimal.ZERO;

    @Column(name = "qty_inspected", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyInspected = BigDecimal.ZERO;

    @Column(name = "qty_accepted", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyAccepted = BigDecimal.ZERO;

    @Column(name = "locator_id")
    private Long locatorId;

    @OneToMany(mappedBy = "receiptLine", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InspectionRecord> inspections = new ArrayList<>();

    public void addInspection(InspectionRecord ir) {
        ir.setReceiptLine(this);
        this.inspections.add(ir);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReceiptLine that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
