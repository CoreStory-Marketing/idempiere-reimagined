package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.api.dto.CreateInventoryCountRequest;
import com.corestory.idempiere.inventory.api.dto.InventoryCountDto;
import com.corestory.idempiere.inventory.api.mapper.InventoryCountMapper;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.model.InventoryCount;
import com.corestory.idempiere.inventory.model.InventoryCountStatus;
import com.corestory.idempiere.inventory.model.Warehouse;
import com.corestory.idempiere.inventory.repo.InventoryCountRepository;
import com.corestory.idempiere.inventory.repo.WarehouseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryCountService {

    private final InventoryCountRepository inventoryCountRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryCountMapper mapper;

    public InventoryCountService(
        InventoryCountRepository inventoryCountRepository,
        WarehouseRepository warehouseRepository,
        InventoryCountMapper mapper
    ) {
        this.inventoryCountRepository = inventoryCountRepository;
        this.warehouseRepository = warehouseRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<InventoryCountDto> list(Pageable pageable) {
        return mapper.toDtoList(inventoryCountRepository.findAll(pageable).getContent());
    }

    @Transactional(readOnly = true)
    public InventoryCountDto get(Long id) {
        return mapper.toDto(inventoryCountRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("InventoryCount", id)));
    }

    @Transactional
    public InventoryCountDto create(CreateInventoryCountRequest req) {
        Warehouse warehouse = warehouseRepository.findById(req.warehouseId())
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", req.warehouseId()));
        InventoryCount c = InventoryCount.builder()
            .warehouse(warehouse)
            .countDate(req.countDate() == null ? LocalDate.now() : req.countDate())
            .status(InventoryCountStatus.DRAFT)
            .build();
        return mapper.toDto(inventoryCountRepository.save(c));
    }
}
