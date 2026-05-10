package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.PriceListDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.PriceList;
import com.corestory.idempiere.orders.repo.PriceListRepository;
import com.corestory.idempiere.orders.service.mapper.PriceListMapper;
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
@RequestMapping("/price-lists")
@RequiredArgsConstructor
public class PriceListController {

    private final PriceListRepository priceListRepository;
    private final PriceListMapper priceListMapper;

    @GetMapping
    public ResponseEntity<PageResponse<PriceListDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<PriceList> result = priceListRepository.findAll(
            PageRequest.of(page, size, Sort.by("name").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            priceListMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PriceListDto> getById(@PathVariable Long id) {
        PriceList pl = priceListRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("PriceList", id));
        return ResponseEntity.ok(priceListMapper.toDto(pl));
    }
}
