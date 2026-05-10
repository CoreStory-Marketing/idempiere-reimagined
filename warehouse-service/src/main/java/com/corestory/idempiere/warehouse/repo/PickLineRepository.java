package com.corestory.idempiere.warehouse.repo;

import com.corestory.idempiere.warehouse.model.PickLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <b>STUBBED.</b>
 */
@Repository
public interface PickLineRepository extends JpaRepository<PickLine, Long> {

    List<PickLine> findByPickId(Long pickId);
}
