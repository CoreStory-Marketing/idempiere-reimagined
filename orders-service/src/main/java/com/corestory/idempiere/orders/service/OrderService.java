package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.common.events.OrderCancelledEvent;
import com.corestory.idempiere.common.events.OrderCompletedEvent;
import com.corestory.idempiere.common.events.OrderConfirmedEvent;
import com.corestory.idempiere.common.events.OrderInvoicedEvent;
import com.corestory.idempiere.common.events.OrderShippedEvent;
import com.corestory.idempiere.orders.events.OrderEventPublisher;
import com.corestory.idempiere.orders.exception.OrderNotFoundException;
import com.corestory.idempiere.orders.exception.ReferenceNotFoundException;
import com.corestory.idempiere.orders.model.Address;
import com.corestory.idempiere.orders.model.Customer;
import com.corestory.idempiere.orders.model.Incoterm;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderLine;
import com.corestory.idempiere.orders.model.OrderStatus;
import com.corestory.idempiere.orders.model.OrderStatusHistory;
import com.corestory.idempiere.orders.model.PaymentTerm;
import com.corestory.idempiere.orders.model.PriceList;
import com.corestory.idempiere.orders.model.TaxRate;
import com.corestory.idempiere.orders.api.dto.CreateOrderLineRequest;
import com.corestory.idempiere.orders.api.dto.CreateOrderRequest;
import com.corestory.idempiere.orders.repo.AddressRepository;
import com.corestory.idempiere.orders.repo.CustomerRepository;
import com.corestory.idempiere.orders.repo.IncotermRepository;
import com.corestory.idempiere.orders.repo.OrderRepository;
import com.corestory.idempiere.orders.repo.OrderStatusHistoryRepository;
import com.corestory.idempiere.orders.repo.PaymentTermRepository;
import com.corestory.idempiere.orders.repo.PriceListRepository;
import com.corestory.idempiere.orders.repo.TaxRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate root service for orders.
 *
 * <p>Owns the document lifecycle:
 * <pre>
 *     create(...) → DRAFT
 *     confirm(id) → CONFIRMED   (computes pricing + tax, emits OrderConfirmedEvent)
 *     ship(id)    → SHIPPED     (emits OrderShippedEvent)
 *     invoice(id) → INVOICED    (emits OrderInvoicedEvent)
 *     complete(id)→ COMPLETE    (emits OrderCompletedEvent)
 *     cancel(id)  → CANCELLED   (only from DRAFT/PENDING/CONFIRMED, emits OrderCancelledEvent)
 * </pre>
 *
 * <p>Every transition writes an {@link OrderStatusHistory} row inside the same
 * transaction, then publishes the matching domain event to the {@code orders.events}
 * topic on transaction commit (best-effort — see {@link OrderEventPublisher}).
 *
 * <p>iDempiere parity: {@code MOrder.completeIt()}, {@code MOrder.voidIt()},
 * {@code MOrder.processIt()} on {@code C_Order.DocStatus}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final PaymentTermRepository paymentTermRepository;
    private final PriceListRepository priceListRepository;
    private final IncotermRepository incotermRepository;
    private final TaxRateRepository taxRateRepository;

    private final OrderStateMachine stateMachine;
    private final PricingService pricingService;
    private final DocumentNumberService documentNumberService;
    private final OrderEventPublisher eventPublisher;
    private final Clock clock;

    // ---------------------------------------------------------------------
    // Read-side
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByDocumentNo(String documentNo) {
        return orderRepository.findByDocumentNo(documentNo);
    }

    @Transactional(readOnly = true)
    public Page<Order> list(Collection<OrderStatus> statuses, Long customerId, Pageable pageable) {
        boolean hasStatuses = statuses != null && !statuses.isEmpty();
        boolean hasCustomer = customerId != null;
        if (hasStatuses && hasCustomer) {
            return orderRepository.findByStatusInAndCustomerId(statuses, customerId, pageable);
        }
        if (hasStatuses) {
            return orderRepository.findByStatusIn(statuses, pageable);
        }
        if (hasCustomer) {
            return orderRepository.findByCustomerId(customerId, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    // ---------------------------------------------------------------------
    // Create
    // ---------------------------------------------------------------------

    @Transactional
    public Order create(CreateOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
            .orElseThrow(() -> new ReferenceNotFoundException("Customer", request.customerId()));

        Order order = new Order();
        order.setDocumentNo(documentNumberService.nextOrderNumber());
        order.setStatus(OrderStatus.DRAFT);
        order.setCustomer(customer);
        order.setBillToAddress(resolveAddress(request.billToAddressId()));
        order.setShipToAddress(resolveAddress(request.shipToAddressId()));
        order.setPaymentTerm(resolvePaymentTerm(request.paymentTermId()));
        order.setPriceList(resolvePriceList(request.priceListId()));
        order.setIncoterm(resolveIncoterm(request.incotermId()));
        order.setCurrency(request.currency());
        order.setOrderDate(request.orderDate() != null
            ? request.orderDate()
            : LocalDate.now(clock));
        order.setPromisedDate(request.promisedDate());
        order.setNotes(request.notes());

        int idx = 1;
        for (CreateOrderLineRequest lineReq : request.lines()) {
            OrderLine line = new OrderLine();
            line.setLineNo(idx++);
            line.setProductId(lineReq.productId());
            line.setQtyOrdered(lineReq.qtyOrdered());
            line.setUnitPrice(lineReq.unitPrice());
            line.setLineDiscountPct(lineReq.lineDiscountPct() != null
                ? lineReq.lineDiscountPct()
                : BigDecimal.ZERO);
            line.setTaxRate(resolveTaxRate(lineReq.taxRateId()));
            order.addLine(line);
        }

        // Compute totals up-front so DRAFT carries a meaningful estimate.
        pricingService.recalculate(order);

        Order saved = orderRepository.save(order);

        statusHistoryRepository.save(history(saved, null, OrderStatus.DRAFT, "Order created"));
        log.info("Created order {} (id={}) for customerId={}",
            saved.getDocumentNo(), saved.getId(), customer.getId());
        return saved;
    }

    // ---------------------------------------------------------------------
    // State transitions
    // ---------------------------------------------------------------------

    @Transactional
    public Order confirm(Long orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus from = order.getStatus();
        stateMachine.requireTransition(from, OrderStatus.CONFIRMED);

        // Recompute totals at confirm-time — line prices may have shifted while in DRAFT.
        pricingService.recalculate(order);
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);

        statusHistoryRepository.save(history(saved, from, OrderStatus.CONFIRMED, reason));
        log.info("Confirmed order {} (id={}): grandTotal={} {}",
            saved.getDocumentNo(), saved.getId(),
            saved.getGrandTotal(), saved.getCurrency());

        eventPublisher.publish(buildConfirmedEvent(saved));
        return saved;
    }

    @Transactional
    public Order cancel(Long orderId, String reason) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus from = order.getStatus();
        stateMachine.requireTransition(from, OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        statusHistoryRepository.save(history(saved, from, OrderStatus.CANCELLED, reason));
        log.info("Cancelled order {} (id={}) from {}: {}",
            saved.getDocumentNo(), saved.getId(), from, reason);

        eventPublisher.publish(new OrderCancelledEvent(
            UUID.randomUUID(),
            Instant.now(clock),
            saved.getTenantId(),
            saved.getOrgId(),
            saved.getId(),
            saved.getDocumentNo(),
            saved.getCustomer().getId(),
            reason,
            from.name()
        ));
        return saved;
    }

    @Transactional
    public Order ship(Long orderId, Long shipmentId, String shipmentDocumentNo) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus from = order.getStatus();
        stateMachine.requireTransition(from, OrderStatus.SHIPPED);

        order.setStatus(OrderStatus.SHIPPED);
        Order saved = orderRepository.save(order);
        statusHistoryRepository.save(history(saved, from, OrderStatus.SHIPPED,
            "Shipment created: " + shipmentDocumentNo));

        eventPublisher.publish(new OrderShippedEvent(
            UUID.randomUUID(),
            Instant.now(clock),
            saved.getTenantId(),
            saved.getOrgId(),
            saved.getId(),
            saved.getDocumentNo(),
            saved.getCustomer().getId(),
            shipmentId,
            shipmentDocumentNo
        ));
        return saved;
    }

    @Transactional
    public Order invoice(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus from = order.getStatus();
        stateMachine.requireTransition(from, OrderStatus.INVOICED);

        order.setStatus(OrderStatus.INVOICED);
        Order saved = orderRepository.save(order);
        statusHistoryRepository.save(history(saved, from, OrderStatus.INVOICED, null));

        eventPublisher.publish(new OrderInvoicedEvent(
            UUID.randomUUID(),
            Instant.now(clock),
            saved.getTenantId(),
            saved.getOrgId(),
            saved.getId(),
            saved.getDocumentNo(),
            saved.getCustomer().getId(),
            saved.getGrandTotal(),
            saved.getCurrency()
        ));
        return saved;
    }

    @Transactional
    public Order complete(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        OrderStatus from = order.getStatus();
        stateMachine.requireTransition(from, OrderStatus.COMPLETE);

        order.setStatus(OrderStatus.COMPLETE);
        Order saved = orderRepository.save(order);
        statusHistoryRepository.save(history(saved, from, OrderStatus.COMPLETE, null));

        eventPublisher.publish(new OrderCompletedEvent(
            UUID.randomUUID(),
            Instant.now(clock),
            saved.getTenantId(),
            saved.getOrgId(),
            saved.getId(),
            saved.getDocumentNo(),
            saved.getCustomer().getId()
        ));
        return saved;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private OrderStatusHistory history(Order order, OrderStatus from, OrderStatus to, String reason) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setOrder(order);
        h.setFromStatus(from);
        h.setToStatus(to);
        h.setReason(reason);
        h.setChangedBy("system");
        return h;
    }

    private OrderConfirmedEvent buildConfirmedEvent(Order order) {
        List<OrderConfirmedEvent.Line> lineEvents = order.getLines().stream()
            .map(l -> new OrderConfirmedEvent.Line(
                l.getId(),
                l.getProductId(),
                null, // SKU lives in inventory-service; left null so consumers can resolve it
                l.getQtyOrdered(),
                l.getUnitPrice(),
                l.getLineAmount()
            ))
            .toList();
        return new OrderConfirmedEvent(
            UUID.randomUUID(),
            Instant.now(clock),
            order.getTenantId(),
            order.getOrgId(),
            order.getId(),
            order.getDocumentNo(),
            order.getCustomer().getId(),
            order.getGrandTotal(),
            order.getCurrency(),
            lineEvents
        );
    }

    private Address resolveAddress(Long id) {
        if (id == null) return null;
        return addressRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Address", id));
    }

    private PaymentTerm resolvePaymentTerm(Long id) {
        if (id == null) return null;
        return paymentTermRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("PaymentTerm", id));
    }

    private PriceList resolvePriceList(Long id) {
        if (id == null) return null;
        return priceListRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("PriceList", id));
    }

    private Incoterm resolveIncoterm(Long id) {
        if (id == null) return null;
        return incotermRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("Incoterm", id));
    }

    private TaxRate resolveTaxRate(Long id) {
        if (id == null) return null;
        return taxRateRepository.findById(id)
            .orElseThrow(() -> new ReferenceNotFoundException("TaxRate", id));
    }
}
