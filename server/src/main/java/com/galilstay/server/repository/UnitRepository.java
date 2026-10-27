package com.galilstay.server.repository;

import com.galilstay.server.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findByListedTrueAndCapacityGreaterThanEqualOrderByWeekdayRate(int guests);

    boolean existsByNameIgnoreCase(String name);

    long countByListedTrue();
}
