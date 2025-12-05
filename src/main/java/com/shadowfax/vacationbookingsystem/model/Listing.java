package com.shadowfax.vacationbookingsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "listings")
@Data
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    // Created & Updated timestamps
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private String category;

    @Column(name = "room_count")
    private Integer roomCount;

    @Column(name = "bathroom_count")
    private Integer bathroomCount;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Embedded
    private Location location;

    // FK column
    @Column(name = "user_id")
    private Long userId;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonBackReference("user-listings")
    private User user;

    private Double price;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = false)
    @JsonManagedReference("listing-reservations")
    private List<Reservation> reservations;
}
