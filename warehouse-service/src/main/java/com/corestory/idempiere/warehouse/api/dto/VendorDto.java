package com.corestory.idempiere.warehouse.api.dto;

import com.corestory.idempiere.warehouse.model.Vendor;

public record VendorDto(
    Long id,
    String documentNo,
    String name,
    Long defaultAddressId,
    Long paymentTermId,
    Boolean active
) {

    public static VendorDto from(Vendor v) {
        return new VendorDto(
            v.getId(), v.getDocumentNo(), v.getName(),
            v.getDefaultAddressId(), v.getPaymentTermId(), v.getActive()
        );
    }
}
