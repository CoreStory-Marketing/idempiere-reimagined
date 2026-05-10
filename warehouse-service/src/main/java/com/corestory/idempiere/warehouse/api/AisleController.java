package com.corestory.idempiere.warehouse.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Placeholder — warehouse aisles are not yet in V1__init.sql. Returns 501.
 */
@RestController
@RequestMapping("/aisles")
public class AisleController {

    @GetMapping
    public Object list() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Aisles not yet modelled — out of scope for the demo");
    }
}
