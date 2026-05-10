package com.corestory.idempiere.inventory.model;

/**
 * iDempiere parity: {@code MAttributeSet.MandatoryType} discriminator.
 * Controls whether attribute-instances must carry a serial number, lot, or neither
 * before stock can be reserved/shipped.
 */
public enum MandatoryType {
    SERIAL,
    LOT,
    NONE
}
