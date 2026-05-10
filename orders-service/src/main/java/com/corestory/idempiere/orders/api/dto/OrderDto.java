package com.corestory.idempiere.orders.api.dto;

import com.corestory.idempiere.orders.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderDto(
    Long id,
    String documentNo,
    OrderStatus status,
    Long customerId,
    Long billToAddressId,
    Long shipToAddressId,
    Long paymentTermId,
    Long priceListId,
    Long incotermId,
    String currency,
    BigDecimal totalAmount,
    BigDecimal taxAmount,
    BigDecimal grandTotal,
    LocalDate orderDate,
    LocalDate promisedDate,
    String notes,
    Boolean active,
    List<OrderLineDto> lines,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String createdBy,
    String updatedBy,
    Long version
) {}
