package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.RegionDto;
import com.corestory.idempiere.orders.model.Region;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    @Mapping(target = "countryId", source = "country.id")
    RegionDto toDto(Region entity);

    List<RegionDto> toDtoList(List<Region> entities);
}
