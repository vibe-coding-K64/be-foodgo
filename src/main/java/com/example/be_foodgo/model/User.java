package com.example.be_foodgo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private String photoUrl;
    private List<Integer> roles;
    private String createdAt;
    private String updatedAt;
    private Boolean isEmailVerified;
}
