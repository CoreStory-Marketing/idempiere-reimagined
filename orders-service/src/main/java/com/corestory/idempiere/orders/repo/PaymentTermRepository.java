package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.PaymentTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTermRepository extends JpaRepository<PaymentTerm, Long> {

    Optional<PaymentTerm> findByCode(String code);
}
