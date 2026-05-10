package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.ReservationDto;
import com.corestory.idempiere.inventory.model.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReservationMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "locator.id", target = "locatorId")
    ReservationDto toDto(Reservation entity);

    List<ReservationDto> toDtoList(List<Reservation> entities);
}
