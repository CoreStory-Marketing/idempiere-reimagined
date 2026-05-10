package com.corestory.idempiere.shipping.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.shipping.api.dto.CarrierDto;
import com.corestory.idempiere.shipping.repo.CarrierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

/**
 * Read-only carrier listing — works because {@code carriers} table is seeded.
 */
@RestController
@RequestMapping("/carriers")
@RequiredArgsConstructor
public class CarrierController {

    private final CarrierRepository carrierRepository;

    @GetMapping
    public PageResponse<CarrierDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<CarrierDto> p = carrierRepository.findAll(PageRequest.of(page, size)).map(CarrierDto::from);
        return PageResponse.of(p.getContent(), page, size, p.getTotalElements());
    }

    @GetMapping("/{id}")
    public CarrierDto get(@PathVariable Long id) {
        return carrierRepository.findById(id).map(CarrierDto::from)
            .orElseThrow(() -> new NoSuchElementException("carrier not found: " + id));
    }
}
