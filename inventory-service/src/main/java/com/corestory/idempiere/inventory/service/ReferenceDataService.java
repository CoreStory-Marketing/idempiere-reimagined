package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.api.dto.AttributeSetDto;
import com.corestory.idempiere.inventory.api.dto.LocatorDto;
import com.corestory.idempiere.inventory.api.dto.ProductCategoryDto;
import com.corestory.idempiere.inventory.api.dto.UnitOfMeasureDto;
import com.corestory.idempiere.inventory.api.dto.WarehouseDto;
import com.corestory.idempiere.inventory.api.mapper.AttributeSetMapper;
import com.corestory.idempiere.inventory.api.mapper.LocatorMapper;
import com.corestory.idempiere.inventory.api.mapper.ProductCategoryMapper;
import com.corestory.idempiere.inventory.api.mapper.UnitOfMeasureMapper;
import com.corestory.idempiere.inventory.api.mapper.WarehouseMapper;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.repo.AttributeSetRepository;
import com.corestory.idempiere.inventory.repo.LocatorRepository;
import com.corestory.idempiere.inventory.repo.ProductCategoryRepository;
import com.corestory.idempiere.inventory.repo.UnitOfMeasureRepository;
import com.corestory.idempiere.inventory.repo.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read-only CRUD over reference data (warehouses, locators, UoMs, attribute sets, categories).
 * Write paths for reference data are owned by the admin UI / DevOps; this service exposes only
 * list + get for the demo surface.
 */
@Service
@Transactional(readOnly = true)
public class ReferenceDataService {

    private final WarehouseRepository warehouseRepository;
    private final LocatorRepository locatorRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final AttributeSetRepository attributeSetRepository;
    private final ProductCategoryRepository categoryRepository;

    private final WarehouseMapper warehouseMapper;
    private final LocatorMapper locatorMapper;
    private final UnitOfMeasureMapper uomMapper;
    private final AttributeSetMapper attributeSetMapper;
    private final ProductCategoryMapper categoryMapper;

    public ReferenceDataService(
        WarehouseRepository warehouseRepository,
        LocatorRepository locatorRepository,
        UnitOfMeasureRepository uomRepository,
        AttributeSetRepository attributeSetRepository,
        ProductCategoryRepository categoryRepository,
        WarehouseMapper warehouseMapper,
        LocatorMapper locatorMapper,
        UnitOfMeasureMapper uomMapper,
        AttributeSetMapper attributeSetMapper,
        ProductCategoryMapper categoryMapper
    ) {
        this.warehouseRepository = warehouseRepository;
        this.locatorRepository = locatorRepository;
        this.uomRepository = uomRepository;
        this.attributeSetRepository = attributeSetRepository;
        this.categoryRepository = categoryRepository;
        this.warehouseMapper = warehouseMapper;
        this.locatorMapper = locatorMapper;
        this.uomMapper = uomMapper;
        this.attributeSetMapper = attributeSetMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<WarehouseDto> listWarehouses() {
        return warehouseMapper.toDtoList(warehouseRepository.findAll());
    }

    public WarehouseDto getWarehouse(Long id) {
        return warehouseMapper.toDto(warehouseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id)));
    }

    public List<LocatorDto> listLocatorsByWarehouse(Long warehouseId) {
        return locatorMapper.toDtoList(
            locatorRepository.findByWarehouseIdOrderByPriorityNoAsc(warehouseId));
    }

    public LocatorDto getLocator(Long id) {
        return locatorMapper.toDto(locatorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Locator", id)));
    }

    public List<UnitOfMeasureDto> listUoms() {
        return uomMapper.toDtoList(uomRepository.findAll());
    }

    public UnitOfMeasureDto getUom(Long id) {
        return uomMapper.toDto(uomRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", id)));
    }

    public List<AttributeSetDto> listAttributeSets() {
        return attributeSetMapper.toDtoList(attributeSetRepository.findAll());
    }

    public AttributeSetDto getAttributeSet(Long id) {
        return attributeSetMapper.toDto(attributeSetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("AttributeSet", id)));
    }

    public List<ProductCategoryDto> listCategories() {
        return categoryMapper.toDtoList(categoryRepository.findAll());
    }

    public ProductCategoryDto getCategory(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", id)));
    }
}
