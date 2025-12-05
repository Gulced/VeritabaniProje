package com.shadowfax.vacationbookingsystem.repository;

import com.shadowfax.vacationbookingsystem.model.UserFavorite;
import com.shadowfax.vacationbookingsystem.model.User;
import com.shadowfax.vacationbookingsystem.model.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    // User + Listing kombinasyonuna göre favori var mı?
    Optional<UserFavorite> findByUserAndListing(User user, Listing listing);

    // userId + listingId ile favoriyi bul
    UserFavorite findByUserIdAndListingId(Long userId, Long listingId);

    // Belirli bir kullanıcının tüm favorileri
    List<UserFavorite> findByUserId(Long userId);
}
