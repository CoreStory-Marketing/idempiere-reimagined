package com.corestory.idempiere.inventory.api.mapper;

import com.corestory.idempiere.inventory.api.dto.ProductDto;
import com.corestory.idempiere.inventory.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(source = "productCategory.id", target = "productCategoryId")
    @Mapping(source = "uom.id", target = "uomId")
    @Mapping(source = "attributeSet.id", target = "attributeSetId")
    @Mapping(source = "stocked", target = "isStocked")
    @Mapping(source = "active", target = "isActive")
    ProductDto toDto(Product entity);

    List<ProductDto> toDtoList(List<Product> entities);
}
