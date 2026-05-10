package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.model.UnitOfMeasure;
import com.corestory.idempiere.inventory.model.UomConversion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Focused unit coverage for {@link UomConversion#convert(BigDecimal)} — the small piece of
 * business logic the entity carries.
 */
class UomConversionTest {

    private UnitOfMeasure kg() {
        return UnitOfMeasure.builder()
            .id(1L).code("KG").name("Kilogram")
            .stdPrecision((short) 3).costingPrecision((short) 4)
            .isActive(true).build();
    }

    private UnitOfMeasure g() {
        return UnitOfMeasure.builder()
            .id(2L).code("G").name("Gram")
            .stdPrecision((short) 0).costingPrecision((short) 2)
            .isActive(true).build();
    }

    @Test
    @DisplayName("KG → G uses multiplyRate=1000")
    void kgToGramsViaMultiply() {
        UomConversion conv = UomConversion.builder()
            .id(100L).fromUom(kg()).toUom(g())
            .multiplyRate(new BigDecimal("1000"))
            .build();

        assertThat(conv.convert(new BigDecimal("2.5"))).isEqualByComparingTo("2500.0");
    }

    @Test
    @DisplayName("G → KG uses divideRate=1000")
    void gramsToKgViaDivide() {
        UomConversion conv = UomConversion.builder()
            .id(101L).fromUom(g()).toUom(kg())
            .divideRate(new BigDecimal("1000"))
            .build();

        BigDecimal result = conv.convert(new BigDecimal("2500"));
        assertThat(result).isEqualByComparingTo("2.500");
    }

    @Test
    @DisplayName("Conversion of null qty returns null")
    void convertNullIsNull() {
        UomConversion conv = UomConversion.builder()
            .fromUom(kg()).toUom(g()).multiplyRate(new BigDecimal("1000")).build();
        assertThat(conv.convert(null)).isNull();
    }

    @Test
    @DisplayName("Conversion with neither rate populated raises IllegalState")
    void rejectsConfigWithoutRates() {
        UomConversion conv = UomConversion.builder()
            .id(1L).fromUom(kg()).toUom(g()).build();
        assertThatThrownBy(() -> conv.convert(BigDecimal.ONE))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("multiplyRate");
    }

    @Test
    @DisplayName("multiplyRate takes precedence over divideRate when both are present")
    void multiplyWinsWhenBothPresent() {
        UomConversion conv = UomConversion.builder()
            .id(102L).fromUom(kg()).toUom(g())
            .multiplyRate(new BigDecimal("1000"))
            .divideRate(new BigDecimal("0.001"))
            .build();
        assertThat(conv.convert(new BigDecimal("1"))).isEqualByComparingTo("1000");
    }
}
