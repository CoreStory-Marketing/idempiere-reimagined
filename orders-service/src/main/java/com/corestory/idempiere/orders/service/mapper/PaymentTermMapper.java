package com.corestory.idempiere.orders.service.mapper;

import com.corestory.idempiere.orders.api.dto.PaymentTermDto;
import com.corestory.idempiere.orders.model.PaymentTerm;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentTermMapper {

    PaymentTermDto toDto(PaymentTerm entity);

    List<PaymentTermDto> toDtoList(List<PaymentTerm> entities);
}
