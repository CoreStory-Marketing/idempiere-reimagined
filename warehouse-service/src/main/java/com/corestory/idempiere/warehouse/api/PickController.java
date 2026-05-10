package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.warehouse.api.dto.PickRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * <b>STUBBED.</b> Every method throws 501. The recorded brownfield-feature-implementation
 * demo (backup story INV-202 or ORD-303 — see {@code docs/jira-stories/}) wires up
 * a {@code PickService}, the {@code OrderConfirmedEvent} consumer, and the locator
 * suggestion logic from {@link com.corestory.idempiere.warehouse.repo.PutAwayRuleRepository}.
 */
@RestController
@RequestMapping("/picks")
public class PickController {

    @PostMapping
    public Object create(@Valid @RequestBody PickRequestDto req) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Pick implementation pending — see SHIP-XXX backup story (INV-202 or ORD-303)");
    }

    @GetMapping
    public Object list() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Pick implementation pending — see SHIP-XXX backup story (INV-202 or ORD-303)");
    }

    @PostMapping("/{id}/release")
    public Object release(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Pick implementation pending — see SHIP-XXX backup story (INV-202 or ORD-303)");
    }
}
