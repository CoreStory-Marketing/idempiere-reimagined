package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.UnitOfMeasureDto;
import com.corestory.idempiere.inventory.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/uoms")
public class UomController {

    private final ReferenceDataService referenceDataService;

    public UomController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<UnitOfMeasureDto> list() {
        return referenceDataService.listUoms();
    }

    @GetMapping("/{id}")
    public UnitOfMeasureDto get(@PathVariable Long id) {
        return referenceDataService.getUom(id);
    }
}
