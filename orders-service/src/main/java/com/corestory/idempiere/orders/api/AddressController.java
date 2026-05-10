package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.AddressDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Address;
import com.corestory.idempiere.orders.repo.AddressRepository;
import com.corestory.idempiere.orders.service.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @GetMapping
    public ResponseEntity<PageResponse<AddressDto>> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Address> result = customerId != null
            ? addressRepository.findByCustomerId(customerId, PageRequest.of(page, size))
            : addressRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.of(
            addressMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDto> getById(@PathVariable Long id) {
        Address a = addressRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Address", id));
        return ResponseEntity.ok(addressMapper.toDto(a));
    }
}
