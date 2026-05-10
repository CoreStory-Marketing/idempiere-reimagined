package com.corestory.idempiere.common.dto;

import java.time.Instant;
import java.util.List;

/**
 * Canonical error envelope. All services return this shape.
 */
public record ApiError(
    String code,
    String message,
    Instant timestamp,
    String path,
    List<Detail> details
) {

    public static ApiError of(String code, String message, String path) {
        return new ApiError(code, message, Instant.now(), path, List.of());
    }

    public record Detail(String field, String reason) {}
}
