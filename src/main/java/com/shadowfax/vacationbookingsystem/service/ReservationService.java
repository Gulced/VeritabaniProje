package com.shadowfax.vacationbookingsystem.service;

import com.shadowfax.vacationbookingsystem.model.Listing;
import com.shadowfax.vacationbookingsystem.model.Reservation;
import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.repository.ListingRepository;
import com.shadowfax.vacationbookingsystem.repository.ReservationRepository;
import com.shadowfax.vacationbookingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;


    // CREATE RESERVATION
    public Reservation createReservation(Reservation reservation) {

        User user = userRepository.findById(reservation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Listing listing = listingRepository.findById(reservation.getListingId())
                .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

        reservation.setUser(user);
        reservation.setListing(listing);

        reservation.setCreatedAt(LocalDateTime.now());
        reservation.setUpdatedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }


    // GET ALL
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }


    // GET BY ID
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }


    // UPDATE RESERVATION
    public Reservation updateReservation(Long id, Reservation details) {

        Reservation reservation = reservationRepository.findById(id).orElse(null);
        if (reservation == null) {
            return null;
        }

        // Eğer userId değiştiyse user entity getir
        if (details.getUserId() != null &&
            !details.getUserId().equals(reservation.getUser().getId())) {

            User user = userRepository.findById(details.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            reservation.setUser(user);
        }

        // Eğer listingId değiştiyse listing entity getir
        if (details.getListingId() != null &&
            !details.getListingId().equals(reservation.getListing().getId())) {

            Listing listing = listingRepository.findById(details.getListingId())
                    .orElseThrow(() -> new IllegalArgumentException("Listing not found"));

            reservation.setListing(listing);
        }

        // Rezervasyon tarih güncellemeleri
        if (details.getStartDate() != null) {
            reservation.setStartDate(details.getStartDate());
        }

        if (details.getEndDate() != null) {
            reservation.setEndDate(details.getEndDate());
        }

        if (details.getTotalPrice() != null) {
            reservation.setTotalPrice(details.getTotalPrice());
        }

        // createdAt asla güncellenmez
        reservation.setUpdatedAt(LocalDateTime.now());

        return reservationRepository.save(reservation);
    }


    // DELETE RESERVATION (daha güvenli)
    public boolean deleteReservation(Long id) {
        Optional<Reservation> optional = reservationRepository.findById(id);

        if (optional.isPresent()) {
            reservationRepository.delete(optional.get());
            return true;
        }
        return false;
    }


    // GET BY USER
    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }


    // GET BY LISTING
    public List<Reservation> getReservationsByListingId(Long listingId) {
        return reservationRepository.findByListingId(listingId);
    }
}
