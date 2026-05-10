package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.warehouse.AbstractIntegrationTest;
import com.corestory.idempiere.warehouse.api.dto.CreateReceiptRequest;
import com.corestory.idempiere.warehouse.api.dto.ReceiptResponse;
import com.corestory.idempiere.warehouse.model.ReceiptStatus;
import com.corestory.idempiere.warehouse.model.Vendor;
import com.corestory.idempiere.warehouse.repo.VendorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private TestRestTemplate rest;
    @Autowired private VendorRepository vendorRepository;

    @Test
    void createAndPostReceiptRoundTrip() {
        Vendor vendor = new Vendor();
        vendor.setDocumentNo("V-IT-" + System.nanoTime());
        vendor.setName("ACME Vendor");
        vendor = vendorRepository.save(vendor);

        CreateReceiptRequest req = new CreateReceiptRequest(
            vendor.getId(), null, "INV-IT-1", 1L, null, "integration test",
            List.of(new CreateReceiptRequest.Line(101L, new BigDecimal("4"), 200L))
        );

        ResponseEntity<ReceiptResponse> created = rest.postForEntity("/receipts", req, ReceiptResponse.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        ReceiptResponse body = created.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(ReceiptStatus.DRAFT);
        assertThat(body.lines()).hasSize(1);

        ResponseEntity<ReceiptResponse> posted = rest.postForEntity(
            "/receipts/" + body.id() + "/post", null, ReceiptResponse.class);
        assertThat(posted.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(posted.getBody()).isNotNull();
        assertThat(posted.getBody().status()).isEqualTo(ReceiptStatus.POSTED);
        assertThat(posted.getBody().lines().get(0).qtyAccepted()).isEqualByComparingTo("4");
    }

    @Test
    void pickEndpointReturns501() {
        ResponseEntity<String> resp = rest.getForEntity("/picks", String.class);
        assertThat(resp.getStatusCode().value()).isEqualTo(501);
    }
}
