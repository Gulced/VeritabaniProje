package com.shadowfax.vacationbookingsystem.service;

import com.shadowfax.vacationbookingsystem.model.UserFavorite;
import com.shadowfax.vacationbookingsystem.repository.UserFavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserFavoriteService {

    @Autowired
    private UserFavoriteRepository userFavoriteRepository;

    // ADD FAVORITE
    public void addFavorite(UserFavorite userFavorite) {

        if (userFavorite.getUser() == null ||
            userFavorite.getUser().getId() == null ||
            userFavorite.getListing() == null ||
            userFavorite.getListing().getId() == null) {
            throw new IllegalArgumentException("User ID and Listing ID cannot be null.");
        }

        Optional<UserFavorite> existingFavorite =
                userFavoriteRepository.findByUserAndListing(
                        userFavorite.getUser(),
                        userFavorite.getListing()
                );

        if (existingFavorite.isPresent()) {
            throw new IllegalArgumentException("This favorite already exists.");
        }

        userFavoriteRepository.save(userFavorite);
    }


    // GET FAVORITE BY USER + LISTING
    public UserFavorite getFavoriteByUserAndListing(Long userId, Long listingId) {
        return userFavoriteRepository.findByUserIdAndListingId(userId, listingId);
    }


    // GET ALL FAVORITES FOR A USER
    public Iterable<UserFavorite> getFavoritesByUser(Long userId) {
        return userFavoriteRepository.findByUserId(userId);
    }


    // REMOVE FAVORITE (returns boolean for controller)
    public boolean removeFavorite(Long userId, Long listingId) {

        UserFavorite userFavorite =
                userFavoriteRepository.findByUserIdAndListingId(userId, listingId);

        if (userFavorite == null) {
            return false;
        }

        userFavoriteRepository.delete(userFavorite);
        return true;
    }
}
