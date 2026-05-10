package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.warehouse.api.dto.CreateReceiptRequest;
import com.corestory.idempiere.warehouse.api.dto.InspectRequest;
import com.corestory.idempiere.warehouse.api.dto.ReceiptResponse;
import com.corestory.idempiere.warehouse.model.Receipt;
import com.corestory.idempiere.warehouse.model.ReceiptStatus;
import com.corestory.idempiere.warehouse.service.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @PostMapping
    public ResponseEntity<ReceiptResponse> create(@Valid @RequestBody CreateReceiptRequest req) {
        Receipt r = receiptService.createReceipt(req);
        return ResponseEntity.created(URI.create("/receipts/" + r.getId()))
            .body(ReceiptResponse.from(r));
    }

    @GetMapping
    public PageResponse<ReceiptResponse> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) ReceiptStatus status,
        @RequestParam(required = false) Long vendorId
    ) {
        Page<Receipt> p = receiptService.listReceipts(PageRequest.of(page, size), status, vendorId);
        return PageResponse.of(p.getContent().stream().map(ReceiptResponse::from).toList(),
            page, size, p.getTotalElements());
    }

    @GetMapping("/{id}")
    public ReceiptResponse get(@PathVariable Long id) {
        return ReceiptResponse.from(receiptService.getReceipt(id));
    }

    @PostMapping("/{id}/post")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.OK)
    public ReceiptResponse post(@PathVariable Long id) {
        return ReceiptResponse.from(receiptService.postReceipt(id));
    }

    /**
     * Per the SOW the inspect endpoint is keyed by line id. Consistent with iDempiere's
     * {@code M_QualityTestResult} being scoped to a specific {@code MInOutLine}.
     */
    @PostMapping("/{lineId}/inspect")
    public ReceiptResponse inspect(@PathVariable Long lineId, @Valid @RequestBody InspectRequest req) {
        return ReceiptResponse.from(receiptService.inspectLine(lineId, req).getReceipt());
    }
}
