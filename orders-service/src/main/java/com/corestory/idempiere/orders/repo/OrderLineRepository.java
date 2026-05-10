package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    List<OrderLine> findByOrderIdOrderByLineNoAsc(Long orderId);
}
