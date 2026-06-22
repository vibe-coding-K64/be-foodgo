package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverProfileResponse {

    private String id;
    private String fullName;
    private String phoneNumber;
    private String vehiclePlate;
    private String vehicleType;
    private String driverLicense;
    private String photoUrl;
    private Double rating;
    private Integer totalTrips;
    private Boolean isActive;
    private Boolean isAvailable;
}
