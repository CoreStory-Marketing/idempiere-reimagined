package com.corestory.idempiere.shipping.carrier;

/**
 * Port for label-PDF generation. <b>Declared, no implementations.</b>
 */
public interface LabelGenerator {

    LabelResult generate(LabelRequest request);

    record LabelRequest(
        Long shipmentId,
        String shipmentDocumentNo,
        String trackingNumber,
        String carrierCode
    ) {}

    record LabelResult(
        byte[] pdfBytes,
        String mimeType,
        String storageUrl
    ) {}
}
