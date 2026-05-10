package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.InventoryCountDto;
import com.corestory.idempiere.inventory.model.InventoryCount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InventoryCountMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    InventoryCountDto toDto(InventoryCount entity);

    List<InventoryCountDto> toDtoList(List<InventoryCount> entities);
}
