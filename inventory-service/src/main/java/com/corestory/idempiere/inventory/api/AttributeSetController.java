package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.AttributeSetDto;
import com.corestory.idempiere.inventory.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/attribute-sets")
public class AttributeSetController {

    private final ReferenceDataService referenceDataService;

    public AttributeSetController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<AttributeSetDto> list() {
        return referenceDataService.listAttributeSets();
    }

    @GetMapping("/{id}")
    public AttributeSetDto get(@PathVariable Long id) {
        return referenceDataService.getAttributeSet(id);
    }
}
