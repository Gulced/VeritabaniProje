package com.shadowfax.vacationbookingsystem.repository;

import com.shadowfax.vacationbookingsystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByListingId(Long listingId);

    // REZERVASYON ÇAKIŞMA KONTROLÜ
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.listingId = :listingId
          AND r.endDate > :startDate
          AND r.startDate < :endDate
    """)
    List<Reservation> checkDateConflict(
            @Param("listingId") Long listingId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // KULLANICI GELECEK REZERVASYONLARI
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.userId = :userId
          AND r.startDate >= :now
        ORDER BY r.startDate ASC
    """)
    List<Reservation> findUpcomingReservations(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // KULLANICI GEÇMİŞ REZERVASYONLARI
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.userId = :userId
          AND r.endDate < :now
        ORDER BY r.endDate DESC
    """)
    List<Reservation> findPastReservations(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now
    );

    // LİSTİNG GELECEK REZERVASYONLARI
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.listingId = :listingId
          AND r.startDate >= :now
        ORDER BY r.startDate ASC
    """)
    List<Reservation> findUpcomingListingReservations(
            @Param("listingId") Long listingId,
            @Param("now") LocalDateTime now
    );

    // LİSTİNG GEÇMİŞ REZERVASYONLARI
    @Query("""
        SELECT r FROM Reservation r
        WHERE r.listingId = :listingId
          AND r.endDate < :now
        ORDER BY r.endDate DESC
    """)
    List<Reservation> findPastListingReservations(
            @Param("listingId") Long listingId,
            @Param("now") LocalDateTime now
    );
}
