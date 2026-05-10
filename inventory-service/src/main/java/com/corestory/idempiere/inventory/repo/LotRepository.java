package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.Lot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long> {

    List<Lot> findByProductId(Long productId);

    Optional<Lot> findByProductIdAndLotNumber(Long productId, String lotNumber);
}
