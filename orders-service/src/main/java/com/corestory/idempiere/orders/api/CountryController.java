package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.CountryDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Country;
import com.corestory.idempiere.orders.repo.CountryRepository;
import com.corestory.idempiere.orders.service.mapper.CountryMapper;
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
@RequestMapping("/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryRepository countryRepository;
    private final CountryMapper countryMapper;

    @GetMapping
    public ResponseEntity<PageResponse<CountryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<Country> result = countryRepository.findAll(
            PageRequest.of(page, size, Sort.by("name").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            countryMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CountryDto> getById(@PathVariable Long id) {
        Country c = countryRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Country", id));
        return ResponseEntity.ok(countryMapper.toDto(c));
    }
}
