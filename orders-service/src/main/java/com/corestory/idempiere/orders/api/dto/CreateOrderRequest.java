package com.corestory.idempiere.orders.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateOrderRequest(

    @NotNull
    Long customerId,

    Long billToAddressId,

    Long shipToAddressId,

    Long paymentTermId,

    Long priceListId,

    Long incotermId,

    @NotNull
    @Size(min = 3, max = 3)
    @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code")
    String currency,

    LocalDate orderDate,

    LocalDate promisedDate,

    String notes,

    @NotEmpty(message = "At least one order line is required")
    @Valid
    List<CreateOrderLineRequest> lines
) {}
