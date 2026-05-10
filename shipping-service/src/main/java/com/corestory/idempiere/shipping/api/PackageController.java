package com.corestory.idempiere.shipping.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * <b>STUB.</b> Pending SHIP-101.
 */
@RestController
@RequestMapping("/packages")
public class PackageController {

    @GetMapping
    public Object list() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Package endpoints pending SHIP-101");
    }

    @PostMapping
    public Object create() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
            "Package endpoints pending SHIP-101");
    }
}
