package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.CurrencyDto;
import com.corestory.idempiere.orders.model.Currency;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CurrencyMapper {

    CurrencyDto toDto(Currency entity);

    List<CurrencyDto> toDtoList(List<Currency> entities);
}
