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
 * Vendor / supplier — inbound side of {@code C_BPartner} where {@code IsVendor=Y}.
 * Maps to the {@code vendors} table.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendors")
public class Vendor extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "document_no", nullable = false, unique = true, length = 64)
    private String documentNo;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "default_address_id")
    private Long defaultAddressId;

    @Column(name = "payment_term_id")
    private Long paymentTermId;

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vendor that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
