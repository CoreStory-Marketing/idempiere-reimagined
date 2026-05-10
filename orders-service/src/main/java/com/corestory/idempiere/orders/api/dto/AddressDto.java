package com.corestory.idempiere.orders.api.dto;

import com.corestory.idempiere.orders.model.AddressType;

public record AddressDto(
    Long id,
    Long customerId,
    String address1,
    String address2,
    String city,
    Long regionId,
    Long countryId,
    String postalCode,
    AddressType addressType,
    Boolean active
) {}
