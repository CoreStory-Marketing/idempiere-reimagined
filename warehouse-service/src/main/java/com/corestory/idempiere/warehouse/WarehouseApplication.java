package com.corestory.idempiere.warehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jms.annotation.EnableJms;

/**
 * warehouse-service entry point.
 *
 * <p><b>Build state: HALF.</b> Receiving works end-to-end (POST /receipts, post, inspect).
 * Picking is stubbed: controller declared but service layer throws {@code NotImplementedException}.
 */
@SpringBootApplication(scanBasePackages = {
    "com.corestory.idempiere.warehouse",
    "com.corestory.idempiere.common"
})
@EnableJms
@EnableJpaAuditing
public class WarehouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(WarehouseApplication.class, args);
    }
}
