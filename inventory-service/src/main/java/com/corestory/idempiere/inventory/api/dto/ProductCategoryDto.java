package com.corestory.idempiere.inventory.api.dto;

public record ProductCategoryDto(
    Long id,
    String name,
    Long parentCategoryId,
    boolean isSelfService,
    boolean isActive
) {}
