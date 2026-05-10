package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.AttributeSetDto;
import com.corestory.idempiere.inventory.model.AttributeSet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AttributeSetMapper {

    @Mapping(source = "lotMandatory", target = "isLotMandatory")
    @Mapping(source = "serialMandatory", target = "isSerialMandatory")
    @Mapping(source = "active", target = "isActive")
    AttributeSetDto toDto(AttributeSet entity);

    List<AttributeSetDto> toDtoList(List<AttributeSet> entities);
}
