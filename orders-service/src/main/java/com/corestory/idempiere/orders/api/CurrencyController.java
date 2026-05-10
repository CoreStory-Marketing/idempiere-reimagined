package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.CurrencyDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Currency;
import com.corestory.idempiere.orders.repo.CurrencyRepository;
import com.corestory.idempiere.orders.service.mapper.CurrencyMapper;
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
@RequestMapping("/currencies")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyRepository currencyRepository;
    private final CurrencyMapper currencyMapper;

    @GetMapping
    public ResponseEntity<PageResponse<CurrencyDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Currency> result = currencyRepository.findAll(
            PageRequest.of(page, size, Sort.by("isoCode").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            currencyMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CurrencyDto> getById(@PathVariable Long id) {
        Currency c = currencyRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Currency", id));
        return ResponseEntity.ok(currencyMapper.toDto(c));
    }
}
