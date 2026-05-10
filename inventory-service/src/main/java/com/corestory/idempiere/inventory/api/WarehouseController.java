package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.WarehouseDto;
import com.corestory.idempiere.inventory.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
public class WarehouseController {

    private final ReferenceDataService referenceDataService;

    public WarehouseController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<WarehouseDto> list() {
        return referenceDataService.listWarehouses();
    }

    @GetMapping("/{id}")
    public WarehouseDto get(@PathVariable Long id) {
        return referenceDataService.getWarehouse(id);
    }
}
