package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.TaxCategoryDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.TaxCategory;
import com.corestory.idempiere.orders.repo.TaxCategoryRepository;
import com.corestory.idempiere.orders.service.mapper.TaxCategoryMapper;
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
@RequestMapping("/tax-categories")
@RequiredArgsConstructor
public class TaxCategoryController {

    private final TaxCategoryRepository taxCategoryRepository;
    private final TaxCategoryMapper taxCategoryMapper;

    @GetMapping
    public ResponseEntity<PageResponse<TaxCategoryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<TaxCategory> result = taxCategoryRepository.findAll(
            PageRequest.of(page, size, Sort.by("code").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            taxCategoryMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaxCategoryDto> getById(@PathVariable Long id) {
        TaxCategory t = taxCategoryRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("TaxCategory", id));
        return ResponseEntity.ok(taxCategoryMapper.toDto(t));
    }
}
