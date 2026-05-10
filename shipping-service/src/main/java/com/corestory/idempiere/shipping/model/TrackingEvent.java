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

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Carrier-webhook ingest target. iDempiere parity: {@code M_Tracking}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tracking_events")
public class TrackingEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "event_code", nullable = false, length = 32)
    private String eventCode;

    @Column(name = "event_description", length = 255)
    private String eventDescription;

    @Column(name = "event_location", length = 255)
    private String eventLocation;

    @Column(name = "event_at", nullable = false)
    private OffsetDateTime eventAt;

    /**
     * Raw carrier-webhook JSON payload. Mapped as text — Postgres stores it in the
     * {@code jsonb} column transparently. Hibernate's strict JSON type handling is
     * deferred to SHIP-101 when the webhook ingestor is implemented.
     */
    @Column(name = "raw_payload_json", columnDefinition = "jsonb")
    private String rawPayloadJson;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TrackingEvent that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
