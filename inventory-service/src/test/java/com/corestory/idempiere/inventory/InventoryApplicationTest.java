package com.corestory.idempiere.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test — verifies the Spring context loads with the test (H2 + embedded Artemis) profile.
 * If this fails, a bean wiring or schema-generation problem is the most likely cause.
 */
@SpringBootTest
class InventoryApplicationTest {

    @Test
    void contextLoads() {
        // intentionally empty — the @SpringBootTest annotation does the heavy lifting
    }
}
