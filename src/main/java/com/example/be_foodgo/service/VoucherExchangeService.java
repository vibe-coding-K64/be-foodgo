package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.VoucherResponse;
import com.example.be_foodgo.dto.VoucherExchangeRequest;
import com.example.be_foodgo.dto.VoucherExchangeResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.MyVoucher;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.repository.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class VoucherExchangeService {

    private static final Logger log = LoggerFactory.getLogger(VoucherExchangeService.class);

    @Autowired
    private VoucherRepository voucherRepository;

    public List<VoucherResponse> getAllSystemVouchers(String userId) {
        try {
            List<Voucher> vouchers = voucherRepository.getExchangeableVouchers();
            Integer diemHienTai = layDiemHienTai(userId);

            List<VoucherResponse> result = new ArrayList<>();
            for (Voucher v : vouchers) {
                boolean coTheDoi = diemHienTai != null && diemHienTai >= v.getPointsRequired();
                String message = null;
                if (diemHienTai == null) {
                    message = "Khong the lay diem cua ban.";
                } else if (coTheDoi) {
                    message = "Co the doi.";
                } else {
                    message = "Can them " + (v.getPointsRequired() - diemHienTai) + " diem de doi.";
                }

                result.add(VoucherResponse.builder()
                        .id(v.getId())
                        .title(v.getTitle())
                        .subtitle(v.getSubtitle())
                        .imageUrl(v.getImageUrl())
                        .type(v.getType())
                        .value(v.getValue())
                        .terms(v.getTerms())
                        .pointsRequired(v.getPointsRequired())
                        .remaining(v.getRemaining())
                        .minOrderValue(v.getMinOrderValue())
                        .isActive(true)
                        .coTheDoi(coTheDoi)
                        .message(message)
                        .build());
            }
            return result;
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.loiHeThong("Khong the lay danh sach voucher: " + e.getMessage());
        }
    }

    public VoucherExchangeResponse doiVoucher(String userId, VoucherExchangeRequest request) {
        String voucherId = request.getVoucherId();
        log.info("User [{}] yeu cau doi voucher [{}]", userId, voucherId);

        try {
            Voucher systemVoucher = voucherRepository.getSystemVoucher(voucherId);
            if (systemVoucher == null) {
                throw BusinessException.voucherKhongTimThay(voucherId);
            }

            if (systemVoucher.getRemaining() <= 0) {
                throw BusinessException.voucherDaHetSoLuong(voucherId);
            }

            if (systemVoucher.getPointsRequired() <= 0) {
                throw BusinessException.voucherKhongTheDoiDiem(voucherId);
            }

            if (systemVoucher.getExpiryDate() != null && Instant.now().isAfter(systemVoucher.getExpiryDate().toInstant())) {
                throw BusinessException.voucherDaHetHan(voucherId);
            }

            Integer diemHienTai = layDiemHienTai(userId);
            if (diemHienTai == null) {
                throw BusinessException.diemKhongTimThay();
            }

            int pointsRequired = systemVoucher.getPointsRequired();
            if (diemHienTai < pointsRequired) {
                throw BusinessException.diemKhongDu(diemHienTai, pointsRequired);
            }

            boolean daCoVoucher = voucherRepository.kiemTraTonTaiMyVoucher(userId, voucherId);
            if (daCoVoucher) {
                throw BusinessException.voucherDaDuocDoi(voucherId);
            }

            voucherRepository.truLoyaltyPoints(userId, pointsRequired);

            String myVoucherId = "mv_" + UUID.randomUUID().toString().substring(0, 8);
            int validityDays = systemVoucher.getValidityDays() > 0 ? systemVoucher.getValidityDays() : 30;
            Instant expiryInstant = ChronoUnit.DAYS.addTo(Instant.now(), validityDays);
            Date expiryDate = Date.from(expiryInstant);

            MyVoucher myVoucher = MyVoucher.builder()
                    .id(myVoucherId)
                    .title(systemVoucher.getTitle())
                    .subtitle(systemVoucher.getSubtitle())
                    .code("SYS-" + voucherId.substring(voucherId.indexOf('_') + 1).toUpperCase())
                    .description(systemVoucher.getSubtitle())
                    .type(systemVoucher.getType())
                    .value(systemVoucher.getValue())
                    .imageUrl(systemVoucher.getImageUrl())
                    .terms(systemVoucher.getTerms())
                    .minOrderValue(systemVoucher.getMinOrderValue())
                    .isActive(true)
                    .isFreeship(systemVoucher.getIsFreeship())
                    .expiryDate(expiryDate)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            voucherRepository.luuMyVoucher(userId, myVoucher);
            voucherRepository.giamRemainingSystemVoucher(voucherId);

            int diemConLai = diemHienTai - pointsRequired;

            log.info("User [{}] da doi thanh cong voucher [{}] -> my_voucher [{}]. Diem con lai: {}",
                    userId, voucherId, myVoucherId, diemConLai);

            return VoucherExchangeResponse.builder()
                    .myVoucherId(myVoucherId)
                    .title(myVoucher.getTitle())
                    .subtitle(myVoucher.getSubtitle())
                    .code(myVoucher.getCode())
                    .description(myVoucher.getDescription())
                    .imageUrl(myVoucher.getImageUrl())
                    .terms(myVoucher.getTerms())
                    .type(myVoucher.getType())
                    .value(myVoucher.getValue())
                    .minOrderValue(myVoucher.getMinOrderValue())
                    .expiryDate(expiryDate.toString())
                    .isActive(myVoucher.isActive())
                    .isFreeship(myVoucher.isFreeship())
                    .diemDaDung(pointsRequired)
                    .diemConLai(diemConLai)
                    .message("Doi voucher thanh cong!")
                    .build();

        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.loiHeThong("Khong the doi voucher: " + e.getMessage());
        }
    }

    public List<MyVoucher> getMyVouchers(String userId) {
        try {
            return voucherRepository.getAllMyVouchers(userId);
        } catch (ExecutionException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw BusinessException.loiHeThong("Khong the lay danh sach voucher: " + e.getMessage());
        }
    }

    private Integer layDiemHienTai(String userId) throws ExecutionException, InterruptedException {
        return voucherRepository.getLoyaltyPoints(userId);
    }
}
