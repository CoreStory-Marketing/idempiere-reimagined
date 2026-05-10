package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.common.dto.PageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Locator (bin) management. The {@code locators} table is owned by inventory-service per
 * §5.2 of the SOW. Read-side endpoints here are placeholders that delegate to the inventory
 * domain in the gateway-routed deployment; for now they return an empty list.
 */
@RestController
@RequestMapping("/locators")
public class LocatorController {

    @GetMapping
    public PageResponse<Object> list() {
        return PageResponse.of(List.of(), 0, 0, 0);
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Locator detail is owned by inventory-service; route via /api/inventory/locators/" + id);
    }

    @PostMapping
    public Object create() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Locator creation is owned by inventory-service");
    }
}
