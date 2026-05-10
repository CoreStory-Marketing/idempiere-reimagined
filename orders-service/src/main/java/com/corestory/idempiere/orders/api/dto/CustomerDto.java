package com.corestory.idempiere.orders.api.dto;

public record CustomerDto(
    Long id,
    String documentNo,
    String name,
    String name2,
    String taxId,
    Boolean customer,
    Boolean vendor,
    Long defaultAddressId,
    Boolean active
) {}
