package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderLine;
import com.corestory.idempiere.orders.model.TaxCategory;
import com.corestory.idempiere.orders.model.TaxRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Unit tests for {@link PricingService}.
 *
 * <p>Pricing rules from §5.1 of the SOW and the orders-service handoff brief:
 * <ul>
 *   <li>line_amount = qty × unit_price × (1 - line_discount_pct/100)</li>
 *   <li>tax = Σ(line_amount × tax_rate / 100)</li>
 *   <li>grand_total = subtotal + tax</li>
 * </ul>
 */
class PricingServiceTest {

    private PricingService pricing;

    @BeforeEach
    void setUp() {
        pricing = new PricingService();
    }

    @Test
    @DisplayName("Line amount with no discount is qty × unit_price")
    void lineAmount_noDiscount() {
        BigDecimal amt = pricing.computeLineAmount(
            new BigDecimal("3"),
            new BigDecimal("10.0000"),
            BigDecimal.ZERO);
        assertThat(amt).isEqualByComparingTo("30.0000");
    }

    @Test
    @DisplayName("Line amount applies the (1 - pct/100) discount factor")
    void lineAmount_with10PctDiscount() {
        // 5 × 100.00 × (1 - 0.10) = 450.0000
        BigDecimal amt = pricing.computeLineAmount(
            new BigDecimal("5"),
            new BigDecimal("100.0000"),
            new BigDecimal("10.00"));
        assertThat(amt).isEqualByComparingTo("450.0000");
    }

    @Test
    @DisplayName("Line amount handles fractional qty + price + discount")
    void lineAmount_fractional() {
        // 2.5 × 19.9900 × (1 - 0.05) = 47.4762 (HALF_EVEN at scale 4)
        BigDecimal amt = pricing.computeLineAmount(
            new BigDecimal("2.5"),
            new BigDecimal("19.9900"),
            new BigDecimal("5.00"));
        assertThat(amt).isCloseTo(new BigDecimal("47.4763"),
            within(new BigDecimal("0.0010")));
    }

    @Test
    @DisplayName("Null inputs are treated as zero, not NPEs")
    void lineAmount_nullSafe() {
        assertThat(pricing.computeLineAmount(null, null, null))
            .isEqualByComparingTo("0.0000");
        assertThat(pricing.computeLineAmount(null, new BigDecimal("10"), null))
            .isEqualByComparingTo("0.0000");
        assertThat(pricing.computeLineAmount(new BigDecimal("3"), new BigDecimal("10"), null))
            .isEqualByComparingTo("30.0000");
    }

    @Test
    @DisplayName("Tax with null rate is zero")
    void tax_nullRate() {
        assertThat(pricing.computeLineTax(new BigDecimal("100"), null))
            .isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("Tax = lineAmount × ratePct / 100")
    void tax_basic() {
        TaxRate r = new TaxRate();
        r.setRatePct(new BigDecimal("8.5000"));
        BigDecimal tax = pricing.computeLineTax(new BigDecimal("100.0000"), r);
        assertThat(tax).isEqualByComparingTo("8.5000");
    }

    @Test
    @DisplayName("Order recalculation rolls up subtotal, tax, and grand total")
    void recalculate_full() {
        TaxRate stdRate = new TaxRate();
        stdRate.setId(99L);
        stdRate.setRatePct(new BigDecimal("10.0000"));
        stdRate.setTaxCategory(new TaxCategory());

        Order order = new Order();
        order.setCurrency("USD");

        // Line 1: 2 × 50.00 × (1 - 0.00) = 100, tax = 10
        OrderLine l1 = new OrderLine();
        l1.setLineNo(1);
        l1.setProductId(1L);
        l1.setQtyOrdered(new BigDecimal("2"));
        l1.setUnitPrice(new BigDecimal("50.0000"));
        l1.setLineDiscountPct(BigDecimal.ZERO);
        l1.setTaxRate(stdRate);
        order.addLine(l1);

        // Line 2: 1 × 200.00 × (1 - 0.10) = 180, tax = 18
        OrderLine l2 = new OrderLine();
        l2.setLineNo(2);
        l2.setProductId(2L);
        l2.setQtyOrdered(new BigDecimal("1"));
        l2.setUnitPrice(new BigDecimal("200.0000"));
        l2.setLineDiscountPct(new BigDecimal("10.00"));
        l2.setTaxRate(stdRate);
        order.addLine(l2);

        // Line 3 (untaxed): 4 × 25.00 = 100, tax = 0
        OrderLine l3 = new OrderLine();
        l3.setLineNo(3);
        l3.setProductId(3L);
        l3.setQtyOrdered(new BigDecimal("4"));
        l3.setUnitPrice(new BigDecimal("25.0000"));
        l3.setLineDiscountPct(BigDecimal.ZERO);
        l3.setTaxRate(null);
        order.addLine(l3);

        pricing.recalculate(order);

        assertThat(l1.getLineAmount()).isEqualByComparingTo("100.0000");
        assertThat(l2.getLineAmount()).isEqualByComparingTo("180.0000");
        assertThat(l3.getLineAmount()).isEqualByComparingTo("100.0000");

        assertThat(order.getTotalAmount()).isEqualByComparingTo("380.0000");
        assertThat(order.getTaxAmount()).isEqualByComparingTo("28.0000");
        assertThat(order.getGrandTotal()).isEqualByComparingTo("408.0000");
    }

    @Test
    @DisplayName("Empty order recalculates to zero across the board")
    void recalculate_empty() {
        Order order = new Order();
        order.setCurrency("USD");
        pricing.recalculate(order);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("0.0000");
        assertThat(order.getTaxAmount()).isEqualByComparingTo("0.0000");
        assertThat(order.getGrandTotal()).isEqualByComparingTo("0.0000");
    }

    @Test
    @DisplayName("100% discount line contributes zero")
    void recalculate_fullDiscount() {
        Order order = new Order();
        order.setCurrency("USD");
        OrderLine line = new OrderLine();
        line.setLineNo(1);
        line.setProductId(1L);
        line.setQtyOrdered(new BigDecimal("5"));
        line.setUnitPrice(new BigDecimal("99.99"));
        line.setLineDiscountPct(new BigDecimal("100.00"));
        order.addLine(line);

        pricing.recalculate(order);

        assertThat(line.getLineAmount()).isEqualByComparingTo("0.0000");
        assertThat(order.getGrandTotal()).isEqualByComparingTo("0.0000");
    }
}
