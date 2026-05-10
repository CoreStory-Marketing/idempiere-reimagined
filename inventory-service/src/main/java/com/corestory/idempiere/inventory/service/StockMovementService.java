package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.api.dto.StockMovementDto;
import com.corestory.idempiere.inventory.api.mapper.StockMovementMapper;
import com.corestory.idempiere.inventory.repo.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@Transactional(readOnly = true)
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper mapper;

    public StockMovementService(
        StockMovementRepository stockMovementRepository,
        StockMovementMapper mapper
    ) {
        this.stockMovementRepository = stockMovementRepository;
        this.mapper = mapper;
    }

    public Page<StockMovementDto> list(
        Long productId, OffsetDateTime from, OffsetDateTime to, Pageable pageable
    ) {
        if (productId != null && from != null && to != null) {
            return stockMovementRepository
                .findByProductIdAndMovementDateBetween(productId, from, to, pageable)
                .map(mapper::toDto);
        }
        if (productId != null) {
            return stockMovementRepository.findByProductId(productId, pageable).map(mapper::toDto);
        }
        if (from != null && to != null) {
            return stockMovementRepository.findByMovementDateBetween(from, to, pageable).map(mapper::toDto);
        }
        return stockMovementRepository.findAll(pageable).map(mapper::toDto);
    }
}
