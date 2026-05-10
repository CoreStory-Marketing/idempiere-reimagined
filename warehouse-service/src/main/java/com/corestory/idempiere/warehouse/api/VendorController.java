package com.corestory.idempiere.warehouse.api;

import com.corestory.idempiere.common.dto.PageResponse;
import com.corestory.idempiere.warehouse.api.dto.CreateVendorRequest;
import com.corestory.idempiere.warehouse.api.dto.VendorDto;
import com.corestory.idempiere.warehouse.model.Vendor;
import com.corestory.idempiere.warehouse.repo.VendorRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRepository vendorRepository;

    @PostMapping
    public ResponseEntity<VendorDto> create(@Valid @RequestBody CreateVendorRequest req) {
        Vendor v = new Vendor();
        v.setDocumentNo(req.documentNo());
        v.setName(req.name());
        v.setDefaultAddressId(req.defaultAddressId());
        v.setPaymentTermId(req.paymentTermId());
        Vendor saved = vendorRepository.save(v);
        return ResponseEntity.created(URI.create("/vendors/" + saved.getId())).body(VendorDto.from(saved));
    }

    @GetMapping
    public PageResponse<VendorDto> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<Vendor> p = vendorRepository.findAll(PageRequest.of(page, size));
        return PageResponse.of(p.getContent().stream().map(VendorDto::from).toList(), page, size, p.getTotalElements());
    }

    @GetMapping("/{id}")
    public VendorDto get(@PathVariable Long id) {
        return VendorDto.from(vendorRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("vendor not found: " + id)));
    }
}
