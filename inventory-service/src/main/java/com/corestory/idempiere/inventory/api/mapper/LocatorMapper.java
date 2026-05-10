package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.LocatorDto;
import com.corestory.idempiere.inventory.model.Locator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LocatorMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "default", target = "isDefault")
    @Mapping(source = "active", target = "isActive")
    LocatorDto toDto(Locator entity);

    List<LocatorDto> toDtoList(List<Locator> entities);
}
