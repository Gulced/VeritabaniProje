package com.shadowfax.vacationbookingsystem.controller;

import com.shadowfax.vacationbookingsystem.model.UserFavorite;
import com.shadowfax.vacationbookingsystem.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/favorites")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    // ADD FAVORITE (USER ONLY)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/addFavorite")
    public ResponseEntity<?> addFavorite(@RequestBody UserFavorite userFavorite) {

        if (userFavorite == null ||
            userFavorite.getUser() == null ||
            userFavorite.getUser().getId() == null ||
            userFavorite.getListing() == null ||
            userFavorite.getListing().getId() == null) {

            return ResponseEntity.badRequest()
                    .body("User ID and Listing ID cannot be null.");
        }

        try {
            userFavoriteService.addFavorite(userFavorite);
            return ResponseEntity.ok("Favorite added successfully.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred.");
        }
    }

    // GET SPECIFIC FAVORITE (USER ONLY)
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{userId}/{listingId}")
    public ResponseEntity<UserFavorite> getFavoriteByUserAndListing(
            @PathVariable Long userId, @PathVariable Long listingId) {

        try {
            UserFavorite userFavorite =
                    userFavoriteService.getFavoriteByUserAndListing(userId, listingId);

            if (userFavorite == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(userFavorite);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET ALL FAVORITES FOR A USER (USER + ADMIN)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getFavoritesByUser(@PathVariable Long userId) {

        try {
            Iterable<UserFavorite> favorites = userFavoriteService.getFavoritesByUser(userId);
            return ResponseEntity.ok(favorites);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not fetch favorites.");
        }
    }

    // REMOVE FAVORITE (USER ONLY)
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/remove/{userId}/{listingId}")
    public ResponseEntity<String> removeFavorite(
            @PathVariable Long userId, @PathVariable Long listingId) {

        try {
            boolean removed = userFavoriteService.removeFavorite(userId, listingId);

            if (!removed) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Favorite not found.");
            }

            return ResponseEntity.ok("Favorite removed successfully.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not remove favorite.");
        }
    }
}
