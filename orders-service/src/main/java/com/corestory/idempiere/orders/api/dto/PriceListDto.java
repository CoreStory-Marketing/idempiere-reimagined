package com.corestory.idempiere.orders.api.dto;

import java.time.LocalDate;

public record PriceListDto(
    Long id,
    String name,
    String currency,
    LocalDate validFrom,
    LocalDate validTo,
    Boolean defaultList,
    Boolean active
) {}
