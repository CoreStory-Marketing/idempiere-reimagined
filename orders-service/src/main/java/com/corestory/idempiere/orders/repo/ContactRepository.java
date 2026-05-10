package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByCustomerId(Long customerId);

    Page<Contact> findByCustomerId(Long customerId, Pageable pageable);
}
