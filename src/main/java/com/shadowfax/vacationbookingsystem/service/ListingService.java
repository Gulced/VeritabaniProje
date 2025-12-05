package com.shadowfax.vacationbookingsystem.service;

import com.shadowfax.vacationbookingsystem.model.Listing;
import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.repository.ListingRepository;
import com.shadowfax.vacationbookingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ListingService {

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private UserRepository userRepository;


    // GET ALL LISTINGS
    public List<Listing> getAllListings() {
        return listingRepository.findAll();
    }

    // GET BY ID
    public Listing getListingById(Long listingId) {
        return listingRepository.findById(listingId).orElse(null);
    }


    // CREATE LISTING
    public Listing createListing(Listing listing) {

        if (listing.getUser() == null || listing.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID is required for creating a listing.");
        }

        User user = userRepository.findById(listing.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        listing.setUser(user);

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        listing.setCreatedAt(now);
        listing.setUpdatedAt(now);

        return listingRepository.save(listing);
    }


    // UPDATE LISTING
    public Listing updateListing(Long listingId, Listing listingDetails) {

        Listing existingListing = listingRepository.findById(listingId).orElse(null);

        if (existingListing == null) {
            return null;
        }

        // Null-check protected updates
        if (listingDetails.getTitle() != null)
            existingListing.setTitle(listingDetails.getTitle());

        if (listingDetails.getDescription() != null)
            existingListing.setDescription(listingDetails.getDescription());

        if (listingDetails.getCategory() != null)
            existingListing.setCategory(listingDetails.getCategory());

        if (listingDetails.getRoomCount() != null)
            existingListing.setRoomCount(listingDetails.getRoomCount());

        if (listingDetails.getBathroomCount() != null)
            existingListing.setBathroomCount(listingDetails.getBathroomCount());

        if (listingDetails.getGuestCount() != null)
            existingListing.setGuestCount(listingDetails.getGuestCount());

        if (listingDetails.getLocation() != null)
            existingListing.setLocation(listingDetails.getLocation());

        if (listingDetails.getPrice() != null)
            existingListing.setPrice(listingDetails.getPrice());

        if (listingDetails.getImageUrl() != null)
            existingListing.setImageUrl(listingDetails.getImageUrl());


        // Timestamp güncelle
        existingListing.setUpdatedAt(LocalDateTime.now());

        return listingRepository.save(existingListing);
    }


    // DELETE LISTING
    public boolean deleteListing(Long listingId) {
        return listingRepository.findById(listingId).map(listing -> {
            listingRepository.delete(listing);
            return true;
        }).orElse(false);
    }


    // GET LISTINGS BY USER
    public List<Listing> getListingsByUserId(Long userId) {
        try {
            return listingRepository.findByUserId(userId);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving listings for user " + userId, e);
        }
    }
}
