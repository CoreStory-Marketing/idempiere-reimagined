package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.WarehouseDto;
import com.corestory.idempiere.inventory.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WarehouseMapper {

    @Mapping(source = "active", target = "isActive")
    WarehouseDto toDto(Warehouse entity);

    List<WarehouseDto> toDtoList(List<Warehouse> entities);
}
