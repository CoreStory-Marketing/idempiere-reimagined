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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * Business partner who can be a customer, vendor, or both.
 *
 * <p>iDempiere parity: {@code C_BPartner} (universal partner pattern with
 * {@code IsCustomer} / {@code IsVendor} flags).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customers")
public class Customer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "document_no", nullable = false, unique = true, length = 64)
    private String documentNo;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "name2", length = 255)
    private String name2;

    @Column(name = "tax_id", length = 64)
    private String taxId;

    @Column(name = "is_customer", nullable = false)
    private Boolean customer = Boolean.TRUE;

    @Column(name = "is_vendor", nullable = false)
    private Boolean vendor = Boolean.FALSE;

    /**
     * Default address — managed via {@code default_address_id} column. Loaded lazily
     * to avoid the chicken-and-egg with {@link Address#getCustomer()}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_address_id")
    private Address defaultAddress;

    @Column(name = "is_active", nullable = false)
    private Boolean active = Boolean.TRUE;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
