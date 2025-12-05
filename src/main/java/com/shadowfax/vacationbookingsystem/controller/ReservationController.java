package com.shadowfax.vacationbookingsystem.controller;

import com.shadowfax.vacationbookingsystem.model.Reservation;
import com.shadowfax.vacationbookingsystem.model.UserPrincipal;
import com.shadowfax.vacationbookingsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;


    // CREATE (USER ONLY)
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody Reservation reservation) {

        try {
            Reservation newReservation = reservationService.createReservation(reservation);
            return new ResponseEntity<>(newReservation, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not create reservation.");
        }
    }


    // GET ALL (ADMIN ONLY)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllReservations() {
        List<Reservation> reservations = reservationService.getAllReservations();

        if (reservations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }


    // GET ONE (ADMIN OR OWNER)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getReservationById(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Reservation> reservationOpt = reservationService.getReservationById(id);

        if (reservationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }

        Reservation reservation = reservationOpt.get();

        // USER only sees own reservation
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            if (!reservation.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized.");
            }
        }

        return ResponseEntity.ok(reservation);
    }


    // UPDATE (ADMIN or Owner)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(
            @PathVariable Long id,
            @RequestBody Reservation reservationDetails,
            Authentication authentication) {

        Reservation existingReservation = reservationService.getReservationById(id).orElse(null);

        if (existingReservation == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        if (!isAdmin && !existingReservation.getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized.");
        }

        Reservation updated = reservationService.updateReservation(id, reservationDetails);

        return ResponseEntity.ok(updated);
    }


    // DELETE (ADMIN or Owner)
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReservation(
            @PathVariable Long id,
            Authentication authentication) {

        Optional<Reservation> existingReservation = reservationService.getReservationById(id);

        if (existingReservation.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reservation not found.");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();

        if (!isAdmin && !existingReservation.get().getUserId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized.");
        }

        reservationService.deleteReservation(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    // GET BY USER
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Reservation>> getReservationsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.getReservationsByUserId(userId));
    }


    // GET BY LISTING
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("/listing/{listingId}")
    public ResponseEntity<List<Reservation>> getReservationsByListingId(@PathVariable Long listingId) {
        return ResponseEntity.ok(reservationService.getReservationsByListingId(listingId));
    }
}
