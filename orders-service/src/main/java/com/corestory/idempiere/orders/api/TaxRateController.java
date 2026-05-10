package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.TaxRateDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.TaxRate;
import com.corestory.idempiere.orders.repo.TaxRateRepository;
import com.corestory.idempiere.orders.service.mapper.TaxRateMapper;
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
@RequestMapping("/tax-rates")
@RequiredArgsConstructor
public class TaxRateController {

    private final TaxRateRepository taxRateRepository;
    private final TaxRateMapper taxRateMapper;

    @GetMapping
    public ResponseEntity<PageResponse<TaxRateDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<TaxRate> result = taxRateRepository.findAll(
            PageRequest.of(page, size, Sort.by("validFrom").descending()));
        return ResponseEntity.ok(PageResponse.of(
            taxRateMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxRateDto> getById(@PathVariable Long id) {
        TaxRate t = taxRateRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("TaxRate", id));
        return ResponseEntity.ok(taxRateMapper.toDto(t));
    }
}
