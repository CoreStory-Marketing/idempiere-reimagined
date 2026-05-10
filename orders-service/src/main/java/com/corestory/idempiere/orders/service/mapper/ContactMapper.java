package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.ContactDto;
import com.corestory.idempiere.orders.model.Contact;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(target = "customerId", source = "customer.id")
    ContactDto toDto(Contact entity);

    List<ContactDto> toDtoList(List<Contact> entities);
}
