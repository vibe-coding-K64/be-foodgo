package com.example.be_foodgo.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    private String id;
    private String storeId;
    private String name;
    private String icon;
    private Integer order;
    private String imageUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
