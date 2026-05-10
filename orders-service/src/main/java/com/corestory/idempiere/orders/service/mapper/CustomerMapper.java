package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.CustomerDto;
import com.corestory.idempiere.orders.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "defaultAddressId", source = "defaultAddress.id")
    CustomerDto toDto(Customer entity);

    List<CustomerDto> toDtoList(List<Customer> entities);
}
