package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.PaymentRequest;
import com.example.be_foodgo.dto.PaymentResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.PaymentMethod;
import com.example.be_foodgo.repository.PaymentRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public List<PaymentResponse> layTatCaPhuongThuc(String userId) {
        log.info("Bat dau lay danh sach phuong thuc thanh toan - Nguoi dung: {}", userId);

        try {
            List<PaymentMethod> paymentMethods = paymentRepository.layTatCaPhuongThuc(userId);
            List<PaymentResponse> responses = new ArrayList<>();
            for (PaymentMethod pm : paymentMethods) {
                responses.add(mapToPaymentResponse(pm));
            }
            log.info("Da lay {} phuong thuc thanh toan cua nguoi dung {}", paymentMethods.size(), userId);
            return responses;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay danh sach phuong thuc thanh toan cua nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay danh sach phuong thuc thanh toan.");
        } catch (ExecutionException e) {
            log.error("Loi khi lay danh sach phuong thuc thanh toan cua nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay danh sach phuong thuc thanh toan.");
        }
    }

    public PaymentResponse layMotPhuongThuc(String userId, String paymentMethodId) {
        log.info("Bat dau lay phuong thuc thanh toan - Nguoi dung: {}, Phuong thuc: {}", userId, paymentMethodId);

        try {
            PaymentMethod paymentMethod = paymentRepository.layMotPhuongThuc(userId, paymentMethodId);
            if (paymentMethod == null) {
                log.warn("Phuong thuc thanh toan [{}] khong ton tai cho nguoi dung {}", paymentMethodId, userId);
                throw BusinessException.phuongThucThanhToanKhongTimThay(paymentMethodId);
            }

            log.info("Da lay phuong thuc thanh toan [{}] cua nguoi dung {} thanh cong", paymentMethodId, userId);
            return mapToPaymentResponse(paymentMethod);

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi lay phuong thuc thanh toan [{}]: {}", paymentMethodId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin phuong thuc thanh toan.");
        } catch (ExecutionException e) {
            log.error("Loi khi lay phuong thuc thanh toan [{}]: {}", paymentMethodId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the lay thong tin phuong thuc thanh toan.");
        }
    }

    public PaymentResponse themPhuongThuc(String userId, PaymentRequest request) {
        log.info("Bat dau xu ly them phuong thuc thanh toan - Nguoi dung: {}, Loai: {}, isDefault: {}",
                userId, request.getType(), request.getIsDefault());

        try {
            String walletBrand = null;
            String cardBrand = null;
            String last4Digits = null;
            boolean isLinked = false;

            switch (request.getType().toLowerCase()) {
                case "momo":
                    walletBrand = "momo";
                    isLinked = true;
                    break;
                case "zalo":
                    walletBrand = "zalopay";
                    isLinked = true;
                    break;
                case "card":
                    walletBrand = null;
                    cardBrand = "Visa";
                    if (request.getDetails() != null && request.getDetails().length() >= 4) {
                        last4Digits = request.getDetails().substring(request.getDetails().length() - 4);
                    }
                    isLinked = true;
                    break;
                case "cash":
                    walletBrand = null;
                    cardBrand = null;
                    last4Digits = null;
                    isLinked = false;
                    break;
                default:
                    log.warn("Loai phuong thuc thanh toan khong hop le: {}", request.getType());
                    throw new IllegalArgumentException("Loai phuong thuc thanh toan khong hop le: " + request.getType() + ". Chi chap nhan: momo, zalo, card, cash.");
            }

            String finalWalletBrand = walletBrand;
            String finalCardBrand = cardBrand;
            String finalLast4Digits = last4Digits;
            boolean finalIsLinked = isLinked;

            if (Boolean.TRUE.equals(request.getIsDefault())) {
                log.info("Yeu cau dat phuong thuc moi lam mac dinh, quet bo tat ca phuong thuc mac dinh cu");
                WriteBatch batch = paymentRepository.taoWriteBatch();

                List<PaymentMethod> phuongThucMacDinhCu = paymentRepository.layTatCaPhuongThucMacDinh(userId);
                for (PaymentMethod pm : phuongThucMacDinhCu) {
                    DocumentReference docRef = paymentRepository.getPaymentMethodDocument(userId, pm.getId());
                    batch.update(docRef, "isDefault", false, "updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
                    log.info("Bo mac dinh phuong thuc [{}]", pm.getId());
                }

                PaymentMethod newMethod = PaymentMethod.builder()
                        .name(request.getName())
                        .type(typeStringToInt(request.getType()))
                        .details(request.getDetails() != null ? request.getDetails() : "")
                        .isDefault(true)
                        .cardBrand(finalCardBrand)
                        .last4Digits(finalLast4Digits)
                        .walletBrand(finalWalletBrand)
                        .isLinked(finalIsLinked)
                        .build();

                String newId = paymentRepository.taoPhuongThucVoiBatch(userId, newMethod, batch);
                paymentRepository.commitBatch(batch);

                PaymentMethod created = paymentRepository.layMotPhuongThuc(userId, newId);
                log.info("Da tao phuong thuc thanh toan [{}] voi isDefault=true bang WriteBatch thanh cong cho nguoi dung {}",
                        newId, userId);
                return mapToPaymentResponse(created);
            } else {
                PaymentMethod newMethod = PaymentMethod.builder()
                        .name(request.getName())
                        .type(typeStringToInt(request.getType()))
                        .details(request.getDetails() != null ? request.getDetails() : "")
                        .isDefault(false)
                        .cardBrand(finalCardBrand)
                        .last4Digits(finalLast4Digits)
                        .walletBrand(finalWalletBrand)
                        .isLinked(finalIsLinked)
                        .build();

                String newId = paymentRepository.taoPhuongThuc(userId, newMethod);

                PaymentMethod created = paymentRepository.layMotPhuongThuc(userId, newId);
                log.info("Da tao phuong thuc thanh toan [{}] (khong phai mac dinh) thanh cong cho nguoi dung {}",
                        newId, userId);
                return mapToPaymentResponse(created);
            }

        } catch (IllegalArgumentException e) {
            log.warn("Loi khi them phuong thuc thanh toan: {}", e.getMessage());
            throw new BusinessException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "INVALID_PAYMENT_TYPE",
                    e.getMessage()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi them phuong thuc thanh toan cho nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the them phuong thuc thanh toan.");
        } catch (ExecutionException e) {
            log.error("Loi khi them phuong thuc thanh toan cho nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the them phuong thuc thanh toan.");
        }
    }

    public void datPhuongThucMacDinh(String userId, String paymentMethodId) {
        log.info("Bat dau dat phuong thuc thanh toan [{}] lam mac dinh - Nguoi dung: {}", paymentMethodId, userId);

        try {
            PaymentMethod phuongThuc = paymentRepository.layMotPhuongThuc(userId, paymentMethodId);
            if (phuongThuc == null) {
                log.warn("Phuong thuc thanh toan [{}] khong ton tai khi dat mac dinh", paymentMethodId);
                throw BusinessException.phuongThucThanhToanKhongTimThay(paymentMethodId);
            }

            if (Boolean.TRUE.equals(phuongThuc.getIsDefault())) {
                log.info("Phuong thuc thanh toan [{}] da la phuong thuc mac dinh, khong can thay doi", paymentMethodId);
                return;
            }

            WriteBatch batch = paymentRepository.taoWriteBatch();

            List<PaymentMethod> phuongThucMacDinhCu = paymentRepository.layTatCaPhuongThucMacDinh(userId);
            for (PaymentMethod pm : phuongThucMacDinhCu) {
                DocumentReference docRef = paymentRepository.getPaymentMethodDocument(userId, pm.getId());
                batch.update(docRef, "isDefault", false, "updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
                log.info("Bo mac dinh phuong thuc [{}]", pm.getId());
            }

            DocumentReference targetDocRef = paymentRepository.getPaymentMethodDocument(userId, paymentMethodId);
            batch.update(targetDocRef, "isDefault", true, "updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
            log.info("Dat phuong thuc [{}] lam mac dinh trong cung batch", paymentMethodId);

            paymentRepository.commitBatch(batch);

            log.info("Da dat phuong thuc thanh toan [{}] lam mac dinh thanh cong cho nguoi dung {} (atomic WriteBatch)",
                    paymentMethodId, userId);

        } catch (BusinessException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi dat phuong thuc thanh toan mac dinh [{}]: {}", paymentMethodId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the dat phuong thuc thanh toan mac dinh.");
        } catch (ExecutionException e) {
            log.error("Loi khi dat phuong thuc thanh toan mac dinh [{}]: {}", paymentMethodId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the dat phuong thuc thanh toan mac dinh.");
        }
    }

    public void xoaPhuongThuc(String userId, String paymentMethodId) {
        log.info("Bat dau xoa phuong thuc thanh toan [{}] - Nguoi dung: {}", paymentMethodId, userId);

        try {
            PaymentMethod phuongThuc = paymentRepository.layMotPhuongThuc(userId, paymentMethodId);
            if (phuongThuc == null) {
                log.warn("Phuong thuc thanh toan [{}] khong ton tai khi xoa, coi nhu xoa thanh cong (idempotent)",
                        paymentMethodId);
                return;
            }

            paymentRepository.xoaPhuongThuc(userId, paymentMethodId);
            log.info("Da xoa phuong thuc thanh toan [{}] cua nguoi dung {} thanh cong", paymentMethodId, userId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Loi khi xoa phuong thuc thanh toan [{}]: {}", paymentMethodId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the xoa phuong thuc thanh toan.");
        } catch (ExecutionException e) {
            log.error("Loi khi xoa phuong thuc thanh toan [{}]: {}", paymentMethodId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the xoa phuong thuc thanh toan.");
        }
    }

    public DocumentReference getPaymentMethodDocument(String userId, String paymentMethodId) {
        return paymentRepository.getPaymentMethodDocument(userId, paymentMethodId);
    }

    private PaymentResponse mapToPaymentResponse(PaymentMethod pm) {
        return PaymentResponse.builder()
                .id(pm.getId())
                .name(pm.getName())
                .type(typeIntToString(pm.getType()))
                .details(pm.getDetails())
                .isDefault(pm.getIsDefault())
                .cardBrand(pm.getCardBrand())
                .last4Digits(pm.getLast4Digits())
                .walletBrand(pm.getWalletBrand())
                .isLinked(pm.getIsLinked())
                .createdAt(pm.getCreatedAt())
                .updatedAt(pm.getUpdatedAt())
                .build();
    }

    private int typeStringToInt(String type) {
        if (type == null) return 1;
        return switch (type.toLowerCase()) {
            case "momo" -> 1;
            case "cash" -> 2;
            case "zalo", "zaloapp" -> 3;
            case "vnpay", "card" -> 4;
            default -> 1;
        };
    }

    private String typeIntToString(int type) {
        return switch (type) {
            case 1 -> "momo";
            case 2 -> "cash";
            case 3 -> "zalo";
            case 4 -> "vnpay";
            default -> "momo";
        };
    }
}
