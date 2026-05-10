package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.TaxRateDto;
import com.corestory.idempiere.orders.model.TaxRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxRateMapper {

    @Mapping(target = "taxCategoryId", source = "taxCategory.id")
    @Mapping(target = "countryId", source = "country.id")
    @Mapping(target = "regionId", source = "region.id")
    TaxRateDto toDto(TaxRate entity);

    List<TaxRateDto> toDtoList(List<TaxRate> entities);
}
