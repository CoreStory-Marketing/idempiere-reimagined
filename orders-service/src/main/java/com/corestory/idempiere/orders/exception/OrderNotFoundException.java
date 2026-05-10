package com.corestory.idempiere.orders.exception;

public class OrderNotFoundException extends OrderException {

    public OrderNotFoundException(Long orderId) {
        super("ORDER_NOT_FOUND", "Order not found: id=" + orderId);
    }

    public OrderNotFoundException(String documentNo) {
        super("ORDER_NOT_FOUND", "Order not found: documentNo=" + documentNo);
    }
}
