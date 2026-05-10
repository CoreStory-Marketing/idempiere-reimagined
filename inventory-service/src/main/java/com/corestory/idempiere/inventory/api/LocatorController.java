package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.LocatorDto;
import com.corestory.idempiere.inventory.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locators")
public class LocatorController {

    private final ReferenceDataService referenceDataService;

    public LocatorController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<LocatorDto> list(@RequestParam Long warehouseId) {
        return referenceDataService.listLocatorsByWarehouse(warehouseId);
    }

    @GetMapping("/{id}")
    public LocatorDto get(@PathVariable Long id) {
        return referenceDataService.getLocator(id);
    }
}
