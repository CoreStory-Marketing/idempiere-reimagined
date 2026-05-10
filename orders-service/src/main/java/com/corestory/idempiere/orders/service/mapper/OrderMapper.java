package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.OrderDto;
import com.corestory.idempiere.orders.api.dto.OrderLineDto;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Entity → DTO mapper for {@link Order} and {@link OrderLine}. The reverse
 * (DTO → Entity) direction is intentionally NOT defined here — order writes go
 * through {@link com.corestory.idempiere.orders.service.OrderService} which
 * resolves FK references and runs business logic.
 */
@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "billToAddressId", source = "billToAddress.id")
    @Mapping(target = "shipToAddressId", source = "shipToAddress.id")
    @Mapping(target = "paymentTermId", source = "paymentTerm.id")
    @Mapping(target = "priceListId", source = "priceList.id")
    @Mapping(target = "incotermId", source = "incoterm.id")
    @Mapping(target = "lines", source = "lines")
    OrderDto toDto(Order entity);

    List<OrderDto> toDtoList(List<Order> entities);

    @Mapping(target = "taxRateId", source = "taxRate.id")
    OrderLineDto toLineDto(OrderLine line);

    List<OrderLineDto> toLineDtoList(List<OrderLine> lines);
}
