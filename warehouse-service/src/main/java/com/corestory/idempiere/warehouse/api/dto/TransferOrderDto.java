package com.corestory.idempiere.warehouse.api.dto;

import com.corestory.idempiere.warehouse.model.TransferOrder;
import com.corestory.idempiere.warehouse.model.TransferOrderLine;

import java.math.BigDecimal;
import java.util.List;

public record TransferOrderDto(
    Long id,
    String documentNo,
    String status,
    Long fromWarehouseId,
    Long toWarehouseId,
    List<Line> lines
) {

    public record Line(Long id, Long productId, BigDecimal qty) {
        public static Line from(TransferOrderLine l) {
            return new Line(l.getId(), l.getProductId(), l.getQty());
        }
    }

    public static TransferOrderDto from(TransferOrder t) {
        return new TransferOrderDto(
            t.getId(), t.getDocumentNo(), t.getStatus(),
            t.getFromWarehouseId(), t.getToWarehouseId(),
            t.getLines().stream().map(Line::from).toList()
        );
    }
}
