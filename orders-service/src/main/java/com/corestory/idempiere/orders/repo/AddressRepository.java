package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCustomerId(Long customerId);

    Page<Address> findByCustomerId(Long customerId, Pageable pageable);
}
