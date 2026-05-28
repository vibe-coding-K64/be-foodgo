package com.example.be_foodgo.dto.driver;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yeu cau tai xe xac nhan nhan don hoac tu choi")
public class DriverRespondRequest {

    @NotBlank(message = "action khong duoc de trong")
    @Pattern(regexp = "^(accept|decline)$", message = "action phai la 'accept' hoac 'decline'")
    @Schema(description = "Han dong: 'accept' de nhan don, 'decline' de tu choi", example = "accept")
    private String action;
}
