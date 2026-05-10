package com.corestory.idempiere.shipping.api.dto;

import com.corestory.idempiere.shipping.model.Carrier;

public record CarrierDto(
    Long id,
    String code,
    String name,
    String scacCode,
    Boolean supportsTracking,
    String apiEndpoint,
    Boolean requiresLabel,
    Boolean active
) {

    public static CarrierDto from(Carrier c) {
        return new CarrierDto(
            c.getId(), c.getCode(), c.getName(), c.getScacCode(),
            c.getSupportsTracking(), c.getApiEndpoint(), c.getRequiresLabel(), c.getActive()
        );
    }
}
