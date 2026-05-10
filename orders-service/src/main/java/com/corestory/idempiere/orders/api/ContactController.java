package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.ContactDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Contact;
import com.corestory.idempiere.orders.repo.ContactRepository;
import com.corestory.idempiere.orders.service.mapper.ContactMapper;
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
@RequestMapping("/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    @GetMapping
    public ResponseEntity<PageResponse<ContactDto>> list(
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<Contact> result = customerId != null
            ? contactRepository.findByCustomerId(customerId, PageRequest.of(page, size))
            : contactRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(PageResponse.of(
            contactMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactDto> getById(@PathVariable Long id) {
        Contact c = contactRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Contact", id));
        return ResponseEntity.ok(contactMapper.toDto(c));
    }
}
