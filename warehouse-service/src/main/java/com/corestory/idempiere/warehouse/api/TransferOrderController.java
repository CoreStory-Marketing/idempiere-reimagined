package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.warehouse.api.dto.CreateTransferOrderRequest;
import com.corestory.idempiere.warehouse.api.dto.TransferOrderDto;
import com.corestory.idempiere.warehouse.model.TransferOrder;
import com.corestory.idempiere.warehouse.model.TransferOrderLine;
import com.corestory.idempiere.warehouse.repo.TransferOrderRepository;
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
@RequestMapping("/transfer-orders")
@RequiredArgsConstructor
public class TransferOrderController {

    private final TransferOrderRepository transferOrderRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<TransferOrderDto> create(@Valid @RequestBody CreateTransferOrderRequest req) {
        TransferOrder t = new TransferOrder();
        t.setDocumentNo(req.documentNo());
        t.setFromWarehouseId(req.fromWarehouseId());
        t.setToWarehouseId(req.toWarehouseId());
        for (CreateTransferOrderRequest.Line l : req.lines()) {
            TransferOrderLine line = new TransferOrderLine();
            line.setProductId(l.productId());
            line.setQty(l.qty());
            t.addLine(line);
        }
        TransferOrder saved = transferOrderRepository.save(t);
        return ResponseEntity.created(URI.create("/transfer-orders/" + saved.getId()))
            .body(TransferOrderDto.from(saved));
    }

    @GetMapping
    public PageResponse<TransferOrderDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<TransferOrder> p = transferOrderRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(p.getContent().stream().map(TransferOrderDto::from).toList(),
            page, size, p.getTotalElements());
    }

    @GetMapping("/{id}")
    public TransferOrderDto get(@PathVariable Long id) {
        return TransferOrderDto.from(transferOrderRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("transfer order not found: " + id)));
    }
}
