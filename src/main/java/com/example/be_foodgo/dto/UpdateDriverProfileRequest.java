package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDriverProfileRequest {

    private String fullName;
    private String phoneNumber;
    private String vehiclePlate;
    private String vehicleType;
    private String driverLicense;
    private String photoUrl;
}
