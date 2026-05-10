package com.corestory.idempiere.shipping.model;

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
 * Carrier (UPS, FedEx, DHL, USPS, generic). iDempiere parity: {@code C_BPartner} where
 * {@code IsShipperCompany=Y} (legacy stores carrier as a partner).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "carriers")
public class Carrier extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "scac_code", length = 8)
    private String scacCode;

    @Column(name = "supports_tracking", nullable = false)
    private Boolean supportsTracking = Boolean.TRUE;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(name = "requires_label", nullable = false)
    private Boolean requiresLabel = Boolean.TRUE;

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carrier that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
