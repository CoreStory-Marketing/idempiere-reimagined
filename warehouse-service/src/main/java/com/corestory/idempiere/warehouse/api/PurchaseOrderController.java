package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.warehouse.api.dto.CreatePurchaseOrderRequest;
import com.corestory.idempiere.warehouse.api.dto.PurchaseOrderDto;
import com.corestory.idempiere.warehouse.model.PurchaseOrder;
import com.corestory.idempiere.warehouse.model.PurchaseOrderLine;
import com.corestory.idempiere.warehouse.model.Vendor;
import com.corestory.idempiere.warehouse.repo.PurchaseOrderRepository;
import com.corestory.idempiere.warehouse.repo.VendorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorRepository vendorRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<PurchaseOrderDto> create(@Valid @RequestBody CreatePurchaseOrderRequest req) {
        Vendor vendor = vendorRepository.findById(req.vendorId())
            .orElseThrow(() -> new NoSuchElementException("vendor not found: " + req.vendorId()));

        PurchaseOrder po = new PurchaseOrder();
        po.setDocumentNo(req.documentNo());
        po.setVendor(vendor);
        po.setExpectedDate(req.expectedDate());

        for (CreatePurchaseOrderRequest.Line l : req.lines()) {
            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProductId(l.productId());
            line.setQtyOrdered(l.qtyOrdered());
            po.addLine(line);
        }
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return ResponseEntity.created(URI.create("/purchase-orders/" + saved.getId()))
            .body(PurchaseOrderDto.from(saved));
    }

    @GetMapping
    public PageResponse<PurchaseOrderDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<PurchaseOrder> p = purchaseOrderRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(p.getContent().stream().map(PurchaseOrderDto::from).toList(),
            page, size, p.getTotalElements());
    }

    @GetMapping("/{id}")
    public PurchaseOrderDto get(@PathVariable Long id) {
        return PurchaseOrderDto.from(purchaseOrderRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("purchase order not found: " + id)));
    }
}
