package com.corestory.idempiere.warehouse.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateVendorRequest(
    @NotBlank String documentNo,
    @NotBlank String name,
    Long defaultAddressId,
    Long paymentTermId
) {}
