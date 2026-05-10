package com.corestory.idempiere.inventory.api;

import com.corestory.idempiere.inventory.api.dto.CreateReservationRequest;
import com.corestory.idempiere.inventory.api.dto.ReservationDto;
import com.corestory.idempiere.inventory.api.mapper.ReservationMapper;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.model.Reservation;
import com.corestory.idempiere.inventory.repo.ReservationRepository;
import com.corestory.idempiere.inventory.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationController(
        ReservationService reservationService,
        ReservationRepository reservationRepository,
        ReservationMapper reservationMapper
    ) {
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }

    @PostMapping
    public ResponseEntity<List<ReservationDto>> create(
        @Valid @RequestBody CreateReservationRequest req
    ) {
        List<Reservation> created = reservationService.reserve(
            req.productId(), req.qty(), req.orderId(), req.orderLineId(),
            req.lotNumber(), req.serialNumber()
        );
        return ResponseEntity.status(201).body(reservationMapper.toDtoList(created));
    }

    @GetMapping("/{id}")
    public ReservationDto get(@PathVariable Long id) {
        Reservation r = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
        return reservationMapper.toDto(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationDto> cancel(@PathVariable Long id) {
        Reservation r = reservationService.cancel(id);
        return ResponseEntity.ok(reservationMapper.toDto(r));
    }
}
