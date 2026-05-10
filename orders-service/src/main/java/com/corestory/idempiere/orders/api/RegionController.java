package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.RegionDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Region;
import com.corestory.idempiere.orders.repo.RegionRepository;
import com.corestory.idempiere.orders.service.mapper.RegionMapper;
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
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @GetMapping
    public ResponseEntity<PageResponse<RegionDto>> list(
            @RequestParam(required = false) Long countryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<Region> result = countryId != null
            ? regionRepository.findByCountryId(countryId,
                PageRequest.of(page, size, Sort.by("name").ascending()))
            : regionRepository.findAll(
                PageRequest.of(page, size, Sort.by("name").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            regionMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionDto> getById(@PathVariable Long id) {
        Region r = regionRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Region", id));
        return ResponseEntity.ok(regionMapper.toDto(r));
    }
}
