package com.example.be_foodgo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cap nhat FCM token de nhan push notification")
public class FCMTokenRequest {

    @NotBlank(message = "fcmToken khong duoc de trong")
    @Schema(description = "FCM device token tu Firebase Cloud Messaging", example = "dQw4w9WgXcQ...")
    private String fcmToken;
}
