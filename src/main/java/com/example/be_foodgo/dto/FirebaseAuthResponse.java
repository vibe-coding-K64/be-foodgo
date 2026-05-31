package com.example.be_foodgo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseAuthResponse {
    private String accessToken;
    private long expiresIn;
    private String refreshToken;
    private long refreshExpiresIn;
    private UserResponse user;

    public static FirebaseAuthResponse of(String accessToken, long expiresIn,
                                          String refreshToken, long refreshExpiresIn, UserResponse user) {
        return FirebaseAuthResponse.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .refreshToken(refreshToken)
                .refreshExpiresIn(refreshExpiresIn)
                .user(user)
                .build();
    }
}
