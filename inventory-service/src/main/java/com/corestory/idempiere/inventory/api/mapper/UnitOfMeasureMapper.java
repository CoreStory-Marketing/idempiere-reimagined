package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.UnitOfMeasureDto;
import com.corestory.idempiere.inventory.model.UnitOfMeasure;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UnitOfMeasureMapper {

    @Mapping(source = "default", target = "isDefault")
    @Mapping(source = "active", target = "isActive")
    UnitOfMeasureDto toDto(UnitOfMeasure entity);

    List<UnitOfMeasureDto> toDtoList(List<UnitOfMeasure> entities);
}
