package com.example.be_foodgo.service;

import com.example.be_foodgo.dto.CartRequest;
import com.example.be_foodgo.dto.CheckoutRequest;
import com.example.be_foodgo.dto.CheckoutResponse;
import com.example.be_foodgo.exception.BusinessException;
import com.example.be_foodgo.model.Address;
import com.example.be_foodgo.model.CartItem;
import com.example.be_foodgo.model.MyVoucher;
import com.example.be_foodgo.model.Store;
import com.example.be_foodgo.model.Voucher;
import com.example.be_foodgo.repository.AddressRepository;
import com.example.be_foodgo.repository.CartRepository;
import com.example.be_foodgo.repository.ProductRepository;
import com.example.be_foodgo.repository.StoreRepository;
import com.example.be_foodgo.repository.VoucherRepository;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);
    private static final double BAN_KINH_TRAI_DAT = 6371.0;
    private static final double GIOI_HAN_KHOANG_CACH_KM = 10.0;
    private static final double PHI_SHIP_CO_BAN = 15000.0;

    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final VoucherRepository voucherRepository;
    private final Firestore firestore;

    public CheckoutService(
            CartRepository cartRepository,
            AddressRepository addressRepository,
            ProductRepository productRepository,
            StoreRepository storeRepository,
            VoucherRepository voucherRepository,
            Firestore firestore
    ) {
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.voucherRepository = voucherRepository;
        this.firestore = firestore;
    }

    public CheckoutResponse thucHienDatHang(CheckoutRequest request) {
        String userId = request.getUserId();
        String addressId = request.getAddressId();
        log.info("Bat dau xu ly dat hang - userId: {}, addressId: {}, voucherId: {}, paymentMethod: {}",
                userId, addressId, request.getVoucherId(), request.getPaymentMethod());

        List<CartItem> gioHang;
        try {
            gioHang = cartRepository.layTatCaMonTrongGio(userId);
        } catch (Exception e) {
            log.error("Loi khi truy van gio hang cua nguoi dung [{}]: {}", userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the truy van gio hang.");
        }

        if (gioHang.isEmpty()) {
            log.warn("Gio hang cua nguoi dung [{}] dang rong.", userId);
            throw BusinessException.gioHangRong();
        }
        log.info("Tim thay {} mon trong gio hang cua nguoi dung [{}].", gioHang.size(), userId);

        String storeId = gioHang.get(0).getStoreId();
        log.info("Cua hang cua gio hang: [{}].", storeId);

        Address diaChi;
        try {
            diaChi = addressRepository.layMotDiaChi(userId, addressId);
        } catch (Exception e) {
            log.error("Loi khi truy van dia chi [{}] cua nguoi dung [{}]: {}", addressId, userId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the truy van thong tin dia chi.");
        }

        if (diaChi == null) {
            log.warn("Khong tim thay dia chi [{}] cua nguoi dung [{}].", addressId, userId);
            throw BusinessException.diaChiKhongTimThay(addressId);
        }
        log.info("Tim thay dia chi giao hang: [{}] - {}, {}.",
                diaChi.getName(), diaChi.getAddress(), diaChi.getReceiverName());

        Store cuaHang;
        try {
            cuaHang = storeRepository.getStoreById(storeId);
        } catch (Exception e) {
            log.error("Loi khi truy van cua hang [{}]: {}", storeId, e.getMessage());
            throw BusinessException.loiHeThong("Khong the truy van thong tin cua hang.");
        }

        if (cuaHang == null) {
            log.error("Khong tim thay cua hang voi ID [{}].", storeId);
            throw BusinessException.loiHeThong("Khong the truy van thong tin cua hang.");
        }
        log.info("Tim thay cua hang: [{}] - [{}].", cuaHang.getId(), cuaHang.getName());

        kiemTraKhoangCach(cuaHang, diaChi);

        List<String> foodIds = gioHang.stream().map(CartItem::getFoodId).toList();
        Map<String, Boolean> trangThaiTonKho = kiemTraTonKhoSanPham(foodIds);
        for (CartItem item : gioHang) {
            Boolean conHang = trangThaiTonKho.get(item.getFoodId());
            if (conHang == null || conHang) {
                log.info("San pham [{}] con hang trong he thong, kiem tra thanh cong.", item.getFoodId());
            } else {
                log.warn("San pham [{}] - [{}] da het hang.", item.getFoodId(), item.getName());
                throw BusinessException.monAnHetHangTrongGio(item.getFoodId());
            }
        }

        double tongTienHang = tinhTongTienHang(gioHang);
        double phiShip = PHI_SHIP_CO_BAN;
        double soTienGiam = 0.0;
        VoucherInfo voucherInfo = null;

        if (request.getVoucherId() != null && !request.getVoucherId().isBlank()) {
            voucherInfo = kiemTraVaXuLyVoucher(userId, request.getVoucherId(), tongTienHang);
            soTienGiam = tinhSoTienGiam(voucherInfo, tongTienHang);
            log.info("Ap dung voucher [{}] - giam {} VND.", request.getVoucherId(), soTienGiam);
        }

        double tongThanhToan = tongTienHang + phiShip - soTienGiam;
        if (tongThanhToan < 0) {
            tongThanhToan = 0;
        }

        log.info("Tinh toan chi phi - Tong tien hang: {}, Phi ship: {}, Giam gia: {}, Tong phai tra: {}.",
                tongTienHang, phiShip, soTienGiam, tongThanhToan);

        CheckoutResponse.OrderItemData[] orderItems = chuanBiOrderItems(gioHang);
        String orderId = taoDonHangAtomic(
                userId, storeId, cuaHang.getName(), diaChi, request,
                orderItems, tongTienHang, phiShip, soTienGiam, tongThanhToan,
                voucherInfo
        );

        String orderCode = orderId.length() >= 6 ? orderId.substring(orderId.length() - 6).toUpperCase() : orderId.toUpperCase();

        log.info("Dat hang thanh cong - orderId: [{}], orderCode: [{}].", orderId, orderCode);

        return CheckoutResponse.builder()
                .orderId(orderId)
                .orderCode(orderCode)
                .storeId(storeId)
                .storeName(cuaHang.getName())
                .userId(userId)
                .items(java.util.Arrays.asList(orderItems))
                .totalAmount(tongTienHang)
                .deliveryFee(phiShip)
                .discountAmount(soTienGiam)
                .finalAmount(tongThanhToan)
                .paymentMethod(request.getPaymentMethod())
                .deliveryAddress(diaChi.getAddress())
                .status(0)
                .createdAt(Instant.now())
                .note(request.getNote())
                .build();
    }

    private void kiemTraKhoangCach(Store cuaHang, Address diaChi) {
        Double storeLat = cuaHang.getLat();
        Double storeLng = cuaHang.getLng();
        Double customerLat = diaChi.getLat();
        Double customerLng = diaChi.getLng();

        if (storeLat == null || storeLng == null || customerLat == null || customerLng == null) {
            log.warn("Thieu toa do cua hang hoac dia chi giao hang. Bo qua kiem tra khoang cach.");
            return;
        }

        double khoangCach = tinhKhoangCachHaversine(storeLat, storeLng, customerLat, customerLng);
        log.info("Khoang cach tu cua hang den dia chi giao hang: {:.1f} km.", Double.valueOf(khoangCach));

        if (khoangCach > GIOI_HAN_KHOANG_CACH_KM) {
            log.warn("Khoang cach {:.1f} km vuot qua gioi han {:.1f} km.", Double.valueOf(khoangCach), Double.valueOf(GIOI_HAN_KHOANG_CACH_KM));
            throw BusinessException.khoangCachVuotGioiHan(khoangCach, GIOI_HAN_KHOANG_CACH_KM);
        }

        log.info("Khoang cach nam trong gioi han cho phep.");
    }

    private double tinhKhoangCachHaversine(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BAN_KINH_TRAI_DAT * c;
    }

    private Map<String, Boolean> kiemTraTonKhoSanPham(List<String> foodIds) {
        Map<String, Boolean> ketQua = new HashMap<>();
        for (String foodId : foodIds) {
            try {
                var product = productRepository.findById(foodId);
                if (product != null) {
                    ketQua.put(foodId, !product.isOutOfStock());
                } else {
                    log.warn("San pham [{}] khong ton tai trong collection products.", foodId);
                    ketQua.put(foodId, false);
                }
            } catch (Exception e) {
                log.error("Loi khi kiem tra ton kho san pham [{}]: {}", foodId, e.getMessage());
                ketQua.put(foodId, false);
            }
        }
        return ketQua;
    }

    private double tinhTongTienHang(List<CartItem> gioHang) {
        double tong = 0.0;
        for (CartItem item : gioHang) {
            tong += item.getPrice() * item.getQuantity();
        }
        return tong;
    }

    private VoucherInfo kiemTraVaXuLyVoucher(String userId, String voucherId, double tongTienHang) {
        VoucherInfo info = new VoucherInfo();
        info.setVoucherId(voucherId);

        try {
            Voucher systemVoucher = voucherRepository.getVoucher(voucherId);
            log.info("Ket qua truy van voucher he thong [{}]: {}", voucherId,
                    systemVoucher != null ? "TIM THAY" : "KHONG TIM THAY");
            if (systemVoucher != null) {
                info.setLoaiVoucher(LoaiVoucher.SYSTEM);
                info.setSystemVoucher(systemVoucher);
                kiemTraVoucherHeThong(systemVoucher, voucherId, tongTienHang);
                log.info("Voucher he thong [{}] hop le va duoc chap nhan.", voucherId);
                return info;
            }
        } catch (BusinessException e) {
            log.warn("BusinessException khi xu ly voucher he thong [{}]: {}", voucherId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception khi truy van voucher he thong [{}]: {} - {}", voucherId, e.getClass().getName(), e.getMessage());
        }

        try {
            MyVoucher caNhanVoucher = voucherRepository.layVoucherCaNhan(userId, voucherId);
            log.info("Ket qua truy van voucher ca nhan [{}] cua user [{}]: {}", voucherId, userId,
                    caNhanVoucher != null ? "TIM THAY" : "KHONG TIM THAY");
            if (caNhanVoucher != null) {
                info.setLoaiVoucher(LoaiVoucher.CA_NHAN);
                info.setMyVoucher(caNhanVoucher);
                kiemTraVoucherCaNhan(caNhanVoucher, voucherId, tongTienHang);
                log.info("Voucher ca nhan [{}] hop le va duoc chap nhan.", voucherId);
                return info;
            }
        } catch (BusinessException e) {
            log.warn("BusinessException khi xu ly voucher ca nhan [{}]: {}", voucherId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception khi truy van voucher ca nhan [{}]: {} - {}", voucherId, e.getClass().getName(), e.getMessage());
        }

        log.warn("Khong tim thay voucher [{}] trong ca vouchers va my_vouchers.", voucherId);
        throw BusinessException.voucherKhongTimThay(voucherId);
    }

    private void kiemTraVoucherHeThong(Voucher voucher, String voucherId, double tongTienHang) {
        int remaining = getRemainingFromSystemVoucher(voucher);
        if (remaining <= 0) {
            log.warn("Voucher he thong [{}] da het so luong (remaining: {}).", voucherId, remaining);
            throw BusinessException.voucherDaHetSoLuong(voucherId);
        }

        double minOrderValue = getMinOrderValueFromSystemVoucher(voucher);
        if (minOrderValue > 0 && tongTienHang < minOrderValue) {
            log.warn("Voucher [{}] yeu cau don hang toi thieu {} VND, nhung gia tri hien tai la {} VND.",
                    voucherId, minOrderValue, tongTienHang);
            throw BusinessException.voucherKhongDatDonToiThieu(voucherId, minOrderValue);
        }

        log.info("Voucher he thong [{}] hop le - remaining: {}, minOrderValue: {}.",
                voucherId, remaining, minOrderValue);
    }

    private void kiemTraVoucherCaNhan(MyVoucher voucher, String voucherId, double tongTienHang) {
        if (voucher.getExpiryDate() != null) {
            Instant now = Instant.now();
            if (now.isAfter(voucher.getExpiryDate())) {
                log.warn("Voucher ca nhan [{}] da het han (ngay het han: {}).", voucherId, voucher.getExpiryDate());
                throw BusinessException.voucherDaHetHan(voucherId);
            }
        }

        double minOrderValue = voucher.getMinOrderValue() != null ? voucher.getMinOrderValue() : 0.0;
        if (minOrderValue > 0 && tongTienHang < minOrderValue) {
            log.warn("Voucher [{}] yeu cau don hang toi thieu {} VND, nhung gia tri hien tai la {} VND.",
                    voucherId, minOrderValue, tongTienHang);
            throw BusinessException.voucherKhongDatDonToiThieu(voucherId, minOrderValue);
        }

        log.info("Voucher ca nhan [{}] hop le - expiryDate: {}, minOrderValue: {}.",
                voucherId, voucher.getExpiryDate(), minOrderValue);
    }

    private int getRemainingFromSystemVoucher(Voucher voucher) {
        int remaining = voucher.getRemaining();
        log.info("Doc truong remaining tu Voucher: {}", remaining);
        return remaining;
    }

    private double getMinOrderValueFromSystemVoucher(Voucher voucher) {
        double minOrderValue = voucher.getMinOrderValue();
        log.info("Doc truong minOrderValue tu Voucher: {}", minOrderValue);
        return minOrderValue;
    }

    private double tinhSoTienGiam(VoucherInfo info, double tongTienHang) {
        if (info.getLoaiVoucher() == LoaiVoucher.SYSTEM) {
            Voucher v = info.getSystemVoucher();
            int type = v.getType();
            double voucherValue = v.getValue();
            log.info("Tinh giam voucher he thong - type: {}, value: {}, tongTienHang: {}", type, voucherValue, tongTienHang);

            if (type == 1) {
                double soTienGiam = tongTienHang * (voucherValue / 100.0);
                log.info("Voucher he thong phan tram - giam {}% tuong duong {} VND.", voucherValue, soTienGiam);
                return soTienGiam;
            } else {
                double soTienGiam = voucherValue;
                if (soTienGiam > tongTienHang) {
                    soTienGiam = tongTienHang;
                }
                log.info("Voucher he thong tien mat - giam {} VND.", soTienGiam);
                return soTienGiam;
            }
        } else {
            MyVoucher v = info.getMyVoucher();
            Boolean isPercentage = v.getIsPercentage() != null ? v.getIsPercentage() : false;
            Double discountValue = v.getDiscountValue() != null ? v.getDiscountValue() : 0.0;

            if (isPercentage) {
                double soTienGiam = tongTienHang * (discountValue / 100.0);
                log.info("Voucher ca nhan phan tram - giam {}% tuong duong {} VND.", discountValue, soTienGiam);
                return soTienGiam;
            } else {
                double soTienGiam = discountValue;
                if (soTienGiam > tongTienHang) {
                    soTienGiam = tongTienHang;
                }
                log.info("Voucher ca nhan tien mat - giam {} VND.", soTienGiam);
                return soTienGiam;
            }
        }
    }

    private CheckoutResponse.OrderItemData[] chuanBiOrderItems(List<CartItem> gioHang) {
        List<CheckoutResponse.OrderItemData> items = new ArrayList<>();
        for (CartItem cartItem : gioHang) {
            List<CartRequest.ToppingOption> toppingOptions = null;
            if (cartItem.getToppings() != null && !cartItem.getToppings().isEmpty()) {
                toppingOptions = new ArrayList<>();
                for (CartItem.ToppingItem t : cartItem.getToppings()) {
                    toppingOptions.add(CartRequest.ToppingOption.builder()
                            .name(t.getName())
                            .price(t.getPrice())
                            .build());
                }
            }

            items.add(CheckoutResponse.OrderItemData.builder()
                    .foodId(cartItem.getFoodId())
                    .name(cartItem.getName())
                    .price(tinhDonGiaDonMon(cartItem))
                    .quantity(cartItem.getQuantity())
                    .imageUrl(cartItem.getImageUrl())
                    .options(toppingOptions)
                    .build());
        }
        return items.toArray(new CheckoutResponse.OrderItemData[0]);
    }

    private Double tinhDonGiaDonMon(CartItem item) {
        Double donGia = item.getPrice() / item.getQuantity();
        return donGia;
    }

    private String taoDonHangAtomic(
            String userId,
            String storeId,
            String storeName,
            Address diaChi,
            CheckoutRequest request,
            CheckoutResponse.OrderItemData[] orderItems,
            double tongTienHang,
            double phiShip,
            double soTienGiam,
            double tongThanhToan,
            VoucherInfo voucherInfo
    ) {
        WriteBatch batch = firestore.batch();
        log.info("Bat dau tao don hang atomi cho nguoi dung [{}].", userId);

        String orderId;
        DocumentReference orderDocRef = firestore.collection("orders").document();
        orderId = orderDocRef.getId();

        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (CheckoutResponse.OrderItemData item : orderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("foodId", item.getFoodId());
            itemMap.put("name", item.getName());
            itemMap.put("price", item.getPrice());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("imageUrl", item.getImageUrl() != null ? item.getImageUrl() : "");
            if (item.getOptions() != null) {
                List<Map<String, Object>> toppingMaps = new ArrayList<>();
                for (CartRequest.ToppingOption t : item.getOptions()) {
                    toppingMaps.add(Map.of("name", t.getName() != null ? t.getName() : "",
                            "price", t.getPrice() != null ? t.getPrice() : 0.0));
                }
                itemMap.put("options", toppingMaps);
            } else {
                itemMap.put("options", new ArrayList<>());
            }
            itemsData.add(itemMap);
        }

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("storeId", storeId);
        orderData.put("storeName", storeName != null ? storeName : "");
        orderData.put("items", itemsData);
        orderData.put("totalAmount", tongTienHang);
        orderData.put("deliveryFee", phiShip);
        orderData.put("discountAmount", soTienGiam);
        orderData.put("finalAmount", tongThanhToan);
        orderData.put("paymentMethod", request.getPaymentMethod());
        orderData.put("deliveryAddress", diaChi.getAddress());
        orderData.put("receiverName", diaChi.getReceiverName() != null ? diaChi.getReceiverName() : "");
        orderData.put("receiverPhone", diaChi.getReceiverPhone() != null ? diaChi.getReceiverPhone() : "");
        orderData.put("status", 0);
        orderData.put("note", request.getNote() != null ? request.getNote() : "");
        orderData.put("createdAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
        orderData.put("updatedAt", com.google.cloud.firestore.FieldValue.serverTimestamp());
        orderData.put("deletedAt", null);

        batch.set(orderDocRef, orderData);
        log.info("Them thao tac tao document don hang [{}] vao WriteBatch.", orderId);

        try {
            List<CartItem> gioHang = cartRepository.layTatCaMonTrongGio(userId);
            for (CartItem cartItem : gioHang) {
                DocumentReference cartDocRef = firestore
                        .collection("customer_profiles")
                        .document(userId)
                        .collection("cart")
                        .document(cartItem.getId());
                batch.delete(cartDocRef);
            }
            log.info("Them {} thao tac xoa gio hang vao WriteBatch.", gioHang.size());
        } catch (Exception e) {
            log.error("Loi khi lay gio hang de xoa: {}", e.getMessage());
        }

        if (voucherInfo != null) {
            String voucherId = voucherInfo.getVoucherId();
            if (voucherInfo.getLoaiVoucher() == LoaiVoucher.SYSTEM) {
                DocumentReference voucherDocRef = firestore.collection("vouchers").document(voucherId);
                batch.update(voucherDocRef, "remaining",
                        com.google.cloud.firestore.FieldValue.increment(-1));
                log.info("Them thao tac giam remaining voucher he thong [{}] vao WriteBatch.", voucherId);
            } else {
                DocumentReference myVoucherDocRef = firestore
                        .collection("customer_profiles")
                        .document(userId)
                        .collection("my_vouchers")
                        .document(voucherId);
                batch.delete(myVoucherDocRef);
                log.info("Them thao tac xoa voucher ca nhan [{}] khoi my_vouchers vao WriteBatch.", voucherId);
            }
        }

        try {
            batch.commit();
            log.info("WriteBatch commit thanh cong - orderId: [{}].", orderId);
        } catch (Exception e) {
            log.error("Loi khi commit WriteBatch: {}", e.getMessage(), e);
            throw BusinessException.loiHeThong("Khong the tao don hang. Vui long thu lai sau.");
        }

        return orderId;
    }

    private enum LoaiVoucher {
        SYSTEM,
        CA_NHAN
    }

    private static class VoucherInfo {
        private LoaiVoucher loaiVoucher;
        private String voucherId;
        private Voucher systemVoucher;
        private MyVoucher myVoucher;

        public LoaiVoucher getLoaiVoucher() { return loaiVoucher; }
        public void setLoaiVoucher(LoaiVoucher loaiVoucher) { this.loaiVoucher = loaiVoucher; }
        public String getVoucherId() { return voucherId; }
        public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
        public Voucher getSystemVoucher() { return systemVoucher; }
        public void setSystemVoucher(Voucher systemVoucher) { this.systemVoucher = systemVoucher; }
        public MyVoucher getMyVoucher() { return myVoucher; }
        public void setMyVoucher(MyVoucher myVoucher) { this.myVoucher = myVoucher; }
    }
}
