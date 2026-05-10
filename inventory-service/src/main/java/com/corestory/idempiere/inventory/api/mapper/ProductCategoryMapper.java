package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.ProductCategoryDto;
import com.corestory.idempiere.inventory.model.ProductCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductCategoryMapper {

    @Mapping(source = "parent.id", target = "parentCategoryId")
    @Mapping(source = "selfService", target = "isSelfService")
    @Mapping(source = "active", target = "isActive")
    ProductCategoryDto toDto(ProductCategory entity);

    List<ProductCategoryDto> toDtoList(List<ProductCategory> entities);
}
