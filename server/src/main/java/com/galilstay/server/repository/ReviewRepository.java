package com.galilstay.server.repository;

import com.galilstay.server.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findAllByOrderByWrittenOnDesc();

    List<Review> findByReservationUnitId(Long unitId);

    boolean existsByReservationId(Long reservationId);

    @Query("select avg(r.stars) from Review r where r.reservation.unit.id = :unitId")
    Double averageForUnit(@Param("unitId") Long unitId);

    @Query("select avg(r.stars) from Review r")
    Double averageOverall();
}
