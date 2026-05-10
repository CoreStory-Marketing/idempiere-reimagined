package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.IncotermDto;
import com.corestory.idempiere.orders.model.Incoterm;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncotermMapper {

    IncotermDto toDto(Incoterm entity);

    List<IncotermDto> toDtoList(List<Incoterm> entities);
}
