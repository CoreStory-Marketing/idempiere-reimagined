package com.corestory.idempiere.inventory.api.dto;

import java.math.BigDecimal;

public record ProductDto(
    Long id,
    String sku,
    String name,
    String description,
    Long productCategoryId,
    Long uomId,
    Long attributeSetId,
    boolean isStocked,
    boolean isActive,
    BigDecimal weight,
    BigDecimal volume
) {}
