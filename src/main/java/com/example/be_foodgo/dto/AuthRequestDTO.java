package com.example.be_foodgo.dto;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String firebaseUid;
}
