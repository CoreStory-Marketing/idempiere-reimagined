package com.corestory.idempiere.warehouse.service;

import com.corestory.idempiere.warehouse.api.dto.CreateReceiptRequest;
import com.corestory.idempiere.warehouse.events.WarehouseEventPublisher;
import com.corestory.idempiere.warehouse.model.PutAwayRule;
import com.corestory.idempiere.warehouse.model.Receipt;
import com.corestory.idempiere.warehouse.model.ReceiptStatus;
import com.corestory.idempiere.warehouse.model.Vendor;
import com.corestory.idempiere.warehouse.repo.PurchaseOrderRepository;
import com.corestory.idempiere.warehouse.repo.PutAwayRuleRepository;
import com.corestory.idempiere.warehouse.repo.ReceiptLineRepository;
import com.corestory.idempiere.warehouse.repo.ReceiptRepository;
import com.corestory.idempiere.warehouse.repo.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceUnitTest {

    @Mock private ReceiptRepository receiptRepository;
    @Mock private ReceiptLineRepository receiptLineRepository;
    @Mock private VendorRepository vendorRepository;
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private PutAwayRuleRepository putAwayRuleRepository;
    @Mock private WarehouseEventPublisher publisher;

    @InjectMocks private ReceiptService service;

    private Vendor vendor;

    @BeforeEach
    void setUp() {
        vendor = new Vendor();
        vendor.setId(1L);
        vendor.setDocumentNo("V-1");
        vendor.setName("ACME");
    }

    @Test
    void createReceipt_assignsLocatorFromPutAwayRule() {
        PutAwayRule rule = new PutAwayRule();
        rule.setId(99L);
        rule.setLocatorId(777L);
        rule.setProductId(10L);
        rule.setWarehouseId(1L);
        rule.setPriority((short) 80);

        when(vendorRepository.findById(1L)).thenReturn(Optional.of(vendor));
        when(putAwayRuleRepository.findByProductIdAndWarehouseIdOrderByPriorityDesc(10L, 1L))
            .thenReturn(List.of(rule));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateReceiptRequest req = new CreateReceiptRequest(
            1L, null, "INV-1", 1L, null, "test",
            List.of(new CreateReceiptRequest.Line(10L, new BigDecimal("5"), null))
        );

        Receipt r = service.createReceipt(req);
        assertThat(r.getStatus()).isEqualTo(ReceiptStatus.DRAFT);
        assertThat(r.getLines()).hasSize(1);
        assertThat(r.getLines().get(0).getLocatorId()).isEqualTo(777L);
    }

    @Test
    void postReceipt_setsStatusAndPublishes() {
        Receipt r = new Receipt();
        r.setId(42L);
        r.setDocumentNo("RCP-1");
        r.setVendor(vendor);
        r.setWarehouseId(1L);
        r.setStatus(ReceiptStatus.DRAFT);

        com.corestory.idempiere.warehouse.model.ReceiptLine line = new com.corestory.idempiere.warehouse.model.ReceiptLine();
        line.setProductId(10L);
        line.setQtyReceived(new BigDecimal("3"));
        line.setLocatorId(500L);
        r.addLine(line);

        when(receiptRepository.findById(42L)).thenReturn(Optional.of(r));
        when(receiptRepository.save(any(Receipt.class))).thenAnswer(inv -> inv.getArgument(0));

        Receipt posted = service.postReceipt(42L);

        assertThat(posted.getStatus()).isEqualTo(ReceiptStatus.POSTED);
        assertThat(posted.getLines().get(0).getQtyAccepted()).isEqualByComparingTo("3");
        verify(publisher, times(1)).publishReceiptPosted(any());
    }

    @Test
    void postReceipt_alreadyPostedThrows() {
        Receipt r = new Receipt();
        r.setId(42L);
        r.setStatus(ReceiptStatus.POSTED);
        r.setDocumentNo("RCP-1");

        when(receiptRepository.findById(42L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.postReceipt(42L))
            .isInstanceOf(IllegalStateException.class);
    }
}
