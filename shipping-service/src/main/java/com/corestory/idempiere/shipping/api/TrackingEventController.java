package com.corestory.idempiere.shipping.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * <b>STUB.</b> Carrier-webhook ingest endpoint pending SHIP-101.
 */
@RestController
@RequestMapping("/tracking-events")
public class TrackingEventController {

    @GetMapping
    public Object list() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Tracking endpoints pending SHIP-101");
    }

    @PostMapping
    public Object ingest() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Carrier-webhook ingest pending SHIP-101");
    }
}
