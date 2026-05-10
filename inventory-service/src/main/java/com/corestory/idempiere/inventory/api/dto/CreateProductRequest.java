package com.corestory.idempiere.inventory.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank @Size(max = 64) String sku,
    @NotBlank @Size(max = 255) String name,
    String description,
    Long productCategoryId,
    @NotNull Long uomId,
    Long attributeSetId,
    Boolean isStocked,
    BigDecimal weight,
    BigDecimal volume
) {}
