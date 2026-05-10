package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findByCountryId(Long countryId);

    Page<Region> findByCountryId(Long countryId, Pageable pageable);
}
