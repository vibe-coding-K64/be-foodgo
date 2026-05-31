package com.example.be_foodgo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    private String id;
    private String token;
    private String userId;
    private String deviceInfo;
    private String createdAt;
    private String expiresAt;
    private boolean revoked;
}
