package com.corestory.idempiere.inventory.service;

import com.corestory.idempiere.inventory.api.dto.CreateProductRequest;
import com.corestory.idempiere.inventory.api.dto.ProductDto;
import com.corestory.idempiere.inventory.api.mapper.ProductMapper;
import com.corestory.idempiere.inventory.exception.ResourceNotFoundException;
import com.corestory.idempiere.inventory.model.AttributeSet;
import com.corestory.idempiere.inventory.model.Product;
import com.corestory.idempiere.inventory.model.ProductCategory;
import com.corestory.idempiere.inventory.model.UnitOfMeasure;
import com.corestory.idempiere.inventory.repo.AttributeSetRepository;
import com.corestory.idempiere.inventory.repo.ProductCategoryRepository;
import com.corestory.idempiere.inventory.repo.ProductRepository;
import com.corestory.idempiere.inventory.repo.UnitOfMeasureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final UnitOfMeasureRepository uomRepository;
    private final AttributeSetRepository attributeSetRepository;
    private final ProductMapper productMapper;

    public ProductService(
        ProductRepository productRepository,
        ProductCategoryRepository categoryRepository,
        UnitOfMeasureRepository uomRepository,
        AttributeSetRepository attributeSetRepository,
        ProductMapper productMapper
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.uomRepository = uomRepository;
        this.attributeSetRepository = attributeSetRepository;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> list(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto get(Long id) {
        Product p = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toDto(p);
    }

    @Transactional(readOnly = true)
    public ProductDto getBySku(String sku) {
        Product p = productRepository.findBySku(sku)
            .orElseThrow(() -> new ResourceNotFoundException("Product[sku]", sku));
        return productMapper.toDto(p);
    }

    @Transactional
    public ProductDto create(CreateProductRequest req) {
        if (productRepository.existsBySku(req.sku())) {
            throw new IllegalArgumentException("Product with sku already exists: " + req.sku());
        }
        UnitOfMeasure uom = uomRepository.findById(req.uomId())
            .orElseThrow(() -> new ResourceNotFoundException("UnitOfMeasure", req.uomId()));
        ProductCategory cat = null;
        if (req.productCategoryId() != null) {
            cat = categoryRepository.findById(req.productCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ProductCategory", req.productCategoryId()));
        }
        AttributeSet attrSet = null;
        if (req.attributeSetId() != null) {
            attrSet = attributeSetRepository.findById(req.attributeSetId())
                .orElseThrow(() -> new ResourceNotFoundException("AttributeSet", req.attributeSetId()));
        }

        Product p = Product.builder()
            .sku(req.sku())
            .name(req.name())
            .description(req.description())
            .productCategory(cat)
            .uom(uom)
            .attributeSet(attrSet)
            .isStocked(req.isStocked() == null ? true : req.isStocked())
            .isActive(true)
            .weight(req.weight())
            .volume(req.volume())
            .build();
        return productMapper.toDto(productRepository.save(p));
    }
}
