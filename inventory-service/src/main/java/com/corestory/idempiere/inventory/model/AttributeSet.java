package com.corestory.idempiere.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * iDempiere parity: {@code M_AttributeSet} / {@code MAttributeSet}.
 *
 * <p>The {@code is_lot_mandatory} / {@code is_serial_mandatory} flags drive
 * reservation-time validation: a product whose attribute set requires a lot must
 * not be reserved without lot context.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attribute_sets")
public class AttributeSet extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "mandatory_type", nullable = false, length = 16)
    private MandatoryType mandatoryType = MandatoryType.NONE;

    @Column(name = "is_lot_mandatory", nullable = false)
    private boolean isLotMandatory = false;

    @Column(name = "is_serial_mandatory", nullable = false)
    private boolean isSerialMandatory = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
