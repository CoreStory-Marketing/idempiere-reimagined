package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.inventory.api.dto.CreateProductRequest;
import com.corestory.idempiere.inventory.api.dto.ProductDto;
import com.corestory.idempiere.inventory.api.dto.ProductStockSummaryDto;
import com.corestory.idempiere.inventory.api.dto.StockAdjustmentRequest;
import com.corestory.idempiere.inventory.api.dto.StockLevelDto;
import com.corestory.idempiere.inventory.api.mapper.ProductMapper;
import com.corestory.idempiere.inventory.service.ProductService;
import com.corestory.idempiere.inventory.service.StockLevelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final StockLevelService stockLevelService;
    private final ProductMapper productMapper;

    public ProductController(
        ProductService productService,
        StockLevelService stockLevelService,
        ProductMapper productMapper
    ) {
        this.productService = productService;
        this.stockLevelService = stockLevelService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public PageResponse<ProductDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDto> result = productService.list(pageable);
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }

    @GetMapping("/{id}")
    public ProductDto get(@PathVariable Long id) {
        return productService.get(id);
    }

    @GetMapping("/by-sku/{sku}")
    public ProductDto getBySku(@PathVariable String sku) {
        return productService.getBySku(sku);
    }

    @GetMapping("/{id}/stock")
    public ProductStockSummaryDto getStock(@PathVariable Long id) {
        return stockLevelService.getStockSummary(id);
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody CreateProductRequest req) {
        ProductDto created = productService.create(req);
        return ResponseEntity.status(201).body(created);
    }

    @PostMapping("/{id}/stock-adjustments")
    public ResponseEntity<StockLevelDto> adjustStock(
        @PathVariable Long id,
        @Valid @RequestBody StockAdjustmentRequest req
    ) {
        StockLevelDto dto = stockLevelService.adjust(id, req);
        return ResponseEntity.status(201).body(dto);
    }
}
