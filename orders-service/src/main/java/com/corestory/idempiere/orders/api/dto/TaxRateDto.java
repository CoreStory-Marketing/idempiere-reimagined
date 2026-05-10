package com.corestory.idempiere.orders.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TaxRateDto(
    Long id,
    Long taxCategoryId,
    BigDecimal ratePct,
    LocalDate validFrom,
    LocalDate validTo,
    Long countryId,
    Long regionId,
    Boolean active
) {}
