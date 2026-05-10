package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.common.events.OrderCancelledEvent;
import com.corestory.idempiere.common.events.OrderCompletedEvent;
import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.common.events.OrderInvoicedEvent;
import com.corestory.idempiere.common.events.OrderShippedEvent;
import com.corestory.idempiere.orders.api.dto.CreateOrderLineRequest;
import com.corestory.idempiere.orders.api.dto.CreateOrderRequest;
import com.corestory.idempiere.orders.events.OrderEventPublisher;
import com.corestory.idempiere.orders.exception.IllegalStateTransitionException;
import com.corestory.idempiere.orders.exception.OrderNotFoundException;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Customer;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderLine;
import com.corestory.idempiere.orders.model.OrderStatus;
import com.corestory.idempiere.orders.model.OrderStatusHistory;
import com.corestory.idempiere.orders.repo.AddressRepository;
import com.corestory.idempiere.orders.repo.CustomerRepository;
import com.corestory.idempiere.orders.repo.IncotermRepository;
import com.corestory.idempiere.orders.repo.OrderRepository;
import com.corestory.idempiere.orders.repo.OrderStatusHistoryRepository;
import com.corestory.idempiere.orders.repo.PaymentTermRepository;
import com.corestory.idempiere.orders.repo.PriceListRepository;
import com.corestory.idempiere.orders.repo.TaxRateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OrderService} state-machine transitions and event emission.
 *
 * <p>Each test stubs the repository / publisher boundary so we can assert the exact
 * event payload that would land on the {@code orders.events} topic without standing
 * up Artemis.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderStatusHistoryRepository statusHistoryRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private PaymentTermRepository paymentTermRepository;
    @Mock private PriceListRepository priceListRepository;
    @Mock private IncotermRepository incotermRepository;
    @Mock private TaxRateRepository taxRateRepository;
    @Mock private DocumentNumberService documentNumberService;
    @Mock private OrderEventPublisher eventPublisher;

    private OrderService orderService;
    private final OrderStateMachine stateMachine = new OrderStateMachine();
    private final PricingService pricingService = new PricingService();
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-05-09T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
            orderRepository,
            statusHistoryRepository,
            customerRepository,
            addressRepository,
            paymentTermRepository,
            priceListRepository,
            incotermRepository,
            taxRateRepository,
            stateMachine,
            pricingService,
            documentNumberService,
            eventPublisher,
            fixedClock
        );
    }

    // -----------------------------------------------------------------
    // create()
    // -----------------------------------------------------------------

    @Test
    @DisplayName("create persists order, computes totals, writes status history")
    void create_happyPath() {
        Customer customer = newCustomer(42L);
        when(customerRepository.findById(42L)).thenReturn(Optional.of(customer));
        when(documentNumberService.nextOrderNumber()).thenReturn("ORD-20260509-00001");
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        CreateOrderRequest req = new CreateOrderRequest(
            42L, null, null, null, null, null,
            "USD", null, null, null,
            List.of(
                new CreateOrderLineRequest(100L, new BigDecimal("2"),
                    new BigDecimal("10.0000"), BigDecimal.ZERO, null),
                new CreateOrderLineRequest(101L, new BigDecimal("1"),
                    new BigDecimal("50.0000"), BigDecimal.ZERO, null)
            )
        );

        Order saved = orderService.create(req);

        assertThat(saved.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(saved.getDocumentNo()).isEqualTo("ORD-20260509-00001");
        assertThat(saved.getCurrency()).isEqualTo("USD");
        assertThat(saved.getLines()).hasSize(2);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("70.0000");
        assertThat(saved.getGrandTotal()).isEqualByComparingTo("70.0000");

        verify(statusHistoryRepository, times(1)).save(any(OrderStatusHistory.class));
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("create throws when customer is missing")
    void create_missingCustomer() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        CreateOrderRequest req = new CreateOrderRequest(
            99L, null, null, null, null, null, "USD", null, null, null,
            List.of(new CreateOrderLineRequest(1L, BigDecimal.ONE, BigDecimal.TEN, null, null))
        );
        assertThatThrownBy(() -> orderService.create(req))
            .isInstanceOf(ReferenceNotFoundException.class)
            .hasMessageContaining("Customer");
    }

    // -----------------------------------------------------------------
    // confirm()
    // -----------------------------------------------------------------

    @Test
    @DisplayName("confirm DRAFT -> CONFIRMED, recomputes totals, emits OrderConfirmedEvent")
    void confirm_happyPath() {
        Order draft = newDraftOrder(1L, 42L, "USD");
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(draft));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order confirmed = orderService.confirm(1L, "ready to ship");

        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(confirmed.getGrandTotal()).isEqualByComparingTo("110.0000"); // 100 line + 10 tax

        ArgumentCaptor<OrderConfirmedEvent> ev = ArgumentCaptor.forClass(OrderConfirmedEvent.class);
        verify(eventPublisher).publish(ev.capture());
        OrderConfirmedEvent event = ev.getValue();
        assertThat(event.orderId()).isEqualTo(1L);
        assertThat(event.documentNo()).isEqualTo("ORD-1");
        assertThat(event.customerId()).isEqualTo(42L);
        assertThat(event.currency()).isEqualTo("USD");
        assertThat(event.grandTotal()).isEqualByComparingTo("110.0000");
        assertThat(event.lines()).hasSize(1);
        assertThat(event.eventType()).isEqualTo("order.confirmed");

        verify(statusHistoryRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    @DisplayName("confirm rejects illegal source state (SHIPPED -> CONFIRMED)")
    void confirm_illegalState() {
        Order shipped = newDraftOrder(1L, 42L, "USD");
        shipped.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(shipped));

        assertThatThrownBy(() -> orderService.confirm(1L, null))
            .isInstanceOf(IllegalStateTransitionException.class)
            .hasMessageContaining("SHIPPED")
            .hasMessageContaining("CONFIRMED");
        verify(eventPublisher, never()).publish(any());
        verify(statusHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirm throws OrderNotFoundException when id is unknown")
    void confirm_notFound() {
        when(orderRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> orderService.confirm(404L, null))
            .isInstanceOf(OrderNotFoundException.class);
        verify(eventPublisher, never()).publish(any());
    }

    // -----------------------------------------------------------------
    // cancel()
    // -----------------------------------------------------------------

    @Test
    @DisplayName("cancel DRAFT -> CANCELLED, emits OrderCancelledEvent")
    void cancel_fromDraft() {
        Order draft = newDraftOrder(2L, 42L, "USD");
        when(orderRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(draft));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancel(2L, "customer changed mind");

        ArgumentCaptor<OrderCancelledEvent> ev = ArgumentCaptor.forClass(OrderCancelledEvent.class);
        verify(eventPublisher).publish(ev.capture());
        OrderCancelledEvent event = ev.getValue();
        assertThat(event.orderId()).isEqualTo(2L);
        assertThat(event.previousStatus()).isEqualTo("DRAFT");
        assertThat(event.reason()).isEqualTo("customer changed mind");
        assertThat(event.eventType()).isEqualTo("order.cancelled");
    }

    @Test
    @DisplayName("cancel CONFIRMED -> CANCELLED is allowed pre-shipment")
    void cancel_fromConfirmed() {
        Order confirmed = newDraftOrder(3L, 42L, "USD");
        confirmed.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(confirmed));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.cancel(3L, "out of stock");
        verify(eventPublisher, times(1)).publish(any(OrderCancelledEvent.class));
    }

    @Test
    @DisplayName("cancel rejects post-SHIPPED transitions")
    void cancel_postShipmentRejected() {
        Order shipped = newDraftOrder(4L, 42L, "USD");
        shipped.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findByIdForUpdate(4L)).thenReturn(Optional.of(shipped));

        assertThatThrownBy(() -> orderService.cancel(4L, "too late"))
            .isInstanceOf(IllegalStateTransitionException.class);
        verify(eventPublisher, never()).publish(any());
    }

    // -----------------------------------------------------------------
    // ship() / invoice() / complete()
    // -----------------------------------------------------------------

    @Test
    @DisplayName("ship CONFIRMED -> SHIPPED emits OrderShippedEvent")
    void ship_happyPath() {
        Order confirmed = newDraftOrder(5L, 42L, "USD");
        confirmed.setStatus(OrderStatus.CONFIRMED);
        when(orderRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(confirmed));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.ship(5L, 999L, "SHIP-20260509-001");

        ArgumentCaptor<OrderShippedEvent> ev = ArgumentCaptor.forClass(OrderShippedEvent.class);
        verify(eventPublisher).publish(ev.capture());
        assertThat(ev.getValue().shipmentId()).isEqualTo(999L);
        assertThat(ev.getValue().shipmentDocumentNo()).isEqualTo("SHIP-20260509-001");
        assertThat(ev.getValue().eventType()).isEqualTo("order.shipped");
    }

    @Test
    @DisplayName("invoice SHIPPED -> INVOICED emits OrderInvoicedEvent")
    void invoice_happyPath() {
        Order shipped = newDraftOrder(6L, 42L, "USD");
        shipped.setStatus(OrderStatus.SHIPPED);
        shipped.setGrandTotal(new BigDecimal("110.0000"));
        when(orderRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(shipped));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.invoice(6L);

        ArgumentCaptor<OrderInvoicedEvent> ev = ArgumentCaptor.forClass(OrderInvoicedEvent.class);
        verify(eventPublisher).publish(ev.capture());
        assertThat(ev.getValue().invoiceAmount()).isEqualByComparingTo("110.0000");
        assertThat(ev.getValue().currency()).isEqualTo("USD");
        assertThat(ev.getValue().eventType()).isEqualTo("order.invoiced");
    }

    @Test
    @DisplayName("complete INVOICED -> COMPLETE emits OrderCompletedEvent")
    void complete_happyPath() {
        Order invoiced = newDraftOrder(7L, 42L, "USD");
        invoiced.setStatus(OrderStatus.INVOICED);
        when(orderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(invoiced));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.complete(7L);

        ArgumentCaptor<OrderCompletedEvent> ev = ArgumentCaptor.forClass(OrderCompletedEvent.class);
        verify(eventPublisher).publish(ev.capture());
        assertThat(ev.getValue().orderId()).isEqualTo(7L);
        assertThat(ev.getValue().eventType()).isEqualTo("order.completed");
    }

    @Test
    @DisplayName("Invalid state on every transition is rejected before publish")
    void everyTransition_rejectsIllegal() {
        Order draft = newDraftOrder(8L, 42L, "USD");
        when(orderRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(draft));

        // DRAFT -> SHIPPED skips a step
        assertThatThrownBy(() -> orderService.ship(8L, 1L, "S"))
            .isInstanceOf(IllegalStateTransitionException.class);
        // DRAFT -> INVOICED skips two steps
        assertThatThrownBy(() -> orderService.invoice(8L))
            .isInstanceOf(IllegalStateTransitionException.class);
        // DRAFT -> COMPLETE skips three steps
        assertThatThrownBy(() -> orderService.complete(8L))
            .isInstanceOf(IllegalStateTransitionException.class);
        verify(eventPublisher, never()).publish(any());
    }

    // -----------------------------------------------------------------
    // list()
    // -----------------------------------------------------------------

    @Test
    @DisplayName("list with no filters falls through to findAll")
    void list_noFilter() {
        when(orderRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
            .thenReturn(org.springframework.data.domain.Page.empty());
        orderService.list(null, null, org.springframework.data.domain.PageRequest.of(0, 10));
        verify(orderRepository).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    @DisplayName("list with status filter delegates to findByStatusIn")
    void list_statusFilter() {
        when(orderRepository.findByStatusIn(eq(List.of(OrderStatus.DRAFT)), any()))
            .thenReturn(org.springframework.data.domain.Page.empty());
        orderService.list(List.of(OrderStatus.DRAFT), null,
            org.springframework.data.domain.PageRequest.of(0, 10));
        verify(orderRepository).findByStatusIn(eq(List.of(OrderStatus.DRAFT)), any());
    }

    // -----------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------

    private static Customer newCustomer(Long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setName("Acme " + id);
        c.setDocumentNo("C" + id);
        return c;
    }

    private static Order newDraftOrder(Long id, Long customerId, String currency) {
        Order o = new Order();
        o.setId(id);
        o.setDocumentNo("ORD-" + id);
        o.setStatus(OrderStatus.DRAFT);
        o.setCustomer(newCustomer(customerId));
        o.setCurrency(currency);
        o.setTenantId(1L);
        o.setOrgId(1L);

        OrderLine l = new OrderLine();
        l.setLineNo(1);
        l.setProductId(7L);
        l.setQtyOrdered(new BigDecimal("2"));
        l.setUnitPrice(new BigDecimal("50.0000"));
        l.setLineDiscountPct(BigDecimal.ZERO);
        com.corestory.idempiere.orders.model.TaxRate r = new com.corestory.idempiere.orders.model.TaxRate();
        r.setRatePct(new BigDecimal("10.0000"));
        l.setTaxRate(r);
        o.addLine(l);
        return o;
    }
}
