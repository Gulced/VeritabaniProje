package com.shadowfax.vacationbookingsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shadowfax.vacationbookingsystem.model.Listing;
import com.shadowfax.vacationbookingsystem.model.UserPrincipal;
import com.shadowfax.vacationbookingsystem.service.ListingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Value("${app.upload.dir:${user.home}}")
    private String uploadDir;

    // ------------------------------------------------------
    // GET ALL LISTINGS (Public)
    // ------------------------------------------------------
    @GetMapping
    public ResponseEntity<List<Listing>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllListings());
    }

    // ------------------------------------------------------
    // GET LISTING BY ID (Public)
    // ------------------------------------------------------
    @GetMapping("/{listingId}")
    public ResponseEntity<Listing> getListingById(@PathVariable Long listingId) {
        Listing listing = listingService.getListingById(listingId);
        return listing != null ? ResponseEntity.ok(listing)
                : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // ------------------------------------------------------
    // CREATE LISTING (USER or ADMIN)
    // ------------------------------------------------------
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<?> createListing(
            @RequestParam("listing") String listingJson,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            Authentication authentication) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            Listing listing = mapper.readValue(listingJson, Listing.class);

            // OWNER CHECK → USER sadece kendi listingini oluşturabilir
            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            if (!principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                if (!listing.getUserId().equals(principal.getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("You cannot create listing for another user.");
                }
            }

            // IMAGE UPLOAD (optional)
            if (imageFile != null && !imageFile.isEmpty()) {

                if (!imageFile.getContentType().startsWith("image/")) {
                    return ResponseEntity.badRequest().body("Only image files are allowed.");
                }

                String fileName = listing.getUserId() + "_listing_" + UUID.randomUUID() + ".jpg";
                Path filePath = Paths.get(uploadDir, fileName);

                Files.createDirectories(filePath.getParent());
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                listing.setImageUrl(filePath.toString());
            }

            Listing savedListing = listingService.createListing(listing);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedListing);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid JSON data format.");
        }
    }

    // ------------------------------------------------------
    // UPDATE LISTING (Only owner or admin)
    // ------------------------------------------------------
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{listingId}")
    public ResponseEntity<?> updateListing(
            @PathVariable Long listingId,
            @RequestBody Listing listingDetails,
            Authentication authentication) {

        Listing existingListing = listingService.getListingById(listingId);

        if (existingListing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found.");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !existingListing.getUserId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not the owner of this listing.");
        }

        Listing updatedListing = listingService.updateListing(listingId, listingDetails);
        return ResponseEntity.ok(updatedListing);
    }

    // ------------------------------------------------------
    // DELETE LISTING (Only owner or admin)
    // ------------------------------------------------------
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{listingId}")
    public ResponseEntity<?> deleteListing(
            @PathVariable Long listingId,
            Authentication authentication) {

        Listing listing = listingService.getListingById(listingId);

        if (listing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Listing not found.");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        boolean isAdmin = principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !listing.getUserId().equals(principal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You cannot delete another user's listing.");
        }

        listingService.deleteListing(listingId);
        return ResponseEntity.ok("Listing deleted successfully.");
    }

    // ------------------------------------------------------
    // LISTINGS BY USER
    // ------------------------------------------------------
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getListingsByUserId(@PathVariable Long userId) {

        List<Listing> listings = listingService.getListingsByUserId(userId);

        return listings.isEmpty()
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No listings found for this user.")
                : ResponseEntity.ok(listings);
    }
}
