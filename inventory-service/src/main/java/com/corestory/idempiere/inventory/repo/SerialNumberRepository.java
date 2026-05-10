package com.corestory.idempiere.inventory.repo;

import com.corestory.idempiere.inventory.model.SerialNumber;
import com.corestory.idempiere.inventory.model.SerialNumberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long> {

    Optional<SerialNumber> findByProductIdAndSerialNumber(Long productId, String serialNumber);

    List<SerialNumber> findByProductIdAndStatus(Long productId, SerialNumberStatus status);
}
