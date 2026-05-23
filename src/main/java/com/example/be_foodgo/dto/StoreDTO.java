package com.example.be_foodgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StoreDTO {
    private String id;
    
    @NotBlank(message = "Tên quán không được để trống")
    private String name;
    
    private String description;
    private String address;
    private String taxCode;
    private String businessLicense;
    private String coverImageUrl;
    private String logoUrl;
    private String bankName;
    private String bankAccountNumber;
    private boolean isAcceptingOrders;
}
