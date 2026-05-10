package com.corestory.idempiere.shipping.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Outbound shipment — shipping side of legacy {@code MInOut}.
 *
 * <p>iDempiere parity: {@code MInOut} where {@code MovementType='C-'} (customer return / shipment).
 * The {@code SendEMail} flag at {@code MInOut.java:599} maps to {@code send_email_flag} below —
 * downstream notifications-service uses it to decide whether to dispatch the shipment-confirmation
 * email when consuming the {@code ShipmentCreatedEvent}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "shipments")
public class Shipment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "document_no", nullable = false, unique = true, length = 64)
    private String documentNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ShipmentStatus status = ShipmentStatus.DRAFT;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "ship_to_address_id", nullable = false)
    private Long shipToAddressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;

    @Column(name = "tracking_number", length = 64)
    private String trackingNumber;

    @Column(name = "send_email_flag", nullable = false)
    private Boolean sendEmailFlag = Boolean.TRUE;

    @Column(name = "weight_total", precision = 19, scale = 4)
    private BigDecimal weightTotal;

    @Column(name = "freight_amount", precision = 19, scale = 4)
    private BigDecimal freightAmount;

    @Column(name = "ship_date")
    private OffsetDateTime shipDate;

    @Column(name = "delivery_date")
    private OffsetDateTime deliveryDate;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Package> packages = new ArrayList<>();

    public void addLine(ShipmentLine line) {
        line.setShipment(this);
        if (line.getLineNo() == null) {
            line.setLineNo(lines.size() + 1);
        }
        lines.add(line);
    }

    public void addPackage(Package pkg) {
        pkg.setShipment(this);
        packages.add(pkg);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shipment that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
