package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.TaxCategoryDto;
import com.corestory.idempiere.orders.model.TaxCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TaxCategoryMapper {

    TaxCategoryDto toDto(TaxCategory entity);

    List<TaxCategoryDto> toDtoList(List<TaxCategory> entities);
}
