package com.corestory.idempiere.orders.exception;

/**
 * Base type for all checked / domain exceptions thrown by orders-service.
 * Subclasses are translated to {@link com.corestory.idempiere.common.dto.ApiError}
 * envelopes by {@link RestExceptionHandler}.
 */
public class OrderException extends RuntimeException {

    private final String code;

    public OrderException(String code, String message) {
        super(message);
        this.code = code;
    }

    public OrderException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
