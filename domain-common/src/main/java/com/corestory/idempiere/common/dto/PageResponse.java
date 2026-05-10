package com.corestory.idempiere.common.dto;

import java.util.List;

/**
 * Standard wrapper for paginated list responses.
 */
public record PageResponse<T>(
    List<T> items,
    int page,
    int pageSize,
    long total,
    int totalPages
) {

    public static <T> PageResponse<T> of(List<T> items, int page, int pageSize, long total) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) total / (double) pageSize);
        return new PageResponse<>(items, page, pageSize, total, totalPages);
    }
}
