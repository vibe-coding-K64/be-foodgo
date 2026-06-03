package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMerchantProfileRequest {
    private String businessName;
    private String phoneNumber;
    private String taxCode;
    private String photoUrl;
}
