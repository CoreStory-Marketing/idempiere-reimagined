package com.corestory.idempiere.orders.api;

import com.corestory.idempiere.orders.exception.IllegalStateTransitionException;
import com.corestory.idempiere.orders.exception.OrderNotFoundException;
import com.corestory.idempiere.orders.exception.RestExceptionHandler;
import com.corestory.idempiere.orders.model.Customer;
import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderStatus;
import com.corestory.idempiere.orders.service.OrderService;
import com.corestory.idempiere.orders.service.mapper.OrderMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc tests for {@link OrderController} — exercises the wiring
 * between HTTP layer, service mock, mapper, and {@link RestExceptionHandler}
 * without booting the full Spring context.
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerWebMvcTest {

    @Mock private OrderService orderService;
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);
    private MockMvc mvc;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        OrderController controller = new OrderController(orderService, mapper);
        mvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("GET /orders/{id} returns the mapped DTO")
    void getOrderById() throws Exception {
        Order order = newOrder(7L);
        when(orderService.findById(7L)).thenReturn(order);

        mvc.perform(get("/orders/{id}", 7L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.documentNo").value("ORD-7"))
            .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    @DisplayName("GET /orders/{id} returns 404 when missing")
    void getOrderById_notFound() throws Exception {
        when(orderService.findById(404L)).thenThrow(new OrderNotFoundException(404L));
        mvc.perform(get("/orders/{id}", 404L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /orders returns 400 on validation failure")
    void create_validationFails() throws Exception {
        // Missing customerId, currency, lines
        String body = "{}";
        mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /orders/{id}/confirm returns 200 and the updated DTO")
    void confirm() throws Exception {
        Order order = newOrder(11L);
        order.setStatus(OrderStatus.CONFIRMED);
        when(orderService.confirm(eq(11L), anyString())).thenReturn(order);

        mvc.perform(post("/orders/{id}/confirm", 11L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"go\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST /orders/{id}/confirm returns 409 on illegal transition")
    void confirm_illegal() throws Exception {
        when(orderService.confirm(eq(99L), any()))
            .thenThrow(new IllegalStateTransitionException(OrderStatus.SHIPPED, OrderStatus.CONFIRMED));

        mvc.perform(post("/orders/{id}/confirm", 99L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("ILLEGAL_STATE_TRANSITION"));
    }

    @Test
    @DisplayName("POST /orders/{id}/cancel requires a reason")
    void cancel_missingReason() throws Exception {
        mvc.perform(post("/orders/{id}/cancel", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("GET /orders returns a PageResponse envelope")
    void listOrders() throws Exception {
        Page<Order> page = new PageImpl<>(List.of(newOrder(1L), newOrder(2L)));
        when(orderService.list(any(), any(), any())).thenReturn(page);

        mvc.perform(get("/orders").param("page", "0").param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.items").isArray())
            .andExpect(jsonPath("$.total").value(2));
    }

    private static Order newOrder(Long id) {
        Order o = new Order();
        o.setId(id);
        o.setDocumentNo("ORD-" + id);
        o.setStatus(OrderStatus.DRAFT);
        o.setCurrency("USD");
        o.setTenantId(1L);
        o.setOrgId(1L);
        Customer c = new Customer();
        c.setId(99L);
        o.setCustomer(c);
        o.setTotalAmount(BigDecimal.ZERO);
        o.setTaxAmount(BigDecimal.ZERO);
        o.setGrandTotal(BigDecimal.ZERO);
        return o;
    }
}
