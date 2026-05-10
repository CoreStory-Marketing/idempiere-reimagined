package com.corestory.idempiere.orders.api.dto;

public record ContactDto(
    Long id,
    Long customerId,
    String name,
    String email,
    String phone,
    Boolean primary,
    Boolean active
) {}
