package com.corestory.idempiere.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * api-gateway entry point.
 *
 * Spring Cloud Gateway: route /orders/** → orders-service, /inventory/** → inventory-service, etc.
 * JWT validation filter on all routes except /auth/login.
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
