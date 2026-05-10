package com.corestory.idempiere.shipping.carrier;

import java.math.BigDecimal;

/**
 * Port for rate quoting. <b>Declared, no implementations.</b>
 */
public interface RateQuoter {

    RateQuote quote(RateRequest request);

    record RateRequest(
        Long carrierId,
        Long carrierServiceId,
        Long originCountryId,
        Long destCountryId,
        BigDecimal weightKg
    ) {}

    record RateQuote(
        BigDecimal amount,
        String currency,
        Integer estimatedTransitDays
    ) {}
}
