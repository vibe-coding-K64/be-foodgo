package com.example.be_foodgo.dto;

import com.example.be_foodgo.model.MyVoucher;
import com.example.be_foodgo.model.Voucher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherListResponse {

    private List<MyVoucher> myVouchers;
    private List<Voucher> vouchers;
    private List<Voucher> freeshipVouchers;
}
