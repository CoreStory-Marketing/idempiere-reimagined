package com.corestory.idempiere.shipping.api.dto;

/**
 * Request body for {@code POST /shipments/{id}/ship}. Stub today; SHIP-101 implements it.
 */
public record ShipRequest(
    String trackingNumber,
    Boolean sendEmailFlag
) {}
