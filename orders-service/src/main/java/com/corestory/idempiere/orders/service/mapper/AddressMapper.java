package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.AddressDto;
import com.corestory.idempiere.orders.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "countryId", source = "country.id")
    AddressDto toDto(Address entity);

    List<AddressDto> toDtoList(List<Address> entities);
}
