package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherExchangeResponse {

    private String myVoucherId;
    private String name;
    private String title;
    private String subtitle;
    private String code;
    private String description;
    private String imageUrl;
    private String terms;
    private Integer type;
    private Double value;
    private Double minOrderValue;
    private String expiryDate;
    private boolean isActive;
    private boolean isFreeship;
    private int diemDaDung;
    private int diemConLai;
    private String message;
}
