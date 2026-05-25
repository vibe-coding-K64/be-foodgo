package com.example.be_foodgo.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ReviewDTO {

    private String id;
    private String orderId;
    private String storeId;
    private String userId;
    private String userName;
    private String userAvatarUrl;
    private Integer starRating;
    private String comment;
    private List<String> imageUrls;
    private Date createdAt;
    private Date updatedAt;
}
