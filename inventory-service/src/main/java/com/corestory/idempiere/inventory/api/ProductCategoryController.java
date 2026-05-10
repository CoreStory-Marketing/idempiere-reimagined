package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.ProductCategoryDto;
import com.corestory.idempiere.inventory.service.ReferenceDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-categories")
public class ProductCategoryController {

    private final ReferenceDataService referenceDataService;

    public ProductCategoryController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @GetMapping
    public List<ProductCategoryDto> list() {
        return referenceDataService.listCategories();
    }

    @GetMapping("/{id}")
    public ProductCategoryDto get(@PathVariable Long id) {
        return referenceDataService.getCategory(id);
    }
}
