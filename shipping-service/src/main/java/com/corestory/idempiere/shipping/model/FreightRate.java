package com.corestory.idempiere.shipping.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "freight_rates")
public class FreightRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_service_id")
    private CarrierService carrierService;

    @Column(name = "origin_country_id")
    private Long originCountryId;

    @Column(name = "dest_country_id")
    private Long destCountryId;

    @Column(name = "weight_min", nullable = false, precision = 19, scale = 4)
    private BigDecimal weightMin = BigDecimal.ZERO;

    @Column(name = "weight_max", nullable = false, precision = 19, scale = 4)
    private BigDecimal weightMax = new BigDecimal("999999");

    @Column(name = "rate_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal rateAmount = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FreightRate that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
