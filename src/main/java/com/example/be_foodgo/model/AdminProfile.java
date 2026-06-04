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
public class AdminProfile {
    private String id; // Matches the userId
    private Integer adminLevel;
    private String department;
    private List<String> permissions; // manage_users, manage_orders, manage_stores, view_reports
    private String createdAt;
    private String updatedAt;
}
