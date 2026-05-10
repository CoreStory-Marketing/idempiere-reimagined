package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.PaymentTermDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.PaymentTerm;
import com.corestory.idempiere.orders.repo.PaymentTermRepository;
import com.corestory.idempiere.orders.service.mapper.PaymentTermMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment-terms")
@RequiredArgsConstructor
public class PaymentTermController {

    private final PaymentTermRepository paymentTermRepository;
    private final PaymentTermMapper paymentTermMapper;

    @GetMapping
    public ResponseEntity<PageResponse<PaymentTermDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<PaymentTerm> result = paymentTermRepository.findAll(
            PageRequest.of(page, size, Sort.by("code").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            paymentTermMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentTermDto> getById(@PathVariable Long id) {
        PaymentTerm pt = paymentTermRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("PaymentTerm", id));
        return ResponseEntity.ok(paymentTermMapper.toDto(pt));
    }
}
