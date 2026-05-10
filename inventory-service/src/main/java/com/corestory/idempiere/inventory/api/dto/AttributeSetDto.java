package com.corestory.idempiere.inventory.api.dto;

import com.corestory.idempiere.inventory.model.MandatoryType;

public record AttributeSetDto(
    Long id,
    String name,
    MandatoryType mandatoryType,
    boolean isLotMandatory,
    boolean isSerialMandatory,
    boolean isActive
) {}
