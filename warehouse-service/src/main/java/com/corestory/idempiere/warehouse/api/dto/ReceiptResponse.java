package com.corestory.idempiere.warehouse.api.dto;

import com.corestory.idempiere.warehouse.model.Receipt;
import com.corestory.idempiere.warehouse.model.ReceiptLine;
import com.corestory.idempiere.warehouse.model.ReceiptStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ReceiptResponse(
    Long id,
    String documentNo,
    ReceiptStatus status,
    Long vendorId,
    String vendorInvoiceNo,
    Long purchaseOrderId,
    Long warehouseId,
    LocalDate receiptDate,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<LineResponse> lines
) {

    public record LineResponse(
        Long id,
        Integer lineNo,
        Long productId,
        BigDecimal qtyReceived,
        BigDecimal qtyInspected,
        BigDecimal qtyAccepted,
        Long locatorId
    ) {
        public static LineResponse from(ReceiptLine line) {
            return new LineResponse(
                line.getId(),
                line.getLineNo(),
                line.getProductId(),
                line.getQtyReceived(),
                line.getQtyInspected(),
                line.getQtyAccepted(),
                line.getLocatorId()
            );
        }
    }

    public static ReceiptResponse from(Receipt r) {
        return new ReceiptResponse(
            r.getId(),
            r.getDocumentNo(),
            r.getStatus(),
            r.getVendor() != null ? r.getVendor().getId() : null,
            r.getVendorInvoiceNo(),
            r.getPurchaseOrder() != null ? r.getPurchaseOrder().getId() : null,
            r.getWarehouseId(),
            r.getReceiptDate(),
            r.getNotes(),
            r.getCreatedAt(),
            r.getUpdatedAt(),
            r.getLines().stream().map(LineResponse::from).toList()
        );
    }
}
