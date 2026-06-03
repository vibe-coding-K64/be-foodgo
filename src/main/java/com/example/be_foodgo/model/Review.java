package com.example.be_foodgo.model;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Review {
    private String id;
    private String orderId;
    private String itemId;
    private String foodId;
    private String storeId;
    private String userId;
    private String userName;
    private String userAvatarUrl;
    private Integer starRating;
    private String comment;
    private List<String> imageUrls;
    private Date createdAt;
    private Date updatedAt;
    private String replyComment;
    private Date repliedAt;
}
