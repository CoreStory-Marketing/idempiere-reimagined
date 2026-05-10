package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.PriceListDto;
import com.corestory.idempiere.orders.model.PriceList;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PriceListMapper {

    PriceListDto toDto(PriceList entity);

    List<PriceListDto> toDtoList(List<PriceList> entities);
}
