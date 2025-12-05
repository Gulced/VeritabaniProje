package com.shadowfax.vacationbookingsystem.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
@JsonIgnoreProperties(ignoreUnknown = true)
public class Location {

    @Column(name = "location_value")
    private String value;   // Example: "Istanbul, Turkey"

    @Column(name = "latitude")
    private Double latitude; 

    @Column(name = "longitude")
    private Double longitude;
}
