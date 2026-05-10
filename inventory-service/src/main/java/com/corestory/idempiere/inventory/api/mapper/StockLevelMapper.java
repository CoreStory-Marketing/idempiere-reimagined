package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.StockLevelDto;
import com.corestory.idempiere.inventory.model.StockLevel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface StockLevelMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "locator.id", target = "locatorId")
    @Mapping(target = "qtyAvailable", expression = "java(entity == null ? java.math.BigDecimal.ZERO : entity.getQtyAvailable())")
    StockLevelDto toDto(StockLevel entity);

    List<StockLevelDto> toDtoList(List<StockLevel> entities);
}
