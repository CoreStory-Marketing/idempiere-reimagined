package com.corestory.idempiere.warehouse.api.dto;

import com.corestory.idempiere.warehouse.model.PurchaseOrder;
import com.corestory.idempiere.warehouse.model.PurchaseOrderLine;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderDto(
    Long id,
    String documentNo,
    Long vendorId,
    String status,
    LocalDate expectedDate,
    List<Line> lines
) {

    public record Line(
        Long id,
        Long productId,
        BigDecimal qtyOrdered,
        BigDecimal qtyDelivered
    ) {
        public static Line from(PurchaseOrderLine pol) {
            return new Line(pol.getId(), pol.getProductId(), pol.getQtyOrdered(), pol.getQtyDelivered());
        }
    }

    public static PurchaseOrderDto from(PurchaseOrder po) {
        return new PurchaseOrderDto(
            po.getId(),
            po.getDocumentNo(),
            po.getVendor() != null ? po.getVendor().getId() : null,
            po.getStatus(),
            po.getExpectedDate(),
            po.getLines().stream().map(Line::from).toList()
        );
    }
}
