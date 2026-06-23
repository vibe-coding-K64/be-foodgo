package com.example.be_foodgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirebaseLinkRequest {
    @NotBlank(message = "Firebase ID token khong duoc de trong")
    private String idToken;

    private String email;
    private String password;
}
