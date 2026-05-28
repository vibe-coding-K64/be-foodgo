package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemVoucherResponse {

    private String id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private Integer type;
    private Double value;
    private String terms;
    private int pointsRequired;
    private int remaining;
    private double minOrderValue;
    private boolean isActive;
    private boolean coTheDoi;
    private String message;
}
