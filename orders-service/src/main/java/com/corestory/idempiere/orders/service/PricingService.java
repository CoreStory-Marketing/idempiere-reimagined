package com.corestory.idempiere.orders.service;

import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderLine;
import com.corestory.idempiere.orders.model.TaxRate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Computes pricing and tax totals for an {@link Order}.
 *
 * <ul>
 *   <li><b>line_amount</b> = qty × unit_price × (1 - line_discount_pct/100)</li>
 *   <li><b>line_tax</b>    = line_amount × tax_rate_pct / 100 (only if a tax rate is attached)</li>
 *   <li><b>total_amount</b> = Σ line_amount</li>
 *   <li><b>tax_amount</b>   = Σ line_tax</li>
 *   <li><b>grand_total</b>  = total_amount + tax_amount</li>
 * </ul>
 *
 * <p>All arithmetic uses {@link BigDecimal} with HALF_EVEN rounding at scale 4 to
 * mirror the Postgres column precision ({@code NUMERIC(19,4)}).
 *
 * <p>iDempiere parity: {@code MOrder.calculateTaxTotal()} on completion.
 */
@Service
public class PricingService {

    private static final int SCALE = 4;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    /**
     * Compute the {@code line_amount} for a single line: qty × unit_price × (1 - discount/100).
     */
    public BigDecimal computeLineAmount(BigDecimal qty,
                                        BigDecimal unitPrice,
                                        BigDecimal lineDiscountPct) {
        BigDecimal q = nullToZero(qty);
        BigDecimal p = nullToZero(unitPrice);
        BigDecimal d = nullToZero(lineDiscountPct);
        BigDecimal discountFactor = BigDecimal.ONE.subtract(d.divide(ONE_HUNDRED, 6, RoundingMode.HALF_EVEN));
        return q.multiply(p).multiply(discountFactor).setScale(SCALE, RoundingMode.HALF_EVEN);
    }

    /**
     * Compute tax for a given line amount + tax rate. Null tax-rate or null
     * percentage returns zero.
     */
    public BigDecimal computeLineTax(BigDecimal lineAmount, TaxRate taxRate) {
        if (taxRate == null || taxRate.getRatePct() == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_EVEN);
        }
        return nullToZero(lineAmount)
            .multiply(taxRate.getRatePct())
            .divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_EVEN);
    }

    /**
     * Recalculate every line's {@code lineAmount} field, then roll up the order
     * totals. Mutates the passed-in order.
     */
    public void recalculate(Order order) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal taxTotal = BigDecimal.ZERO;

        for (OrderLine line : order.getLines()) {
            BigDecimal lineAmount = computeLineAmount(
                line.getQtyOrdered(),
                line.getUnitPrice(),
                line.getLineDiscountPct()
            );
            line.setLineAmount(lineAmount);
            subtotal = subtotal.add(lineAmount);

            BigDecimal lineTax = computeLineTax(lineAmount, line.getTaxRate());
            taxTotal = taxTotal.add(lineTax);
        }

        order.setTotalAmount(subtotal.setScale(SCALE, RoundingMode.HALF_EVEN));
        order.setTaxAmount(taxTotal.setScale(SCALE, RoundingMode.HALF_EVEN));
        order.setGrandTotal(subtotal.add(taxTotal).setScale(SCALE, RoundingMode.HALF_EVEN));
    }

    private static BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
