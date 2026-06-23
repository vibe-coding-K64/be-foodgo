package com.example.be_foodgo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherExchangeRequest {

    @NotBlank(message = "Voucher ID khong duoc de trong")
    private String voucherId;
}
