package com.corestory.idempiere.warehouse.service;

import com.corestory.idempiere.common.events.ReceiptPostedEvent;
import com.corestory.idempiere.warehouse.api.dto.CreateReceiptRequest;
import com.corestory.idempiere.warehouse.api.dto.InspectRequest;
import com.corestory.idempiere.warehouse.events.WarehouseEventPublisher;
import com.corestory.idempiere.warehouse.model.InspectionRecord;
import com.corestory.idempiere.warehouse.model.PurchaseOrder;
import com.corestory.idempiere.warehouse.model.PutAwayRule;
import com.corestory.idempiere.warehouse.model.Receipt;
import com.corestory.idempiere.warehouse.model.ReceiptLine;
import com.corestory.idempiere.warehouse.model.ReceiptStatus;
import com.corestory.idempiere.warehouse.model.Vendor;
import com.corestory.idempiere.warehouse.repo.PurchaseOrderRepository;
import com.corestory.idempiere.warehouse.repo.PutAwayRuleRepository;
import com.corestory.idempiere.warehouse.repo.ReceiptLineRepository;
import com.corestory.idempiere.warehouse.repo.ReceiptRepository;
import com.corestory.idempiere.warehouse.repo.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Receiving lifecycle. iDempiere parity: {@code MInOut} on the receipt side, with
 * {@code MInOut.completeIt()} as the analogue for {@link #postReceipt(Long)}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final DateTimeFormatter DOC_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong DOC_SEQUENCE = new AtomicLong(1);

    private final ReceiptRepository receiptRepository;
    private final ReceiptLineRepository receiptLineRepository;
    private final VendorRepository vendorRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PutAwayRuleRepository putAwayRuleRepository;
    private final WarehouseEventPublisher publisher;

    @Transactional
    public Receipt createReceipt(CreateReceiptRequest req) {
        Vendor vendor = vendorRepository.findById(req.vendorId())
            .orElseThrow(() -> new NoSuchElementException("vendor not found: " + req.vendorId()));

        PurchaseOrder po = req.purchaseOrderId() == null
            ? null
            : purchaseOrderRepository.findById(req.purchaseOrderId())
                .orElseThrow(() -> new NoSuchElementException("purchase order not found: " + req.purchaseOrderId()));

        Receipt receipt = new Receipt();
        receipt.setDocumentNo(generateDocumentNo());
        receipt.setStatus(ReceiptStatus.DRAFT);
        receipt.setVendor(vendor);
        receipt.setVendorInvoiceNo(req.vendorInvoiceNo());
        receipt.setPurchaseOrder(po);
        receipt.setWarehouseId(req.warehouseId());
        receipt.setReceiptDate(req.receiptDate() != null ? req.receiptDate() : LocalDate.now());
        receipt.setNotes(req.notes());

        for (CreateReceiptRequest.Line l : req.lines()) {
            ReceiptLine line = new ReceiptLine();
            line.setProductId(l.productId());
            line.setQtyReceived(l.qtyReceived());
            line.setLocatorId(resolveLocator(l.locatorId(), l.productId(), receipt.getWarehouseId()));
            receipt.addLine(line);
        }

        return receiptRepository.save(receipt);
    }

    @Transactional(readOnly = true)
    public Receipt getReceipt(Long id) {
        return receiptRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("receipt not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Receipt> listReceipts(Pageable pageable, ReceiptStatus statusFilter, Long vendorIdFilter) {
        if (statusFilter != null) {
            return receiptRepository.findByStatus(statusFilter, pageable);
        }
        if (vendorIdFilter != null) {
            return receiptRepository.findByVendorId(vendorIdFilter, pageable);
        }
        return receiptRepository.findAll(pageable);
    }

    @Transactional
    public Receipt postReceipt(Long id) {
        Receipt receipt = getReceipt(id);
        if (receipt.getStatus() == ReceiptStatus.POSTED) {
            throw new IllegalStateException("receipt already posted: " + receipt.getDocumentNo());
        }
        if (receipt.getStatus() == ReceiptStatus.CANCELLED) {
            throw new IllegalStateException("cannot post cancelled receipt: " + receipt.getDocumentNo());
        }

        for (ReceiptLine line : receipt.getLines()) {
            if (line.getQtyAccepted().compareTo(BigDecimal.ZERO) == 0) {
                line.setQtyAccepted(line.getQtyReceived());
            }
            if (line.getLocatorId() == null) {
                line.setLocatorId(resolveLocator(null, line.getProductId(), receipt.getWarehouseId()));
            }
        }

        receipt.setStatus(ReceiptStatus.POSTED);
        Receipt saved = receiptRepository.save(receipt);

        publisher.publishReceiptPosted(toEvent(saved));
        log.info("posted receipt {} ({} lines)", saved.getDocumentNo(), saved.getLines().size());
        return saved;
    }

    @Transactional
    public ReceiptLine inspectLine(Long lineId, InspectRequest req) {
        ReceiptLine line = receiptLineRepository.findById(lineId)
            .orElseThrow(() -> new NoSuchElementException("receipt line not found: " + lineId));

        if (req.qtyAccepted().compareTo(req.qtyInspected()) > 0) {
            throw new IllegalArgumentException("qtyAccepted cannot exceed qtyInspected");
        }
        if (req.qtyInspected().compareTo(line.getQtyReceived()) > 0) {
            throw new IllegalArgumentException("qtyInspected cannot exceed qtyReceived");
        }

        line.setQtyInspected(req.qtyInspected());
        line.setQtyAccepted(req.qtyAccepted());

        InspectionRecord ir = new InspectionRecord();
        ir.setStatus(req.status());
        ir.setInspectorId(req.inspectorId());
        ir.setNotes(req.notes());
        line.addInspection(ir);

        ReceiptLine saved = receiptLineRepository.save(line);

        Receipt receipt = saved.getReceipt();
        if (receipt != null && receipt.getStatus() == ReceiptStatus.DRAFT) {
            receipt.setStatus(ReceiptStatus.IN_PROGRESS);
            receiptRepository.save(receipt);
        }
        return saved;
    }

    private Long resolveLocator(Long explicit, Long productId, Long warehouseId) {
        if (explicit != null) {
            return explicit;
        }
        return putAwayRuleRepository.findBest(productId, warehouseId)
            .map(PutAwayRule::getLocatorId)
            .orElse(null);
    }

    private String generateDocumentNo() {
        return "RCP-" + LocalDate.now().format(DOC_DATE_FMT) + "-" + String.format("%05d", DOC_SEQUENCE.getAndIncrement());
    }

    private ReceiptPostedEvent toEvent(Receipt r) {
        List<ReceiptPostedEvent.Line> lines = r.getLines().stream()
            .map(rl -> new ReceiptPostedEvent.Line(
                rl.getId(),
                rl.getProductId(),
                null,
                rl.getQtyAccepted(),
                rl.getLocatorId()
            ))
            .toList();
        return new ReceiptPostedEvent(
            UUID.randomUUID(),
            Instant.now(),
            r.getTenantId(),
            r.getOrgId(),
            r.getId(),
            r.getDocumentNo(),
            r.getWarehouseId(),
            r.getVendor() != null ? r.getVendor().getId() : null,
            lines
        );
    }
}
