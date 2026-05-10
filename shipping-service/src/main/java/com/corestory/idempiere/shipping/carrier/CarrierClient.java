package com.corestory.idempiere.shipping.carrier;

/**
 * Port for outbound carrier API integration (UPS, FedEx, etc.). <b>Declared, no
 * implementations.</b> The recorded brownfield-feature-implementation demo (SHIP-101)
 * surfaces the missing adapters as gap-analysis findings.
 */
public interface CarrierClient {

    String carrierCode();

    /**
     * Submit a shipment to the carrier and obtain a tracking number / external id.
     */
    CarrierResponse submitShipment(CarrierShipmentRequest request);

    record CarrierShipmentRequest(
        String shipmentDocumentNo,
        String fromAddressJson,
        String toAddressJson,
        Double weightKg,
        String serviceCode
    ) {}

    record CarrierResponse(
        boolean accepted,
        String trackingNumber,
        String externalReference,
        String errorCode,
        String errorMessage
    ) {}
}
