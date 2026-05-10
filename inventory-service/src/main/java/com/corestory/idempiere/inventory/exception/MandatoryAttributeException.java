package com.corestory.idempiere.inventory.exception;

/**
 * Thrown when an operation requires a lot or serial number (per the product's attribute set
 * mandatory_type) and none was supplied. iDempiere parity: validation in {@code MAttributeSet.checkMandatory}.
 */
public class MandatoryAttributeException extends RuntimeException {

    private final Long productId;
    private final String missingAttribute;

    public MandatoryAttributeException(Long productId, String missingAttribute) {
        super(String.format(
            "Product %d requires %s; none supplied", productId, missingAttribute));
        this.productId = productId;
        this.missingAttribute = missingAttribute;
    }

    public Long getProductId() { return productId; }
    public String getMissingAttribute() { return missingAttribute; }
}
