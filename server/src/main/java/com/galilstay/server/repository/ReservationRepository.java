package com.galilstay.server.repository;

import com.galilstay.server.model.Reservation;
import com.galilstay.server.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByArrivalDesc();

    List<Reservation> findByGuestIdOrderByArrivalDesc(Long guestId);

    List<Reservation> findByUnitId(Long unitId);

    List<Reservation> findByStatusNot(ReservationStatus status);

    long countByStatus(ReservationStatus status);

    boolean existsByUnitId(Long unitId);

    boolean existsByGuestId(Long guestId);

    /** האם קיימת הזמנה פעילה שחופפת לטווח התאריכים */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.unit.id = :unitId
              and r.status <> com.galilstay.server.model.ReservationStatus.CANCELLED
              and r.arrival < :departure
              and r.departure > :arrival
            """)
    boolean isUnitBooked(@Param("unitId") Long unitId,
                         @Param("arrival") LocalDate arrival,
                         @Param("departure") LocalDate departure);

    /** כל ההזמנות הפעילות שנוגעות בטווח (לחישובי תפוסה ולוח שנה) */
    @Query("""
            select r from Reservation r
            where r.status <> com.galilstay.server.model.ReservationStatus.CANCELLED
              and r.arrival < :to
              and r.departure > :from
            """)
    List<Reservation> findActiveBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
