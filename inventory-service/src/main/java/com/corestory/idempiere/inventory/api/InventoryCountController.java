package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.CreateInventoryCountRequest;
import com.corestory.idempiere.inventory.api.dto.InventoryCountDto;
import com.corestory.idempiere.inventory.service.InventoryCountService;
import jakarta.validation.Valid;
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

import java.util.List;

@RestController
@RequestMapping("/inventory-counts")
public class InventoryCountController {

    private final InventoryCountService inventoryCountService;

    public InventoryCountController(InventoryCountService inventoryCountService) {
        this.inventoryCountService = inventoryCountService;
    }

    @GetMapping
    public List<InventoryCountDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return inventoryCountService.list(pageable);
    }

    @GetMapping("/{id}")
    public InventoryCountDto get(@PathVariable Long id) {
        return inventoryCountService.get(id);
    }

    @PostMapping
    public ResponseEntity<InventoryCountDto> create(
        @Valid @RequestBody CreateInventoryCountRequest req
    ) {
        InventoryCountDto created = inventoryCountService.create(req);
        return ResponseEntity.status(201).body(created);
    }
}
