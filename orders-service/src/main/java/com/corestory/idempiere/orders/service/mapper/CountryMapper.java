package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.CountryDto;
import com.corestory.idempiere.orders.model.Country;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    @Mapping(target = "defaultCurrencyId", source = "defaultCurrency.id")
    CountryDto toDto(Country entity);

    List<CountryDto> toDtoList(List<Country> entities);
}
