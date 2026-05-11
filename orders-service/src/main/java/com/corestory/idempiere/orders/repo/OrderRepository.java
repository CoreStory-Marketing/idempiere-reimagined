package com.corestory.idempiere.orders.repo;

import com.corestory.idempiere.orders.model.Order;
import com.corestory.idempiere.orders.model.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByDocumentNo(String documentNo);

    @Override
    @EntityGraph(attributePaths = "lines")
    Optional<Order> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "lines")
    Page<Order> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "lines")
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    @EntityGraph(attributePaths = "lines")
    Page<Order> findByStatusIn(Collection<OrderStatus> statuses, Pageable pageable);

    @EntityGraph(attributePaths = "lines")
    Page<Order> findByStatusInAndCustomerId(Collection<OrderStatus> statuses,
                                            Long customerId,
                                            Pageable pageable);

    /**
     * Pessimistic-lock variant used by {@code OrderService.confirm()} to serialize
     * concurrent confirms of the same order id. The DB-level row lock prevents
     * double-publishing of {@code OrderConfirmedEvent}.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    /**
     * Used by {@link com.corestory.idempiere.orders.service.DocumentNumberService}
     * to count the number of orders dated today and produce a sequence suffix.
     */
    @Query("select count(o) from Order o where o.documentNo like :prefix")
    long countByDocumentNoStartingWith(@Param("prefix") String prefix);
}
