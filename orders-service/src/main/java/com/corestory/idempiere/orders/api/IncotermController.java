package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.IncotermDto;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Incoterm;
import com.corestory.idempiere.orders.repo.IncotermRepository;
import com.corestory.idempiere.orders.service.mapper.IncotermMapper;
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
@RequestMapping("/incoterms")
@RequiredArgsConstructor
public class IncotermController {

    private final IncotermRepository incotermRepository;
    private final IncotermMapper incotermMapper;

    @GetMapping
    public ResponseEntity<PageResponse<IncotermDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Incoterm> result = incotermRepository.findAll(
            PageRequest.of(page, size, Sort.by("code").ascending()));
        return ResponseEntity.ok(PageResponse.of(
            incotermMapper.toDtoList(result.getContent()),
            page, size, result.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncotermDto> getById(@PathVariable Long id) {
        Incoterm i = incotermRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Incoterm", id));
        return ResponseEntity.ok(incotermMapper.toDto(i));
    }
}
