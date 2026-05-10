package com.corestory.idempiere.shipping.api;

import com.corestory.idempiere.shipping.api.dto.CreateShipmentRequest;
import com.corestory.idempiere.shipping.api.dto.ShipRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * <b>STUB.</b> Every method throws 501. There is intentionally no {@code ShipmentService};
 * the recorded brownfield-feature-implementation skill (JIRA story
 * <code>SHIP-101 — see docs/jira-stories/SHIP-101-shipping-notification-flow.md</code>)
 * adds it during the demo, wires the {@code POST /shipments/{id}/ship} flow, and emits
 * {@link com.corestory.idempiere.common.events.ShipmentCreatedEvent} via the
 * {@link com.corestory.idempiere.shipping.events.ShipmentEventPublisher}.
 */
@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private static final String STUB_MSG =
        "Shipment flow not yet implemented — see SHIP-101 (docs/jira-stories/SHIP-101-shipping-notification-flow.md)";

    @PostMapping
    public Object create(@Valid @RequestBody CreateShipmentRequest req) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, STUB_MSG);
    }

    @GetMapping
    public Object list() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, STUB_MSG);
    }

    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, STUB_MSG);
    }

    @PostMapping("/{id}/ship")
    public Object ship(@PathVariable Long id, @RequestBody(required = false) ShipRequest req) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, STUB_MSG);
    }

    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, STUB_MSG);
    }
}
