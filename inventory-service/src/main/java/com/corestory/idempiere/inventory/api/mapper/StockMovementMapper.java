package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.StockMovementDto;
import com.corestory.idempiere.inventory.model.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockMovementMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "fromLocator.id", target = "fromLocatorId")
    @Mapping(source = "toLocator.id", target = "toLocatorId")
    StockMovementDto toDto(StockMovement entity);

    List<StockMovementDto> toDtoList(List<StockMovement> entities);
}
