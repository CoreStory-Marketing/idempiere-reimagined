package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.orders.api.dto.CancelOrderRequest;
import com.corestory.idempiere.orders.api.dto.ConfirmOrderRequest;
import com.corestory.idempiere.orders.api.dto.CreateOrderRequest;
import com.corestory.idempiere.orders.api.dto.OrderDto;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderStatus;
import com.corestory.idempiere.orders.service.OrderService;
import com.corestory.idempiere.orders.service.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * Orders REST controller. Exposes the endpoints listed in §5.1 of the SOW:
 * <ul>
 *   <li>{@code POST   /orders}</li>
 *   <li>{@code GET    /orders/{id}}</li>
 *   <li>{@code GET    /orders}        (filter + paginate)</li>
 *   <li>{@code POST   /orders/{id}/confirm}</li>
 *   <li>{@code POST   /orders/{id}/cancel}</li>
 * </ul>
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        Order created = orderService.create(request);
        URI location = UriComponentsBuilder.fromPath("/orders/{id}")
            .buildAndExpand(created.getId())
            .toUri();
        return ResponseEntity.created(location).body(orderMapper.toDto(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        Order order = orderService.findById(id);
        return ResponseEntity.ok(orderMapper.toDto(order));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderDto>> list(
            @RequestParam(required = false) Set<OrderStatus> status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Page<Order> result = orderService.list(status, customerId,
            PageRequest.of(page, size, parseSort(sort)));

        List<OrderDto> dtos = orderMapper.toDtoList(result.getContent());
        return ResponseEntity.ok(PageResponse.of(dtos, page, size, result.getTotalElements()));
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderDto> confirm(@PathVariable Long id,
                                            @RequestBody(required = false) ConfirmOrderRequest body) {
        String reason = body == null ? null : body.reason();
        Order confirmed = orderService.confirm(id, reason);
        return ResponseEntity.ok(orderMapper.toDto(confirmed));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDto> cancel(@PathVariable Long id,
                                           @Valid @RequestBody CancelOrderRequest body) {
        Order cancelled = orderService.cancel(id, body.reason());
        return ResponseEntity.ok(orderMapper.toDto(cancelled));
    }

    private static Sort parseSort(String sortParam) {
        if (sortParam == null || sortParam.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        String[] parts = sortParam.split(",");
        String prop = parts[0].trim();
        Sort.Direction dir = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;
        return Sort.by(dir, prop);
    }
}
